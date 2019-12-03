package com.criteo.publisher.integration;

import static com.criteo.publisher.CriteoUtil.givenInitializedCriteo;
import static com.criteo.publisher.StubConstants.STUB_CREATIVE_IMAGE;
import static com.criteo.publisher.ThreadingUtil.runOnMainThreadAndWait;
import static com.criteo.publisher.ThreadingUtil.waitForAllThreads;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.os.Handler;
import android.os.Looper;
import android.support.test.InstrumentationRegistry;
import com.criteo.publisher.Criteo;
import com.criteo.publisher.CriteoInitException;
import com.criteo.publisher.TestAdUnits;
import com.criteo.publisher.Util.MockedDependenciesRule;
import com.criteo.publisher.Util.WebViewLookup;
import com.criteo.publisher.model.BannerAdUnit;
import com.mopub.common.MoPub;
import com.mopub.common.SdkConfiguration;
import com.mopub.common.logging.MoPubLog.LogLevel;
import com.mopub.mobileads.DefaultBannerAdListener;
import com.mopub.mobileads.MoPubErrorCode;
import com.mopub.mobileads.MoPubView;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;
import org.junit.Rule;
import org.junit.Test;

public class MoPubHeaderBiddingFunctionalTest {

  private static final Pattern EXPECTED_KEYWORDS = Pattern.compile("(.+,)?crt_cpm:[0-9]+\\.[0-9]{2},crt_displayUrl:.+");

  /**
   * This is an adunit ID that is declared on MoPub server.
   * We need it to get an accepted bid from MoPub.
   */
  private static final String MOPUB_BANNER_ID = "d2f3ed80e5da4ae1acde0971eac30fa4";

  @Rule
  public MockedDependenciesRule mockedDependenciesRule = new MockedDependenciesRule();

  private final BannerAdUnit validBannerAdUnit = TestAdUnits.BANNER_320_50;
  private final BannerAdUnit invalidBannerAdUnit = TestAdUnits.BANNER_UNKNOWN;
  private final BannerAdUnit demoBannerAdUnit = TestAdUnits.BANNER_DEMO;

  private final WebViewLookup webViewLookup = new WebViewLookup();

  @Test
  public void exampleOfExpectedKeywords() throws Exception {
    assertFalse("Keywords should not be empty",
        EXPECTED_KEYWORDS.matcher("").matches());

    assertTrue("Keywords should use crt_cpm and crt_displayUrl",
        EXPECTED_KEYWORDS.matcher("crt_cpm:1234.56,crt_displayUrl:http://my.super/creative").matches());

    assertTrue("Keywords should accept older keywords from outside",
        EXPECTED_KEYWORDS.matcher("previous keywords setup by someone,crt_cpm:1234.56,crt_displayUrl:http://my.super/creative").matches());
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
  public void whenGettingBid_GivenValidCpIdAndPrefetchValidBannerId_CriteoKeywordsAreInjectedInMoPubBuilder()
      throws Exception {
    whenGettingBid_GivenValidCpIdAndPrefetchValidAdUnit_CriteoKeywordsAreInjectedInMoPubBuilder(
        validBannerAdUnit);
  }

  @Test
  public void whenGettingBid_GivenValidCpIdAndPrefetchDemoBannerId_CriteoKeywordsAreInjectedInMoPubBuilder()
      throws Exception {
    whenGettingBid_GivenValidCpIdAndPrefetchValidAdUnit_CriteoKeywordsAreInjectedInMoPubBuilder(
        demoBannerAdUnit);
  }

  private void whenGettingBid_GivenValidCpIdAndPrefetchValidAdUnit_CriteoKeywordsAreInjectedInMoPubBuilder(
      BannerAdUnit adUnit) throws CriteoInitException {
    givenInitializedCriteo(adUnit);
    waitForBids();

    MoPubView moPubView = createMoPubView();

    Criteo.getInstance().setBidsForAdUnit(moPubView, adUnit);

    assertCriteoKeywordsAreInjectedInMoPubView(moPubView);
  }

  @Test
  public void loadingMoPubBanner_GivenValidBanner_MoPubViewContainsCreative() throws Exception {
    givenInitializedCriteo(validBannerAdUnit);
    waitForBids();

    givenInitializedMoPub();

    MoPubView moPubView = createMoPubView();
    moPubView.setAdUnitId(MOPUB_BANNER_ID);

    Criteo.getInstance().setBidsForAdUnit(moPubView, validBannerAdUnit);

    MoPubSync moPubSync = new MoPubSync(moPubView);
    runOnMainThreadAndWait(moPubView::loadAd);
    moPubSync.waitForBid();

    String html = webViewLookup.lookForHtmlContent(moPubView).get();

    assertTrue(html.contains(STUB_CREATIVE_IMAGE));
  }

  private void assertCriteoKeywordsAreInjectedInMoPubView(MoPubView moPubView) {
    String keywords = moPubView.getKeywords();
    assertNotNull(keywords);

    boolean isMatched = EXPECTED_KEYWORDS.matcher(keywords).matches();
    assertTrue(isMatched);
  }

  private MoPubView createMoPubView() {
    AtomicReference<MoPubView> moPubViewRef = new AtomicReference<>();
    runOnMainThreadAndWait(() -> {
      moPubViewRef.set(new MoPubView(InstrumentationRegistry.getContext()));
    });
    return moPubViewRef.get();
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
    waitForAllThreads(mockedDependenciesRule.getTrackingCommandsExecutor());
  }

  private static final class MoPubSync {

    private final CountDownLatch isLoaded;
    private final Handler handler;

    MoPubSync(MoPubView moPubView) {
      this.isLoaded = new CountDownLatch(1);
      this.handler = new Handler(Looper.getMainLooper());
      moPubView.setBannerAdListener(new SyncAdListener());
    }

    void waitForBid() throws InterruptedException {
      isLoaded.await();
    }

    private class SyncAdListener extends DefaultBannerAdListener {

      @Override
      public void onBannerLoaded(MoPubView banner) {
        // MoPub does not seem to totally be ready at this point.
        // It seems to be ready few times after the end of this method.
        // So we should still wait a little in a non-deterministic way, but not in this method.
        handler.postDelayed(isLoaded::countDown, 500);
      }

      @Override
      public void onBannerFailed(MoPubView banner, MoPubErrorCode errorCode) {
        isLoaded.countDown();
      }
    }
  }
}
