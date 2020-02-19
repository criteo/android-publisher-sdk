package com.criteo.publisher.degraded;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

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

public class HeaderBiddingDegradedTest {

  @Rule
  public MockedDependenciesRule mockedDependenciesRule = new MockedDependenciesRule();

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
  public void whenSettingABids_ShouldNotDoAnyCallToCdb() throws Exception {
    Object builder = mock(Object.class);
    criteo.setBidsForAdUnit(builder, adUnit);
    mockedDependenciesRule.getTrackingCommandsExecutor().waitCommands();
    verifyZeroInteractions(api);
  }

  @Test
  public void whenSettingABids_ShouldNotEnrichGivenBuilder() throws Exception {
    Object builder = mock(Object.class);
    criteo.setBidsForAdUnit(builder, adUnit);
    mockedDependenciesRule.getTrackingCommandsExecutor().waitCommands();
    verifyZeroInteractions(builder);
  }
}
