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

import com.google.cloud.tools.jib.image.DescriptorDigest;
import java.nio.file.Path;
import java.security.DigestException;
import javax.annotation.Nullable;

/**
 * Resolves the files used in the default cache storage engine.
 */
class DefaultCacheStorageFiles {

    private static final String LAYERS_DIRECTORY = "layers";

    private static final String SELECTORS_DIRECTORY = "selectors";

    private static final String TEMPORARY_DIRECTORY = "tmp";

    private static final String TEMPORARY_LAYER_FILE_NAME = ".tmp.layer";

    /**
     * Returns whether or not {@code file} is a layer contents file.
     *
     * @param file the file to check
     * @return {@code true} if {@code file} is a layer contents file; {@code false} otherwise
     */
    static boolean isLayerFile(Path file) {
        return file.getFileName().toString().length() == DescriptorDigest.HASH_LENGTH;
    }

    /**
     * Gets the diff ID portion of the layer filename.
     *
     * @param layerFile the layer file to parse for the diff ID
     * @return the diff ID portion of the layer file filename
     * @throws CacheCorruptedException if no valid diff ID could be parsed
     */
    static DescriptorDigest getDiffId(Path layerFile) throws CacheCorruptedException {
        try {
            String diffId = layerFile.getFileName().toString();
            return DescriptorDigest.fromHash(diffId);
        } catch (DigestException | IndexOutOfBoundsException ex) {
            throw new CacheCorruptedException("Layer file did not include valid diff ID: " + layerFile, ex);
        }
    }

    private final Path cacheDirectory;

    DefaultCacheStorageFiles(Path cacheDirectory) {
        this.cacheDirectory = cacheDirectory;
    }

    /**
     * Resolves the layer contents file.
     *
     * @param layerDigest the layer digest
     * @param layerDiffId the layer diff Id
     * @return the layer contents file
     */
    Path getLayerFile(DescriptorDigest layerDigest, DescriptorDigest layerDiffId) {
        return getLayerDirectory(layerDigest).resolve(getLayerFilename(layerDiffId));
    }

    /**
     * Gets the filename for the layer file. The filename is in the form {@code <layer diff
     * ID>.layer}.
     *
     * @param layerDiffId the layer's diff ID
     * @return the layer filename
     */
    String getLayerFilename(DescriptorDigest layerDiffId) {
        return layerDiffId.getHash();
    }

    /**
     * Resolves a selector file.
     *
     * @param selector the selector digest
     * @return the selector file
     */
    Path getSelectorFile(DescriptorDigest selector) {
        return cacheDirectory.resolve(SELECTORS_DIRECTORY).resolve(selector.getHash());
    }

    /**
     * Resolves the {@link #LAYERS_DIRECTORY} in the {@link #cacheDirectory}.
     *
     * @return the directory containing all the layer directories
     */
    Path getLayersDirectory() {
        return cacheDirectory.resolve(LAYERS_DIRECTORY);
    }

    /**
     * Gets the directory for the layer with digest {@code layerDigest}.
     *
     * @param layerDigest the digest of the layer
     * @return the directory for that {@code layerDigest}
     */
    Path getLayerDirectory(DescriptorDigest layerDigest) {
        return getLayersDirectory().resolve(layerDigest.getHash());
    }

    /**
     * Gets the directory to store temporary files.
     *
     * @return the directory for temporary files
     */
    Path getTemporaryDirectory() {
        return cacheDirectory.resolve(TEMPORARY_DIRECTORY);
    }

    /**
     * Resolves a file to use as a temporary file to write layer contents to.
     *
     * @param layerDirectory the directory in which to resolve the temporary layer file
     * @return the temporary layer file
     */
    Path getTemporaryLayerFile(Path layerDirectory) {
        Path temporaryLayerFile = layerDirectory.resolve(TEMPORARY_LAYER_FILE_NAME);
        temporaryLayerFile.toFile().deleteOnExit();
        return temporaryLayerFile;
    }
}
