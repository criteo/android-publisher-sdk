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

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import android.content.res.Configuration;
import android.os.Bundle;
import com.criteo.publisher.integration.Integration;
import com.criteo.publisher.mock.MockBean;
import com.criteo.publisher.mock.MockedDependenciesRule;
import com.criteo.publisher.model.AdSize;
import com.criteo.publisher.model.AdUnit;
import com.criteo.publisher.model.BannerAdUnit;
import com.criteo.publisher.model.CdbResponseSlot;
import com.criteo.publisher.model.InterstitialAdUnit;
import com.criteo.publisher.model.nativeads.NativeAssets;
import com.criteo.publisher.model.nativeads.NativeProduct;
import com.criteo.publisher.util.AndroidUtil;
import com.criteo.publisher.util.DeviceUtil;
import com.google.android.gms.ads.doubleclick.PublisherAdRequest;
import java.net.URI;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

/**
 * This is an instrumented test because DFP use Android objects
 */
public class DfpHeaderBiddingTest {

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

  private static final String MOBILE_INTERSTITIAL_PORTRAIT_SIZE = "320x480";
  private static final String MOBILE_INTERSTITIAL_LANDSCAPE_SIZE = "480x320";
  private static final String TABLET_INTERSTITIAL_PORTRAIT_SIZE = "768x1024";
  private static final String TABLET_INTERSTITIAL_LANDSCAPE_SIZE = "1024x768";

  @Rule
  public MockedDependenciesRule mockedDependenciesRule = new MockedDependenciesRule();

  @MockBean
  private AndroidUtil androidUtil;

  @MockBean
  private DeviceUtil deviceUtil;

  private DfpHeaderBidding headerBidding;

  @Before
  public void setUp() throws Exception {
    headerBidding = new DfpHeaderBidding(androidUtil, deviceUtil);
  }

  @Test
  public void getIntegration_ReturnGamAppBidding() throws Exception {
    Integration integration = headerBidding.getIntegration();

    assertThat(integration).isEqualTo(Integration.GAM_APP_BIDDING);
  }

  @Test
  public void canHandle_GivenSimpleObject_ReturnFalse() throws Exception {
    boolean handling = headerBidding.canHandle(mock(Object.class));

    assertFalse(handling);
  }

  @Test
  public void canHandle_GivenDfpBuilder_ReturnTrue() throws Exception {
    PublisherAdRequest.Builder builder = new PublisherAdRequest.Builder();

    boolean handling = headerBidding.canHandle(builder);

    assertTrue(handling);
  }

  @Test
  public void enrichBid_GivenNotHandledObject_DoNothing() throws Exception {
    Object builder = mock(Object.class);

    headerBidding.enrichBid(builder, mock(AdUnit.class), mock(CdbResponseSlot.class));

    verifyNoMoreInteractions(builder);
  }

  @Test
  public void enrichBid_GivenDfpBuilderAndBannerBidAvailable_EnrichBuilder() throws Exception {
    BannerAdUnit adUnit = new BannerAdUnit("adUnit", new AdSize(42, 1337));

    CdbResponseSlot slot = mock(CdbResponseSlot.class);
    when(slot.isNative()).thenReturn(false);
    when(slot.getCpm()).thenReturn("0.10");
    when(slot.getDisplayUrl()).thenReturn("http://display.url");
    when(slot.getWidth()).thenReturn(42);
    when(slot.getHeight()).thenReturn(1337);

    PublisherAdRequest.Builder builder = new PublisherAdRequest.Builder();
    headerBidding.enrichBid(builder, adUnit, slot);
    PublisherAdRequest request = builder.build();
    Bundle customTargeting = request.getCustomTargeting();

    assertEquals(3, customTargeting.size());
    assertEquals("0.10", customTargeting.get(CRT_CPM));
    assertEquals(encodeForDfp("http://display.url"), customTargeting.get(CRT_DISPLAY_URL));
    assertEquals("42x1337", customTargeting.get(CRT_SIZE));
  }

