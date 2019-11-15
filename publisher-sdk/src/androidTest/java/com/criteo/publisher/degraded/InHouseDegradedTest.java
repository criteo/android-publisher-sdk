package com.criteo.publisher.degraded;

import static com.criteo.publisher.ThreadingUtil.waitForMockedBid;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import com.criteo.publisher.BidResponse;
import com.criteo.publisher.Criteo;
import com.criteo.publisher.CriteoUtil;
import com.criteo.publisher.DependencyProvider;
import com.criteo.publisher.Util.MockedDependenciesRule;
import com.criteo.publisher.model.AdUnit;
import com.criteo.publisher.network.PubSdkApi;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class InHouseDegradedTest {
  @Rule
  public MockedDependenciesRule mockedDependenciesRule  = new MockedDependenciesRule();

  @Mock
  private AdUnit adUnit;

  @Mock
  private PubSdkApi api;

  private Criteo criteo;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    DependencyProvider dependencyProvider = mockedDependenciesRule.getDependencyProvider();
    when(dependencyProvider.providePubSdkApi()).thenReturn(api);

    DegradedUtil.assumeIsDegraded();

    criteo = CriteoUtil.givenInitializedCriteo();
  }

  @Test
  public void whenGettingABidResponse_ShouldNotDoAnyCallToCdb() throws Exception {
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
