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
package com.google.cloud.tools.skaffold.filesystem;

import com.google.common.annotations.VisibleForTesting;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;
import javax.annotation.Nullable;

/**
 * Obtains an OS-specific user cache directory based on the XDG Base Directory Specification.
 *
 * <p>Specifically, from the specification:
 *
 * <ul>
 *   <li>This directory is defined by the environment variable {@code $XDG_CACHE_HOME}.
 *   <li>If {@code $XDG_CACHE_HOME} is either not set or empty, a default equal to {@code
 *       $HOME/.cache} should be used.
 * </ul>
 *
 * @see <a
 *     href="https://specifications.freedesktop.org/basedir-spec/basedir-spec-latest.html">https://specifications.freedesktop.org/basedir-spec/basedir-spec-latest.html</a>
 */
public class UserCacheHome {

    private static final Logger logger = Logger.getLogger(UserCacheHome.class.getName());

    public static Path getCacheHome() {
        return getCacheHome(System.getProperties(), System.getenv());
    }

    /**
     * Returns {@code $XDG_CACHE_HOME}, if available, or resolves the OS-specific user cache home
     * based.
     *
     * <p>For Linux, this is {@code $HOME/.cache/}.
     *
     * <p>For Windows, this is {@code %LOCALAPPDATA%}.
     *
     * <p>For macOS, this is {@code $HOME/Library/Application Support/}.
     */
    @VisibleForTesting
    static Path getCacheHome(Properties properties, Map<String, String> environment) {
        // Use environment variable $XDG_CACHE_HOME if set and not empty.
        String xdgCacheHome = environment.get("XDG_CACHE_HOME");
        if (xdgCacheHome != null && !xdgCacheHome.trim().isEmpty()) {
            return Paths.get(xdgCacheHome);
        }
        String userHome = properties.getProperty("user.home");
        Path userHomeDirectory = Paths.get(userHome);
        Path xdgPath = userHomeDirectory.resolve(".cache");
        OperatingSystem operatingSystem = OperatingSystem.resolve(properties);
        switch(operatingSystem) {
            case LINUX:
                return xdgPath;
            case WINDOWS:
                // Use %LOCALAPPDATA% for Windows.
                String localAppDataEnv = environment.get("LOCALAPPDATA");
                if (localAppDataEnv == null || localAppDataEnv.trim().isEmpty()) {
                    logger.warning("LOCALAPPDATA environment is invalid or missing");
                    return xdgPath;
                }
                Path localAppData = Paths.get(localAppDataEnv);
                if (!Files.exists(localAppData)) {
                    logger.warning(localAppData + " does not exist");
                    return xdgPath;
                }
                return localAppData;
            case MAC_OS:
                // Use '~/Library/Application Support/' for macOS.
                Path applicationSupport = userHomeDirectory.resolve("Library").resolve("Application Support");
                if (!Files.exists(applicationSupport)) {
                    logger.warning(applicationSupport + " does not exist");
                    return xdgPath;
                }
                return applicationSupport;
            default:
                throw new IllegalStateException("unreachable");
        }
    }

    private UserCacheHome() {
    }
}
