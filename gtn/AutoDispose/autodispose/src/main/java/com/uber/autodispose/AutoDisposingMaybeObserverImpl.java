/*
 * Copyright (C) 2017. Uber Technologies
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.uber.autodispose;

import com.uber.autodispose.observers.AutoDisposingMaybeObserver;
import io.reactivex.CompletableSource;
import io.reactivex.MaybeObserver;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableCompletableObserver;
import java.util.concurrent.atomic.AtomicReference;
import io.reactivex.annotations.Nullable;

final class AutoDisposingMaybeObserverImpl<T> implements AutoDisposingMaybeObserver<T> {

    // Package private for synthetic accessor saving
    @SuppressWarnings("WeakerAccess")
    final AtomicReference<Disposable> mainDisposable = new AtomicReference<>();

    // Package private for synthetic accessor saving
    @SuppressWarnings("WeakerAccess")
    final AtomicReference<Disposable> scopeDisposable = new AtomicReference<>();

    private final CompletableSource scope;

    private final MaybeObserver<? super T> delegate;

    AutoDisposingMaybeObserverImpl(CompletableSource scope, MaybeObserver<? super T> delegate) {
        this.scope = scope;
        this.delegate = delegate;
    }

    @Override
    public MaybeObserver<? super T> delegateObserver() {
        return delegate;
    }

    @Override
    public void onSubscribe(final Disposable d) {
        DisposableCompletableObserver o = new DisposableCompletableObserver() {

            @Override
            public void onError(Throwable e) {
                scopeDisposable.lazySet(AutoDisposableHelper.DISPOSED);
                AutoDisposingMaybeObserverImpl.this.onError(e);
            }

            @Override
            public void onComplete() {
                scopeDisposable.lazySet(AutoDisposableHelper.DISPOSED);
                AutoDisposableHelper.dispose(mainDisposable);
            }
        };
        if (AutoDisposeEndConsumerHelper.setOnce(scopeDisposable, o, getClass())) {
            delegate.onSubscribe(this);
            scope.subscribe(o);
            AutoDisposeEndConsumerHelper.setOnce(mainDisposable, d, getClass());
        }
    }

    @Override
    public boolean isDisposed() {
        return mainDisposable.get() == AutoDisposableHelper.DISPOSED;
    }

    @Override
    public void dispose() {
        AutoDisposableHelper.dispose(scopeDisposable);
        AutoDisposableHelper.dispose(mainDisposable);
    }

    @Override
    public void onSuccess(T value) {
        if (!isDisposed()) {
            mainDisposable.lazySet(AutoDisposableHelper.DISPOSED);
            AutoDisposableHelper.dispose(scopeDisposable);
            delegate.onSuccess(value);
        }
    }

    @Override
    public void onError(Throwable e) {
        if (!isDisposed()) {
            mainDisposable.lazySet(AutoDisposableHelper.DISPOSED);
            AutoDisposableHelper.dispose(scopeDisposable);
            delegate.onError(e);
        }
    }

    @Override
    public void onComplete() {
        if (!isDisposed()) {
            mainDisposable.lazySet(AutoDisposableHelper.DISPOSED);
            AutoDisposableHelper.dispose(scopeDisposable);
            delegate.onComplete();
        }
    }
}
