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

import android.content.res.Configuration;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import com.criteo.publisher.model.AdUnit;
import com.criteo.publisher.model.BannerAdUnit;
import com.criteo.publisher.model.InterstitialAdUnit;
import com.criteo.publisher.model.Slot;
import com.criteo.publisher.model.nativeads.NativeAssets;
import com.criteo.publisher.model.nativeads.NativeProduct;
import com.criteo.publisher.util.AndroidUtil;
import com.criteo.publisher.util.Base64;
import com.criteo.publisher.util.DeviceUtil;
import com.criteo.publisher.util.TextUtils;
import com.google.android.gms.ads.doubleclick.PublisherAdRequest;
import com.google.android.gms.ads.doubleclick.PublisherAdRequest.Builder;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.List;

public class DfpHeaderBidding implements HeaderBiddingHandler {

  private static final String CRT_CPM = "crt_cpm";
  private static final String CRT_DISPLAY_URL = "crt_displayurl";
  private static final String CRT_SIZE = "crt_size";
  private static final String CRT_NATIVE_TITLE = "crtn_title";
  private static final String CRT_NATIVE_DESC = "crtn_desc";
  private static final String CRT_NATIVE_PRICE = "crtn_price";
  private static final String CRT_NATIVE_CLICK_URL = "crtn_clickurl";
  private static final String CRT_NATIVE_CTA = "crtn_cta";
  private static final String CRT_NATIVE_IMAGE_URL = "crtn_imageurl";
  private static final String CRT_NATIVE_ADV_NAME = "crtn_advname";
  private static final String CRT_NATIVE_ADV_DOMAIN = "crtn_advdomain";
  private static final String CRT_NATIVE_ADV_LOGO_URL = "crtn_advlogourl";
  private static final String CRT_NATIVE_ADV_URL = "crtn_advurl";
  private static final String CRT_NATIVE_PR_URL = "crtn_prurl";
  private static final String CRT_NATIVE_PR_IMAGE_URL = "crtn_primageurl";
  private static final String CRT_NATIVE_PR_TEXT = "crtn_prtext";
  private static final String CRT_NATIVE_PIXEL_URL = "crtn_pixurl_";
  private static final String CRT_NATIVE_PIXEL_COUNT = "crtn_pixcount";

  @NonNull
  private final AndroidUtil androidUtil;

  @NonNull
  private final DeviceUtil deviceUtil;

  public DfpHeaderBidding(
      @NonNull AndroidUtil androidUtil,
      @NonNull DeviceUtil deviceUtil
  ) {
    this.androidUtil = androidUtil;
    this.deviceUtil = deviceUtil;
  }

  @Override
  public boolean canHandle(@NonNull Object object) {
    return SafeDfpBuilder.isDfpBuilder(object);
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

    SafeDfpBuilder builder = new SafeDfpBuilder((Builder) object);
    builder.addCustomTargeting(CRT_CPM, slot.getCpm());

    if (slot.isNative()) {
      enrichNativeRequest(builder, slot);
    } else if (adUnit instanceof BannerAdUnit) {
      checkAndReflect(builder, slot.getDisplayUrl(), CRT_DISPLAY_URL);
      builder.addCustomTargeting(CRT_SIZE, slot.getWidth() + "x" + slot.getHeight());
    } else if (adUnit instanceof InterstitialAdUnit) {
      checkAndReflect(builder, slot.getDisplayUrl(), CRT_DISPLAY_URL);
      builder.addCustomTargeting(CRT_SIZE, getDfpSizeForInterstitial(slot));
    }
  }

  /**
   * Return constant sizes for interstitial:
   * <ul>
   *   <li>320x480 (portrait) or 480x320 (landscape) for mobile devices</li>
   *   <li>768x1024 (portrait) and 1024x768 (landscape) for tablets</li>
   *   <li>If a device isn't big enough to fit a 1024x768 or 768x1024, it will fall back to the 320x480 or 480x320 size</li>
   * </ul>
   * <p>
   * Those sizes are constant because they have a meaning in DFP.
   */
  @NonNull
  private String getDfpSizeForInterstitial(@NonNull Slot slot) {
    boolean isPortrait = androidUtil.getOrientation() == Configuration.ORIENTATION_PORTRAIT;

    if (deviceUtil.isTablet()) {
      // This dimension are when device is in landscape and should be transposed for portrait.
      int minTabletWidth = 1024;
      int minTabletHeight = 768;

      if (isPortrait && slot.getWidth() >= minTabletHeight && slot.getHeight() >= minTabletWidth) {
        return "768x1024";
      } else if (!isPortrait && slot.getWidth() >= minTabletWidth && slot.getHeight() >= minTabletHeight) {
        return "1024x768";
      }
    }

    if (isPortrait) {
      return "320x480";
    } else {
      return "480x320";
    }
  }

