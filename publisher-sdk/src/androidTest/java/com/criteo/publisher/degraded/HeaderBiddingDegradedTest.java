package com.criteo.publisher.degraded;

import static com.criteo.publisher.ThreadingUtil.waitForMockedBid;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;

import android.app.Application;
import android.support.test.InstrumentationRegistry;
import com.criteo.publisher.Criteo;
import com.criteo.publisher.CriteoUtil;
import com.criteo.publisher.model.AdUnit;
import com.criteo.publisher.network.PubSdkApi;
import com.criteo.publisher.network.PubSdkApiHelper;
import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class HeaderBiddingDegradedTest {

  @Mock
  private AdUnit adUnit;

  @Mock
  private PubSdkApi api;

  private Criteo criteo;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    DegradedUtil.assumeIsDegraded();

    criteo = CriteoUtil.givenInitializedCriteo();
  }

  @Test
  public void whenSettingABids_ShouldNotDoAnyCallToCdb() throws Exception {
    Object builder = mock(Object.class);

    PubSdkApiHelper.withApi(api, () -> {
      criteo.setBidsForAdUnit(builder, adUnit);
    });

    waitForMockedBid();

    verifyZeroInteractions(api);
  }

  @Test
  public void whenSettingABids_ShouldNotEnrichGivenBuilder() throws Exception {
    Object builder = mock(Object.class);
    criteo.setBidsForAdUnit(builder, adUnit);

    waitForMockedBid();

    verifyZeroInteractions(builder);
  }

}
