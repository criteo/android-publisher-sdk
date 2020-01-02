package com.criteo.publisher.integration;

import static com.criteo.publisher.CriteoUtil.givenInitializedCriteo;
import static com.criteo.publisher.StubConstants.STUB_CREATIVE_IMAGE;
import static com.criteo.publisher.StubConstants.STUB_DISPLAY_URL;
import static com.criteo.publisher.StubConstants.STUB_NATIVE_ASSETS;
import static com.criteo.publisher.ThreadingUtil.runOnMainThreadAndWait;
import static com.criteo.publisher.ThreadingUtil.waitForAllThreads;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.os.Bundle;
import android.support.test.rule.ActivityTestRule;
import android.view.View;
import com.criteo.publisher.Criteo;
import com.criteo.publisher.TestAdUnits;
import com.criteo.publisher.Util.MockedDependenciesRule;
import com.criteo.publisher.Util.WebViewLookup;
import com.criteo.publisher.mock.ResultCaptor;
import com.criteo.publisher.model.AdUnit;
import com.criteo.publisher.model.BannerAdUnit;
import com.criteo.publisher.model.CdbResponse;
import com.criteo.publisher.model.InterstitialAdUnit;
import com.criteo.publisher.model.NativeAdUnit;
import com.criteo.publisher.model.NativeAssets;
import com.criteo.publisher.model.NativeProduct;
import com.criteo.publisher.test.activity.DummyActivity;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.doubleclick.PublisherAdRequest;
import com.google.android.gms.ads.doubleclick.PublisherAdRequest.Builder;
import com.google.android.gms.ads.doubleclick.PublisherAdView;
import com.google.android.gms.ads.doubleclick.PublisherInterstitialAd;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class DfpHeaderBiddingFunctionalTest {

  private static final String MACRO_CPM = "crt_cpm";
  private static final String MACRO_DISPLAY_URL = "crt_displayurl";
  private static final String MACRO_NATIVE_TITLE = "crtn_title";
  private static final String MACRO_NATIVE_DESCRIPTION = "crtn_desc";
  private static final String MACRO_NATIVE_IMAGE = "crtn_imageurl";
  private static final String MACRO_NATIVE_PRICE = "crtn_price";
  private static final String MACRO_NATIVE_CLICK = "crtn_clickurl";
  private static final String MACRO_NATIVE_CTA = "crtn_cta";
  private static final String MACRO_NATIVE_ADVERTISER_NAME = "crtn_advname";
  private static final String MACRO_NATIVE_ADVERTISER_DOMAIN = "crtn_advdomain";
  private static final String MACRO_NATIVE_ADVERTISER_LOGO = "crtn_advlogourl";
  private static final String MACRO_NATIVE_ADVERTISER_CLICK = "crtn_advurl";
  private static final String MACRO_NATIVE_PRIVACY_LINK = "crtn_prurl";
  private static final String MACRO_NATIVE_PRIVACY_IMAGE = "crtn_primageurl";
  private static final String MACRO_NATIVE_PRIVACY_LEGAL = "crtn_prtext";
  private static final String MACRO_NATIVE_PIXEL_COUNT = "crtn_pixcount";
  private static final String MACRO_NATIVE_PIXEL_1 = "crtn_pixurl_0";
  private static final String MACRO_NATIVE_PIXEL_2 = "crtn_pixurl_1";

  private static final String DFP_BANNER_ID = "/140800857/Endeavour_320x50";
  private static final String DFP_INTERSTITIAL_ID = "/140800857/Endeavour_Interstitial_320x480";
  private static final String DFP_NATIVE_ID = "/140800857/Endeavour_Native";

  /**
   * Time (in milliseconds) to wait for the interstitial activity of DFP to open itself after
   * calling {@link PublisherInterstitialAd#show()}.
   */
  private static final long DFP_INTERSTITIAL_OPENING_TIMEOUT_MS = 1000;

  /**
   * Charset used to encode/decode data with DFP.
   */
  private static final Charset CHARSET = StandardCharsets.UTF_8;

  @Rule
  public MockedDependenciesRule mockedDependenciesRule = new MockedDependenciesRule();

  @Rule
  public ActivityTestRule<DummyActivity> activityRule = new ActivityTestRule<>(DummyActivity.class);

  private final BannerAdUnit validBannerAdUnit = TestAdUnits.BANNER_320_50;
  private final BannerAdUnit invalidBannerAdUnit = TestAdUnits.BANNER_UNKNOWN;
  private final BannerAdUnit demoBannerAdUnit = TestAdUnits.BANNER_DEMO;

  private final InterstitialAdUnit validInterstitialAdUnit = TestAdUnits.INTERSTITIAL;
  private final InterstitialAdUnit invalidInterstitialAdUnit = TestAdUnits.INTERSTITIAL_UNKNOWN;
  private final InterstitialAdUnit demoInterstitialAdUnit = TestAdUnits.INTERSTITIAL_DEMO;

  private final NativeAdUnit validNativeAdUnit = TestAdUnits.NATIVE;
  private final NativeAdUnit invalidNativeAdUnit = TestAdUnits.NATIVE_UNKNOWN;

  private final WebViewLookup webViewLookup = new WebViewLookup();

  private Context context;

  @Before
  public void setUp() throws Exception {
    context = activityRule.getActivity().getApplicationContext();
  }

  @Test
  public void whenGettingBid_GivenValidCpIdAndPrefetchValidBannerId_CriteoMacroAreInjectedInDfpBuilder()
      throws Exception {
    whenGettingBid_GivenValidCpIdAndPrefetchValidAdUnit_CriteoMacroAreInjectedInDfpBuilder(
        validBannerAdUnit);
  }

  @Test
  public void whenGettingBid_GivenValidCpIdAndPrefetchValidInterstitialId_CriteoMacroAreInjectedInDfpBuilder()
      throws Exception {
    whenGettingBid_GivenValidCpIdAndPrefetchValidAdUnit_CriteoMacroAreInjectedInDfpBuilder(
        validInterstitialAdUnit);
  }

  private void whenGettingBid_GivenValidCpIdAndPrefetchValidAdUnit_CriteoMacroAreInjectedInDfpBuilder(AdUnit adUnit)
      throws Exception {
    givenInitializedCriteo(adUnit);
    waitForBids();

    Builder builder = new Builder();

    Criteo.getInstance().setBidsForAdUnit(builder, adUnit);

    assertCriteoMacroAreInjectedInDfpBuilder(builder);
  }

  @Test
  public void whenGettingBid_GivenValidCpIdAndPrefetchValidNativeId_CriteoNativeMacroAreInjectedInDfpBuilder()
      throws Exception {
    givenInitializedCriteo(validNativeAdUnit);
    waitForBids();

    Builder builder = new Builder();

    Criteo.getInstance().setBidsForAdUnit(builder, validNativeAdUnit);

    Bundle customTargeting = builder.build().getCustomTargeting();

    // Note: CDB stub does not return any legal text in the payload.
    // But this may be covered by unit tests.
    assertNotNull(customTargeting.getString(MACRO_CPM));
    assertNull(customTargeting.getString(MACRO_DISPLAY_URL));
    assertNotNull(customTargeting.getString(MACRO_NATIVE_TITLE));
    assertNotNull(customTargeting.getString(MACRO_NATIVE_DESCRIPTION));
    assertNotNull(customTargeting.getString(MACRO_NATIVE_IMAGE));
    assertNotNull(customTargeting.getString(MACRO_NATIVE_PRICE));
    assertNotNull(customTargeting.getString(MACRO_NATIVE_CLICK));
    assertNotNull(customTargeting.getString(MACRO_NATIVE_CTA));
    assertNotNull(customTargeting.getString(MACRO_NATIVE_ADVERTISER_NAME));
    assertNotNull(customTargeting.getString(MACRO_NATIVE_ADVERTISER_DOMAIN));
    assertNotNull(customTargeting.getString(MACRO_NATIVE_ADVERTISER_LOGO));
    assertNotNull(customTargeting.getString(MACRO_NATIVE_ADVERTISER_CLICK));
    assertNotNull(customTargeting.getString(MACRO_NATIVE_PRIVACY_LINK));
    assertNotNull(customTargeting.getString(MACRO_NATIVE_PRIVACY_IMAGE));
    assertNotNull(customTargeting.getString(MACRO_NATIVE_PIXEL_1));
    assertNotNull(customTargeting.getString(MACRO_NATIVE_PIXEL_2));
    assertEquals("2", customTargeting.getString(MACRO_NATIVE_PIXEL_COUNT));
    assertEquals(16, customTargeting.size());
  }

  @Test
  public void whenGettingBid_GivenValidCpIdAndPrefetchInvalidBannerId_CriteoMacroAreNotInjectedInDfpBuilder()
      throws Exception {
    whenGettingBid_GivenValidCpIdAndPrefetchInvalidAdUnit_CriteoMacroAreNotInjectedInDfpBuilder(
        invalidBannerAdUnit);
  }

  @Test
  public void whenGettingBid_GivenValidCpIdAndPrefetchInvalidInterstitialId_CriteoMacroAreNotInjectedInDfpBuilder()
      throws Exception {
    whenGettingBid_GivenValidCpIdAndPrefetchInvalidAdUnit_CriteoMacroAreNotInjectedInDfpBuilder(
        invalidInterstitialAdUnit);
  }

  @Test
  public void whenGettingBid_GivenValidCpIdAndPrefetchInvalidNativeId_CriteoMacroAreNotInjectedInDfpBuilder()
      throws Exception {
    whenGettingBid_GivenValidCpIdAndPrefetchInvalidAdUnit_CriteoMacroAreNotInjectedInDfpBuilder(
        invalidNativeAdUnit);
  }

  private void whenGettingBid_GivenValidCpIdAndPrefetchInvalidAdUnit_CriteoMacroAreNotInjectedInDfpBuilder(AdUnit adUnit)
      throws Exception {
    givenInitializedCriteo(adUnit);
    waitForBids();

    Builder builder = new Builder();

    Criteo.getInstance().setBidsForAdUnit(builder, adUnit);

    Bundle customTargeting = builder.build().getCustomTargeting();

    assertNull(customTargeting.getString(MACRO_CPM));
    assertNull(customTargeting.getString(MACRO_DISPLAY_URL));
    assertNull(customTargeting.getString(MACRO_NATIVE_TITLE));
    assertNull(customTargeting.getString(MACRO_NATIVE_DESCRIPTION));
    assertNull(customTargeting.getString(MACRO_NATIVE_IMAGE));
    assertNull(customTargeting.getString(MACRO_NATIVE_PRICE));
    assertNull(customTargeting.getString(MACRO_NATIVE_CLICK));
    assertNull(customTargeting.getString(MACRO_NATIVE_CTA));
    assertNull(customTargeting.getString(MACRO_NATIVE_ADVERTISER_NAME));
    assertNull(customTargeting.getString(MACRO_NATIVE_ADVERTISER_DOMAIN));
    assertNull(customTargeting.getString(MACRO_NATIVE_ADVERTISER_LOGO));
    assertNull(customTargeting.getString(MACRO_NATIVE_ADVERTISER_CLICK));
    assertNull(customTargeting.getString(MACRO_NATIVE_PRIVACY_LINK));
    assertNull(customTargeting.getString(MACRO_NATIVE_PRIVACY_IMAGE));
    assertNull(customTargeting.getString(MACRO_NATIVE_PRIVACY_LEGAL));
    assertNull(customTargeting.getString(MACRO_NATIVE_PIXEL_1));
    assertNull(customTargeting.getString(MACRO_NATIVE_PIXEL_2));
    assertNull(customTargeting.getString(MACRO_NATIVE_PIXEL_COUNT));
    assertEquals(0, customTargeting.size());
  }

  @Test
  public void whenGettingTestBid_GivenValidCpIdAndPrefetchDemoBannerId_CriteoMacroAreInjectedInDfpBuilder()
      throws Exception {
    whenGettingTestBid_GivenValidCpIdAndPrefetchDemoAdUnit_CriteoMacroAreInjectedInDfpBuilder(
        demoBannerAdUnit);
  }

  @Test
  public void whenGettingTestBid_GivenValidCpIdAndPrefetchDemoInterstitialId_CriteoMacroAreInjectedInDfpBuilder()
      throws Exception {
    whenGettingTestBid_GivenValidCpIdAndPrefetchDemoAdUnit_CriteoMacroAreInjectedInDfpBuilder(
        demoInterstitialAdUnit);
  }

  private void whenGettingTestBid_GivenValidCpIdAndPrefetchDemoAdUnit_CriteoMacroAreInjectedInDfpBuilder(AdUnit adUnit)
      throws Exception {
    givenInitializedCriteo(adUnit);
    waitForBids();

    Builder builder = new Builder();

    Criteo.getInstance().setBidsForAdUnit(builder, adUnit);

    assertCriteoMacroAreInjectedInDfpBuilder(builder);

    // The amount is not that important, but the format is
    String cpm = builder.build().getCustomTargeting().getString(MACRO_CPM);
    assertEquals("20.00", cpm);
  }

  @Test
  public void whenEnrichingDisplayUrl_GivenValidCpIdAndPrefetchBannerId_DisplayUrlIsEncodedInASpecificManner()
      throws Exception {
    whenEnrichingDisplayUrl_GivenValidCpIdAndPrefetchAdUnit_DisplayUrlIsEncodedInASpecificManner(
        validBannerAdUnit);
  }

  @Test
  public void whenEnrichingDisplayUrl_GivenValidCpIdAndPrefetchInterstitialId_DisplayUrlIsEncodedInASpecificManner()
      throws Exception {
    whenEnrichingDisplayUrl_GivenValidCpIdAndPrefetchAdUnit_DisplayUrlIsEncodedInASpecificManner(
        validInterstitialAdUnit);
  }

  private void whenEnrichingDisplayUrl_GivenValidCpIdAndPrefetchAdUnit_DisplayUrlIsEncodedInASpecificManner(
      AdUnit adUnit)
      throws Exception {
    givenInitializedCriteo(adUnit);
    waitForBids();

    Builder builder = new Builder();

    Criteo.getInstance().setBidsForAdUnit(builder, adUnit);

    String encodedDisplayUrl = builder.build().getCustomTargeting().getString(MACRO_DISPLAY_URL);
    String decodedDisplayUrl = decodeDfpPayloadComponent(encodedDisplayUrl);

    assertEquals(STUB_DISPLAY_URL, decodedDisplayUrl);
  }

  @Test
  public void whenEnrichingNativePayload_GivenValidCpIdAndPrefetchNative_PayloadIsEncodedInASpecificManner()
      throws Exception {
    NativeAssets expectedAssets = STUB_NATIVE_ASSETS;
    NativeProduct expectedProduct = expectedAssets.nativeProducts.get(0);

    givenInitializedCriteo(validNativeAdUnit);
    waitForBids();

    Builder builder = new Builder();

    Criteo.getInstance().setBidsForAdUnit(builder, validNativeAdUnit);

    Bundle bundle = builder.build().getCustomTargeting();

    assertEquals(expectedProduct.title, decodeDfpPayloadComponent(bundle.getString(MACRO_NATIVE_TITLE)));
    assertEquals(expectedProduct.description, decodeDfpPayloadComponent(bundle.getString(MACRO_NATIVE_DESCRIPTION)));
    assertEquals(expectedProduct.imageUrl, decodeDfpPayloadComponent(bundle.getString(MACRO_NATIVE_IMAGE)));
    assertEquals(expectedProduct.price, decodeDfpPayloadComponent(bundle.getString(MACRO_NATIVE_PRICE)));
    assertEquals(expectedProduct.clickUrl, decodeDfpPayloadComponent(bundle.getString(MACRO_NATIVE_CLICK)));
    assertEquals(expectedProduct.callToAction, decodeDfpPayloadComponent(bundle.getString(MACRO_NATIVE_CTA)));
    assertEquals(expectedAssets.advertiserDescription, decodeDfpPayloadComponent(bundle.getString(MACRO_NATIVE_ADVERTISER_NAME)));
    assertEquals(expectedAssets.advertiserDomain, decodeDfpPayloadComponent(bundle.getString(MACRO_NATIVE_ADVERTISER_DOMAIN)));
    assertEquals(expectedAssets.advertiserLogoUrl, decodeDfpPayloadComponent(bundle.getString(MACRO_NATIVE_ADVERTISER_LOGO)));
    assertEquals(expectedAssets.advertiserLogoClickUrl, decodeDfpPayloadComponent(bundle.getString(MACRO_NATIVE_ADVERTISER_CLICK)));
    assertEquals(expectedAssets.privacyOptOutClickUrl, decodeDfpPayloadComponent(bundle.getString(MACRO_NATIVE_PRIVACY_LINK)));
    assertEquals(expectedAssets.privacyOptOutImageUrl, decodeDfpPayloadComponent(bundle.getString(MACRO_NATIVE_PRIVACY_IMAGE)));
    assertEquals(expectedAssets.impressionPixels.get(0), decodeDfpPayloadComponent(bundle.getString(MACRO_NATIVE_PIXEL_1)));
    assertEquals(expectedAssets.impressionPixels.get(1), decodeDfpPayloadComponent(bundle.getString(MACRO_NATIVE_PIXEL_2)));
    assertEquals(String.valueOf(expectedAssets.impressionPixels.size()), bundle.getString(MACRO_NATIVE_PIXEL_COUNT));
  }

  private String decodeDfpPayloadComponent(String component) throws Exception {
    // Payload component (such as display URL) should be encoded with:
    // Base64 encoder + URL encoder + URL encoder
    // So we should get back a good component by applying the reverse
    // The reverse: URL decoder + URL decoder + Base64 decoder

    String step1 = URLDecoder.decode(component, CHARSET.name());
    String step2 = URLDecoder.decode(step1, CHARSET.name());
    byte[] step3 = Base64.getDecoder().decode(step2);
    return new String(step3, CHARSET);
  }

  @Test
  public void loadingDfpBanner_GivenValidBanner_DfpViewContainsCreative() throws Exception {
    String html = loadDfpHtmlBanner(validBannerAdUnit);

    assertTrue(html.contains(STUB_CREATIVE_IMAGE));
  }

  @Test
  public void loadingDfpBanner_GivenValidInterstitial_DfpViewContainsCreative() throws Exception {
    String html = loadDfpHtmlInterstitial(validInterstitialAdUnit);

    assertTrue(html.contains(STUB_CREATIVE_IMAGE));
  }

  @Test
  public void loadingDfpBanner_GivenValidNative_DfpViewContainsNativePayload() throws Exception {
    NativeAssets expectedAssets = STUB_NATIVE_ASSETS;
    NativeProduct expectedProduct = expectedAssets.nativeProducts.get(0);

    // URL are encoded because they are given, as query param, to a Google URL that will redirect
    // user.
    // TODO add a test that verify that users are properly redirected to the expected URL.
    String expectedClickUrl = URLEncoder.encode(expectedProduct.clickUrl, CHARSET.name());
    String expectedPrivacyUrl = URLEncoder.encode(expectedAssets.privacyOptOutClickUrl, CHARSET.name());

    String html = loadDfpHtmlNative(validNativeAdUnit);

    // TODO In native template, in DFP configuration, there is nothing that use advertiser data.
    //  This prevent automated testing on this part. We may extends the actual template to use them.
    //  However, we should verify that it's a dedicated DFP ad unit for our tests. Else we may have
    //  a side effect.

    assertTrue(html.contains(expectedProduct.title));
    assertTrue(html.contains(expectedProduct.description));
    assertTrue(html.contains(expectedProduct.imageUrl));
    assertTrue(html.contains(expectedProduct.price));
    assertTrue(html.contains(expectedClickUrl));
    assertTrue(html.contains(expectedProduct.callToAction));
    assertTrue(html.contains(expectedAssets.privacyOptOutImageUrl));
    assertTrue(html.contains(expectedPrivacyUrl));
    assertTrue(html.contains(expectedAssets.privacyLongLegalText));
  }

  @Test
  public void loadingDfpBanner_GivenValidNative_HtmlInDfpViewShouldTriggerImpressionPixels() throws Exception {
    List<String> expectedPixels = new ArrayList<>(STUB_NATIVE_ASSETS.impressionPixels);

    String html = loadDfpHtmlNative(validNativeAdUnit);

    List<String> firedUrls = webViewLookup.lookForLoadedResources(html, CHARSET).get();
    expectedPixels.removeAll(firedUrls);

    assertTrue(expectedPixels.isEmpty());
  }

  @Test
  public void loadingDfpBanner_GivenDemoBanner_DfpViewContainsDisplayUrl() throws Exception {
    ResultCaptor<CdbResponse> cdbResultCaptor = mockedDependenciesRule.captorCdbResult(context);

    String html = loadDfpHtmlBanner(demoBannerAdUnit);

    assertDfpViewContainsDisplayUrl(cdbResultCaptor, html);

  }

  @Test
  public void loadingDfpBanner_GivenDemoInterstitial_DfpViewContainsDisplayUrl() throws Exception {
    ResultCaptor<CdbResponse> cdbResultCaptor = mockedDependenciesRule.captorCdbResult(context);

    String html = loadDfpHtmlInterstitial(demoInterstitialAdUnit);

    assertDfpViewContainsDisplayUrl(cdbResultCaptor, html);
  }

  private void assertDfpViewContainsDisplayUrl(ResultCaptor<CdbResponse> cdbResultCaptor, String html) {
    // The DFP webview replace the & by &amp; in attribute values.
    // So we need to replace them back in order to compare its content to our display URL.
    // This is valid HTML. See https://www.w3.org/TR/xhtml1/guidelines.html#C_12
    html = html.replace("&amp;", "&");

    String displayUrl = cdbResultCaptor.getLastCaptureValue().getSlots().get(0).getDisplayUrl();
    assertTrue(html.contains(displayUrl));
  }

  @Test
  public void loadingDfpBanner_GivenInvalidBanner_DfpViewDoesNotContainCreative() throws Exception {
    String html = loadDfpHtmlBanner(invalidBannerAdUnit);

    assertFalse(html.contains(STUB_CREATIVE_IMAGE));
  }

  @Test
  public void loadingDfpBanner_GivenInvalidInterstitial_DfpViewDoesNotContainCreative() throws Exception {
    String html = loadDfpHtmlInterstitial(invalidInterstitialAdUnit);

    assertFalse(html.contains(STUB_CREATIVE_IMAGE));
  }

  @Test
  public void loadingDfpBanner_GivenInvalidNative_DfpViewDoesNotContainProductImage() throws Exception {
    String html = loadDfpHtmlNative(invalidNativeAdUnit);

    assertFalse(html.contains(STUB_CREATIVE_IMAGE));
  }

  private String loadDfpHtmlBanner(BannerAdUnit adUnit) throws Exception {
    return loadDfpHtmlBannerOrNative(adUnit, createDfpBannerView());
  }

  private String loadDfpHtmlNative(NativeAdUnit adUnit) throws Exception {
    return loadDfpHtmlBannerOrNative(adUnit, createDfpNativeView());
  }

  private String loadDfpHtmlBannerOrNative(AdUnit adUnit, PublisherAdView publisherAdView) throws Exception {
    givenInitializedCriteo(adUnit);
    waitForBids();

    Builder builder = new Builder();

    Criteo.getInstance().setBidsForAdUnit(builder, adUnit);

    builder.addTestDevice(AdRequest.DEVICE_ID_EMULATOR);
    PublisherAdRequest request = builder.build();

    DfpSync dfpSync = new DfpSync(publisherAdView);

    runOnMainThreadAndWait(() -> publisherAdView.loadAd(request));
    dfpSync.waitForBid();

    return webViewLookup.lookForHtmlContent(publisherAdView).get();
  }

  private String loadDfpHtmlInterstitial(InterstitialAdUnit adUnit) throws Exception {
    givenInitializedCriteo(adUnit);
    waitForBids();

    Builder builder = new Builder();

    Criteo.getInstance().setBidsForAdUnit(builder, adUnit);

    builder.addTestDevice(AdRequest.DEVICE_ID_EMULATOR);
    PublisherAdRequest request = builder.build();

    PublisherInterstitialAd publisherInterstitialAd = createDfpInterstitialView();
    DfpSync dfpSync = new DfpSync(publisherInterstitialAd);

    runOnMainThreadAndWait(() -> publisherInterstitialAd.loadAd(request));
    dfpSync.waitForBid();

    View interstitialView = webViewLookup.lookForResumedActivityView(() -> {
      runOnMainThreadAndWait(publisherInterstitialAd::show);
    }).get(DFP_INTERSTITIAL_OPENING_TIMEOUT_MS, TimeUnit.MILLISECONDS);

    return webViewLookup.lookForHtmlContent(interstitialView).get();
  }

  private PublisherAdView createDfpBannerView() {
    PublisherAdView publisherAdView = new PublisherAdView(
        activityRule.getActivity().getApplicationContext());
    publisherAdView.setAdSizes(com.google.android.gms.ads.AdSize.BANNER);
    publisherAdView.setAdUnitId(DFP_BANNER_ID);
    return publisherAdView;
  }

  private PublisherInterstitialAd createDfpInterstitialView() {
    PublisherInterstitialAd publisherInterstitialAd = new PublisherInterstitialAd(
        activityRule.getActivity().getApplicationContext());
    publisherInterstitialAd.setAdUnitId(DFP_INTERSTITIAL_ID);
    return publisherInterstitialAd;
  }

  private PublisherAdView createDfpNativeView() {
    PublisherAdView publisherAdView = new PublisherAdView(
        activityRule.getActivity().getApplicationContext());
    publisherAdView.setAdSizes(com.google.android.gms.ads.AdSize.FLUID);
    publisherAdView.setAdUnitId(DFP_NATIVE_ID);
    return publisherAdView;
  }

  private void assertCriteoMacroAreInjectedInDfpBuilder(Builder builder) {
    Bundle customTargeting = builder.build().getCustomTargeting();

    assertNotNull(customTargeting.getString(MACRO_CPM));
    assertNotNull(customTargeting.getString(MACRO_DISPLAY_URL));
    assertEquals(2, customTargeting.size());
  }

  private void waitForBids() {
    waitForAllThreads(mockedDependenciesRule.getTrackingCommandsExecutor());
  }

  private static final class DfpSync {

    private final CountDownLatch isLoaded = new CountDownLatch(1);

    private DfpSync(PublisherAdView adView) {
      adView.setAdListener(new SyncAdListener());
    }

    private DfpSync(PublisherInterstitialAd interstitialAd) {
      interstitialAd.setAdListener(new SyncAdListener());
    }

    void waitForBid() throws InterruptedException {
      isLoaded.await();
    }

    private class SyncAdListener extends AdListener {
      @Override
      public void onAdLoaded() {
        isLoaded.countDown();
      }

      @Override
      public void onAdFailedToLoad(int i) {
        isLoaded.countDown();
      }
    }
  }

}