  private void enrichNativeRequest(@NonNull SafeDfpBuilder builder, @NonNull Slot slot) {
    NativeAssets nativeAssets = slot.getNativeAssets();
    if (nativeAssets == null) {
      return;
    }

    NativeProduct product = nativeAssets.getProduct();
    checkAndReflect(builder, product.getTitle(), CRT_NATIVE_TITLE);
    checkAndReflect(builder, product.getDescription(), CRT_NATIVE_DESC);
    checkAndReflect(builder, product.getPrice(), CRT_NATIVE_PRICE);
    checkAndReflect(builder, product.getClickUrl().toString(), CRT_NATIVE_CLICK_URL);
    checkAndReflect(builder, product.getCallToAction(), CRT_NATIVE_CTA);
    checkAndReflect(builder, product.getImageUrl().toString(), CRT_NATIVE_IMAGE_URL);

    // Inject advertiser fields
    checkAndReflect(builder, nativeAssets.getAdvertiserDescription(), CRT_NATIVE_ADV_NAME);
    checkAndReflect(builder, nativeAssets.getAdvertiserDomain(), CRT_NATIVE_ADV_DOMAIN);
    checkAndReflect(builder, nativeAssets.getAdvertiserLogoUrl().toString(), CRT_NATIVE_ADV_LOGO_URL);
    checkAndReflect(builder, nativeAssets.getAdvertiserLogoClickUrl().toString(), CRT_NATIVE_ADV_URL);

    // Inject privacy fields
    checkAndReflect(builder, nativeAssets.getPrivacyOptOutClickUrl().toString(), CRT_NATIVE_PR_URL);
    checkAndReflect(builder, nativeAssets.getPrivacyOptOutImageUrl().toString(), CRT_NATIVE_PR_IMAGE_URL);
    checkAndReflect(builder, nativeAssets.getPrivacyLongLegalText(), CRT_NATIVE_PR_TEXT);

    // Inject impression pixels
    List<URL> impressionPixels = nativeAssets.getImpressionPixels();
    for (int i = 0; i < impressionPixels.size(); i++) {
      checkAndReflect(builder, impressionPixels.get(i).toString(), CRT_NATIVE_PIXEL_URL + i);
    }

    builder.addCustomTargeting(CRT_NATIVE_PIXEL_COUNT, impressionPixels.size() + "");
  }

  private void checkAndReflect(
      @NonNull SafeDfpBuilder builder,
      @Nullable String value,
      @NonNull String key
  ) {
    if (!TextUtils.isEmpty(value)) {
      builder.addCustomTargeting(key, createDfpCompatibleString(value));
    }
  }

  @VisibleForTesting
  String createDfpCompatibleString(@Nullable String stringToEncode) {
    if (TextUtils.isEmpty(stringToEncode)) {
      return null;
    }

    try {
      byte[] byteUrl = stringToEncode.getBytes(Charset.forName("UTF-8"));
      String base64Url = base64(byteUrl);
      String utf8 = Charset.forName("UTF-8").name();
      return URLEncoder.encode(URLEncoder.encode(base64Url, utf8), utf8);
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
    return null;
  }

  @VisibleForTesting
  String base64(byte[] input) {
    return Base64.encodeToString(input, Base64.NO_WRAP);
  }

  private static class SafeDfpBuilder {

    @NonNull
    private final PublisherAdRequest.Builder builder;

    private SafeDfpBuilder(@NonNull Builder builder) {
      this.builder = builder;
    }

    static boolean isDfpBuilder(@NonNull Object candidate) {
      try {
        return candidate instanceof Builder;
      } catch (LinkageError e) {
        return false;
      }
    }

    void addCustomTargeting(String key, String value) {
      try {
        builder.addCustomTargeting(key, value);
      } catch (LinkageError e) {
        Log.d(SafeDfpBuilder.class.getSimpleName(), "Error while adding custom target", e);
      }
    }
  }

}
