/*
 * Copyright 2018 Ben Manes. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.benmanes.caffeine.cache;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A semi-persistent mapping from keys to values. Cache entries are manually added using
 * {@link #get(Object, Function)} or {@link #put(Object, CompletableFuture)}, and are stored in the
 * cache until either evicted or manually invalidated.
 * <p>
 * Implementations of this interface are expected to be thread-safe, and can be safely accessed by
 * multiple concurrent threads.
 *
 * @author ben.manes@gmail.com (Ben Manes)
 * @param <K> the type of keys maintained by this cache
 * @param <V> the type of mapped values
 */
public interface AsyncCache<K, V> {

    /**
     * Returns the future associated with {@code key} in this cache, or {@code null} if there is no
     * cached future for {@code key}.
     *
     * @param key key whose associated value is to be returned
     * @return the current (existing or computed) future value to which the specified key is mapped,
     *         or {@code null} if this map contains no mapping for the key
     * @throws NullPointerException if the specified key is null
     */
    @Nullable Object get(@Nullable Object key);

    /**
     * Returns the future associated with {@code key} in this cache, obtaining that value from
     * {@code mappingFunction} if necessary. This method provides a simple substitute for the
     * conventional "if cached, return; otherwise create, cache and return" pattern.
     * <p>
     * If the specified key is not already associated with a value, attempts to compute its value
     * asynchronously and enters it into this cache unless {@code null}. The entire method invocation
     * is performed atomically, so the function is applied at most once per key. If the asynchronous
     * computation fails, the entry will be automatically removed from this cache.
     * <p>
     * <b>Warning:</b> as with {@link CacheLoader#load}, {@code mappingFunction} <b>must not</b>
     * attempt to update any other mappings of this cache.
     *
     * @param key key with which the specified value is to be associated
     * @param mappingFunction the function to asynchronously compute a value
     * @return the current (existing or computed) future value associated with the specified key
     * @throws NullPointerException if the specified key or mappingFunction is null
     */
    @NonNull
    CompletableFuture<V> get(@NonNull K key, @NonNull Function<? super K, ? extends V> mappingFunction);

    /**
     * Returns the future associated with {@code key} in this cache, obtaining that value from
     * {@code mappingFunction} if necessary. This method provides a simple substitute for the
     * conventional "if cached, return; otherwise create, cache and return" pattern.
     * <p>
     * If the specified key is not already associated with a value, attempts to compute its value
     * asynchronously and enters it into this cache unless {@code null}. The entire method invocation
     * is performed atomically, so the function is applied at most once per key. If the asynchronous
     * computation fails, the entry will be automatically removed from this cache.
     * <p>
     * <b>Warning:</b> as with {@link CacheLoader#load}, {@code mappingFunction} <b>must not</b>
     * attempt to update any other mappings of this cache.
     *
     * @param key key with which the specified value is to be associated
     * @param mappingFunction the function to asynchronously compute a value
     * @return the current (existing or computed) future value associated with the specified key
     * @throws NullPointerException if the specified key or mappingFunction is null, or if the
     *         future returned by the mappingFunction is null
     * @throws RuntimeException or Error if the mappingFunction does when constructing the future,
     *         in which case the mapping is left unestablished
     */
    @NonNull
    CompletableFuture<V> get(@NonNull K key, @NonNull BiFunction<? super K, Executor, CompletableFuture<V>> mappingFunction);

    /**
     * Associates {@code value} with {@code key} in this cache. If the cache previously contained a
     * value associated with {@code key}, the old value is replaced by {@code value}. If the
     * asynchronous computation fails, the entry will be automatically removed.
     * <p>
     * Prefer {@link #get(Object, Function)} when using the conventional "if cached, return; otherwise
     * create, cache and return" pattern.
     *
     * @param key key with which the specified value is to be associated
     * @param valueFuture value to be associated with the specified key
     * @throws NullPointerException if the specified key or value is null
     */
    void put(@NonNull K key, @NonNull CompletableFuture<V> valueFuture);

    /**
     * Returns a view of the entries stored in this cache as a synchronous {@link Cache}. A mapping is
     * not present if the value is currently being loaded. Modifications made to the synchronous cache
     * directly affect the asynchronous cache. If a modification is made to a mapping that is
     * currently loading, the operation blocks until the computation completes.
     *
     * @return a thread-safe synchronous view of this cache
     */
    @NonNull
    Cache<K, V> synchronous();
}
