/*
 * Copyright 2018 Google LLC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.cloud.tools.jib.builder.steps;

import com.google.cloud.tools.jib.ProjectInfo;
import com.google.cloud.tools.jib.async.AsyncStep;
import com.google.cloud.tools.jib.async.NonBlockingSteps;
import com.google.cloud.tools.jib.builder.TimerEventDispatcher;
import com.google.cloud.tools.jib.configuration.BuildConfiguration;
import com.google.cloud.tools.jib.configuration.ContainerConfiguration;
import com.google.cloud.tools.jib.event.events.LogEvent;
import com.google.cloud.tools.jib.event.events.ProgressEvent;
import com.google.cloud.tools.jib.event.progress.Allocation;
import com.google.cloud.tools.jib.image.Image;
import com.google.cloud.tools.jib.image.Layer;
import com.google.cloud.tools.jib.image.LayerPropertyNotFoundException;
import com.google.cloud.tools.jib.image.json.HistoryEntry;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import javax.annotation.Nullable;

/**
 * Builds a model {@link Image}.
 */
class BuildImageStep implements AsyncStep<AsyncStep<Image<Layer>>>, Callable<AsyncStep<Image<Layer>>> {

    private static final String DESCRIPTION = "Building container configuration";

    private final ListeningExecutorService listeningExecutorService;

    private final BuildConfiguration buildConfiguration;

    private final Allocation parentProgressAllocation;

    private final PullBaseImageStep pullBaseImageStep;

    private final PullAndCacheBaseImageLayersStep pullAndCacheBaseImageLayersStep;

    private final ImmutableList<BuildAndCacheApplicationLayerStep> buildAndCacheApplicationLayerSteps;

    private final ListenableFuture<AsyncStep<Image<Layer>>> listenableFuture;

    BuildImageStep(ListeningExecutorService listeningExecutorService, BuildConfiguration buildConfiguration, Allocation parentProgressAllocation, PullBaseImageStep pullBaseImageStep, PullAndCacheBaseImageLayersStep pullAndCacheBaseImageLayersStep, ImmutableList<BuildAndCacheApplicationLayerStep> buildAndCacheApplicationLayerSteps) {
        this.listeningExecutorService = listeningExecutorService;
        this.buildConfiguration = buildConfiguration;
        this.parentProgressAllocation = parentProgressAllocation;
        this.pullBaseImageStep = pullBaseImageStep;
        this.pullAndCacheBaseImageLayersStep = pullAndCacheBaseImageLayersStep;
        this.buildAndCacheApplicationLayerSteps = buildAndCacheApplicationLayerSteps;
        listenableFuture = Futures.whenAllSucceed(pullBaseImageStep.getFuture(), pullAndCacheBaseImageLayersStep.getFuture()).call(this, listeningExecutorService);
    }

    @Override
    public ListenableFuture<AsyncStep<Image<Layer>>> getFuture() {
        return listenableFuture;
    }

    @Override
    public AsyncStep<Image<Layer>> call() throws ExecutionException {
        List<ListenableFuture<?>> dependencies = new ArrayList<>();
        for (PullAndCacheBaseImageLayerStep pullAndCacheBaseImageLayerStep : NonBlockingSteps.get(pullAndCacheBaseImageLayersStep)) {
            dependencies.add(pullAndCacheBaseImageLayerStep.getFuture());
        }
        for (BuildAndCacheApplicationLayerStep buildAndCacheApplicationLayerStep : buildAndCacheApplicationLayerSteps) {
            dependencies.add(buildAndCacheApplicationLayerStep.getFuture());
        }
        ListenableFuture<Image<Layer>> future = Futures.whenAllSucceed(dependencies).call(this::afterCachedLayerSteps, listeningExecutorService);
        return () -> future;
    }

