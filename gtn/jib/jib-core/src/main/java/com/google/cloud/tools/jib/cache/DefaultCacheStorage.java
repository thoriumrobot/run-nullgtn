/*
 * Copyright 2018 Google LLC. All rights reserved.
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
package com.google.cloud.tools.jib.cache;

import com.google.cloud.tools.jib.blob.Blob;
import com.google.cloud.tools.jib.image.DescriptorDigest;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nullable;

/**
 * Default implementation of {@link CacheStorage}. This storage engine stores cache entries in a
 * directory in the format:
 *
 * <pre>{@code
 * layers/
 *   <layer digest>/
 *     <layer diff ID>
 *   ...
 * selectors/
 *   <selector digest>
 *   ...
 * }</pre>
 *
 * Layers entries are stored in their own directories under the {@code layers/} directory. Each
 * layer directory is named by the layer digest. Inside each layer directory, the layer contents
 * file is named by the layer diff ID.
 *
 * <p>Selectors are stored in the {@code selectors/} directory. Each selector file is named by the
 * selector digest. The contents of a selector file is the digest of the layer it selects.
 */
class DefaultCacheStorage implements CacheStorage {

    /**
     * Instantiates a {@link CacheStorage} backed by this storage engine.
     *
     * @param cacheDirectory the directory for this cache
     * @return a new {@link CacheStorage}
     */
    static CacheStorage withDirectory(Path cacheDirectory) {
        return new DefaultCacheStorage(new DefaultCacheStorageFiles(cacheDirectory));
    }

    private final DefaultCacheStorageWriter defaultCacheStorageWriter;

    private final DefaultCacheStorageReader defaultCacheStorageReader;

    private DefaultCacheStorage(DefaultCacheStorageFiles defaultCacheStorageFiles) {
        this.defaultCacheStorageWriter = new DefaultCacheStorageWriter(defaultCacheStorageFiles);
        this.defaultCacheStorageReader = new DefaultCacheStorageReader(defaultCacheStorageFiles);
    }

    @Override
    public CachedLayer write(UncompressedCacheWrite uncompressedCacheWrite) throws IOException {
        return defaultCacheStorageWriter.write(uncompressedCacheWrite);
    }

    @Override
    public CachedLayer write(Blob compressedLayerBlob) throws IOException {
        return defaultCacheStorageWriter.write(compressedLayerBlob);
    }

    @Override
    public Set<DescriptorDigest> fetchDigests() throws IOException, CacheCorruptedException {
        return defaultCacheStorageReader.fetchDigests();
    }

    @Override
    public Optional<CachedLayer> retrieve(DescriptorDigest layerDigest) throws IOException, CacheCorruptedException {
        return defaultCacheStorageReader.retrieve(layerDigest);
    }

    @Override
    public Optional<DescriptorDigest> select(DescriptorDigest selector) throws IOException, CacheCorruptedException {
        return defaultCacheStorageReader.select(selector);
    }
}
