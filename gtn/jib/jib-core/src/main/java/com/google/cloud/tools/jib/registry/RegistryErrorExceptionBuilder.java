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
package com.google.cloud.tools.jib.registry;

import com.google.cloud.tools.jib.ProjectInfo;
import com.google.cloud.tools.jib.registry.json.ErrorEntryTemplate;
import javax.annotation.Nullable;

/**
 * Builds a {@link RegistryErrorException} with multiple causes.
 */
class RegistryErrorExceptionBuilder {

    @Nullable
    private final Throwable cause;

    private final StringBuilder errorMessageBuilder = new StringBuilder();

    private boolean firstErrorReason = true;

    /**
     * Gets the reason for certain errors.
     *
     * @param errorCodeString string form of {@link ErrorCodes}
     * @param message the original received error message, which may or may not be used depending on
     *     the {@code errorCode}
     */
    private static String getReason(String errorCodeString, String message) {
        if (message == null) {
            message = "no details";
        }
        try {
            if (errorCodeString == null) {
                throw new IllegalArgumentException();
            }
            ErrorCodes errorCode = ErrorCodes.valueOf(errorCodeString);
            if (errorCode == ErrorCodes.MANIFEST_INVALID || errorCode == ErrorCodes.BLOB_UNKNOWN) {
                return message + " (something went wrong)";
            } else if (errorCode == ErrorCodes.MANIFEST_UNKNOWN || errorCode == ErrorCodes.TAG_INVALID || errorCode == ErrorCodes.MANIFEST_UNVERIFIED) {
                return message;
            } else {
                return "other: " + message;
            }
        } catch (IllegalArgumentException ex) {
            // Unknown errorCodeString
            return "unknown: " + message;
        }
    }

    /**
     * @param method the registry method that errored
     */
    RegistryErrorExceptionBuilder(String method, @Nullable Throwable cause) {
        this.cause = cause;
        errorMessageBuilder.append("Tried to ");
        errorMessageBuilder.append(method);
        errorMessageBuilder.append(" but failed because: ");
    }

    /**
     * @param method the registry method that errored
     */
    RegistryErrorExceptionBuilder(String method) {
        this(method, null);
    }

    // TODO: Don't use a JsonTemplate as a data object to pass around.
    /**
     * Builds an entry to the error reasons from an {@link ErrorEntryTemplate}.
     *
     * @param errorEntry the {@link ErrorEntryTemplate} to add
     */
    RegistryErrorExceptionBuilder addReason(ErrorEntryTemplate errorEntry) {
        String reason = getReason(errorEntry.getCode(), errorEntry.getMessage());
        addReason(reason);
        return this;
    }

    /**
     * Adds an entry to the error reasons.
     */
    RegistryErrorExceptionBuilder addReason(String reason) {
        if (!firstErrorReason) {
            errorMessageBuilder.append(", ");
        }
        errorMessageBuilder.append(reason);
        firstErrorReason = false;
        return this;
    }

    RegistryErrorException build() {
        // Provides a feedback channel.
        errorMessageBuilder.append(" | If this is a bug, please file an issue at " + ProjectInfo.GITHUB_NEW_ISSUE_URL);
        return new RegistryErrorException(errorMessageBuilder.toString(), cause);
    }
}
