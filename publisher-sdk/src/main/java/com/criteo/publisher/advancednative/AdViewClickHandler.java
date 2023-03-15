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
import com.criteo.publisher.adview.RedirectionListener;
import java.lang.ref.Reference;
import java.net.URI;

class AdViewClickHandler implements NativeViewClickHandler {

  @NonNull
  private final URI uri;

  @NonNull
  private final Reference<CriteoNativeAdListener> listenerRef;

  @NonNull
  private final ClickHelper helper;

  AdViewClickHandler(
      @NonNull URI uri,
      @NonNull Reference<CriteoNativeAdListener> listenerRef,
      @NonNull ClickHelper helper
  ) {
    this.uri = uri;
    this.listenerRef = listenerRef;
    this.helper = helper;
  }

  @Override
  public void onClick() {
    helper.notifyUserClickAsync(listenerRef.get());

    helper.redirectUserTo(uri, new RedirectionListener() {
      @Override
      public void onUserRedirectedToAd() {
        helper.notifyUserIsLeavingApplicationAsync(listenerRef.get());
      }

      @Override
      public void onRedirectionFailed() {
        // no-op
      }

      @Override
      public void onUserBackFromAd() {
        helper.notifyUserIsBackToApplicationAsync(listenerRef.get());
      }
    });
  }
}
