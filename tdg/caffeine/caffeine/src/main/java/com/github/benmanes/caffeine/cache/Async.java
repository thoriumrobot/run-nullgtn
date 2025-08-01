/*
 * Copyright 2015 Ben Manes. All Rights Reserved.
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

import static com.github.benmanes.caffeine.cache.BoundedLocalCache.MAXIMUM_EXPIRY;
import static java.util.Objects.requireNonNull;
import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Static utility methods and classes pertaining to asynchronous operations.
 *
 * @author ben.manes@gmail.com (Ben Manes)
 */
final class Async {

    // 220 years
    static final long ASYNC_EXPIRY = (Long.MAX_VALUE >> 1) + (Long.MAX_VALUE >> 2);

    private Async() {
    }

    /**
     * Returns if the future has successfully completed.
     */
    @Nullable static boolean isReady(@Nullable CompletableFuture<?> future) {
        return (future != null) && future.isDone() && !future.isCompletedExceptionally() && (future.join() != null);
    }

    /**
     * Returns the current value or null if either not done or failed.
     */
    @SuppressWarnings("NullAway")
    @Nullable static <V> V getIfReady(@Nullable CompletableFuture<V> future) {
        return isReady(future) ? future.join() : null;
    }

    /**
     * Returns the value when completed successfully or null if failed.
     */
    @Nullable static <V> V getWhenSuccessful(@Nullable CompletableFuture<V> future) {
        try {
            return (future == null) ? null : future.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        } catch (ExecutionException e) {
            return null;
        }
    }

    /**
     * A removal listener that asynchronously forwards the value stored in a {@link CompletableFuture}
     * if successful to the user-supplied removal listener.
     */
    static final class AsyncRemovalListener<K, V> implements RemovalListener<K, CompletableFuture<V>>, Serializable {

        private static final long serialVersionUID = 1L;

        @Nullable final RemovalListener<K, V> delegate;

        final Executor executor;

        AsyncRemovalListener(RemovalListener<K, V> delegate, Executor executor) {
            this.delegate = requireNonNull(delegate);
            this.executor = requireNonNull(executor);
        }

        @Override
        @SuppressWarnings("FutureReturnValueIgnored")
        public void onRemoval(@Nullable K key, @Nullable CompletableFuture<V> future, RemovalCause cause) {
            if (future != null) {
                future.thenAcceptAsync(value -> delegate.onRemoval(key, value, cause), executor);
            }
        }

        Object writeReplace() {
            return delegate;
        }
    }

    /**
     * A weigher for asynchronous computations. When the value is being loaded this weigher returns
     * {@code 0} to indicate that the entry should not be evicted due to a size constraint. If the
     * value is computed successfully the entry must be reinserted so that the weight is updated and
     * the expiration timeouts reflect the value once present. This can be done safely using
     * {@link Map#replace(Object, Object, Object)}.
     */
    static final class AsyncWeigher<K, V> implements Weigher<K, CompletableFuture<V>>, Serializable {

        private static final long serialVersionUID = 1L;

        final Weigher<K, V> delegate;

        AsyncWeigher(Weigher<K, V> delegate) {
            this.delegate = requireNonNull(delegate);
        }

        @Override
        public int weigh(@Nullable K key, @Nullable CompletableFuture<V> future) {
            return isReady(future) ? delegate.weigh(key, future.join()) : 0;
        }

        Object writeReplace() {
            return delegate;
        }
    }

    /**
     * An expiry for asynchronous computations. When the value is being loaded this expiry returns
     * {@code ASYNC_EXPIRY} to indicate that the entry should not be evicted due to an expiry
     * constraint. If the value is computed successfully the entry must be reinserted so that the
     * expiration is updated and the expiration timeouts reflect the value once present. The value
     * maximum range is reserved to coordinate the asynchronous life cycle.
     */
    static final class AsyncExpiry<K, V> implements Expiry<K, CompletableFuture<V>>, Serializable {

        private static final long serialVersionUID = 1L;

        final Expiry<K, V> delegate;

        AsyncExpiry(Expiry<K, V> delegate) {
            this.delegate = requireNonNull(delegate);
        }

        @Override
        public long expireAfterCreate(K key, @Nullable CompletableFuture<V> future, long currentTime) {
            if (isReady(future)) {
                long duration = delegate.expireAfterCreate(key, future.join(), currentTime);
                return Math.min(duration, MAXIMUM_EXPIRY);
            }
            return ASYNC_EXPIRY;
        }

        @Override
        public long expireAfterUpdate(K key, @Nullable CompletableFuture<V> future, @Nullable long currentTime, long currentDuration) {
            if (isReady(future)) {
                long duration = (currentDuration > MAXIMUM_EXPIRY) ? delegate.expireAfterCreate(key, future.join(), currentTime) : delegate.expireAfterUpdate(key, future.join(), currentTime, currentDuration);
                return Math.min(duration, MAXIMUM_EXPIRY);
            }
            return ASYNC_EXPIRY;
        }

        @Override
        @Nullable public long expireAfterRead(K key, CompletableFuture<V> future, long currentTime, @Nullable long currentDuration) {
            if (isReady(future)) {
                long duration = delegate.expireAfterRead(key, future.join(), currentTime, currentDuration);
                return Math.min(duration, MAXIMUM_EXPIRY);
            }
            return ASYNC_EXPIRY;
        }

        Object writeReplace() {
            return delegate;
        }
    }
}