    @Nullable()
    private Image<Layer> afterCachedLayerSteps() throws ExecutionException, LayerPropertyNotFoundException {
        Allocation progressAllocation = parentProgressAllocation.newChild("build image format", 1);
        buildConfiguration.getEventDispatcher().dispatch(new ProgressEvent(progressAllocation, 0));
        try (TimerEventDispatcher ignored = new TimerEventDispatcher(buildConfiguration.getEventDispatcher(), DESCRIPTION)) {
            // Constructs the image.
            Image.Builder<Layer> imageBuilder = Image.builder(buildConfiguration.getTargetFormat());
            Image<Layer> baseImage = NonBlockingSteps.get(pullBaseImageStep).getBaseImage();
            ContainerConfiguration containerConfiguration = buildConfiguration.getContainerConfiguration();
            // Base image layers
            List<PullAndCacheBaseImageLayerStep> baseImageLayers = NonBlockingSteps.get(pullAndCacheBaseImageLayersStep);
            for (PullAndCacheBaseImageLayerStep pullAndCacheBaseImageLayerStep : baseImageLayers) {
                imageBuilder.addLayer(NonBlockingSteps.get(pullAndCacheBaseImageLayerStep));
            }
            // Passthrough config and count non-empty history entries
            int nonEmptyLayerCount = 0;
            for (HistoryEntry historyObject : baseImage.getHistory()) {
                imageBuilder.addHistory(historyObject);
                if (!historyObject.hasCorrespondingLayer()) {
                    nonEmptyLayerCount++;
                }
            }
            imageBuilder.addEnvironment(baseImage.getEnvironment()).addLabels(baseImage.getLabels()).setHealthCheck(baseImage.getHealthCheck()).addExposedPorts(baseImage.getExposedPorts()).addVolumes(baseImage.getVolumes()).setWorkingDirectory(baseImage.getWorkingDirectory());
            // Add history elements for non-empty layers that don't have one yet
            Instant layerCreationTime = containerConfiguration == null ? ContainerConfiguration.DEFAULT_CREATION_TIME : containerConfiguration.getCreationTime();
            for (int count = 0; count < baseImageLayers.size() - nonEmptyLayerCount; count++) {
                imageBuilder.addHistory(HistoryEntry.builder().setCreationTimestamp(layerCreationTime).setComment("auto-generated by Jib").build());
            }
            // Add built layers/configuration
            for (BuildAndCacheApplicationLayerStep buildAndCacheApplicationLayerStep : buildAndCacheApplicationLayerSteps) {
                imageBuilder.addLayer(NonBlockingSteps.get(buildAndCacheApplicationLayerStep)).addHistory(HistoryEntry.builder().setCreationTimestamp(layerCreationTime).setAuthor("Jib").setCreatedBy(buildConfiguration.getToolName() + ":" + ProjectInfo.VERSION).setComment(buildAndCacheApplicationLayerStep.getLayerType()).build());
            }
            if (containerConfiguration != null) {
                imageBuilder.addEnvironment(containerConfiguration.getEnvironmentMap()).setCreated(containerConfiguration.getCreationTime()).setUser(containerConfiguration.getUser()).setEntrypoint(computeEntrypoint(baseImage, containerConfiguration)).setProgramArguments(computeProgramArguments(baseImage, containerConfiguration)).addExposedPorts(containerConfiguration.getExposedPorts()).addVolumes(containerConfiguration.getVolumes()).addLabels(containerConfiguration.getLabels());
                if (containerConfiguration.getWorkingDirectory() != null) {
                    imageBuilder.setWorkingDirectory(containerConfiguration.getWorkingDirectory().toString());
                }
            }
            // Gets the container configuration content descriptor.
            Image<Layer> image = imageBuilder.build();
            buildConfiguration.getEventDispatcher().dispatch(new ProgressEvent(progressAllocation, 1));
            return image;
        }
    }

    /**
     * Computes the image entrypoint. If {@link ContainerConfiguration#getEntrypoint()} is null, the
     * entrypoint is inherited from the base image. Otherwise {@link
     * ContainerConfiguration#getEntrypoint()} is returned.
     *
     * @param baseImage the base image
     * @param containerConfiguration the container configuration
     * @return the container entrypoint
     */
    @Nullable()
    private ImmutableList<String> computeEntrypoint(Image<Layer> baseImage, ContainerConfiguration containerConfiguration) {
        boolean shouldInherit = baseImage.getEntrypoint() != null && containerConfiguration.getEntrypoint() == null;
        ImmutableList<String> entrypointToUse = shouldInherit ? baseImage.getEntrypoint() : containerConfiguration.getEntrypoint();
        if (entrypointToUse != null) {
            String logSuffix = shouldInherit ? " (inherited from base image)" : "";
            String message = "Container entrypoint set to " + entrypointToUse + logSuffix;
            buildConfiguration.getEventDispatcher().dispatch(LogEvent.lifecycle(""));
            buildConfiguration.getEventDispatcher().dispatch(LogEvent.lifecycle(message));
        }
        return entrypointToUse;
    }

    /**
     * Computes the image program arguments. If {@link ContainerConfiguration#getEntrypoint()} and
     * {@link ContainerConfiguration#getProgramArguments()} are null, the program arguments are
     * inherited from the base image. Otherwise {@link ContainerConfiguration#getProgramArguments()}
     * is returned.
     *
     * @param baseImage the base image
     * @param containerConfiguration the container configuration
     * @return the container program arguments
     */
    @Nullable()
    private ImmutableList<String> computeProgramArguments(Image<Layer> baseImage, ContainerConfiguration containerConfiguration) {
        // Inherit CMD only when inheriting ENTRYPOINT.
        boolean // Inherit CMD only when inheriting ENTRYPOINT.
        shouldInherit = baseImage.getProgramArguments() != null && containerConfiguration.getEntrypoint() == null && containerConfiguration.getProgramArguments() == null;
        ImmutableList<String> programArgumentsToUse = shouldInherit ? baseImage.getProgramArguments() : containerConfiguration.getProgramArguments();
        if (programArgumentsToUse != null) {
            String logSuffix = shouldInherit ? " (inherited from base image)" : "";
            String message = "Container program arguments set to " + programArgumentsToUse + logSuffix;
            buildConfiguration.getEventDispatcher().dispatch(LogEvent.lifecycle(message));
        }
        return programArgumentsToUse;
    }
}
