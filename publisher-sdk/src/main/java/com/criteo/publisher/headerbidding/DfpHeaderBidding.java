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
import android.util.Base64;
import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import com.criteo.publisher.integration.Integration;
import com.criteo.publisher.logging.Logger;
import com.criteo.publisher.logging.LoggerFactory;
import com.criteo.publisher.model.CdbResponseSlot;
import com.criteo.publisher.model.nativeads.NativeAssets;
import com.criteo.publisher.model.nativeads.NativeProduct;
import com.criteo.publisher.util.AdUnitType;
import com.criteo.publisher.util.AndroidUtil;
import com.criteo.publisher.util.DeviceUtil;
import com.criteo.publisher.util.PreconditionsUtil;
import com.criteo.publisher.util.TextUtils;
import com.google.android.gms.ads.admanager.AdManagerAdRequest;
import com.google.android.gms.ads.admanager.AdManagerAdRequest.Builder;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.List;

public class DfpHeaderBidding implements HeaderBiddingHandler {

  private static final String CRT_CPM = "crt_cpm";
  private static final String CRT_DISPLAY_URL = "crt_displayurl";
  private static final String CRT_SIZE = "crt_size";
  private static final String CRT_FORMAT = "crt_format";
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

  private static final String VIDEO = "video";

  @NonNull
  private final AndroidUtil androidUtil;

  @NonNull
  private final DeviceUtil deviceUtil;

  @NonNull
  private final Logger logger = LoggerFactory.getLogger(getClass());

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

  @NonNull
  @Override
  public Integration getIntegration() {
    return Integration.GAM_APP_BIDDING;
  }

  @Override
  public void cleanPreviousBid(@NonNull Object object) {
    // Do nothing: DFP bid object is a builder object. By definition, it should not be reused by
    // publisher. And it's an append only object so removing data is not supported.
    // Moreover, doing not make Auto-Refreshing Ads work.
  }

  @Override
  public void enrichBid(
      @NonNull Object object,
      @NonNull AdUnitType adUnitType,
      @NonNull CdbResponseSlot slot
  ) {
    SafeDfpBuilder builder = SafeDfpBuilder.newBuilder(object);
    if (builder == null) {
      return;
    }

    builder.addCustomTargeting(CRT_CPM, slot.getCpm());

    switch (adUnitType) {
      case CRITEO_BANNER:
        addEncodedDisplayUrl(builder, slot);
        builder.addCustomTargeting(CRT_SIZE, slot.getWidth() + "x" + slot.getHeight());
        break;
      case CRITEO_INTERSTITIAL:
      case CRITEO_REWARDED:
        addEncodedDisplayUrl(builder, slot);
        builder.addCustomTargeting(CRT_SIZE, getDfpSizeForFullScreen(slot));
        break;
      case CRITEO_CUSTOM_NATIVE:
        enrichNativeRequest(builder, slot);
        break;
    }

    if (slot.isVideo()) {
      builder.addCustomTargeting(CRT_FORMAT, VIDEO);
    }

    logger.log(AppBiddingLogMessage.onAdObjectEnrichedSuccessfully(getIntegration(), builder.getDescription()));
  }