  @Test
  public void enrichBid_GivenInterstitialBidAvailableOnMobileInPortrait_EnrichBuilder() throws Exception {
    when(deviceUtil.isTablet()).thenReturn(false);
    when(androidUtil.getOrientation()).thenReturn(Configuration.ORIENTATION_PORTRAIT);

    enrichBid_GivenInterstitialBidAvailable_EnrichBuilder(42, 1337, MOBILE_INTERSTITIAL_PORTRAIT_SIZE);
  }

  @Test
  public void enrichBid_GivenInterstitialBidAvailableOnMobileInLandscape_EnrichBuilder() throws Exception {
    when(deviceUtil.isTablet()).thenReturn(false);
    when(androidUtil.getOrientation()).thenReturn(Configuration.ORIENTATION_LANDSCAPE);

    enrichBid_GivenInterstitialBidAvailable_EnrichBuilder(42, 1337, MOBILE_INTERSTITIAL_LANDSCAPE_SIZE);
  }

  @Test
  public void enrichBid_GivenInterstitialBidAvailableOnSmallTabletInPortrait_EnrichBuilderAndFallbackOnMobileSize() throws Exception {
    when(deviceUtil.isTablet()).thenReturn(true);
    when(androidUtil.getOrientation()).thenReturn(Configuration.ORIENTATION_PORTRAIT);

    enrichBid_GivenInterstitialBidAvailable_EnrichBuilder(767, 1023, MOBILE_INTERSTITIAL_PORTRAIT_SIZE);
  }

  @Test
  public void enrichBid_GivenInterstitialBidAvailableOnSmallTabletInLandscape_EnrichBuilderAndFallbackOnMobileSize() throws Exception {
    when(deviceUtil.isTablet()).thenReturn(true);
    when(androidUtil.getOrientation()).thenReturn(Configuration.ORIENTATION_LANDSCAPE);

    enrichBid_GivenInterstitialBidAvailable_EnrichBuilder(1023, 767, MOBILE_INTERSTITIAL_LANDSCAPE_SIZE);
  }

  @Test
  public void enrichBid_GivenInterstitialBidAvailableOnBigTabletInPortrait_EnrichBuilder() throws Exception {
    when(deviceUtil.isTablet()).thenReturn(true);
    when(androidUtil.getOrientation()).thenReturn(Configuration.ORIENTATION_PORTRAIT);

    enrichBid_GivenInterstitialBidAvailable_EnrichBuilder(768, 1024, TABLET_INTERSTITIAL_PORTRAIT_SIZE);
  }

  @Test
  public void enrichBid_GivenInterstitialBidAvailableOnBigTabletInLandscape_EnrichBuilder() throws Exception {
    when(deviceUtil.isTablet()).thenReturn(true);
    when(androidUtil.getOrientation()).thenReturn(Configuration.ORIENTATION_LANDSCAPE);

    enrichBid_GivenInterstitialBidAvailable_EnrichBuilder(1024, 768, TABLET_INTERSTITIAL_LANDSCAPE_SIZE);
  }

  private void enrichBid_GivenInterstitialBidAvailable_EnrichBuilder(int slotWidth, int slotHeight, String expectedInjectedSize) {
    InterstitialAdUnit adUnit = new InterstitialAdUnit("adUnit");

    CdbResponseSlot slot = mock(CdbResponseSlot.class);
    when(slot.isNative()).thenReturn(false);
    when(slot.getCpm()).thenReturn("0.10");
    when(slot.getDisplayUrl()).thenReturn("http://display.url");
    when(slot.getWidth()).thenReturn(slotWidth);
    when(slot.getHeight()).thenReturn(slotHeight);

    PublisherAdRequest.Builder builder = new PublisherAdRequest.Builder();
    headerBidding.enrichBid(builder, adUnit, slot);
    PublisherAdRequest request = builder.build();
    Bundle customTargeting = request.getCustomTargeting();

    assertEquals(3, customTargeting.size());
    assertEquals("0.10", customTargeting.get(CRT_CPM));
    assertEquals(encodeForDfp("http://display.url"), customTargeting.get(CRT_DISPLAY_URL));
    assertEquals(expectedInjectedSize, customTargeting.get(CRT_SIZE));
  }

