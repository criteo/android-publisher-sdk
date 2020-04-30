package com.criteo.publisher.headerbidding;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import android.os.Bundle;
import com.criteo.publisher.BidManager;
import com.criteo.publisher.model.AdSize;
import com.criteo.publisher.model.AdUnit;
import com.criteo.publisher.model.BannerAdUnit;
import com.criteo.publisher.model.InterstitialAdUnit;
import com.criteo.publisher.model.Slot;
import com.criteo.publisher.model.nativeads.NativeAssets;
import com.criteo.publisher.model.nativeads.NativeProduct;
import com.google.android.gms.ads.doubleclick.PublisherAdRequest;
import java.net.URI;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

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

  @Mock
  private BidManager bidManager;

  private DfpHeaderBidding headerBidding;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    headerBidding = new DfpHeaderBidding(bidManager);
  }

  @Test
  public void isHandling_GivenSimpleObject_ReturnFalse() throws Exception {
    boolean handling = headerBidding.canHandle(mock(Object.class));

    assertFalse(handling);
  }

  @Test
  public void isHandling_GivenDfpBuilder_ReturnTrue() throws Exception {
    PublisherAdRequest.Builder builder = new PublisherAdRequest.Builder();

    boolean handling = headerBidding.canHandle(builder);

    assertTrue(handling);
  }

  @Test
  public void enrichBid_GivenNotHandledObject_DoNothing() throws Exception {
    Object builder = mock(Object.class);

    headerBidding.enrichBid(builder, mock(AdUnit.class));

    verifyNoMoreInteractions(bidManager);
  }

  @Test
  public void enrichBid_GivenDfpBuilderAndNoBidAvailable_DoNotEnrich() throws Exception {
    BannerAdUnit adUnit = new BannerAdUnit("adUnit", new AdSize(1, 2));

    when(bidManager.getBidForAdUnitAndPrefetch(adUnit)).thenReturn(null);

    PublisherAdRequest.Builder builder = new PublisherAdRequest.Builder();
    headerBidding.enrichBid(builder, adUnit);
    PublisherAdRequest request = builder.build();

    assertNull(request.getCustomTargeting().getString(CRT_CPM));
    assertNull(request.getCustomTargeting().getString(CRT_DISPLAY_URL));
    assertNull(request.getCustomTargeting().getString(CRT_SIZE));
  }

  @Test
  public void enrichBid_GivenDfpBuilderAndBannerBidAvailable_EnrichBuilder() throws Exception {
    BannerAdUnit adUnit = new BannerAdUnit("adUnit", new AdSize(42, 1337));

    Slot slot = mock(Slot.class);
    when(slot.isNative()).thenReturn(false);
    when(slot.getCpm()).thenReturn("0.10");
    when(slot.getDisplayUrl()).thenReturn("http://display.url");
    when(slot.getWidth()).thenReturn(42);
    when(slot.getHeight()).thenReturn(1337);

    when(bidManager.getBidForAdUnitAndPrefetch(adUnit)).thenReturn(slot);

    PublisherAdRequest.Builder builder = new PublisherAdRequest.Builder();
    headerBidding.enrichBid(builder, adUnit);
    PublisherAdRequest request = builder.build();
    Bundle customTargeting = request.getCustomTargeting();

    assertEquals(3, customTargeting.size());
    assertEquals("0.10", customTargeting.get(CRT_CPM));
    assertEquals(encodeForDfp("http://display.url"), customTargeting.get(CRT_DISPLAY_URL));
    assertEquals("42x1337", customTargeting.get(CRT_SIZE));
  }

  @Test
  public void enrichBid_GivenDfpBuilderAndInterstitialBidAvailable_EnrichBuilder() throws Exception {
    InterstitialAdUnit adUnit = new InterstitialAdUnit("adUnit");

    Slot slot = mock(Slot.class);
    when(slot.isNative()).thenReturn(false);
    when(slot.getCpm()).thenReturn("0.10");
    when(slot.getDisplayUrl()).thenReturn("http://display.url");

    when(bidManager.getBidForAdUnitAndPrefetch(adUnit)).thenReturn(slot);

    PublisherAdRequest.Builder builder = new PublisherAdRequest.Builder();
    headerBidding.enrichBid(builder, adUnit);
    PublisherAdRequest request = builder.build();
    Bundle customTargeting = request.getCustomTargeting();

    assertEquals(2, customTargeting.size());
    assertEquals("0.10", customTargeting.get(CRT_CPM));
    assertEquals(encodeForDfp("http://display.url"), customTargeting.get(CRT_DISPLAY_URL));
  }

  @Test
  public void enrichBid_GivenDfpBuilderAndNativeBidAvailable_EnrichBuilder() throws Exception {
    BannerAdUnit adUnit = new BannerAdUnit("adUnit", new AdSize(1, 2));

    NativeProduct product = mock(NativeProduct.class);
    when(product.getTitle()).thenReturn("title");
    when(product.getDescription()).thenReturn("description");
    when(product.getPrice()).thenReturn("$1337");
    when(product.getClickUrl()).thenReturn(URI.create("http://click.url").toURL());
    when(product.getImageUrl()).thenReturn(URI.create("http://image.url").toURL());
    when(product.getCallToAction()).thenReturn("call to action");

    NativeAssets nativeAssets = mock(NativeAssets.class);
    when(nativeAssets.getProduct()).thenReturn(product);
    when(nativeAssets.getAdvertiserDescription()).thenReturn("advertiser name");
    when(nativeAssets.getAdvertiserDomain()).thenReturn("advertiser domain");
    when(nativeAssets.getAdvertiserLogoClickUrl()).thenReturn(URI.create("http://advertiser.url").toURL());
    when(nativeAssets.getAdvertiserLogoUrl()).thenReturn(URI.create("http://advertiser.logo.url").toURL());
    when(nativeAssets.getPrivacyOptOutImageUrl()).thenReturn(URI.create("http://privacy.image.url").toURL());
    when(nativeAssets.getPrivacyOptOutClickUrl()).thenReturn(URI.create("http://privacy.url").toURL());
    when(nativeAssets.getPrivacyLongLegalText()).thenReturn("privacy legal text");
    when(nativeAssets.getImpressionPixels()).thenReturn(asList(
        URI.create("http://pixel.url/0").toURL(),
        URI.create("http://pixel.url/1").toURL()));

    Slot slot = mock(Slot.class);
    when(slot.isNative()).thenReturn(true);
    when(slot.getCpm()).thenReturn("0.42");
    when(slot.getNativeAssets()).thenReturn(nativeAssets);

    when(bidManager.getBidForAdUnitAndPrefetch(adUnit)).thenReturn(slot);

    PublisherAdRequest.Builder builder = new PublisherAdRequest.Builder();
    headerBidding.enrichBid(builder, adUnit);
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