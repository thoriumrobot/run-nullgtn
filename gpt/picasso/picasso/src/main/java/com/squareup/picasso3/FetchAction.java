/*
 * Copyright (C) 2013 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.squareup.picasso3;

import androidx.annotation.Nullable;

class FetchAction extends Action {

    @Nullable
    private Callback callback;

    FetchAction(Picasso picasso, Request data, Callback callback) {
        super(picasso, data);
        this.callback = callback;
    }

    @Override
    void complete(RequestHandler.Result result) {
        if (callback != null) {
            callback.onSuccess();
        }
    }

    @Override
    void error(Exception e) {
        if (callback != null) {
            callback.onError(e);
        }
    }

    @Override
    Object getTarget() {
        return this;
    }

    @Override
    void cancel() {
        super.cancel();
        callback = null;
    }
}
