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

package com.criteo.publisher.integration;

import static com.criteo.publisher.CriteoUtil.PROD_CDB_URL;
import static com.criteo.publisher.CriteoUtil.PROD_CP_ID;
import static com.criteo.publisher.CriteoUtil.givenInitializedCriteo;
import static com.criteo.publisher.StubConstants.STUB_CREATIVE_IMAGE;
import static com.criteo.publisher.concurrent.ThreadingUtil.runOnMainThreadAndWait;
import static com.criteo.publisher.view.WebViewLookup.getRootView;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.webkit.WebView;
import androidx.test.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;
import com.criteo.publisher.Criteo;
import com.criteo.publisher.TestAdUnits;
import com.criteo.publisher.mock.MockedDependenciesRule;
import com.criteo.publisher.mock.ResultCaptor;
import com.criteo.publisher.mock.SpyBean;
import com.criteo.publisher.model.AdUnit;
import com.criteo.publisher.model.BannerAdUnit;
import com.criteo.publisher.model.CdbResponse;
import com.criteo.publisher.model.InterstitialAdUnit;
import com.criteo.publisher.test.activity.DummyActivity;
import com.criteo.publisher.util.BuildConfigWrapper;
import com.criteo.publisher.view.WebViewLookup;
import com.mopub.common.MoPub;
import com.mopub.common.SdkConfiguration;
import com.mopub.common.logging.MoPubLog.LogLevel;
import com.mopub.mobileads.DefaultBannerAdListener;
import com.mopub.mobileads.DefaultInterstitialAdListener;
import com.mopub.mobileads.MoPubErrorCode;
import com.mopub.mobileads.MoPubInterstitial;
import com.mopub.mobileads.MoPubView;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

public class MoPubHeaderBiddingFunctionalTest {

  private static final Pattern EXPECTED_KEYWORDS = Pattern
      .compile("(.+,)?crt_cpm:[0-9]+\\.[0-9]{2},crt_displayUrl:.+");

  private static final Pattern EXPECTED_KEYWORDS_FOR_BANNER = Pattern
      .compile("(.+,)?crt_cpm:[0-9]+\\.[0-9]{2},crt_displayUrl:.+,crt_size:[0-9]+x[0-9]+");

  /**
   * Those are adunit IDs that are declared on MoPub server. We need it to get an accepted bid from
   * MoPub.
   */
  private static final String MOPUB_BANNER_ID = "d2f3ed80e5da4ae1acde0971eac30fa4";
  private static final String MOPUB_INTERSTITIAL_ID = "83a2996696284da881edaf1a480e5d7c";

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

  private final WebViewLookup webViewLookup = new WebViewLookup();

  @SpyBean
  private BuildConfigWrapper buildConfigWrapper;

  @Test
  public void exampleOfExpectedKeywords() throws Exception {
    assertFalse("Keywords should not be empty",
        EXPECTED_KEYWORDS.matcher("").matches());

    assertFalse("Keywords should not be empty",
        EXPECTED_KEYWORDS_FOR_BANNER.matcher("").matches());

    assertTrue("Keywords should use crt_cpm and crt_displayUrl",
        EXPECTED_KEYWORDS.matcher("crt_cpm:1234.56,crt_displayUrl:http://my.super/creative")
            .matches());

    assertTrue("Keywords for banner should use crt_cpm and crt_displayUrl and crt_size",
        EXPECTED_KEYWORDS_FOR_BANNER.matcher("crt_cpm:1234.56,crt_displayUrl:http://my.super/creative,crt_size:42x1337")
            .matches());

    assertTrue("Keywords should accept older keywords from outside",
        EXPECTED_KEYWORDS.matcher(
            "previous keywords setup by someone,crt_cpm:1234.56,crt_displayUrl:http://my.super/creative")
            .matches());

    assertTrue("Keywords for banner should accept older keywords from outside",
        EXPECTED_KEYWORDS_FOR_BANNER.matcher("previous keywords setup by someone,crt_cpm:1234.56,crt_displayUrl:http://my.super/creative,crt_size:42x1337")
            .matches());
  }

