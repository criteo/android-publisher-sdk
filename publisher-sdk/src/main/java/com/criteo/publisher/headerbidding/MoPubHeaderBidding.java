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

package com.criteo.publisher.headerbidding;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.criteo.publisher.model.AdUnit;
import com.criteo.publisher.model.BannerAdUnit;
import com.criteo.publisher.model.Slot;
import com.criteo.publisher.util.ReflectionUtil;

public class MoPubHeaderBidding implements HeaderBiddingHandler{

  private static final String MOPUB_ADVIEW_CLASS = "com.mopub.mobileads.MoPubView";
  private static final String MOPUB_INTERSTITIAL_CLASS = "com.mopub.mobileads.MoPubInterstitial";

  private static final String CRT_CPM = "crt_cpm";
  private static final String CRT_DISPLAY_URL = "crt_displayUrl";
  private static final String CRT_SIZE = "crt_size";

  public boolean canHandle(@NonNull Object object) {
    return object.getClass().getName().equals(MOPUB_ADVIEW_CLASS)
        || object.getClass().getName().equals(MOPUB_INTERSTITIAL_CLASS);
  }

  @Override
  public void cleanPreviousBid(@NonNull Object object) {
    // TODO
  }

  @Override
  public void enrichBid(@NonNull Object object, @Nullable AdUnit adUnit, @NonNull Slot slot) {
    if (!canHandle(object)) {
      return;
    }

    StringBuilder keywords = new StringBuilder();
    Object existingKeywords = ReflectionUtil.callMethodOnObject(object, "getKeywords");
    if (existingKeywords != null) {
      keywords.append(existingKeywords);
      keywords.append(",");
    }
    keywords.append(CRT_CPM);
    keywords.append(":");
    keywords.append(slot.getCpm());
    keywords.append(",");
    keywords.append(CRT_DISPLAY_URL);
    keywords.append(":");
    keywords.append(slot.getDisplayUrl());

    if (adUnit instanceof BannerAdUnit) {
      keywords.append(",");
      keywords.append(CRT_SIZE);
      keywords.append(":");
      keywords.append(slot.getWidth()).append("x").append(slot.getHeight());
    }

    ReflectionUtil.callMethodOnObject(object, "setKeywords", keywords.toString());
  }

}
