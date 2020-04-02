package com.criteo.publisher.csm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.criteo.publisher.Clock;
import com.criteo.publisher.Criteo;
import com.criteo.publisher.CriteoUtil;
import com.criteo.publisher.TestAdUnits;
import com.criteo.publisher.Util.BuildConfigWrapper;
import com.criteo.publisher.concurrent.ThreadingUtil;
import com.criteo.publisher.csm.MetricRequest.MetricRequestFeedback;
import com.criteo.publisher.mock.MockedDependenciesRule;
import com.criteo.publisher.mock.SpyBean;
import com.criteo.publisher.network.PubSdkApi;
import java.io.IOException;
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

  @SpyBean
  private Clock clock;

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

  @Test
  public void givenConsumedExpiredBid_CallApiWithCsmOfConsumedExpiredBid() throws Exception {
    when(clock.getCurrentTimeInMillis()).thenReturn(0L);
    CriteoUtil.givenInitializedCriteo(TestAdUnits.BANNER_320_50, TestAdUnits.INTERSTITIAL);
    waitForIdleState();

    when(clock.getCurrentTimeInMillis()).thenReturn(Long.MAX_VALUE);
    Criteo.getInstance().getBidResponse(TestAdUnits.INTERSTITIAL);
    waitForIdleState();

    verify(api).postCsm(argThat(request -> {
      assertRequestHeaderIsExpected(request);

      assertEquals(1, request.getFeedbacks().size());
      MetricRequestFeedback feedback = request.getFeedbacks().get(0);
      assertItRepresentsExpiredConsumedBid(feedback);

      return true;
    }));
  }

  @Test
  public void givenNoBidFromCdb_CallApiWithCsmOfNoBid() throws Exception {
    CriteoUtil.givenInitializedCriteo(TestAdUnits.BANNER_320_50, TestAdUnits.INTERSTITIAL_UNKNOWN);
    waitForIdleState();

    Criteo.getInstance().getBidResponse(TestAdUnits.INTERSTITIAL_UNKNOWN);
    waitForIdleState();

    verify(api).postCsm(argThat(request -> {
      assertRequestHeaderIsExpected(request);

      assertEquals(1, request.getFeedbacks().size());
      MetricRequestFeedback feedback = request.getFeedbacks().get(0);
      assertItRepresentsNoBid(feedback);

      return true;
    }));
  }

  @Test
  public void givenNetworkErrorFromCdb_CallApiWithCsmOfNetworkError() throws Exception {
    doThrow(IOException.class).when(api).loadCdb(any(), any());

    CriteoUtil.givenInitializedCriteo(TestAdUnits.BANNER_320_50, TestAdUnits.INTERSTITIAL);
    waitForIdleState();

    Criteo.getInstance().getBidResponse(TestAdUnits.INTERSTITIAL_UNKNOWN);
    waitForIdleState();

    verify(api).postCsm(argThat(request -> {
      assertRequestHeaderIsExpected(request);

      assertEquals(2, request.getFeedbacks().size());
      MetricRequestFeedback feedback1 = request.getFeedbacks().get(0);
      MetricRequestFeedback feedback2 = request.getFeedbacks().get(1);
      assertItRepresentsNetworkError(feedback1);
      assertItRepresentsNetworkError(feedback2);

      assertNotEquals(
          feedback1.getSlots().get(0).getImpressionId(),
          feedback2.getSlots().get(0).getImpressionId()
      );

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
    assertFalse(feedback.isTimeout());
    assertEquals(1, feedback.getSlots().size());
    assertTrue(feedback.getSlots().get(0).getCachedBidUsed());
  }

  private void assertItRepresentsExpiredConsumedBid(MetricRequestFeedback feedback) {
    assertEquals(0, feedback.getCdbCallStartElapsed());
    assertNotNull(feedback.getCdbCallEndElapsed());
    assertNull(feedback.getElapsed());
    assertFalse(feedback.isTimeout());
    assertEquals(1, feedback.getSlots().size());
    assertTrue(feedback.getSlots().get(0).getCachedBidUsed());
  }

  private void assertItRepresentsNoBid(MetricRequestFeedback feedback) {
    assertEquals(0, feedback.getCdbCallStartElapsed());
    assertNotNull(feedback.getCdbCallEndElapsed());
    assertNull(feedback.getElapsed());
    assertFalse(feedback.isTimeout());
    assertEquals(1, feedback.getSlots().size());
    assertFalse(feedback.getSlots().get(0).getCachedBidUsed());
  }

  private void assertItRepresentsNetworkError(MetricRequestFeedback feedback) {
    assertEquals(0, feedback.getCdbCallStartElapsed());
    assertNull(feedback.getCdbCallEndElapsed());
    assertNull(feedback.getElapsed());
    assertFalse(feedback.isTimeout());
    assertEquals(1, feedback.getSlots().size());
    assertFalse(feedback.getSlots().get(0).getCachedBidUsed());
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
