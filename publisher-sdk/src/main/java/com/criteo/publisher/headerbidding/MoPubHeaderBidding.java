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

import static com.criteo.publisher.util.ReflectionUtil.isInstanceOf;

import androidx.annotation.NonNull;
import com.criteo.publisher.integration.Integration;
import com.criteo.publisher.logging.Logger;
import com.criteo.publisher.logging.LoggerFactory;
import com.criteo.publisher.model.CdbResponseSlot;
import com.criteo.publisher.util.AdUnitType;
import com.criteo.publisher.util.ReflectionUtil;
import java.util.Arrays;
import java.util.List;

public class MoPubHeaderBidding implements HeaderBiddingHandler {

  private static final String MOPUB_ADVIEW_CLASS = "com.mopub.mobileads.MoPubView";
  private static final String MOPUB_INTERSTITIAL_CLASS = "com.mopub.mobileads.MoPubInterstitial";

  private static final String CRT_CPM = "crt_cpm";
  private static final String CRT_DISPLAY_URL = "crt_displayUrl";
  private static final String CRT_SIZE = "crt_size";

  private static final List<String> CRITEO_KEYWORDS = Arrays.asList(
      CRT_CPM,
      CRT_DISPLAY_URL,
      CRT_SIZE
  );

  public final Logger logger = LoggerFactory.getLogger(getClass());

  public boolean canHandle(@NonNull Object object) {
    return isInstanceOf(object, MOPUB_ADVIEW_CLASS)
        || isInstanceOf(object, MOPUB_INTERSTITIAL_CLASS);
  }

  @NonNull
  @Override
  public Integration getIntegration() {
    return Integration.MOPUB_APP_BIDDING;
  }

  @Override
  public void cleanPreviousBid(@NonNull Object object) {
    if (!canHandle(object)) {
      return;
    }

    String keywords = (String) ReflectionUtil.callMethodOnObject(object, "getKeywords");
    if (keywords == null) {
      return;
    }

    String cleanedKeywords = removeCriteoKeywords(keywords);
    ReflectionUtil.callMethodOnObject(object, "setKeywords", cleanedKeywords);
  }

  @NonNull
  private String removeCriteoKeywords(@NonNull String keywords) {
    // clean previous Criteo keywords starting with "crt_"
    StringBuilder cleanedKeywords = new StringBuilder();
    for (String keyword : keywords.split(",")) {
      if (!isCriteoKeyword(keyword)) {
        cleanedKeywords.append(keyword).append(",");
      }
    }
    return cleanedKeywords.toString().replaceAll(",$", "");
  }

  private boolean isCriteoKeyword(@NonNull String keyword) {
    for (String criteoKeyword : CRITEO_KEYWORDS) {
      if (keyword.startsWith(criteoKeyword + ":")) {
        return true;
      }
    }
    return false;
  }

  @Override
  public void enrichBid(
      @NonNull Object object,
      @NonNull AdUnitType adUnitType,
      @NonNull CdbResponseSlot slot
  ) {
    if (!canHandle(object)) {
      return;
    }

    StringBuilder keywords = new StringBuilder();
    keywords.append(CRT_CPM);
    keywords.append(":");
    keywords.append(slot.getCpm());
    keywords.append(",");
    keywords.append(CRT_DISPLAY_URL);
    keywords.append(":");
    keywords.append(slot.getDisplayUrl());

    if (adUnitType == AdUnitType.CRITEO_BANNER) {
      keywords.append(",");
      keywords.append(CRT_SIZE);
      keywords.append(":");
      keywords.append(slot.getWidth()).append("x").append(slot.getHeight());
    }

    Object existingKeywords = ReflectionUtil.callMethodOnObject(object, "getKeywords");
    String newKeywords;
    if (existingKeywords != null) {
      newKeywords = existingKeywords + "," + keywords.toString();
    } else {
      newKeywords = keywords.toString();
    }

    ReflectionUtil.callMethodOnObject(object, "setKeywords", newKeywords);

    logger.log(AppBiddingLogMessage.onAdObjectEnrichedSuccessfully(getIntegration(), keywords.toString()));
  }

}
