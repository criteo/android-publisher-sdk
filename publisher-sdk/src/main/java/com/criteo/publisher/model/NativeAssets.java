package com.criteo.publisher.model;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class NativeAssets {

  private static final String PRODUCTS = "products";
  private static final String DESCRIPTION = "description";
  private static final String NATIVE_URL = "url";
  private static final String ADVERTISER = "advertiser";
  private static final String DOMAIN = "domain";
  private static final String LOGO = "logo";
  private static final String LOGO_CLICK_URL = "logoClickUrl";
  private static final String PRIVACY = "privacy";
  private static final String OPT_OUT_CLICK_URL = "optoutClickUrl";
  private static final String OPT_OUT_IMAGE_URL = "optoutImageUrl";
  private static final String LONG_LEGAL_TEXT = "longLegalText";
  private static final String IMPRESSION_PIXELS = "impressionPixels";
  private static final String HEIGHT = "height";
  private static final String WIDTH = "width";

  public List<NativeProduct> nativeProducts;
  public String advertiserDescription;
  public String advertiserDomain;
  public String advertiserLogoUrl;
  public int advertiserLogoHeight;
  public int advertiserLogoWidth;
  public String advertiserLogoClickUrl;
  public String privacyOptOutClickUrl;
  public String privacyOptOutImageUrl;
  public String privacyLongLegalText;
  public List<String> impressionPixels;

  public NativeAssets(JSONObject jsonNative) throws JSONException {
    // products
    if (jsonNative.has(PRODUCTS)) {
      JSONArray products = jsonNative.getJSONArray(PRODUCTS);
      this.nativeProducts = new ArrayList<>();
      for (int i = 0; i < products.length(); i++) {
        this.nativeProducts.add(new NativeProduct(products.getJSONObject(i)));
      }
    }

    // advertiser
    if (jsonNative.has(ADVERTISER)) {
      JSONObject advertiser = jsonNative.getJSONObject(ADVERTISER);
      this.advertiserDescription = advertiser.optString(DESCRIPTION);
      this.advertiserDomain = advertiser.optString(DOMAIN);
      if (advertiser.has(LOGO)) {
        this.advertiserLogoUrl = advertiser.getJSONObject(LOGO).optString(NATIVE_URL);
        this.advertiserLogoHeight = advertiser.getJSONObject(LOGO).optInt(HEIGHT);
        this.advertiserLogoWidth = advertiser.getJSONObject(LOGO).optInt(WIDTH);
      }
      this.advertiserLogoClickUrl = advertiser.optString(LOGO_CLICK_URL);
    }

    // privacy
    if (jsonNative.has(PRIVACY)) {
      JSONObject privacy = jsonNative.getJSONObject(PRIVACY);
      this.privacyOptOutClickUrl = privacy.optString(OPT_OUT_CLICK_URL);
      this.privacyOptOutImageUrl = privacy.optString(OPT_OUT_IMAGE_URL);
      this.privacyLongLegalText = privacy.optString(LONG_LEGAL_TEXT);
    }

    // impression pixels
    if (jsonNative.has(IMPRESSION_PIXELS)) {
      JSONArray impressionPixels = jsonNative.getJSONArray(IMPRESSION_PIXELS);
      this.impressionPixels = new ArrayList<>();
      for (int i = 0; i < impressionPixels.length(); i++) {
        this.impressionPixels.add(impressionPixels.getJSONObject(i).optString(NATIVE_URL));
      }
    }
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof NativeAssets) {
      NativeAssets other = (NativeAssets) obj;
      return (this.nativeProducts.equals(other.nativeProducts) &&
          (this.advertiserDescription == other.advertiserDescription || this.advertiserDescription
              .equals(other.advertiserDescription)) &&
          (this.advertiserDomain == other.advertiserDomain || this.advertiserDomain
              .equals(other.advertiserDomain)) &&
          (this.advertiserLogoUrl == other.advertiserLogoUrl || this.advertiserLogoUrl
              .equals(other.advertiserLogoUrl)) &&
          this.advertiserLogoHeight == other.advertiserLogoHeight &&
          this.advertiserLogoWidth == other.advertiserLogoWidth &&
          (this.advertiserLogoClickUrl == other.advertiserLogoClickUrl
              || this.advertiserLogoClickUrl.equals(other.advertiserLogoClickUrl)) &&
          (this.privacyOptOutClickUrl == other.privacyOptOutClickUrl || this.privacyOptOutClickUrl
              .equals(other.privacyOptOutClickUrl)) &&
          (this.privacyOptOutImageUrl == other.privacyOptOutImageUrl || this.privacyOptOutImageUrl
              .equals(other.privacyOptOutImageUrl)) &&
          (this.privacyLongLegalText == other.privacyLongLegalText || this.privacyLongLegalText
              .equals(other.privacyLongLegalText)) &&
          this.impressionPixels.equals(other.impressionPixels));
    }
    return false;
  }
}
