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

import com.google.cloud.tools.jib.async.AsyncStep;
import com.google.cloud.tools.jib.async.NonBlockingSteps;
import com.google.cloud.tools.jib.configuration.BuildConfiguration;
import com.google.cloud.tools.jib.docker.DockerClient;
import com.google.cloud.tools.jib.docker.ImageToTarballTranslator;
import com.google.cloud.tools.jib.event.events.LogEvent;
import com.google.cloud.tools.jib.event.events.ProgressEvent;
import com.google.cloud.tools.jib.event.progress.Allocation;
import com.google.cloud.tools.jib.image.Image;
import com.google.cloud.tools.jib.image.ImageReference;
import com.google.cloud.tools.jib.image.Layer;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import javax.annotation.Nullable;

/**
 * Adds image layers to a tarball and loads into Docker daemon.
 */
class LoadDockerStep implements AsyncStep<BuildResult>, Callable<BuildResult> {

    private final ListeningExecutorService listeningExecutorService;

    private final BuildConfiguration buildConfiguration;

    private final Allocation parentProgressAllocation;

    private final DockerClient dockerClient;

    private final PullAndCacheBaseImageLayersStep pullAndCacheBaseImageLayersStep;

    private final ImmutableList<BuildAndCacheApplicationLayerStep> buildAndCacheApplicationLayerSteps;

    private final BuildImageStep buildImageStep;

    private final ListenableFuture<BuildResult> listenableFuture;

    LoadDockerStep(ListeningExecutorService listeningExecutorService, BuildConfiguration buildConfiguration, Allocation parentProgressAllocation, DockerClient dockerClient, PullAndCacheBaseImageLayersStep pullAndCacheBaseImageLayersStep, ImmutableList<BuildAndCacheApplicationLayerStep> buildAndCacheApplicationLayerSteps, BuildImageStep buildImageStep) {
        this.listeningExecutorService = listeningExecutorService;
        this.buildConfiguration = buildConfiguration;
        this.parentProgressAllocation = parentProgressAllocation;
        this.dockerClient = dockerClient;
        this.pullAndCacheBaseImageLayersStep = pullAndCacheBaseImageLayersStep;
        this.buildAndCacheApplicationLayerSteps = buildAndCacheApplicationLayerSteps;
        this.buildImageStep = buildImageStep;
        listenableFuture = Futures.whenAllSucceed(pullAndCacheBaseImageLayersStep.getFuture(), buildImageStep.getFuture()).call(this, listeningExecutorService);
    }

    @Override
    public ListenableFuture<BuildResult> getFuture() {
        return listenableFuture;
    }

    @Override
    public BuildResult call() throws ExecutionException, InterruptedException {
        ImmutableList.Builder<ListenableFuture<?>> dependenciesBuilder = ImmutableList.builder();
        for (PullAndCacheBaseImageLayerStep pullAndCacheBaseImageLayerStep : NonBlockingSteps.get(pullAndCacheBaseImageLayersStep)) {
            dependenciesBuilder.add(pullAndCacheBaseImageLayerStep.getFuture());
        }
        for (BuildAndCacheApplicationLayerStep buildAndCacheApplicationLayerStep : buildAndCacheApplicationLayerSteps) {
            dependenciesBuilder.add(buildAndCacheApplicationLayerStep.getFuture());
        }
        dependenciesBuilder.add(NonBlockingSteps.get(buildImageStep).getFuture());
        return Futures.whenAllSucceed(dependenciesBuilder.build()).call(this::afterPushBaseImageLayerFuturesFuture, listeningExecutorService).get();
    }

    @Nullable()
    private BuildResult afterPushBaseImageLayerFuturesFuture() throws ExecutionException, InterruptedException, IOException {
        buildConfiguration.getEventDispatcher().dispatch(LogEvent.lifecycle("Loading to Docker daemon..."));
        Allocation progressAllocation = parentProgressAllocation.newChild("Load to Docker daemon", 1);
        buildConfiguration.getEventDispatcher().dispatch(new ProgressEvent(progressAllocation, 0));
        Image<Layer> image = NonBlockingSteps.get(NonBlockingSteps.get(buildImageStep));
        ImageReference targetImageReference = buildConfiguration.getTargetImageConfiguration().getImage();
        // Load the image to docker daemon.
        dockerClient.load(new ImageToTarballTranslator(image).toTarballBlob(targetImageReference));
        // Tags the image with all the additional tags, skipping the one 'docker load' already loaded.
        for (String tag : buildConfiguration.getAllTargetImageTags()) {
            if (tag.equals(targetImageReference.getTag())) {
                continue;
            }
            dockerClient.tag(targetImageReference, targetImageReference.withTag(tag));
        }
        buildConfiguration.getEventDispatcher().dispatch(new ProgressEvent(progressAllocation, 1));
        return BuildResult.fromImage(image, buildConfiguration.getTargetFormat());
    }
}
