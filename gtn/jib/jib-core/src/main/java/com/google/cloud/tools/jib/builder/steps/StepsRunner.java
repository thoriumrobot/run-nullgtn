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
import com.google.cloud.tools.jib.async.AsyncSteps;
import com.google.cloud.tools.jib.configuration.BuildConfiguration;
import com.google.cloud.tools.jib.docker.DockerClient;
import com.google.cloud.tools.jib.event.events.ProgressEvent;
import com.google.cloud.tools.jib.event.progress.Allocation;
import com.google.cloud.tools.jib.global.JibSystemProperties;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import javax.annotation.Nullable;

/**
 * Runs steps for building an image.
 *
 * <p>Use by first calling {@link #begin} and then calling the individual step running methods. Note
 * that order matters, so make sure that steps are run before other steps that depend on them. Wait
 * on the last step by calling the respective {@code wait...} methods.
 */
public class StepsRunner {

    /**
     * Holds the individual steps.
     */
    private static class Steps {

        @Nullable()
        private RetrieveRegistryCredentialsStep retrieveTargetRegistryCredentialsStep;

        @Nullable()
        private AuthenticatePushStep authenticatePushStep;

        @Nullable()
        private PullBaseImageStep pullBaseImageStep;

        @Nullable()
        private PullAndCacheBaseImageLayersStep pullAndCacheBaseImageLayersStep;

        @Nullable()
        private ImmutableList<BuildAndCacheApplicationLayerStep> buildAndCacheApplicationLayerSteps;

        @Nullable()
        private PushLayersStep pushBaseImageLayersStep;

        @Nullable()
        private PushLayersStep pushApplicationLayersStep;

        @Nullable()
        private BuildImageStep buildImageStep;

        @Nullable()
        private PushContainerConfigurationStep pushContainerConfigurationStep;

        @Nullable()
        private AsyncStep<BuildResult> finalStep;
    }

    /**
     * Starts building the steps to run.
     *
     * @param buildConfiguration the {@link BuildConfiguration}
     * @return a new {@link StepsRunner}
     */
    public static StepsRunner begin(BuildConfiguration buildConfiguration) {
        ExecutorService executorService = JibSystemProperties.isSerializedExecutionEnabled() ? MoreExecutors.newDirectExecutorService() : buildConfiguration.getExecutorService();
        return new StepsRunner(MoreExecutors.listeningDecorator(executorService), buildConfiguration);
    }

    private final Steps steps = new Steps();

    private final ListeningExecutorService listeningExecutorService;

    private final BuildConfiguration buildConfiguration;

    /**
     * Runnable to run all the steps.
     */
    @Nullable()
    private Runnable stepsRunnable = () -> {
    };

    /**
     * The total number of steps added.
     */
    private int stepsCount = 0;

    @Nullable()
    private Allocation rootProgressAllocation;

    private StepsRunner(ListeningExecutorService listeningExecutorService, BuildConfiguration buildConfiguration) {
        this.listeningExecutorService = listeningExecutorService;
        this.buildConfiguration = buildConfiguration;
    }

    @Nullable()
    public StepsRunner retrieveTargetRegistryCredentials() {
        return enqueueStep(() -> steps.retrieveTargetRegistryCredentialsStep = RetrieveRegistryCredentialsStep.forTargetImage(listeningExecutorService, buildConfiguration, Preconditions.checkNotNull(rootProgressAllocation)));
    }

    @Nullable()
    public StepsRunner authenticatePush() {
        return enqueueStep(() -> steps.authenticatePushStep = new AuthenticatePushStep(listeningExecutorService, buildConfiguration, Preconditions.checkNotNull(rootProgressAllocation), Preconditions.checkNotNull(steps.retrieveTargetRegistryCredentialsStep)));
    }

    @Nullable()
    public StepsRunner pullBaseImage() {
        return enqueueStep(() -> steps.pullBaseImageStep = new PullBaseImageStep(listeningExecutorService, buildConfiguration, Preconditions.checkNotNull(rootProgressAllocation)));
    }

    @Nullable()
    public StepsRunner pullAndCacheBaseImageLayers() {
        return enqueueStep(() -> steps.pullAndCacheBaseImageLayersStep = new PullAndCacheBaseImageLayersStep(listeningExecutorService, buildConfiguration, Preconditions.checkNotNull(rootProgressAllocation), Preconditions.checkNotNull(steps.pullBaseImageStep)));
    }

    @Nullable()
    public StepsRunner pushBaseImageLayers() {
        return enqueueStep(() -> steps.pushBaseImageLayersStep = new PushLayersStep(listeningExecutorService, buildConfiguration, Preconditions.checkNotNull(rootProgressAllocation), Preconditions.checkNotNull(steps.authenticatePushStep), Preconditions.checkNotNull(steps.pullAndCacheBaseImageLayersStep)));
    }

