/*
 *    Copyright 2020 Criteo
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.criteo.publisher.advancednative;

import androidx.annotation.NonNull;
import java.lang.ref.Reference;
import java.net.URL;
import java.util.concurrent.atomic.AtomicBoolean;

class ImpressionTask implements VisibilityListener {

  @NonNull
  private final Iterable<URL> impressionPixels;

  @NonNull
  private final Reference<CriteoNativeAdListener> listenerRef;

  @NonNull
  private final ImpressionHelper helper;

  @NonNull
  private final AtomicBoolean isAlreadyTriggered;

  ImpressionTask(
      @NonNull Iterable<URL> impressionPixels,
      @NonNull Reference<CriteoNativeAdListener> listenerRef,
      @NonNull ImpressionHelper helper) {
    this.impressionPixels = impressionPixels;
    this.listenerRef = listenerRef;
    this.helper = helper;
    this.isAlreadyTriggered = new AtomicBoolean(false);
  }

  @Override
  public void onVisible() {
    if (!isAlreadyTriggered.compareAndSet(false, true)) {
      return;
    }

    helper.firePixels(impressionPixels);

    CriteoNativeAdListener listener = listenerRef.get();
    if (listener != null) {
      helper.notifyImpression(listener);
    }
  }
}
