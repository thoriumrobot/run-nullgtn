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
import com.google.cloud.tools.jib.builder.TimerEventDispatcher;
import com.google.cloud.tools.jib.cache.Cache;
import com.google.cloud.tools.jib.cache.CacheCorruptedException;
import com.google.cloud.tools.jib.cache.CachedLayer;
import com.google.cloud.tools.jib.configuration.BuildConfiguration;
import com.google.cloud.tools.jib.event.events.ProgressEvent;
import com.google.cloud.tools.jib.event.progress.Allocation;
import com.google.cloud.tools.jib.http.Authorization;
import com.google.cloud.tools.jib.image.DescriptorDigest;
import com.google.cloud.tools.jib.registry.RegistryClient;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.Callable;
import javax.annotation.Nullable;

/**
 * Pulls and caches a single base image layer.
 */
class PullAndCacheBaseImageLayerStep implements AsyncStep<CachedLayer>, Callable<CachedLayer> {

    private static final String DESCRIPTION = "Pulling base image layer %s";

    private final BuildConfiguration buildConfiguration;

    private final Allocation parentProgressAllocation;

    private final DescriptorDigest layerDigest;

    @Nullable
    private final Authorization pullAuthorization;

    private final ListenableFuture<CachedLayer> listenableFuture;

    PullAndCacheBaseImageLayerStep(ListeningExecutorService listeningExecutorService, BuildConfiguration buildConfiguration, Allocation parentProgressAllocation, DescriptorDigest layerDigest, @Nullable Authorization pullAuthorization) {
        this.buildConfiguration = buildConfiguration;
        this.parentProgressAllocation = parentProgressAllocation;
        this.layerDigest = layerDigest;
        this.pullAuthorization = pullAuthorization;
        listenableFuture = listeningExecutorService.submit(this);
    }

    @Override
    public ListenableFuture<CachedLayer> getFuture() {
        return listenableFuture;
    }

    @Override
    @Nullable()
    public CachedLayer call() throws IOException, CacheCorruptedException {
        Allocation progressAllocation = parentProgressAllocation.newChild("pull base image layer " + layerDigest, 1);
        buildConfiguration.getEventDispatcher().dispatch(new ProgressEvent(progressAllocation, 0));
        try (TimerEventDispatcher ignored = new TimerEventDispatcher(buildConfiguration.getEventDispatcher(), String.format(DESCRIPTION, layerDigest))) {
            Cache cache = buildConfiguration.getBaseImageLayersCache();
            // Checks if the layer already exists in the cache.
            Optional<CachedLayer> optionalCachedLayer = cache.retrieve(layerDigest);
            if (optionalCachedLayer.isPresent()) {
                buildConfiguration.getEventDispatcher().dispatch(new ProgressEvent(progressAllocation, 1));
                return optionalCachedLayer.get();
            }
            RegistryClient registryClient = buildConfiguration.newBaseImageRegistryClientFactory().setAuthorization(pullAuthorization).newRegistryClient();
            CachedLayer cachedLayer = cache.writeCompressedLayer(registryClient.pullBlob(layerDigest));
            buildConfiguration.getEventDispatcher().dispatch(new ProgressEvent(progressAllocation, 1));
            return cachedLayer;
        }
    }
}
