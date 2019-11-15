package com.criteo.publisher.degraded;

import static com.criteo.publisher.ThreadingUtil.waitForMockedBid;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.verifyZeroInteractions;

import android.app.Application;
import android.support.test.InstrumentationRegistry;
import com.criteo.publisher.BidResponse;
import com.criteo.publisher.Criteo;
import com.criteo.publisher.model.AdUnit;
import com.criteo.publisher.network.PubSdkApi;
import com.criteo.publisher.network.PubSdkApiHelper;
import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class InHouseDegradedTest {

  @Mock
  private AdUnit adUnit;

  @Mock
  private PubSdkApi api;

  private Criteo criteo;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    DegradedUtil.assumeIsDegraded();

    Application app = (Application) InstrumentationRegistry.getTargetContext()
        .getApplicationContext();

    Criteo.init(app, "B-056946", Collections.emptyList());
    criteo = Criteo.getInstance();
  }

  @Test
  public void whenGettingABidResponse_ShouldNotDoAnyCallToCdb() throws Exception {
    PubSdkApiHelper.withApi(api, () -> {
      criteo.getBidResponse(adUnit);
    });

    waitForMockedBid();

    verifyZeroInteractions(api);
  }

  @Test
  public void whenGettingABidResponseTwice_ShouldReturnANoBid() throws Exception {
    BidResponse bidResponse = criteo.getBidResponse(adUnit);
    waitForMockedBid();

    assertIsNoBid(bidResponse);

    bidResponse = criteo.getBidResponse(adUnit);
    waitForMockedBid();

    assertIsNoBid(bidResponse);
  }

  private void assertIsNoBid(BidResponse bidResponse) {
    assertFalse(bidResponse.isBidSuccess());
    assertEquals(0.0, bidResponse.getPrice(), 0.0);
    assertNull(bidResponse.getBidToken());
  }

}
