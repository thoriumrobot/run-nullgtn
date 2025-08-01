package com.uber.autodispose;

import io.reactivex.CompletableSource;
import io.reactivex.parallel.ParallelFlowable;
import org.reactivestreams.Subscriber;
import io.reactivex.annotations.Nullable;

final class AutoDisposeParallelFlowable<T> extends ParallelFlowable<T> {

    private final ParallelFlowable<T> source;

    private final CompletableSource scope;

    AutoDisposeParallelFlowable(ParallelFlowable<T> source, CompletableSource scope) {
        this.source = source;
        this.scope = scope;
    }

    @Override
    public void subscribe(Subscriber<? super T>[] subscribers) {
        if (!validate(subscribers)) {
            return;
        }
        @SuppressWarnings("unchecked")
        Subscriber<? super T>[] newSubscribers = new Subscriber[subscribers.length];
        for (int i = 0; i < subscribers.length; i++) {
            AutoDisposingSubscriberImpl<? super T> subscriber = new AutoDisposingSubscriberImpl<>(scope, subscribers[i]);
            newSubscribers[i] = subscriber;
        }
        source.subscribe(newSubscribers);
    }

    @Override
    public int parallelism() {
        return source.parallelism();
    }
}
