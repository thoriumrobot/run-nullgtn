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
import com.google.cloud.tools.jib.blob.Blob;
import com.google.cloud.tools.jib.blob.BlobDescriptor;
import com.google.cloud.tools.jib.builder.TimerEventDispatcher;
import com.google.cloud.tools.jib.configuration.BuildConfiguration;
import com.google.cloud.tools.jib.event.events.ProgressEvent;
import com.google.cloud.tools.jib.event.progress.Allocation;
import com.google.cloud.tools.jib.image.Image;
import com.google.cloud.tools.jib.image.Layer;
import com.google.cloud.tools.jib.image.json.ImageToJsonTranslator;
import com.google.common.io.ByteStreams;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import javax.annotation.Nullable;

/**
 * Pushes the container configuration.
 */
class PushContainerConfigurationStep implements AsyncStep<AsyncStep<PushBlobStep>>, Callable<AsyncStep<PushBlobStep>> {

    private static final String DESCRIPTION = "Pushing container configuration";

    private final BuildConfiguration buildConfiguration;

    private final ListeningExecutorService listeningExecutorService;

    private final Allocation parentProgressAllocation;

    private final AuthenticatePushStep authenticatePushStep;

    private final BuildImageStep buildImageStep;

    private final ListenableFuture<AsyncStep<PushBlobStep>> listenableFuture;

    PushContainerConfigurationStep(ListeningExecutorService listeningExecutorService, BuildConfiguration buildConfiguration, Allocation parentProgressAllocation, AuthenticatePushStep authenticatePushStep, BuildImageStep buildImageStep) {
        this.listeningExecutorService = listeningExecutorService;
        this.buildConfiguration = buildConfiguration;
        this.parentProgressAllocation = parentProgressAllocation;
        this.authenticatePushStep = authenticatePushStep;
        this.buildImageStep = buildImageStep;
        listenableFuture = Futures.whenAllSucceed(buildImageStep.getFuture()).call(this, listeningExecutorService);
    }

    @Override
    public ListenableFuture<AsyncStep<PushBlobStep>> getFuture() {
        return listenableFuture;
    }

    @Override
    public AsyncStep<PushBlobStep> call() throws ExecutionException {
        ListenableFuture<PushBlobStep> pushBlobStepFuture = Futures.whenAllSucceed(authenticatePushStep.getFuture(), NonBlockingSteps.get(buildImageStep).getFuture()).call(this::afterBuildConfigurationFutureFuture, listeningExecutorService);
        return () -> pushBlobStepFuture;
    }

    private PushBlobStep afterBuildConfigurationFutureFuture() throws ExecutionException, IOException {
        Allocation progressAllocation = parentProgressAllocation.newChild("push container configuration", 1);
        buildConfiguration.getEventDispatcher().dispatch(new ProgressEvent(progressAllocation, 0));
        try (TimerEventDispatcher ignored = new TimerEventDispatcher(buildConfiguration.getEventDispatcher(), DESCRIPTION)) {
            Image<Layer> image = NonBlockingSteps.get(NonBlockingSteps.get(buildImageStep));
            Blob containerConfigurationBlob = new ImageToJsonTranslator(image).getContainerConfigurationBlob();
            BlobDescriptor blobDescriptor = containerConfigurationBlob.writeTo(ByteStreams.nullOutputStream());
            return new PushBlobStep(listeningExecutorService, buildConfiguration, progressAllocation, authenticatePushStep, blobDescriptor, containerConfigurationBlob);
        }
    }
}