  private void addEncodedDisplayUrl(@NonNull SafeDfpBuilder builder, @NonNull CdbResponseSlot slot) {
    String displayUrl = slot.getDisplayUrl();
    if (TextUtils.isEmpty(displayUrl)) {
      return;
    }

    String encodedDisplayUrl;
    if (slot.isVideo()) {
      try {
        encodedDisplayUrl = encode(encode(displayUrl));
      } catch (UnsupportedEncodingException e) {
        PreconditionsUtil.throwOrLog(e);
        return;
      }
    } else {
      encodedDisplayUrl = createDfpCompatibleString(displayUrl);
    }

    builder.addCustomTargeting(CRT_DISPLAY_URL, encodedDisplayUrl);
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
  private String getDfpSizeForFullScreen(@NonNull CdbResponseSlot slot) {
    boolean isPortrait = androidUtil.getOrientation() == Configuration.ORIENTATION_PORTRAIT;

    if (deviceUtil.isTablet()) {
      // This dimension are when device is in landscape and should be transposed for portrait.
      int minTabletWidth = 1024;
      int minTabletHeight = 768;

      if (isPortrait && slot.getWidth() >= minTabletHeight && slot.getHeight() >= minTabletWidth) {
        return "768x1024";
      } else if (!isPortrait && slot.getWidth() >= minTabletWidth
          && slot.getHeight() >= minTabletHeight) {
        return "1024x768";
      }
    }

    if (isPortrait) {
      return "320x480";
    } else {
      return "480x320";
    }
  }

  private void enrichNativeRequest(@NonNull SafeDfpBuilder builder, @NonNull CdbResponseSlot slot) {
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

    byte[] byteUrl = stringToEncode.getBytes(Charset.forName("UTF-8"));
    String base64Url = Base64.encodeToString(byteUrl, Base64.NO_WRAP);

    try {
      return encode(encode(base64Url));
    } catch (UnsupportedEncodingException e) {
      PreconditionsUtil.throwOrLog(e);
    }

    return null;
  }

  @NonNull
  String encode(@NonNull String encoded) throws UnsupportedEncodingException {
    return URLEncoder.encode(encoded, Charset.forName("UTF-8").name());
  }

  private static abstract class SafeDfpBuilder {

    @NonNull
    private final String name;

    @NonNull
    private final StringBuilder description;

    private SafeDfpBuilder(@NonNull String name) {
      this.name = name;
      this.description = new StringBuilder();
    }

    static boolean isDfpBuilder(@NonNull Object candidate) {
      return SafeDfp20Builder.isDfpBuilder(candidate) || SafeDfp19Builder.isDfpBuilder(candidate);
    }

    @Nullable
    static SafeDfpBuilder newBuilder(@NonNull Object candidate) {
      if (SafeDfp20Builder.isDfpBuilder(candidate)) {
        return new SafeDfp20Builder((Builder) candidate);
      } else if (SafeDfp19Builder.isDfpBuilder(candidate)) {
        return new SafeDfp19Builder(candidate);
      }
      return null;
    }

    @CallSuper
    protected void addCustomTargeting(String key, String value) {
      if (description.length() != 0) {
        description.append(",");
      } else {
        description.append(name).append(':');
      }
      description.append(key).append("=").append(value);
    }

    final String getDescription() {
      return description.toString();
    }
  }

  private static class SafeDfp20Builder extends SafeDfpBuilder {

    @NonNull
    private final AdManagerAdRequest.Builder builder;

    private SafeDfp20Builder(@NonNull Builder builder) {
      super("AdMob20");
      this.builder = builder;
    }

    static boolean isDfpBuilder(@NonNull Object candidate) {
      try {
        return candidate instanceof Builder;
      } catch (LinkageError e) {
        return false;
      }
    }

    @Override
    protected void addCustomTargeting(String key, String value) {
      try {
        builder.addCustomTargeting(key, value);
      } catch (LinkageError e) {
        PreconditionsUtil.throwOrLog(e);
        return;
      }

      super.addCustomTargeting(key, value);
    }
  }

  private static class SafeDfp19Builder extends SafeDfpBuilder {

    @Nullable
    private static Class<?> builderClass;

    @Nullable
    private static Method addCustomTargeting;

    @NonNull
    private final Object builder;

    private SafeDfp19Builder(@NonNull Object builder) {
      super("AdMob19");
      this.builder = builder;
    }

    static boolean isDfpBuilder(@NonNull Object candidate) {
      return initStaticState(candidate.getClass().getClassLoader()) && builderClass.isAssignableFrom(candidate.getClass());
    }

    @Override
    protected void addCustomTargeting(String key, String value) {
      try {
        addCustomTargeting.invoke(builder, key, value);
      } catch (IllegalAccessException e) {
        PreconditionsUtil.throwOrLog(e);
      } catch (InvocationTargetException e) {
        PreconditionsUtil.throwOrLog(e);
      }

      super.addCustomTargeting(key, value);
    }

    private static boolean initStaticState(ClassLoader classLoader) {
      if (builderClass != null && addCustomTargeting != null) {
        return true;
      }

      try {
        builderClass = Class.forName(
            "com.google.android.gms.ads.doubleclick.PublisherAdRequest$Builder",
            false,
            classLoader
        );

        addCustomTargeting = builderClass.getMethod("addCustomTargeting", String.class, String.class);

        return true;
      } catch (ClassNotFoundException e) {
        // This is normal if AdMob 19 is not in the classpath
      } catch (NoSuchMethodException e) {
        PreconditionsUtil.throwOrLog(e);
      }

      return false;
    }
  }

}
