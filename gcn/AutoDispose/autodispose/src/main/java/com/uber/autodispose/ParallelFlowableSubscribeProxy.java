package com.uber.autodispose;

import io.reactivex.parallel.ParallelFlowable;
import org.reactivestreams.Subscriber;
import io.reactivex.annotations.Nullable;

/**
 * Subscribe proxy that matches {@link ParallelFlowable}'s subscribe overloads.
 */
public interface ParallelFlowableSubscribeProxy<T> {

    /**
     * Proxy for {@link ParallelFlowable#subscribe(Subscriber[])}.
     */
    void subscribe(@Nullable() Subscriber<? super T>[] subscribers);
}
