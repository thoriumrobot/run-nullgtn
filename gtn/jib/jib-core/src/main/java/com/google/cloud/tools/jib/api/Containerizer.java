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
package com.google.cloud.tools.jib.api;

// TODO: Move to com.google.cloud.tools.jib once that package is cleaned up.
import com.google.cloud.tools.jib.configuration.CacheDirectoryCreationException;
import com.google.cloud.tools.jib.event.EventHandlers;
import com.google.cloud.tools.jib.filesystem.UserCacheHome;
import com.google.cloud.tools.jib.image.ImageReference;
import com.google.common.base.Preconditions;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.annotation.Nullable;

/**
 * Configures how to containerize.
 */
// TODO: Add tests once JibContainerBuilder#containerize() is added.
public class Containerizer {

    /**
     * The default directory for caching the base image layers, in {@code [user cache
     * home]/google-cloud-tools-java/jib}.
     */
    // TODO: Reduce scope once plugins are migrated to use the new Jib Core API.
    public static final Path DEFAULT_BASE_CACHE_DIRECTORY = UserCacheHome.getCacheHome().resolve("google-cloud-tools-java").resolve("jib");

    private static final String DEFAULT_TOOL_NAME = "jib-core";

    /**
     * Gets a new {@link Containerizer} that containerizes to a container registry.
     *
     * @param registryImage the {@link RegistryImage} that defines target container registry and
     *     credentials
     * @return a new {@link Containerizer}
     */
    public static Containerizer to(RegistryImage registryImage) {
        return new Containerizer(registryImage);
    }

    /**
     * Gets a new {@link Containerizer} that containerizes to a Docker daemon.
     *
     * @param dockerDaemonImage the {@link DockerDaemonImage} that defines target Docker daemon
     * @return a new {@link Containerizer}
     */
    public static Containerizer to(DockerDaemonImage dockerDaemonImage) {
        return new Containerizer(dockerDaemonImage);
    }

    /**
     * Gets a new {@link Containerizer} that containerizes to a tarball archive.
     *
     * @param tarImage the {@link TarImage} that defines target output file
     * @return a new {@link Containerizer}
     */
    public static Containerizer to(TarImage tarImage) {
        return new Containerizer(tarImage);
    }

    private final TargetImage targetImage;

    private final Set<String> additionalTags = new HashSet<>();

    @Nullable
    private ExecutorService executorService;

    private Path baseImageLayersCacheDirectory = DEFAULT_BASE_CACHE_DIRECTORY;

    @Nullable
    private Path applicationLayersCacheDirectory;

    @Nullable
    private EventHandlers eventHandlers;

    private boolean allowInsecureRegistries = false;

    private String toolName = DEFAULT_TOOL_NAME;

    /**
     * Instantiate with {@link #to}.
     */
    private Containerizer(TargetImage targetImage) {
        this.targetImage = targetImage;
    }

    /**
     * Adds an additional tag to tag the target image with. For example, the following would
     * containerize to both {@code gcr.io/my-project/my-image:tag} and {@code
     * gcr.io/my-project/my-image:tag2}:
     *
     * <pre>{@code
     * Containerizer.to(RegistryImage.named("gcr.io/my-project/my-image:tag")).withAdditionalTag("tag2");
     * }</pre>
     *
     * @param tag the additional tag to push to
     * @return this
     */
    public Containerizer withAdditionalTag(String tag) {
        Preconditions.checkArgument(ImageReference.isValidTag(tag));
        additionalTags.add(tag);
        return this;
    }

    /**
     * Sets the {@link ExecutorService} Jib executes on. Jib, by default, uses {@link
     * Executors#newCachedThreadPool}.
     *
     * @param executorService the {@link ExecutorService}
     * @return this
     */
    public Containerizer setExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
        return this;
    }

    /**
     * Sets the directory to use for caching base image layers. This cache can (and should) be shared
     * between multiple images. The default base image layers cache directory is {@code [user cache
     * home]/google-cloud-tools-java/jib} ({@link #DEFAULT_BASE_CACHE_DIRECTORY}. This directory can
     * be the same directory used for {@link #setApplicationLayersCache}.
     *
     * @param cacheDirectory the cache directory
     * @return this
     */
    public Containerizer setBaseImageLayersCache(Path cacheDirectory) {
        baseImageLayersCacheDirectory = cacheDirectory;
        return this;
    }

    /**
     * Sets the directory to use for caching application layers. This cache can be shared between
     * multiple images. If not set, a temporary directory will be used as the application layers
     * cache. This directory can be the same directory used for {@link #setBaseImageLayersCache}.
     *
     * @param cacheDirectory the cache directory
     * @return this
     */
    public Containerizer setApplicationLayersCache(Path cacheDirectory) {
        applicationLayersCacheDirectory = cacheDirectory;
        return this;
    }

    /**
     * Sets the {@link EventHandlers} to handle events dispatched during Jib's execution.
     *
     * @param eventHandlers the {@link EventHandlers}
     * @return this
     */
    public Containerizer setEventHandlers(EventHandlers eventHandlers) {
        this.eventHandlers = eventHandlers;
        return this;
    }

    /**
     * Sets whether or not to allow communication over HTTP/insecure HTTPS.
     *
     * @param allowInsecureRegistries if {@code true}, insecure connections will be allowed
     * @return this
     */
    public Containerizer setAllowInsecureRegistries(boolean allowInsecureRegistries) {
        this.allowInsecureRegistries = allowInsecureRegistries;
        return this;
    }

    /**
     * Sets the name of the tool that is using Jib Core. The tool name is sent as part of the {@code
     * User-Agent} in registry requests and set as the {@code created_by} in the container layer
     * history. Defaults to {@code jib-core}.
     *
     * @param toolName the name of the tool using this library
     * @return this
     */
    public Containerizer setToolName(String toolName) {
        this.toolName = toolName;
        return this;
    }

    TargetImage getTargetImage() {
        return targetImage;
    }

    Set<String> getAdditionalTags() {
        return additionalTags;
    }

    @Nullable()
    Optional<ExecutorService> getExecutorService() {
        return Optional.ofNullable(executorService);
    }

    Path getBaseImageLayersCacheDirectory() {
        return baseImageLayersCacheDirectory;
    }

    @Nullable()
    Path getApplicationLayersCacheDirectory() throws CacheDirectoryCreationException {
        if (applicationLayersCacheDirectory == null) {
            // Uses a temporary directory if application layers cache directory is not set.
            try {
                Path temporaryDirectory = Files.createTempDirectory(null);
                temporaryDirectory.toFile().deleteOnExit();
                this.applicationLayersCacheDirectory = temporaryDirectory;
            } catch (IOException ex) {
                throw new CacheDirectoryCreationException(ex);
            }
        }
        return applicationLayersCacheDirectory;
    }

    @Nullable()
    Optional<EventHandlers> getEventHandlers() {
        return Optional.ofNullable(eventHandlers);
    }

    boolean getAllowInsecureRegistries() {
        return allowInsecureRegistries;
    }

    String getToolName() {
        return toolName;
    }
}
