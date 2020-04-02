package com.criteo.publisher.csm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.verify;

import com.criteo.publisher.Criteo;
import com.criteo.publisher.CriteoUtil;
import com.criteo.publisher.TestAdUnits;
import com.criteo.publisher.Util.BuildConfigWrapper;
import com.criteo.publisher.concurrent.ThreadingUtil;
import com.criteo.publisher.csm.MetricRequest.MetricRequestFeedback;
import com.criteo.publisher.mock.MockedDependenciesRule;
import com.criteo.publisher.mock.SpyBean;
import com.criteo.publisher.network.PubSdkApi;
import java.util.concurrent.atomic.AtomicReference;
import javax.inject.Inject;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;

public class CsmFunctionalTest {

  @Rule
  public MockedDependenciesRule mockedDependenciesRule = new MockedDependenciesRule();

  @Inject
  private MetricSendingQueue queue;

  @Inject
  private MetricRepository repository;

  @Inject
  private BuildConfigWrapper buildConfigWrapper;

  @SpyBean
  private PubSdkApi api;

  @After
  public void tearDown() throws Exception {
    cleanState();
  }

  @Test
  public void givenPrefetchAdUnitsWithBidsThenConsumption_CallApiWithCsmOfConsumedBid() throws Exception {
    CriteoUtil.givenInitializedCriteo(TestAdUnits.BANNER_320_50, TestAdUnits.INTERSTITIAL);
    waitForIdleState();

    Criteo.getInstance().getBidResponse(TestAdUnits.BANNER_320_50);
    waitForIdleState();

    AtomicReference<String> firstImpressionId = new AtomicReference<>();

    verify(api).postCsm(argThat(request -> {
      assertRequestHeaderIsExpected(request);

      // There is only one expected because the second one was not ready when sending was triggered.
      assertEquals(1, request.getFeedbacks().size());
      MetricRequestFeedback feedback = request.getFeedbacks().get(0);
      assertItRepresentsConsumedBid(feedback);
      firstImpressionId.set(feedback.getSlots().get(0).getImpressionId());

      return true;
    }));
    clearInvocations(api);

    Criteo.getInstance().getBidResponse(TestAdUnits.INTERSTITIAL);
    waitForIdleState();

    verify(api).postCsm(argThat(request -> {
      assertRequestHeaderIsExpected(request);

      assertEquals(1, request.getFeedbacks().size());
      MetricRequestFeedback feedback = request.getFeedbacks().get(0);
      assertItRepresentsConsumedBid(feedback);
      assertNotEquals(firstImpressionId.get(), feedback.getSlots().get(0).getImpressionId());

      return true;
    }));
  }

  private void assertRequestHeaderIsExpected(MetricRequest request) {
    assertEquals(buildConfigWrapper.getProfileId(), request.getProfileId());
    assertEquals(buildConfigWrapper.getSdkVersion(), request.getWrapperVersion());
  }

  private void assertItRepresentsConsumedBid(MetricRequestFeedback feedback) {
    assertEquals(0, feedback.getCdbCallStartElapsed());
    assertNotNull(feedback.getCdbCallEndElapsed());
    assertNotNull(feedback.getElapsed());
    assertEquals(1, feedback.getSlots().size());
    assertTrue(feedback.getSlots().get(0).getCachedBidUsed());
  }

  private void waitForIdleState() {
    ThreadingUtil.waitForAllThreads(mockedDependenciesRule.getTrackingCommandsExecutor());
  }

  private void cleanState() {
    queue.poll(Integer.MAX_VALUE);

    for (Metric metric : repository.getAllStoredMetrics()) {
      repository.moveById(metric.getImpressionId(), ignored -> true);
    }
  }

}
