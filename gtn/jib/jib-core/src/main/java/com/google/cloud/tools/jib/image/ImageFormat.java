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
package com.google.cloud.tools.jib.image;

import com.google.cloud.tools.jib.image.json.BuildableManifestTemplate;
import com.google.cloud.tools.jib.image.json.OCIManifestTemplate;
import com.google.cloud.tools.jib.image.json.V22ManifestTemplate;
import javax.annotation.Nullable;

/**
 * Enumeration of {@link BuildableManifestTemplate}s that indicates the format of the image.
 */
public enum ImageFormat {

    /**
     * @see <a href="https://docs.docker.com/registry/spec/manifest-v2-2/">Docker V2.2</a>
     */
    Docker(V22ManifestTemplate.class),
    /**
     * @see <a href="https://github.com/opencontainers/image-spec/blob/master/manifest.md">OCI</a>
     */
    OCI(OCIManifestTemplate.class);

    private final Class<? extends BuildableManifestTemplate> manifestTemplateClass;

    ImageFormat(Class<? extends BuildableManifestTemplate> manifestTemplateClass) {
        this.manifestTemplateClass = manifestTemplateClass;
    }

    public Class<? extends BuildableManifestTemplate> getManifestTemplateClass() {
        return manifestTemplateClass;
    }
}
