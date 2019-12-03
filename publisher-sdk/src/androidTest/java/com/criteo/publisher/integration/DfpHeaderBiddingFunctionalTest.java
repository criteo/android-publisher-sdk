package com.criteo.publisher.integration;

import static com.criteo.publisher.CriteoUtil.givenInitializedCriteo;
import static com.criteo.publisher.StubConstants.STUB_CREATIVE_IMAGE;
import static com.criteo.publisher.StubConstants.STUB_DISPLAY_URL;
import static com.criteo.publisher.ThreadingUtil.runOnMainThreadAndWait;
import static com.criteo.publisher.ThreadingUtil.waitForAllThreads;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.os.Bundle;
import android.support.test.InstrumentationRegistry;
import com.criteo.publisher.Criteo;
import com.criteo.publisher.TestAdUnits;
import com.criteo.publisher.Util.MockedDependenciesRule;
import com.criteo.publisher.Util.WebViewLookup;
import com.criteo.publisher.model.BannerAdUnit;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.doubleclick.PublisherAdRequest;
import com.google.android.gms.ads.doubleclick.PublisherAdRequest.Builder;
import com.google.android.gms.ads.doubleclick.PublisherAdView;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.CountDownLatch;
import org.junit.Rule;
import org.junit.Test;

public class DfpHeaderBiddingFunctionalTest {

  private static final String MACRO_CPM = "crt_cpm";
  private static final String MACRO_DISPLAY_URL = "crt_displayurl";

  private static final String DFP_BANNER_ID = "/140800857/Endeavour_320x50";

  @Rule
  public MockedDependenciesRule mockedDependenciesRule = new MockedDependenciesRule();

  private final BannerAdUnit validBannerAdUnit = TestAdUnits.BANNER_320_50;
  private final BannerAdUnit invalidBannerAdUnit = TestAdUnits.BANNER_UNKNOWN;
  private final BannerAdUnit demoBannerAdUnit = TestAdUnits.BANNER_DEMO;

  private final WebViewLookup webViewLookup = new WebViewLookup();

  @Test
  public void whenGettingBid_GivenValidCpIdAndPrefetchValidBannerId_CriteoMacroAreInjectedInDfpBuilder()
      throws Exception {
    givenInitializedCriteo(validBannerAdUnit);
    waitForBids();

    Builder builder = new Builder();

    Criteo.getInstance().setBidsForAdUnit(builder, validBannerAdUnit);

    assertCriteoMacroAreInjectedInDfpBuilder(builder);
  }

  @Test
  public void whenGettingBid_GivenValidCpIdAndPrefetchInvalidBannerId_CriteoMacroAreNotInjectedInDfpBuilder()
      throws Exception {
    givenInitializedCriteo(invalidBannerAdUnit);
    waitForBids();

    Builder builder = new Builder();

    Criteo.getInstance().setBidsForAdUnit(builder, invalidBannerAdUnit);

    Bundle customTargeting = builder.build().getCustomTargeting();

    assertNull(customTargeting.getString(MACRO_CPM));
    assertNull(customTargeting.getString(MACRO_DISPLAY_URL));
    assertEquals(0, customTargeting.size());
  }

  @Test
  public void whenGettingTestBid_GivenValidCpIdAndPrefetchDemoBannerId_CriteoMacroAreInjectedInDfpBuilder()
      throws Exception {
    givenInitializedCriteo(demoBannerAdUnit);
    waitForBids();

    Builder builder = new Builder();

    Criteo.getInstance().setBidsForAdUnit(builder, demoBannerAdUnit);

    assertCriteoMacroAreInjectedInDfpBuilder(builder);

    // The amount is not that important, but the format is
    String cpm = builder.build().getCustomTargeting().getString(MACRO_CPM);
    assertEquals("20.00", cpm);
  }

  @Test
  public void whenEnrichingDisplayUrl_GivenValidCpIdAndPrefetchBannerId_DisplayUrlIsEncodedInASpecificManner()
      throws Exception {
    givenInitializedCriteo(validBannerAdUnit);
    waitForBids();

    Builder builder = new Builder();

    Criteo.getInstance().setBidsForAdUnit(builder, validBannerAdUnit);

    String encodedDisplayUrl = builder.build().getCustomTargeting().getString(MACRO_DISPLAY_URL);

    // Display should be encoded with: Base64 encoder + URL encoder + URL encoder
    // So we should get back a good display url by applying the reverse
    // The reverse: URL decoder + URL decoder + Base64 decoder

    Charset charset = StandardCharsets.UTF_8;
    String step1 = URLDecoder.decode(encodedDisplayUrl, charset.name());
    String step2 = URLDecoder.decode(step1, charset.name());
    byte[] step3 = Base64.getDecoder().decode(step2);
    String decodedDisplayUrl = new String(step3, charset);

    assertEquals(STUB_DISPLAY_URL, decodedDisplayUrl);
  }

  @Test
  public void loadingDfpBanner_GivenValidBanner_DfpViewContainsCreative() throws Exception {
    String html = loadDfpHtmlBanner(validBannerAdUnit);

    assertTrue(html.contains(STUB_CREATIVE_IMAGE));
  }

  @Test
  public void loadingDfpBanner_GivenDemoBanner_DfpViewContainsCreative() throws Exception {
    String html = loadDfpHtmlBanner(demoBannerAdUnit);

    // The demo creative is a true Criteo creative with a lot of iframe, generated code, ...
    // It's hard to have a deterministic element inside. Trying to get this ID should be sufficient
    // to determine if the creative was well loaded.
    assertTrue(html.contains("#cto_banner_content"));
  }

  @Test
  public void loadingDfpBanner_GivenInvalidBanner_DfpViewDoesNotContainCreative() throws Exception {
    String html = loadDfpHtmlBanner(invalidBannerAdUnit);

    assertFalse(html.contains(STUB_CREATIVE_IMAGE));
  }

  private String loadDfpHtmlBanner(BannerAdUnit adUnit) throws Exception {
    givenInitializedCriteo(adUnit);
    waitForBids();

    Builder builder = new Builder();

    Criteo.getInstance().setBidsForAdUnit(builder, adUnit);

    builder.addTestDevice(AdRequest.DEVICE_ID_EMULATOR);
    PublisherAdRequest request = builder.build();

    PublisherAdView publisherAdView = new PublisherAdView(InstrumentationRegistry.getContext());
    publisherAdView.setAdSizes(com.google.android.gms.ads.AdSize.BANNER);
    publisherAdView.setAdUnitId(DFP_BANNER_ID);

    DfpSync dfpSync = new DfpSync(publisherAdView);

    runOnMainThreadAndWait(() -> publisherAdView.loadAd(request));
    dfpSync.waitForBid();

    return webViewLookup.lookForHtmlContent(publisherAdView).get();
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

    private final CountDownLatch isLoaded;

    private DfpSync(PublisherAdView adView) {
      this.isLoaded = new CountDownLatch(1);
      adView.setAdListener(new SyncAdListener());
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