  @Test
  public void enrichBid_GivenDfpBuilderAndNativeBidAvailable_EnrichBuilder() throws Exception {
    BannerAdUnit adUnit = new BannerAdUnit("adUnit", new AdSize(1, 2));

    NativeProduct product = mock(NativeProduct.class);
    when(product.getTitle()).thenReturn("title");
    when(product.getDescription()).thenReturn("description");
    when(product.getPrice()).thenReturn("$1337");
    when(product.getClickUrl()).thenReturn(URI.create("http://click.url"));
    when(product.getImageUrl()).thenReturn(URI.create("http://image.url").toURL());
    when(product.getCallToAction()).thenReturn("call to action");

    NativeAssets nativeAssets = mock(NativeAssets.class);
    when(nativeAssets.getProduct()).thenReturn(product);
    when(nativeAssets.getAdvertiserDescription()).thenReturn("advertiser name");
    when(nativeAssets.getAdvertiserDomain()).thenReturn("advertiser domain");
    when(nativeAssets.getAdvertiserLogoClickUrl()).thenReturn(URI.create("http://advertiser.url"));
    when(nativeAssets.getAdvertiserLogoUrl()).thenReturn(URI.create("http://advertiser.logo.url").toURL());
    when(nativeAssets.getPrivacyOptOutImageUrl()).thenReturn(URI.create("http://privacy.image.url").toURL());
    when(nativeAssets.getPrivacyOptOutClickUrl()).thenReturn(URI.create("http://privacy.url"));
    when(nativeAssets.getPrivacyLongLegalText()).thenReturn("privacy legal text");
    when(nativeAssets.getImpressionPixels()).thenReturn(asList(
        URI.create("http://pixel.url/0").toURL(),
        URI.create("http://pixel.url/1").toURL()));

    CdbResponseSlot slot = mock(CdbResponseSlot.class);
    when(slot.isNative()).thenReturn(true);
    when(slot.getCpm()).thenReturn("0.42");
    when(slot.getNativeAssets()).thenReturn(nativeAssets);

    PublisherAdRequest.Builder builder = new PublisherAdRequest.Builder();
    headerBidding.enrichBid(builder, adUnit, slot);
    PublisherAdRequest request = builder.build();
    Bundle customTargeting = request.getCustomTargeting();

    assertEquals(17, customTargeting.size());
    assertEquals("0.42", customTargeting.get(CRT_CPM));

    assertEquals(
        encodeForDfp("title"),
        customTargeting.get(CRT_NATIVE_TITLE));

    assertEquals(
        encodeForDfp("description"),
        customTargeting.get(CRT_NATIVE_DESC));

    assertEquals(
        encodeForDfp("$1337"),
        customTargeting.get(CRT_NATIVE_PRICE));

    assertEquals(
        encodeForDfp("http://click.url"),
        customTargeting.get(CRT_NATIVE_CLICK_URL));

    assertEquals(
        encodeForDfp("call to action"),
        customTargeting.get(CRT_NATIVE_CTA));

    assertEquals(
        encodeForDfp("http://image.url"),
        customTargeting.get(CRT_NATIVE_IMAGE_URL));

    assertEquals(
        encodeForDfp("advertiser name"),
        customTargeting.get(CRT_NATIVE_ADV_NAME));

    assertEquals(
        encodeForDfp("advertiser domain"),
        customTargeting.get(CRT_NATIVE_ADV_DOMAIN));

    assertEquals(
        encodeForDfp("http://advertiser.logo.url"),
        customTargeting.get(CRT_NATIVE_ADV_LOGO_URL));

    assertEquals(
        encodeForDfp("http://advertiser.url"),
        customTargeting.get(CRT_NATIVE_ADV_URL));

    assertEquals(
        encodeForDfp("http://privacy.url"),
        customTargeting.get(CRT_NATIVE_PR_URL));

    assertEquals(
        encodeForDfp("http://privacy.image.url"),
        customTargeting.get(CRT_NATIVE_PR_IMAGE_URL));

    assertEquals(
        encodeForDfp("privacy legal text"),
        customTargeting.get(CRT_NATIVE_PR_TEXT));

    assertEquals(
        encodeForDfp("http://pixel.url/0"),
        customTargeting.get(CRT_NATIVE_PIXEL_URL + "0"));

    assertEquals(
        encodeForDfp("http://pixel.url/1"),
        customTargeting.get(CRT_NATIVE_PIXEL_URL + "1"));

    assertEquals("2", customTargeting.get(CRT_NATIVE_PIXEL_COUNT));
  }