  @Test
  public void whenGettingBid_GivenValidCpIdAndPrefetchInvalidBannerId_MoPubKeywordsAreNotChange()
      throws Exception {
    givenInitializedCriteo(invalidBannerAdUnit);
    waitForBids();

    MoPubView moPubView = createMoPubView();
    moPubView.setKeywords("old keywords");

    Criteo.getInstance().setBidsForAdUnit(moPubView, invalidBannerAdUnit);

    assertEquals("old keywords", moPubView.getKeywords());
  }

  @Test
  public void whenGettingBid_GivenValidCpIdAndPrefetchInvalidInterstitialId_MoPubKeywordsAreNotChange()
      throws Exception {
    givenInitializedCriteo(invalidInterstitialAdUnit);
    waitForBids();

    MoPubInterstitial moPubInterstitial = createMoPubInterstitial();
    moPubInterstitial.setKeywords("old keywords");

    Criteo.getInstance().setBidsForAdUnit(moPubInterstitial, invalidInterstitialAdUnit);

    Assert.assertEquals("old keywords", moPubInterstitial.getKeywords());
  }

  @Test
  public void whenGettingBid_GivenValidCpIdAndPrefetchValidBannerId_CriteoKeywordsAreInjectedInMoPubBuilder()
      throws Exception {
    whenGettingBid_GivenValidCpIdAndPrefetchValidBannerAdUnit_CriteoKeywordsAreInjectedInMoPubBuilder(
        validBannerAdUnit);
  }

  @Test
  public void whenGettingBid_GivenValidCpIdAndPrefetchDemoBannerId_CriteoKeywordsAreInjectedInMoPubBuilder()
      throws Exception {
    givenUsingCdbProd();
    whenGettingBid_GivenValidCpIdAndPrefetchValidBannerAdUnit_CriteoKeywordsAreInjectedInMoPubBuilder(
        demoBannerAdUnit);
  }

  private void whenGettingBid_GivenValidCpIdAndPrefetchValidBannerAdUnit_CriteoKeywordsAreInjectedInMoPubBuilder(
      BannerAdUnit adUnit) throws Exception {
    givenInitializedCriteo(adUnit);
    waitForBids();

    MoPubView moPubView = createMoPubView();

    Criteo.getInstance().setBidsForAdUnit(moPubView, adUnit);

    assertCriteoKeywordsAreInjectedInMoPubView(moPubView.getKeywords(), adUnit);
  }

  @Test
  public void whenGettingBid_GivenValidCpIdAndPrefetchValidInterstitialId_CriteoKeywordsAreInjectedInMoPubBuilder()
      throws Exception {
    whenGettingBid_GivenValidCpIdAndPrefetchInterstitialId_CriteoKeywordsAreInjectedInMoPubBuilder(
        validInterstitialAdUnit);
  }

  @Test
  public void whenGettingBid_GivenValidCpIdAndPrefetchDemoInterstitialId_CriteoKeywordsAreInjectedInMoPubBuilder()
      throws Exception {
    givenUsingCdbProd();
    whenGettingBid_GivenValidCpIdAndPrefetchInterstitialId_CriteoKeywordsAreInjectedInMoPubBuilder(
        demoInterstitialAdUnit);
  }

  private void whenGettingBid_GivenValidCpIdAndPrefetchInterstitialId_CriteoKeywordsAreInjectedInMoPubBuilder(
      InterstitialAdUnit interstitialAdUnit)
      throws Exception {
    givenInitializedCriteo(interstitialAdUnit);
    waitForBids();

    MoPubInterstitial moPubInterstitial = createMoPubInterstitial();

    Criteo.getInstance().setBidsForAdUnit(moPubInterstitial, interstitialAdUnit);

    assertCriteoKeywordsAreInjectedInMoPubView(moPubInterstitial.getKeywords(), interstitialAdUnit);
  }

