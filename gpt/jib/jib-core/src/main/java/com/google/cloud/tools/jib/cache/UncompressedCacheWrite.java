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
package com.google.cloud.tools.jib.cache;

import com.google.cloud.tools.jib.blob.Blob;
import com.google.cloud.tools.jib.image.DescriptorDigest;
import java.util.Optional;
import javax.annotation.concurrent.Immutable;

/**
 * Represents uncompressed layer data to write to the cache with an additional selector digest.
 *
 * <p><b>Implementation is immutable and thread-safe.</b>
 */
@Immutable
class UncompressedCacheWrite {

    private final Blob uncompressedLayerBlob;

    @Nullable
    private final DescriptorDigest selector;

    UncompressedCacheWrite(Blob uncompressedLayerBlob, DescriptorDigest selector) {
        this.uncompressedLayerBlob = uncompressedLayerBlob;
        this.selector = selector;
    }

    /**
     * Gets the {@link Blob} containing the uncompressed layer contents.
     *
     * @return the layer {@link Blob}
     */
    Blob getUncompressedLayerBlob() {
        return uncompressedLayerBlob;
    }

    /**
     * Gets the optional selector digest to also reference this layer data. A selector digest may be a
     * secondary identifier for a layer that is distinct from the default layer digest.
     *
     * <p>For example, it is useful as an inexpensive alternative reference to a layer compared to
     * calculating the primary layer digest (SHA256 of compressed tarball).
     *
     * @return the selector digest
     */
    Optional<DescriptorDigest> getSelector() {
        return Optional.ofNullable(selector);
    }
}
