/*
 * Copyright 2017 Google LLC.
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
package com.google.cloud.tools.jib.image.json;

import com.google.cloud.tools.jib.registry.RegistryException;
import javax.annotation.Nullable;

/**
 * Exception thrown when trying to parse an unknown image manifest format.
 */
public class UnknownManifestFormatException extends RegistryException {

    public UnknownManifestFormatException(String message) {
        super(message);
    }
}
