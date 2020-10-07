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
import static com.criteo.publisher.StubConstants.STUB_CREATIVE_IMAGE;
import static com.criteo.publisher.concurrent.ThreadingUtil.callOnMainThreadAndWait;
import static com.criteo.publisher.concurrent.ThreadingUtil.runOnMainThreadAndWait;
import static com.criteo.publisher.view.WebViewLookup.getRootView;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.view.View;
import android.webkit.WebView;
import androidx.annotation.NonNull;
import androidx.test.rule.ActivityTestRule;
import com.criteo.publisher.Criteo;
import com.criteo.publisher.CriteoInitException;
import com.criteo.publisher.CriteoUtil;
import com.criteo.publisher.TestAdUnits;
import com.criteo.publisher.mock.MockedDependenciesRule;
import com.criteo.publisher.mock.ResultCaptor;
import com.criteo.publisher.mock.SpyBean;
import com.criteo.publisher.model.AdUnit;
import com.criteo.publisher.model.BannerAdUnit;
import com.criteo.publisher.model.CdbResponse;
import com.criteo.publisher.model.Config;
import com.criteo.publisher.model.InterstitialAdUnit;
import com.criteo.publisher.network.PubSdkApi;
import com.criteo.publisher.test.activity.DummyActivity;
import com.criteo.publisher.util.BuildConfigWrapper;
import com.criteo.publisher.view.WebViewLookup;
import com.mopub.common.MoPub;
import com.mopub.common.SdkConfiguration;
import com.mopub.common.logging.MoPubLog.LogLevel;
import com.mopub.mobileads.DefaultBannerAdListener;
import com.mopub.mobileads.MoPubErrorCode;
import com.mopub.mobileads.MoPubInterstitial;
import com.mopub.mobileads.MoPubInterstitial.InterstitialAdListener;
import com.mopub.mobileads.MoPubView;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Pattern;
import javax.inject.Inject;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
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

  @Parameters(name = "{0}")
  public static Iterable<?> data() {
    return Arrays.asList(false, true);
  }

  @Parameter
  public boolean isLiveBiddingEnabled;

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

  @Inject
  private Context context;

  @SpyBean
  private BuildConfigWrapper buildConfigWrapper;

  @SpyBean
  private Config config;

  @SpyBean
  private PubSdkApi api;

  @Before
  public void setUp() {
    doReturn(isLiveBiddingEnabled).when(config).isLiveBiddingEnabled();
  }

  @Test
  public void exampleOfExpectedKeywords() throws Exception {
    assertFalse(
        "Keywords should not be empty",
        EXPECTED_KEYWORDS.matcher("").matches()
    );

    assertFalse(
        "Keywords should not be empty",
        EXPECTED_KEYWORDS_FOR_BANNER.matcher("").matches()
    );

    assertTrue(
        "Keywords should use crt_cpm and crt_displayUrl",
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

    MoPubView moPubView = createMoPubView();
    moPubView.setKeywords("old keywords");

    Criteo.getInstance().loadBid(
        invalidBannerAdUnit,
        bid -> Criteo.getInstance().enrichAdObjectWithBid(moPubView, bid)
    );
    waitForBids();

    assertEquals("old keywords", moPubView.getKeywords());
  }

  @Test
  public void whenGettingBid_GivenValidCpIdAndPrefetchInvalidInterstitialId_MoPubKeywordsAreNotChange()
      throws Exception {
    givenInitializedCriteo(invalidInterstitialAdUnit);

    MoPubInterstitial moPubInterstitial = createMoPubInterstitial();
    moPubInterstitial.setKeywords("old keywords");

    Criteo.getInstance().loadBid(
        invalidBannerAdUnit,
        bid -> Criteo.getInstance().enrichAdObjectWithBid(moPubInterstitial, bid)
    );
    waitForBids();

    assertEquals("old keywords", moPubInterstitial.getKeywords());
  }

  @Test
  public void whenGettingBid_GivenValidCpIdAndPrefetchValidBannerId_CriteoKeywordsAreInjectedInMoPub()
      throws Exception {
    whenGettingBid_GivenValidCpIdAndValidBannerAdUnit_CriteoKeywordsAreInjectedInMoPub(validBannerAdUnit);
  }

  @Test
  public void whenGettingBid_GivenValidCpIdAndPrefetchDemoBannerId_CriteoKeywordsAreInjectedInMoPub()
      throws Exception {
    givenUsingCdbProd();
    whenGettingBid_GivenValidCpIdAndValidBannerAdUnit_CriteoKeywordsAreInjectedInMoPub(demoBannerAdUnit);
  }

  private void whenGettingBid_GivenValidCpIdAndValidBannerAdUnit_CriteoKeywordsAreInjectedInMoPub(BannerAdUnit adUnit
  ) throws Exception {
    givenInitializedCriteo(adUnit);

    MoPubView moPubView = createMoPubView();

    Criteo.getInstance().loadBid(
        adUnit,
        bid -> Criteo.getInstance().enrichAdObjectWithBid(moPubView, bid)
    );
    waitForBids();

    assertCriteoKeywordsAreInjectedInMoPubView(moPubView.getKeywords(), adUnit);
    assertBidRequestHasGoodProfileId();
  }

  @Test
  public void whenGettingBid_GivenValidCpIdAndPrefetchValidInterstitialId_CriteoKeywordsAreInjectedInMoPub()
      throws Exception {
    whenGettingBid_GivenValidCpIdAndValidInterstitialId_CriteoKeywordsAreInjectedInMoPub(validInterstitialAdUnit);
  }

  @Test
  public void whenGettingBid_GivenValidCpIdAndPrefetchDemoInterstitialId_CriteoKeywordsAreInjectedInMoPub()
      throws Exception {
    givenUsingCdbProd();
    whenGettingBid_GivenValidCpIdAndValidInterstitialId_CriteoKeywordsAreInjectedInMoPub(demoInterstitialAdUnit);
  }

  private void whenGettingBid_GivenValidCpIdAndValidInterstitialId_CriteoKeywordsAreInjectedInMoPub(
      InterstitialAdUnit interstitialAdUnit
  ) throws Exception {
    givenInitializedCriteo(interstitialAdUnit);

    MoPubInterstitial moPubInterstitial = createMoPubInterstitial();

    Criteo.getInstance().loadBid(
        interstitialAdUnit,
        bid -> Criteo.getInstance().enrichAdObjectWithBid(moPubInterstitial, bid)
    );
    waitForBids();

    assertCriteoKeywordsAreInjectedInMoPubView(moPubInterstitial.getKeywords(), interstitialAdUnit);
    assertBidRequestHasGoodProfileId();
  }

  @Test
  @Ignore("JIRA EE-1192")
  public void loadingMoPubBanner_GivenValidBanner_MoPubViewContainsCreative() throws Exception {
    String html = loadMoPubHtmlBanner(validBannerAdUnit);

    assertTrue(html.contains(STUB_CREATIVE_IMAGE));
  }

  @Test
  @Ignore("JIRA EE-1192")
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
    givenInitializedMoPub();

    MoPubView moPubView = createMoPubView();

    Criteo.getInstance().loadBid(
        adUnit,
        bid -> Criteo.getInstance().enrichAdObjectWithBid(moPubView, bid)
    );
    waitForBids();

    MoPubSync moPubSync = new MoPubSync(moPubView);
    runOnMainThreadAndWait(moPubView::loadAd);
    moPubSync.waitForBid();

    return moPubView;
  }

  private MoPubInterstitial loadMoPubInterstitial(InterstitialAdUnit adUnit) throws Exception {
    givenInitializedCriteo(adUnit);
    givenInitializedMoPub();

    MoPubInterstitial moPubInterstitial = createMoPubInterstitial();

    Criteo.getInstance().loadBid(
        adUnit,
        bid -> Criteo.getInstance().enrichAdObjectWithBid(moPubInterstitial, bid)
    );
    waitForBids();

    MoPubSync moPubSync = new MoPubSync(moPubInterstitial);
    runOnMainThreadAndWait(moPubInterstitial::load);
    moPubSync.waitForBid();

    return moPubInterstitial;
  }

  private void assertCriteoKeywordsAreInjectedInMoPubView(String keywords, AdUnit adUnit) {
    Pattern expectedKeywords = adUnit instanceof BannerAdUnit ? EXPECTED_KEYWORDS_FOR_BANNER : EXPECTED_KEYWORDS;
    assertThat(keywords).matches(expectedKeywords);
  }

  private void assertBidRequestHasGoodProfileId() throws Exception {
    if (config.isLiveBiddingEnabled()) {
      // With live bidding, AppBidding declaration is delayed when bid is consumed. So the declaration is only visible
      // from the next bid.
      Criteo.getInstance().loadBid(invalidBannerAdUnit, ignored -> { /* no op */ });
      waitForBids();
    }

    verify(api, atLeastOnce()).loadCdb(
        argThat(request -> request.getProfileId() == Integration.MOPUB_APP_BIDDING.getProfileId()),
        any()
    );
  }

  private MoPubView createMoPubView() {
    return callOnMainThreadAndWait(() -> {
      MoPubView moPubView = new MoPubView(context);
      moPubView.setAdUnitId(MOPUB_BANNER_ID);
      return moPubView;
    });
  }

  private MoPubInterstitial createMoPubInterstitial() {
    return callOnMainThreadAndWait(() -> new MoPubInterstitial(activityRule.getActivity(), MOPUB_INTERSTITIAL_ID));
  }

  private void givenUsingCdbProd() {
    when(buildConfigWrapper.getCdbUrl()).thenReturn(PROD_CDB_URL);
    when(mockedDependenciesRule.getDependencyProvider().provideCriteoPublisherId()).thenReturn(PROD_CP_ID);
  }

  private void givenInitializedCriteo(@NonNull AdUnit... adUnits) throws CriteoInitException {
    if (config.isLiveBiddingEnabled()) {
      // Empty it to show that prefetch has no influence
      adUnits = new AdUnit[]{};
    }

    CriteoUtil.givenInitializedCriteo(adUnits);
    waitForBids();
  }

  private void givenInitializedMoPub() throws InterruptedException {
    CountDownLatch moPubIsInitialized = new CountDownLatch(1);

    SdkConfiguration moPubConfig = new SdkConfiguration.Builder(MOPUB_BANNER_ID)
        .withLogLevel(LogLevel.INFO)
        .build();

    runOnMainThreadAndWait(() -> {
      MoPub.initializeSdk(context, moPubConfig, moPubIsInitialized::countDown);
    });

    moPubIsInitialized.await();
  }

  private void waitForBids() {
    mockedDependenciesRule.waitForIdleState();
  }

  private static final class MoPubSync {

    private final CountDownLatch isLoaded;

    MoPubSync(MoPubView moPubView) {
      this.isLoaded = new CountDownLatch(1);
      moPubView.setBannerAdListener(new SyncBannerAdListener());
    }

    MoPubSync(MoPubInterstitial moPubInterstitial) {
      this.isLoaded = new CountDownLatch(1);
      moPubInterstitial.setInterstitialAdListener(new SyncInterstitialAdListener());
    }

    void waitForBid() throws InterruptedException {
      isLoaded.await();
    }

    private void onLoaded() {
      isLoaded.countDown();
    }

    private void onFailed() {
      isLoaded.countDown();
    }

    private class SyncBannerAdListener extends DefaultBannerAdListener {

      @Override
      public void onBannerLoaded(@NonNull MoPubView banner) {
        onLoaded();
      }

      @Override
      public void onBannerFailed(MoPubView banner, MoPubErrorCode errorCode) {
        onFailed();
      }
    }

    private class SyncInterstitialAdListener implements InterstitialAdListener {

      @Override
      public void onInterstitialLoaded(MoPubInterstitial interstitial) {
        onLoaded();
      }

      @Override
      public void onInterstitialFailed(MoPubInterstitial interstitial, MoPubErrorCode errorCode) {
        onFailed();
      }

      @Override
      public void onInterstitialShown(MoPubInterstitial interstitial) {
      }

      @Override
      public void onInterstitialClicked(MoPubInterstitial interstitial) {
      }

      @Override
      public void onInterstitialDismissed(MoPubInterstitial interstitial) {
      }
    }
  }
}