    @Nullable()
    public StepsRunner buildAndCacheApplicationLayers() {
        return enqueueStep(() -> steps.buildAndCacheApplicationLayerSteps = BuildAndCacheApplicationLayerStep.makeList(listeningExecutorService, buildConfiguration, Preconditions.checkNotNull(rootProgressAllocation)));
    }

    @Nullable()
    public StepsRunner buildImage() {
        return enqueueStep(() -> steps.buildImageStep = new BuildImageStep(listeningExecutorService, buildConfiguration, Preconditions.checkNotNull(rootProgressAllocation), Preconditions.checkNotNull(steps.pullBaseImageStep), Preconditions.checkNotNull(steps.pullAndCacheBaseImageLayersStep), Preconditions.checkNotNull(steps.buildAndCacheApplicationLayerSteps)));
    }

    @Nullable()
    public StepsRunner pushContainerConfiguration() {
        return enqueueStep(() -> steps.pushContainerConfigurationStep = new PushContainerConfigurationStep(listeningExecutorService, buildConfiguration, Preconditions.checkNotNull(rootProgressAllocation), Preconditions.checkNotNull(steps.authenticatePushStep), Preconditions.checkNotNull(steps.buildImageStep)));
    }

    @Nullable()
    public StepsRunner pushApplicationLayers() {
        return enqueueStep(() -> steps.pushApplicationLayersStep = new PushLayersStep(listeningExecutorService, buildConfiguration, Preconditions.checkNotNull(rootProgressAllocation), Preconditions.checkNotNull(steps.authenticatePushStep), AsyncSteps.immediate(Preconditions.checkNotNull(steps.buildAndCacheApplicationLayerSteps))));
    }

    public StepsRunner finalizingPush() {
        return enqueueStep(() -> new FinalizingStep(listeningExecutorService, buildConfiguration, Arrays.asList(Preconditions.checkNotNull(steps.pushBaseImageLayersStep), Preconditions.checkNotNull(steps.pushApplicationLayersStep)), Collections.emptyList()));
    }

    public StepsRunner finalizingBuild() {
        return enqueueStep(() -> new FinalizingStep(listeningExecutorService, buildConfiguration, Collections.singletonList(Preconditions.checkNotNull(steps.pullAndCacheBaseImageLayersStep)), Preconditions.checkNotNull(steps.buildAndCacheApplicationLayerSteps)));
    }

    @Nullable()
    public StepsRunner pushImage() {
        createRootProgressAllocation("Build to registry");
        return enqueueStep(() -> steps.finalStep = new PushImageStep(listeningExecutorService, buildConfiguration, Preconditions.checkNotNull(rootProgressAllocation), Preconditions.checkNotNull(steps.authenticatePushStep), Preconditions.checkNotNull(steps.pushBaseImageLayersStep), Preconditions.checkNotNull(steps.pushApplicationLayersStep), Preconditions.checkNotNull(steps.pushContainerConfigurationStep), Preconditions.checkNotNull(steps.buildImageStep)));
    }

    @Nullable()
    public StepsRunner loadDocker(DockerClient dockerClient) {
        createRootProgressAllocation("Build to Docker daemon");
        return enqueueStep(() -> steps.finalStep = new LoadDockerStep(listeningExecutorService, buildConfiguration, Preconditions.checkNotNull(rootProgressAllocation), dockerClient, Preconditions.checkNotNull(steps.pullAndCacheBaseImageLayersStep), Preconditions.checkNotNull(steps.buildAndCacheApplicationLayerSteps), Preconditions.checkNotNull(steps.buildImageStep)));
    }

    @Nullable()
    public StepsRunner writeTarFile(Path outputPath) {
        createRootProgressAllocation("Build to tar file");
        return enqueueStep(() -> steps.finalStep = new WriteTarFileStep(listeningExecutorService, buildConfiguration, Preconditions.checkNotNull(rootProgressAllocation), outputPath, Preconditions.checkNotNull(steps.pullAndCacheBaseImageLayersStep), Preconditions.checkNotNull(steps.buildAndCacheApplicationLayerSteps), Preconditions.checkNotNull(steps.buildImageStep)));
    }

    public BuildResult run() throws ExecutionException, InterruptedException {
        Preconditions.checkNotNull(rootProgressAllocation);
        stepsRunnable.run();
        return Preconditions.checkNotNull(steps.finalStep).getFuture().get();
    }

    @Nullable()
    private StepsRunner enqueueStep(Runnable stepRunnable) {
        Runnable previousStepsRunnable = stepsRunnable;
        stepsRunnable = () -> {
            previousStepsRunnable.run();
            stepRunnable.run();
        };
        stepsCount++;
        return this;
    }

    private void createRootProgressAllocation(String description) {
        rootProgressAllocation = Allocation.newRoot(description, stepsCount);
        buildConfiguration.getEventDispatcher().dispatch(new ProgressEvent(rootProgressAllocation, 0L));
    }
}
