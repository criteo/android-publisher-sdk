package com.criteo.publisher.degraded;


import static com.criteo.publisher.CriteoUtil.givenInitializedCriteo;
import static com.criteo.publisher.concurrent.ThreadingUtil.waitForAllThreads;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.criteo.publisher.BidResponse;
import com.criteo.publisher.Criteo;
import com.criteo.publisher.mock.MockedDependenciesRule;
import com.criteo.publisher.mock.SpyBean;
import com.criteo.publisher.model.AdUnit;
import com.criteo.publisher.network.PubSdkApi;
import com.criteo.publisher.util.DeviceUtil;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class InHouseDegradedTest {

  @Rule
  public MockedDependenciesRule mockedDependenciesRule = new MockedDependenciesRule();

  @Mock
  private AdUnit adUnit;

  @Mock
  private PubSdkApi api;

  @SpyBean
  private DeviceUtil deviceUtil;

  private Criteo criteo;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    when(deviceUtil.isVersionSupported()).thenReturn(false);

    criteo = givenInitializedCriteo();
  }

  @Test
  public void whenGettingABidResponse_ShouldNotDoAnyCallToCdb() throws Exception {
    criteo.getBidResponse(adUnit);
    waitForIdleState();

    verifyNoInteractions(api);
  }

  @Test
  public void whenGettingABidResponseTwice_ShouldReturnANoBid() throws Exception {
    BidResponse bidResponse1 = criteo.getBidResponse(adUnit);
    waitForIdleState();

    BidResponse bidResponse2 = criteo.getBidResponse(adUnit);
    waitForIdleState();

    assertIsNoBid(bidResponse1);
    assertIsNoBid(bidResponse2);
  }

  private void assertIsNoBid(BidResponse bidResponse) {
    assertFalse(bidResponse.isBidSuccess());
    assertEquals(0.0, bidResponse.getPrice(), 0.0);
    assertNull(bidResponse.getBidToken());
  }

  private void waitForIdleState() {
    waitForAllThreads(mockedDependenciesRule.getTrackingCommandsExecutor());
  }
}
