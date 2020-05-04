package com.criteo.publisher.headerbidding;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.util.Log;
import com.criteo.publisher.model.AdUnit;
import com.criteo.publisher.model.BannerAdUnit;
import com.criteo.publisher.model.InterstitialAdUnit;
import com.criteo.publisher.model.Slot;
import com.criteo.publisher.model.nativeads.NativeAssets;
import com.criteo.publisher.model.nativeads.NativeProduct;
import com.criteo.publisher.util.Base64;
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

  @Override
  public boolean canHandle(@NonNull Object object) {
    return SafeDfpBuilder.isDfpBuilder(object);
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
