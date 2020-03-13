package com.criteo.publisher.headerbidding;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import com.criteo.publisher.BidManager;
import com.criteo.publisher.Util.Base64;
import com.criteo.publisher.Util.ReflectionUtil;
import com.criteo.publisher.Util.TextUtils;
import com.criteo.publisher.model.AdUnit;
import com.criteo.publisher.model.NativeAssets;
import com.criteo.publisher.model.NativeProduct;
import com.criteo.publisher.model.Slot;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.List;

public class DfpHeaderBidding {

  private static final String CRT_CPM = "crt_cpm";
  private static final String CRT_DISPLAY_URL = "crt_displayurl";
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
  private final BidManager bidManager;

  public DfpHeaderBidding(@NonNull BidManager bidManager) {
    this.bidManager = bidManager;
  }

  public void enrichBid(@NonNull Object object, @Nullable AdUnit adUnit) {
    Slot slot = bidManager.getBidForAdUnitAndPrefetch(adUnit);
    if (slot == null) {
      return;
    }

    ReflectionUtil.callMethodOnObject(object, "addCustomTargeting", CRT_CPM, slot.getCpm());

    if (slot.isNative()) {
      enrichNativeRequest(object, slot);
    } else {
      enrichDfpRequest(object, slot);
    }
  }

  private void enrichDfpRequest(@NonNull Object object, @NonNull Slot slot) {
    checkAndReflect(object, slot.getDisplayUrl(), CRT_DISPLAY_URL);
  }

  private void enrichNativeRequest(@NonNull Object object, @NonNull Slot slot) {
    NativeAssets nativeAssets = slot.getNativeAssets();
    if (nativeAssets == null) {
      return;
    }

    // Inject first product fields (there is no room for more product, so we rely on CDB logic to
    // only bid on one product).
    List<NativeProduct> products = nativeAssets.getNativeProducts();
    if (products != null && !products.isEmpty()) {
      NativeProduct product = products.get(0);

      checkAndReflect(object, product.getTitle(), CRT_NATIVE_TITLE);
      checkAndReflect(object, product.getDescription(), CRT_NATIVE_DESC);
      checkAndReflect(object, product.getPrice(), CRT_NATIVE_PRICE);
      checkAndReflect(object, product.getClickUrl(), CRT_NATIVE_CLICK_URL);
      checkAndReflect(object, product.getCallToAction(), CRT_NATIVE_CTA);
      checkAndReflect(object, product.getImageUrl(), CRT_NATIVE_IMAGE_URL);
    }

    // Inject advertiser fields
    checkAndReflect(object, nativeAssets.getAdvertiserDescription(), CRT_NATIVE_ADV_NAME);
    checkAndReflect(object, nativeAssets.getAdvertiserDomain(), CRT_NATIVE_ADV_DOMAIN);
    checkAndReflect(object, nativeAssets.getAdvertiserLogoUrl(), CRT_NATIVE_ADV_LOGO_URL);
    checkAndReflect(object, nativeAssets.getAdvertiserLogoClickUrl(), CRT_NATIVE_ADV_URL);

    // Inject privacy fields
    checkAndReflect(object, nativeAssets.getPrivacyOptOutClickUrl(), CRT_NATIVE_PR_URL);
    checkAndReflect(object, nativeAssets.getPrivacyOptOutImageUrl(), CRT_NATIVE_PR_IMAGE_URL);
    checkAndReflect(object, nativeAssets.getPrivacyLongLegalText(), CRT_NATIVE_PR_TEXT);

    // Inject impression pixels
    List<String> impressionPixels = nativeAssets.getImpressionPixels();
    if (impressionPixels != null) {
      for (int i = 0; i < impressionPixels.size(); i++) {
        checkAndReflect(object, impressionPixels.get(i), CRT_NATIVE_PIXEL_URL + i);
      }

      ReflectionUtil.callMethodOnObject(object, "addCustomTargeting", CRT_NATIVE_PIXEL_COUNT,
          impressionPixels.size() + "");
    }
  }

  private void checkAndReflect(
      @NonNull Object object,
      @Nullable String value,
      @NonNull String enrichmentKey
  ) {
    if (!TextUtils.isEmpty(value)) {
      ReflectionUtil.callMethodOnObject(object, "addCustomTargeting", enrichmentKey,
          createDfpCompatibleString(value));
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

}
