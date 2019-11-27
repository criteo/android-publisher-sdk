package com.criteo.publisher.integration;

import static com.criteo.publisher.CriteoUtil.givenInitializedCriteo;
import static com.criteo.publisher.ThreadingUtil.waitForAllThreads;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import android.os.Bundle;
import com.criteo.publisher.Criteo;
import com.criteo.publisher.TestAdUnits;
import com.criteo.publisher.Util.MockedDependenciesRule;
import com.criteo.publisher.model.BannerAdUnit;
import com.google.android.gms.ads.doubleclick.PublisherAdRequest.Builder;
import org.junit.Rule;
import org.junit.Test;

public class DfpHeaderBiddingFunctionalTest {

  @Rule
  public MockedDependenciesRule mockedDependenciesRule  = new MockedDependenciesRule();

  private final BannerAdUnit bannerAdUnit = TestAdUnits.BANNER_320_50;

  @Test
  public void whenGettingBid_GivenValidCpIdAndPrefetchValidBannerId_CriteoMacroAreInjectedInDfpBuilder()
      throws Exception {
    givenInitializedCriteo(bannerAdUnit);
    waitForBids();

    Builder builder = new Builder();

    Criteo.getInstance().setBidsForAdUnit(builder, bannerAdUnit);

    Bundle customTargeting = builder.build().getCustomTargeting();

    assertNotNull(customTargeting.getString("crt_cpm"));
    assertNotNull(customTargeting.getString("crt_displayurl"));
    assertEquals(2, customTargeting.size());
  }

  private void waitForBids() {
    waitForAllThreads(mockedDependenciesRule.getTrackingCommandsExecutor());
  }

}
