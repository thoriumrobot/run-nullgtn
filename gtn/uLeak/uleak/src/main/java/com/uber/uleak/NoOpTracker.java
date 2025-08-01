/*
 * Copyright (C) 2017 Uber Technologies
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
package com.uber.uleak;

import java.util.concurrent.ConcurrentLinkedQueue;
import android.support.annotation.Nullable;

class NoOpTracker implements LeakInstanceTracker {

    @Override
    public void flushAllQueuedReferences(ConcurrentLinkedQueue<ULeak.QueuedLeakEvent> events) {
        events.clear();
    }

    @Override
    public void registerNewReference(@Nullable() Object object, @Nullable() String className, @Nullable() final long timeCreatedMs, @Nullable() final LeakType leakType) {
    }
}
