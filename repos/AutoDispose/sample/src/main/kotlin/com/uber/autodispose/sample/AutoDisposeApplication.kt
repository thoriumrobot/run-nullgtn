/*
 * Copyright (c) 2018. Uber Technologies
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

package com.uber.autodispose.sample

import android.app.Application
// import com.squareup.leakcanary.LeakCanary

class AutoDisposeApplication: Application() {

  override fun onCreate() {
    super.onCreate()

    // Temporarily disable LeakCanary to fix build issues
    /*
    if (LeakCanary.isInAnalyzerProcess(this)) {
      // Process dedicated to LeakCanary. Shouldn't use
      // this to init app process.
      return
    }
    LeakCanary.install(this)
    */
  }
}
