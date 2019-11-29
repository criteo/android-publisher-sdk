package com.criteo.publisher.integration;

import static com.criteo.publisher.CriteoUtil.givenInitializedCriteo;
import static com.criteo.publisher.ThreadingUtil.waitForAllThreads;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import android.os.Bundle;
import com.criteo.publisher.Criteo;
import com.criteo.publisher.TestAdUnits;
import com.criteo.publisher.Util.MockedDependenciesRule;
import com.criteo.publisher.model.BannerAdUnit;
import com.google.android.gms.ads.doubleclick.PublisherAdRequest.Builder;
import org.junit.Rule;
import org.junit.Test;

public class DfpHeaderBiddingFunctionalTest {

  private static final String MACRO_CPM = "crt_cpm";
  private static final String MACRO_DISPLAY_URL = "crt_displayurl";

  @Rule
  public MockedDependenciesRule mockedDependenciesRule  = new MockedDependenciesRule();

  private final BannerAdUnit validBannerAdUnit = TestAdUnits.BANNER_320_50;
  private final BannerAdUnit invalidBannerAdUnit = TestAdUnits.BANNER_UNKNOWN;

  @Test
  public void whenGettingBid_GivenValidCpIdAndPrefetchValidBannerId_CriteoMacroAreInjectedInDfpBuilder()
      throws Exception {
    givenInitializedCriteo(validBannerAdUnit);
    waitForBids();

    Builder builder = new Builder();

    Criteo.getInstance().setBidsForAdUnit(builder, validBannerAdUnit);

    Bundle customTargeting = builder.build().getCustomTargeting();

    assertNotNull(customTargeting.getString(MACRO_CPM));
    assertNotNull(customTargeting.getString(MACRO_DISPLAY_URL));
    assertEquals(2, customTargeting.size());
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

  private void waitForBids() {
    waitForAllThreads(mockedDependenciesRule.getTrackingCommandsExecutor());
  }

}