  @Test
  public void createDfpCompatibleString_GivenNull_ReturnNull() {
    assertNull(headerBidding.createDfpCompatibleString(null));
  }

  @Test
  public void createDfpCompatibleString_GivenExpectedInput_ReturnExpectedOutput() {
    String displayUrl = "https://ads.us.criteo.com/delivery/r/ajs.php?did=5c560a19383b7ad93bb37508deb03a00&u=%7CHX1eM0zpPitVbf0xT24vaM6U4AiY1TeYgfjDUVVbdu4%3D%7C&c1=eG9IAZIK2MKnlif_A3VZ1-8PEx5_bFVofQVrPPiKhda8JkCsKWBsD2zYvC_F9owWsiKQANPjzJs2iM3m5bCHei3w1zNKxtB3Cx_TBleNKtL5VK1aqyK68XTa0A43qlwLNaStT5NXB3Mz7kx6fDZ20Rh6eAGAW2F9SXVN_7xiLgP288-4OqtK-R7pziZDS04LRUhkL7ohLmAFFyVuwQTREHbpx-4NoonsiQRHKn7ZkuIqZR_rqEewHQ2YowxbI3EOowxo6OV50faWCc7QO5M388FHv8NxeOgOH03LHZT_a2PEKF1xh0-G_qdu5wiyGjJYyPEoNVxB0OaEnDaFVtM7cVaHDm4jrjKlfFhtIGuJb8mg2EeHN0mhUL_0eyv9xWUUQ6osYh3B-jiawHq4592kDDCpS2kYYeqR073IOoRNFNRCR7Fnl0yhIA";
    String expectedEncodedValue = "aHR0cHM6Ly9hZHMudXMuY3JpdGVvLmNvbS9kZWxpdmVyeS9yL2Fqcy5waHA%252FZGlkPTVjNTYwYTE5MzgzYjdhZDkzYmIzNzUwOGRlYjAzYTAwJnU9JTdDSFgxZU0wenBQaXRWYmYweFQyNHZhTTZVNEFpWTFUZVlnZmpEVVZWYmR1NCUzRCU3QyZjMT1lRzlJQVpJSzJNS25saWZfQTNWWjEtOFBFeDVfYkZWb2ZRVnJQUGlLaGRhOEprQ3NLV0JzRDJ6WXZDX0Y5b3dXc2lLUUFOUGp6SnMyaU0zbTViQ0hlaTN3MXpOS3h0QjNDeF9UQmxlTkt0TDVWSzFhcXlLNjhYVGEwQTQzcWx3TE5hU3RUNU5YQjNNejdreDZmRFoyMFJoNmVBR0FXMkY5U1hWTl83eGlMZ1AyODgtNE9xdEstUjdwemlaRFMwNExSVWhrTDdvaExtQUZGeVZ1d1FUUkVIYnB4LTROb29uc2lRUkhLbjdaa3VJcVpSX3JxRWV3SFEyWW93eGJJM0VPb3d4bzZPVjUwZmFXQ2M3UU81TTM4OEZIdjhOeGVPZ09IMDNMSFpUX2EyUEVLRjF4aDAtR19xZHU1d2l5R2pKWXlQRW9OVnhCME9hRW5EYUZWdE03Y1ZhSERtNGpyaktsZkZodElHdUpiOG1nMkVlSE4wbWhVTF8wZXl2OXhXVVVRNm9zWWgzQi1qaWF3SHE0NTkya0REQ3BTMmtZWWVxUjA3M0lPb1JORk5SQ1I3Rm5sMHloSUE%253D";

    String encodedValue = headerBidding.createDfpCompatibleString(displayUrl);
    assertEquals(encodedValue, expectedEncodedValue);
  }

  private String encodeForDfp(String displayUrl) {
    return headerBidding.createDfpCompatibleString(displayUrl);
  }

}