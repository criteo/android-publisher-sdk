package com.criteo.publisher.degraded;

import static com.criteo.publisher.CriteoUtil.givenInitializedCriteo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

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

public class HeaderBiddingDegradedTest {

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
  public void whenSettingABids_ShouldNotDoAnyCallToCdb() throws Exception {
    Object builder = mock(Object.class);

    criteo.setBidsForAdUnit(builder, adUnit);
    waitForIdleState();

    verifyNoInteractions(api);
  }

  @Test
  public void whenSettingABids_ShouldNotEnrichGivenBuilder() throws Exception {
    Object builder = mock(Object.class);

    criteo.setBidsForAdUnit(builder, adUnit);
    waitForIdleState();

    verifyNoInteractions(builder);
  }

  private void waitForIdleState() {
    mockedDependenciesRule.waitForIdleState();
  }
}