  @Ignore("FIXME EE-1180: Test does not pass on Github Actions")
  @Test
  public void loadingMoPubBanner_GivenValidBanner_MoPubViewContainsCreative() throws Exception {
    String html = loadMoPubHtmlBanner(validBannerAdUnit);

    assertTrue(html.contains(STUB_CREATIVE_IMAGE));
  }

  @Ignore("FIXME EE-1180: Test does not pass on Github Actions")
  @Test
  public void loadingMoPubBanner_GivenDemoBanner_MoPubViewUsesDemoDisplayUrl() throws Exception {
    givenUsingCdbProd();
    ResultCaptor<CdbResponse> cdbResultCaptor = mockedDependenciesRule.captorCdbResult();

    String html = loadMoPubHtmlBanner(demoBannerAdUnit);

    String displayUrl = cdbResultCaptor.getLastCaptureValue().getSlots().get(0).getDisplayUrl();
    assertTrue(html.contains(displayUrl));
  }

  @Test
  public void loadingMoPubInterstitial_GivenValidInterstitial_MoPubViewContainsCreative()
      throws Exception {
    String html = loadMoPubHtmlInterstitial(validInterstitialAdUnit);

    assertTrue(html.contains(STUB_CREATIVE_IMAGE));
  }

  @Test
  public void loadingMoPubInterstitial_GivenDemoInterstitial_MoPubViewUsesDemoDisplayUrl()
      throws Exception {
    givenUsingCdbProd();
    ResultCaptor<CdbResponse> cdbResultCaptor = mockedDependenciesRule.captorCdbResult();

    String html = loadMoPubHtmlInterstitial(demoInterstitialAdUnit);

    String displayUrl = cdbResultCaptor.getLastCaptureValue().getSlots().get(0).getDisplayUrl();
    assertTrue(html.contains(displayUrl));
  }

  @Test
  public void loadingMoPubBanner_GivenInvalidBanner_MoPubViewDoesNotContainWebView()
      throws Exception {
    MoPubView moPubView = loadMoPubBanner(invalidBannerAdUnit);

    List<WebView> webViews = webViewLookup.lookForWebViews(moPubView);

    assertTrue(webViews.isEmpty());
  }

  @Test
  public void loadingMoPubBanner_GivenInvalidInterstitial_MoPubViewIsNotReady()
      throws Exception {
    MoPubInterstitial moPubInterstitial = loadMoPubInterstitial(invalidInterstitialAdUnit);

    assertFalse(moPubInterstitial.isReady());
  }

  private String loadMoPubHtmlBanner(BannerAdUnit adUnit)
      throws Exception {
    MoPubView moPubView = loadMoPubBanner(adUnit);

    return webViewLookup.lookForHtmlContent(moPubView).get();
  }

  private String loadMoPubHtmlInterstitial(InterstitialAdUnit adUnit)
      throws Exception {
    View moPubView = getRootView(webViewLookup.lookForResumedActivity(() -> {
      MoPubInterstitial moPubInterstitial = loadMoPubInterstitial(adUnit);
      assertTrue(moPubInterstitial.isReady());
      moPubInterstitial.show();
    }).get());

    return webViewLookup.lookForHtmlContent(moPubView).get();
  }

  private MoPubView loadMoPubBanner(BannerAdUnit adUnit) throws Exception {
    givenInitializedCriteo(adUnit);
    waitForBids();

    givenInitializedMoPub();

    MoPubView moPubView = createMoPubView();

    Criteo.getInstance().setBidsForAdUnit(moPubView, adUnit);

    MoPubSync moPubSync = new MoPubSync(moPubView);
    runOnMainThreadAndWait(moPubView::loadAd);
    moPubSync.waitForBid();

    return moPubView;
  }

  private MoPubInterstitial loadMoPubInterstitial(InterstitialAdUnit adUnit) throws Exception {
    givenInitializedCriteo(adUnit);
    waitForBids();

    givenInitializedMoPub();

    MoPubInterstitial moPubInterstitial = createMoPubInterstitial();

    Criteo.getInstance().setBidsForAdUnit(moPubInterstitial, adUnit);

    MoPubSync moPubSync = new MoPubSync(moPubInterstitial);
    runOnMainThreadAndWait(moPubInterstitial::load);
    moPubSync.waitForBid();

    return moPubInterstitial;
  }

