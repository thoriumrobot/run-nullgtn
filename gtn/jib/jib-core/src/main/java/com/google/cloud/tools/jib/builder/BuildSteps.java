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
package com.google.cloud.tools.jib.builder;

import com.google.cloud.tools.jib.builder.steps.BuildResult;
import com.google.cloud.tools.jib.builder.steps.StepsRunner;
import com.google.cloud.tools.jib.configuration.BuildConfiguration;
import com.google.cloud.tools.jib.docker.DockerClient;
import java.nio.file.Path;
import java.util.concurrent.ExecutionException;
import javax.annotation.Nullable;

/**
 * Steps for building an image.
 */
public class BuildSteps {

    private static final String DESCRIPTION_FOR_DOCKER_REGISTRY = "Building and pushing image";

    private static final String DESCRIPTION_FOR_DOCKER_DAEMON = "Building image to Docker daemon";

    private static final String DESCRIPTION_FOR_TARBALL = "Building image tarball";

    /**
     * All the steps to build an image to a Docker registry.
     *
     * @param buildConfiguration the configuration parameters for the build
     * @return a new {@link BuildSteps} for building to a registry
     */
    public static BuildSteps forBuildToDockerRegistry(BuildConfiguration buildConfiguration) {
        return new BuildSteps(DESCRIPTION_FOR_DOCKER_REGISTRY, buildConfiguration, StepsRunner.begin(buildConfiguration).retrieveTargetRegistryCredentials().authenticatePush().pullBaseImage().pullAndCacheBaseImageLayers().pushBaseImageLayers().buildAndCacheApplicationLayers().buildImage().pushContainerConfiguration().pushApplicationLayers().finalizingPush().pushImage());
    }

    /**
     * All the steps to build to Docker daemon
     *
     * @param dockerClient the {@link DockerClient} for running {@code docker} commands
     * @param buildConfiguration the configuration parameters for the build
     * @return a new {@link BuildSteps} for building to a Docker daemon
     */
    public static BuildSteps forBuildToDockerDaemon(DockerClient dockerClient, BuildConfiguration buildConfiguration) {
        return new BuildSteps(DESCRIPTION_FOR_DOCKER_DAEMON, buildConfiguration, StepsRunner.begin(buildConfiguration).pullBaseImage().pullAndCacheBaseImageLayers().buildAndCacheApplicationLayers().buildImage().finalizingBuild().loadDocker(dockerClient));
    }

    /**
     * All the steps to build an image tarball.
     *
     * @param outputPath the path to output the tarball to
     * @param buildConfiguration the configuration parameters for the build
     * @return a new {@link BuildSteps} for building a tarball
     */
    public static BuildSteps forBuildToTar(Path outputPath, BuildConfiguration buildConfiguration) {
        return new BuildSteps(DESCRIPTION_FOR_TARBALL, buildConfiguration, StepsRunner.begin(buildConfiguration).pullBaseImage().pullAndCacheBaseImageLayers().buildAndCacheApplicationLayers().buildImage().finalizingBuild().writeTarFile(outputPath));
    }

    private final String description;

    private final BuildConfiguration buildConfiguration;

    private final StepsRunner stepsRunner;

    /**
     * @param description a description of what the steps do
     * @param buildConfiguration the configuration parameters for the build
     * @param stepsRunner runs the necessary steps to build an image
     */
    private BuildSteps(String description, BuildConfiguration buildConfiguration, StepsRunner stepsRunner) {
        this.description = description;
        this.buildConfiguration = buildConfiguration;
        this.stepsRunner = stepsRunner;
    }

    public BuildConfiguration getBuildConfiguration() {
        return buildConfiguration;
    }

    /**
     * Executes the build.
     *
     * @return the build result
     * @throws InterruptedException if the execution is interrupted
     * @throws ExecutionException if an exception occurs during execution
     */
    public BuildResult run() throws InterruptedException, ExecutionException {
        try (TimerEventDispatcher ignored = new TimerEventDispatcher(buildConfiguration.getEventDispatcher(), description)) {
            return stepsRunner.run();
        }
    }
}
