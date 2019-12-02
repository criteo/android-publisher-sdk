package com.criteo.publisher.integration;

import static com.criteo.publisher.CriteoUtil.givenInitializedCriteo;
import static com.criteo.publisher.ThreadingUtil.runOnMainThreadAndWait;
import static com.criteo.publisher.ThreadingUtil.waitForAllThreads;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.support.test.InstrumentationRegistry;
import com.criteo.publisher.Criteo;
import com.criteo.publisher.TestAdUnits;
import com.criteo.publisher.Util.MockedDependenciesRule;
import com.criteo.publisher.model.BannerAdUnit;
import com.mopub.mobileads.MoPubView;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;
import org.junit.Rule;
import org.junit.Test;

public class MoPubHeaderBiddingFunctionalTest {

  private static final Pattern EXPECTED_KEYWORDS = Pattern.compile("(.+,)?crt_cpm:[0-9]+\\.[0-9]{2},crt_displayUrl:.+");

  @Rule
  public MockedDependenciesRule mockedDependenciesRule = new MockedDependenciesRule();

  private final BannerAdUnit validBannerAdUnit = TestAdUnits.BANNER_320_50;

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
  public void whenGettingBid_GivenValidCpIdAndPrefetchValidBannerId_CriteoKeywordsAreInjectedInMoPubBuilder()
      throws Exception {
    givenInitializedCriteo(validBannerAdUnit);
    waitForBids();

    MoPubView moPubView = createMoPubView();

    Criteo.getInstance().setBidsForAdUnit(moPubView, validBannerAdUnit);

    assertCriteoKeywordsAreInjectedInMoPubView(moPubView);
  }

  private void assertCriteoKeywordsAreInjectedInMoPubView(MoPubView moPubView) {
    String keywords = moPubView.getKeywords();

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

  private void waitForBids() {
    waitForAllThreads(mockedDependenciesRule.getTrackingCommandsExecutor());
  }

}
