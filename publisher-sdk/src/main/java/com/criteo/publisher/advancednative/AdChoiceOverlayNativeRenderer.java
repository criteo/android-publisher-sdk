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

import static com.criteo.publisher.annotation.Internal.ADMOB_ADAPTER;
import static com.criteo.publisher.annotation.Internal.MOPUB_ADAPTER;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.criteo.publisher.DependencyProvider;
import com.criteo.publisher.annotation.Internal;

@Keep
class AdChoiceOverlayNativeRenderer implements CriteoNativeRenderer {

  @NonNull
  private final CriteoNativeRenderer delegate;

  @NonNull
  private final AdChoiceOverlay adChoiceOverlay;

  /**
   * Used by MoMediation adapter because it has no access to the dependency provider.
   *
   * As this constructor eagerly accesses the dependency provider, an exception will be thrown if
   * the publisher did not call the SDK initialisation (but with a fancy message indicating how to
   * fix the issue).
   */
  @Internal({MOPUB_ADAPTER, ADMOB_ADAPTER})
  AdChoiceOverlayNativeRenderer(@NonNull CriteoNativeRenderer delegate) {
    this(delegate, DependencyProvider.getInstance().provideAdChoiceOverlay());
  }

  AdChoiceOverlayNativeRenderer(
      @NonNull CriteoNativeRenderer delegate,
      @NonNull AdChoiceOverlay adChoiceOverlay
  ) {
    this.delegate = delegate;
    this.adChoiceOverlay = adChoiceOverlay;
  }

  @NonNull
  @Override
  public View createNativeView(
      @NonNull Context context,
      @Nullable ViewGroup parent
  ) {
    return adChoiceOverlay.addOverlay(delegate.createNativeView(context, parent));
  }

  @Override
  public void renderNativeView(
      @NonNull RendererHelper helper,
      @NonNull View nativeView,
      @NonNull CriteoNativeAd nativeAd
  ) {
    View delegateView = adChoiceOverlay.getInitialView(nativeView);
    if (delegateView != null) {
      delegate.renderNativeView(helper, delegateView, nativeAd);
    }
  }
}
