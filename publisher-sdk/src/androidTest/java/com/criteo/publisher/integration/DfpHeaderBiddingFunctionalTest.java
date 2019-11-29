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
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.junit.Rule;
import org.junit.Test;

public class DfpHeaderBiddingFunctionalTest {

  private static final String MACRO_CPM = "crt_cpm";
  private static final String MACRO_DISPLAY_URL = "crt_displayurl";

  private static final String STUB_DISPLAY_URL = "https://publisherdirect.criteo.com/publishertag/preprodtest/FakeAJS.js";

  @Rule
  public MockedDependenciesRule mockedDependenciesRule  = new MockedDependenciesRule();

  private final BannerAdUnit validBannerAdUnit = TestAdUnits.BANNER_320_50;
  private final BannerAdUnit invalidBannerAdUnit = TestAdUnits.BANNER_UNKNOWN;
  private final BannerAdUnit demoBannerAdUnit = TestAdUnits.BANNER_DEMO;

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
  public void whenEnrichingDisplayUrl_GivenValidCpIdAndPrefetchBannerId_DisplayUrlIsEncodedInASpecificManner() throws Exception {
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

  private void assertCriteoMacroAreInjectedInDfpBuilder(Builder builder) {
    Bundle customTargeting = builder.build().getCustomTargeting();

    assertNotNull(customTargeting.getString(MACRO_CPM));
    assertNotNull(customTargeting.getString(MACRO_DISPLAY_URL));
    assertEquals(2, customTargeting.size());
  }

  private void waitForBids() {
    waitForAllThreads(mockedDependenciesRule.getTrackingCommandsExecutor());
  }

}
