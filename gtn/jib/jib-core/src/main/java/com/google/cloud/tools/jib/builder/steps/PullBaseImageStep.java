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
import com.google.cloud.tools.jib.blob.Blobs;
import com.google.cloud.tools.jib.builder.TimerEventDispatcher;
import com.google.cloud.tools.jib.builder.steps.PullBaseImageStep.BaseImageWithAuthorization;
import com.google.cloud.tools.jib.configuration.BuildConfiguration;
import com.google.cloud.tools.jib.configuration.credentials.Credential;
import com.google.cloud.tools.jib.event.events.LogEvent;
import com.google.cloud.tools.jib.event.events.ProgressEvent;
import com.google.cloud.tools.jib.event.progress.Allocation;
import com.google.cloud.tools.jib.http.Authorization;
import com.google.cloud.tools.jib.http.Authorizations;
import com.google.cloud.tools.jib.image.Image;
import com.google.cloud.tools.jib.image.Layer;
import com.google.cloud.tools.jib.image.LayerCountMismatchException;
import com.google.cloud.tools.jib.image.LayerPropertyNotFoundException;
import com.google.cloud.tools.jib.image.json.BadContainerConfigurationFormatException;
import com.google.cloud.tools.jib.image.json.ContainerConfigurationTemplate;
import com.google.cloud.tools.jib.image.json.JsonToImageTranslator;
import com.google.cloud.tools.jib.image.json.ManifestTemplate;
import com.google.cloud.tools.jib.image.json.UnknownManifestFormatException;
import com.google.cloud.tools.jib.image.json.V21ManifestTemplate;
import com.google.cloud.tools.jib.image.json.V22ManifestTemplate;
import com.google.cloud.tools.jib.json.JsonTemplateMapper;
import com.google.cloud.tools.jib.registry.RegistryAuthenticationFailedException;
import com.google.cloud.tools.jib.registry.RegistryAuthenticator;
import com.google.cloud.tools.jib.registry.RegistryClient;
import com.google.cloud.tools.jib.registry.RegistryException;
import com.google.cloud.tools.jib.registry.RegistryUnauthorizedException;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import javax.annotation.Nullable;

/**
 * Pulls the base image manifest.
 */
class PullBaseImageStep implements AsyncStep<BaseImageWithAuthorization>, Callable<BaseImageWithAuthorization> {

    private static final String DESCRIPTION = "Pulling base image manifest";

    /**
     * Structure for the result returned by this step.
     */
    static class BaseImageWithAuthorization {

        private final Image<Layer> baseImage;

        @Nullable
        private final Authorization baseImageAuthorization;

        @VisibleForTesting
        BaseImageWithAuthorization(Image<Layer> baseImage, @Nullable Authorization baseImageAuthorization) {
            this.baseImage = baseImage;
            this.baseImageAuthorization = baseImageAuthorization;
        }

        Image<Layer> getBaseImage() {
            return baseImage;
        }

        @Nullable()
        Authorization getBaseImageAuthorization() {
            return baseImageAuthorization;
        }
    }

    private final BuildConfiguration buildConfiguration;

    private final Allocation parentProgressAllocation;

    private final ListenableFuture<BaseImageWithAuthorization> listenableFuture;

    PullBaseImageStep(ListeningExecutorService listeningExecutorService, BuildConfiguration buildConfiguration, Allocation parentProgressAllocation) {
        this.buildConfiguration = buildConfiguration;
        this.parentProgressAllocation = parentProgressAllocation;
        listenableFuture = listeningExecutorService.submit(this);
    }

    @Override
    public ListenableFuture<BaseImageWithAuthorization> getFuture() {
        return listenableFuture;
    }