  private void assertCriteoKeywordsAreInjectedInMoPubView(String keywords, AdUnit adUnit) {
    assertNotNull(keywords);

    Pattern expectedKeywords = adUnit instanceof BannerAdUnit ? EXPECTED_KEYWORDS_FOR_BANNER : EXPECTED_KEYWORDS;
    boolean isMatched = expectedKeywords.matcher(keywords).matches();
    assertTrue(isMatched);
  }

  private MoPubView createMoPubView() {
    AtomicReference<MoPubView> moPubViewRef = new AtomicReference<>();
    runOnMainThreadAndWait(() -> {
      moPubViewRef.set(new MoPubView(InstrumentationRegistry.getContext()));
    });
    moPubViewRef.get().setAdUnitId(MOPUB_BANNER_ID);
    return moPubViewRef.get();
  }

  private MoPubInterstitial createMoPubInterstitial() {
    AtomicReference<MoPubInterstitial> moPubInterstitialRef = new AtomicReference<>();
    runOnMainThreadAndWait(() -> {
      moPubInterstitialRef
          .set(new MoPubInterstitial(activityRule.getActivity(), MOPUB_INTERSTITIAL_ID));
    });
    return moPubInterstitialRef.get();
  }

  private void givenUsingCdbProd() {
    when(buildConfigWrapper.getCdbUrl()).thenReturn(PROD_CDB_URL);
    when(mockedDependenciesRule.getDependencyProvider().provideCriteoPublisherId()).thenReturn(PROD_CP_ID);
  }

  private void givenInitializedMoPub() throws InterruptedException {
    CountDownLatch moPubIsInitialized = new CountDownLatch(1);

    SdkConfiguration moPubConfig = new SdkConfiguration.Builder(MOPUB_BANNER_ID)
        .withLogLevel(LogLevel.INFO)
        .build();

    runOnMainThreadAndWait(() -> {
      MoPub.initializeSdk(InstrumentationRegistry.getContext(), moPubConfig,
          moPubIsInitialized::countDown);
    });

    moPubIsInitialized.await();
  }

  private void waitForBids() {
    mockedDependenciesRule.waitForIdleState();
  }

  private static final class MoPubSync {

    private final CountDownLatch isLoaded;
    private final Handler handler;

    MoPubSync(MoPubView moPubView) {
      this.isLoaded = new CountDownLatch(1);
      this.handler = new Handler(Looper.getMainLooper());
      moPubView.setBannerAdListener(new SyncBannerAdListener());
    }

    MoPubSync(MoPubInterstitial moPubInterstitial) {
      this.isLoaded = new CountDownLatch(1);
      this.handler = new Handler(Looper.getMainLooper());
      moPubInterstitial.setInterstitialAdListener(new SyncInterstitialAdListener());
    }

    void waitForBid() throws InterruptedException {
      isLoaded.await();
    }

    private void onLoaded() {
      // MoPub does not seem to totally be ready at this point.
      // It seems to be ready few times after the end of this method.
      // So we should still wait a little in a non-deterministic way, but not in this method.
      handler.postDelayed(isLoaded::countDown, 500);
    }

    private void onFailed() {
      isLoaded.countDown();
    }

    private class SyncBannerAdListener extends DefaultBannerAdListener {

      @Override
      public void onBannerLoaded(MoPubView banner) {
        onLoaded();
      }

      @Override
      public void onBannerFailed(MoPubView banner, MoPubErrorCode errorCode) {
        onFailed();
      }
    }

    private class SyncInterstitialAdListener extends DefaultInterstitialAdListener {

      @Override
      public void onInterstitialLoaded(MoPubInterstitial interstitial) {
        onLoaded();
      }

      @Override
      public void onInterstitialFailed(MoPubInterstitial interstitial, MoPubErrorCode errorCode) {
        onFailed();
      }
    }
  }
}