    @Override
    @Nullable()
    public BaseImageWithAuthorization call() throws IOException, RegistryException, LayerPropertyNotFoundException, LayerCountMismatchException, ExecutionException, BadContainerConfigurationFormatException, RegistryAuthenticationFailedException {
        buildConfiguration.getEventDispatcher().dispatch(LogEvent.lifecycle("Getting base image " + buildConfiguration.getBaseImageConfiguration().getImage() + "..."));
        Allocation progressAllocation = parentProgressAllocation.newChild("pull base image", 2);
        buildConfiguration.getEventDispatcher().dispatch(new ProgressEvent(progressAllocation, 0));
        try (TimerEventDispatcher ignored = new TimerEventDispatcher(buildConfiguration.getEventDispatcher(), DESCRIPTION)) {
            // First, try with no credentials.
            try {
                BaseImageWithAuthorization baseImageWithAuthorization = new BaseImageWithAuthorization(pullBaseImage(null), null);
                buildConfiguration.getEventDispatcher().dispatch(new ProgressEvent(progressAllocation, 2));
                return baseImageWithAuthorization;
            } catch (RegistryUnauthorizedException ex) {
                buildConfiguration.getEventDispatcher().dispatch(LogEvent.lifecycle("The base image requires auth. Trying again for " + buildConfiguration.getBaseImageConfiguration().getImage() + "..."));
                // If failed, then, retrieve base registry credentials and try with retrieved credentials.
                // TODO: Refactor the logic in RetrieveRegistryCredentialsStep out to
                // registry.credentials.RegistryCredentialsRetriever to avoid this direct executor hack.
                ListeningExecutorService directExecutorService = MoreExecutors.newDirectExecutorService();
                RetrieveRegistryCredentialsStep retrieveBaseRegistryCredentialsStep = RetrieveRegistryCredentialsStep.forBaseImage(directExecutorService, buildConfiguration, progressAllocation);
                Credential registryCredential = NonBlockingSteps.get(retrieveBaseRegistryCredentialsStep);
                Authorization registryAuthorization = registryCredential == null ? null : Authorizations.withBasicCredentials(registryCredential.getUsername(), registryCredential.getPassword());
                try {
                    BaseImageWithAuthorization baseImageWithAuthorization = new BaseImageWithAuthorization(pullBaseImage(registryAuthorization), registryAuthorization);
                    buildConfiguration.getEventDispatcher().dispatch(new ProgressEvent(progressAllocation, 1));
                    return baseImageWithAuthorization;
                } catch (RegistryUnauthorizedException registryUnauthorizedException) {
                    // The registry requires us to authenticate using the Docker Token Authentication.
                    // See https://docs.docker.com/registry/spec/auth/token
                    RegistryAuthenticator registryAuthenticator = RegistryAuthenticator.initializer(buildConfiguration.getEventDispatcher(), buildConfiguration.getBaseImageConfiguration().getImageRegistry(), buildConfiguration.getBaseImageConfiguration().getImageRepository()).setAllowInsecureRegistries(buildConfiguration.getAllowInsecureRegistries()).initialize();
                    if (registryAuthenticator == null) {
                        buildConfiguration.getEventDispatcher().dispatch(LogEvent.error("Failed to retrieve authentication challenge for registry that required token authentication"));
                        throw registryUnauthorizedException;
                    }
                    registryAuthorization = registryAuthenticator.setAuthorization(registryAuthorization).authenticatePull();
                    BaseImageWithAuthorization baseImageWithAuthorization = new BaseImageWithAuthorization(pullBaseImage(registryAuthorization), registryAuthorization);
                    buildConfiguration.getEventDispatcher().dispatch(new ProgressEvent(progressAllocation, 1));
                    return baseImageWithAuthorization;
                }
            }
        }
    }

    /**
     * Pulls the base image.
     *
     * @param registryAuthorization authentication credentials to possibly use
     * @return the pulled image
     * @throws IOException when an I/O exception occurs during the pulling
     * @throws RegistryException if communicating with the registry caused a known error
     * @throws LayerCountMismatchException if the manifest and configuration contain conflicting layer
     *     information
     * @throws LayerPropertyNotFoundException if adding image layers fails
     * @throws BadContainerConfigurationFormatException if the container configuration is in a bad
     *     format
     */
    private Image<Layer> pullBaseImage(Authorization registryAuthorization) throws IOException, RegistryException, LayerPropertyNotFoundException, LayerCountMismatchException, BadContainerConfigurationFormatException {
        RegistryClient registryClient = buildConfiguration.newBaseImageRegistryClientFactory().setAuthorization(registryAuthorization).newRegistryClient();
        ManifestTemplate manifestTemplate = registryClient.pullManifest(buildConfiguration.getBaseImageConfiguration().getImageTag());
        // TODO: Make schema version be enum.
        switch(manifestTemplate.getSchemaVersion()) {
            case 1:
                V21ManifestTemplate v21ManifestTemplate = (V21ManifestTemplate) manifestTemplate;
                return JsonToImageTranslator.toImage(v21ManifestTemplate);
            case 2:
                V22ManifestTemplate v22ManifestTemplate = (V22ManifestTemplate) manifestTemplate;
                if (v22ManifestTemplate.getContainerConfiguration() == null || v22ManifestTemplate.getContainerConfiguration().getDigest() == null) {
                    throw new UnknownManifestFormatException("Invalid container configuration in Docker V2.2 manifest: \n" + Blobs.writeToString(JsonTemplateMapper.toBlob(v22ManifestTemplate)));
                }
                String containerConfigurationString = Blobs.writeToString(registryClient.pullBlob(v22ManifestTemplate.getContainerConfiguration().getDigest()));
                ContainerConfigurationTemplate containerConfigurationTemplate = JsonTemplateMapper.readJson(containerConfigurationString, ContainerConfigurationTemplate.class);
                return JsonToImageTranslator.toImage(v22ManifestTemplate, containerConfigurationTemplate);
        }
        throw new IllegalStateException("Unknown manifest schema version");
    }
}
