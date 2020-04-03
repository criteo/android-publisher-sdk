package com.criteo.publisher.csm;

import static com.criteo.publisher.CriteoUtil.givenInitializedCriteo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.support.v4.util.Consumer;
import com.criteo.publisher.Clock;
import com.criteo.publisher.Criteo;
import com.criteo.publisher.TestAdUnits;
import com.criteo.publisher.util.BuildConfigWrapper;
import com.criteo.publisher.concurrent.ThreadingUtil;
import com.criteo.publisher.csm.MetricRequest.MetricRequestFeedback;
import com.criteo.publisher.mock.MockedDependenciesRule;
import com.criteo.publisher.mock.SpyBean;
import com.criteo.publisher.network.PubSdkApi;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import javax.inject.Inject;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class CsmFunctionalTest {

  @Rule
  public MockedDependenciesRule mockedDependenciesRule = new MockedDependenciesRule();

  @Inject
  private MetricSendingQueue queue;

  @Inject
  private MetricRepository repository;

  @SpyBean
  private BuildConfigWrapper buildConfigWrapper;

  @SpyBean
  private PubSdkApi api;

  @SpyBean
  private Clock clock;

  @Before
  public void setUp() throws Exception {
    cleanState();
  }

  @After
  public void tearDown() throws Exception {
    cleanState();
  }

  @Test
  public void givenPrefetchAdUnitsWithBidsThenConsumption_CallApiWithCsmOfConsumedBid() throws Exception {
    givenInitializedCriteo(TestAdUnits.BANNER_320_50, TestAdUnits.INTERSTITIAL);
    waitForIdleState();

    Criteo.getInstance().getBidResponse(TestAdUnits.BANNER_320_50);
    waitForIdleState();

    AtomicReference<String> firstImpressionId = new AtomicReference<>();
    AtomicReference<String> firstRequestGroupId = new AtomicReference<>();

    verify(api).postCsm(argThat(request -> {
      assertRequestHeaderIsExpected(request);

      // There is only one expected because the second one was not ready when sending was triggered.
      assertEquals(1, request.getFeedbacks().size());
      MetricRequestFeedback feedback = request.getFeedbacks().get(0);
      assertItRepresentsConsumedBid(feedback);
      firstImpressionId.set(feedback.getSlots().get(0).getImpressionId());
      firstRequestGroupId.set(feedback.getRequestGroupId());

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

      // This two metrics come from the same CDB request (prefetch) and should have the same ID
      assertEquals(firstRequestGroupId.get(), feedback.getRequestGroupId());

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

      // This metric come from another CDB request and should have another ID
      assertNotEquals(firstRequestGroupId.get(), feedback.getRequestGroupId());

      return true;
    }));
  }

  @Test
  public void givenConsumedExpiredBid_CallApiWithCsmOfConsumedExpiredBid() throws Exception {
    when(clock.getCurrentTimeInMillis()).thenReturn(0L);
    givenInitializedCriteo(TestAdUnits.BANNER_320_50, TestAdUnits.INTERSTITIAL);
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
    givenInitializedCriteo(TestAdUnits.BANNER_320_50, TestAdUnits.INTERSTITIAL_UNKNOWN);
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

    givenInitializedCriteo(TestAdUnits.BANNER_320_50, TestAdUnits.INTERSTITIAL);
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

      assertEquals(feedback1.getRequestGroupId(), feedback2.getRequestGroupId());

      return true;
    }));
  }

  @Test
  public void givenTimeoutErrorFromCdb_CallApiWithCsmOfTimeoutError() throws Exception {
    when(buildConfigWrapper.getNetworkTimeoutInMillis()).thenReturn(1);

    givenInitializedCriteo(TestAdUnits.BANNER_320_50, TestAdUnits.INTERSTITIAL);
    waitForIdleState();

    when(buildConfigWrapper.getNetworkTimeoutInMillis()).thenCallRealMethod();

    Criteo.getInstance().getBidResponse(TestAdUnits.INTERSTITIAL_UNKNOWN);
    waitForIdleState();

    verify(api).postCsm(argThat(request -> {
      assertRequestHeaderIsExpected(request);

      assertEquals(2, request.getFeedbacks().size());
      MetricRequestFeedback feedback1 = request.getFeedbacks().get(0);
      MetricRequestFeedback feedback2 = request.getFeedbacks().get(1);
      assertItRepresentsTimeoutError(feedback1);
      assertItRepresentsTimeoutError(feedback2);

      assertNotEquals(
          feedback1.getSlots().get(0).getImpressionId(),
          feedback2.getSlots().get(0).getImpressionId()
      );

      assertEquals(feedback1.getRequestGroupId(), feedback2.getRequestGroupId());

      return true;
    }));
  }

  @Test
  public void givenErrorWhenSendingCsm_QueueMetricsUntilCsmRequestWorksAgain() throws Exception {
    when(buildConfigWrapper.isDebug()).thenReturn(false);
    doThrow(IOException.class).when(api).postCsm(any());

    when(clock.getCurrentTimeInMillis()).thenReturn(0L);
    Criteo criteo = givenInitializedCriteo(TestAdUnits.BANNER_320_50, TestAdUnits.INTERSTITIAL);
    waitForIdleState();

    // Consumed and not expired
    criteo.getBidResponse(TestAdUnits.INTERSTITIAL);
    waitForIdleState();

    // Consumed but expired
    // There is also a bid request here and consumed at timeout step
    when(clock.getCurrentTimeInMillis()).thenCallRealMethod();
    criteo.getBidResponse(TestAdUnits.INTERSTITIAL);
    waitForIdleState();

    // No bid
    criteo.getBidResponse(TestAdUnits.INTERSTITIAL_UNKNOWN);
    waitForIdleState();

    // Timeout
    when(buildConfigWrapper.getNetworkTimeoutInMillis()).thenReturn(1);
    criteo.getBidResponse(TestAdUnits.INTERSTITIAL);
    waitForIdleState();
    when(buildConfigWrapper.getNetworkTimeoutInMillis()).thenCallRealMethod();

    // Network error
    doThrow(IOException.class).when(api).loadCdb(any(), any());
    criteo.getBidResponse(TestAdUnits.INTERSTITIAL);
    waitForIdleState();
    doCallRealMethod().when(api).loadCdb(any(), any());

    // CSM endpoint works again: on next bid request, there should metrics for all previous bids.
    clearInvocations(api);
    doCallRealMethod().when(api).postCsm(any());
    criteo.getBidResponse(TestAdUnits.BANNER_UNKNOWN);
    waitForIdleState();

    verify(api).postCsm(argThat(request -> {
      assertRequestHeaderIsExpected(request);

      List<MetricRequestFeedback> feedbacks = request.getFeedbacks();
      assertEquals(6, feedbacks.size());
      assertOnlyNSatisfy(feedbacks, 2, this::assertItRepresentsConsumedBid);
      assertOnlyNSatisfy(feedbacks, 1, this::assertItRepresentsExpiredConsumedBid);
      assertOnlyNSatisfy(feedbacks, 1, this::assertItRepresentsNoBid);
      assertOnlyNSatisfy(feedbacks, 1, this::assertItRepresentsTimeoutError);
      assertOnlyNSatisfy(feedbacks, 1, this::assertItRepresentsNetworkError);

      Set<String> impressionIds = new HashSet<>();
      Set<String> requestGroupIds = new HashSet<>();
      for (MetricRequestFeedback feedback : feedbacks) {
        impressionIds.add(feedback.getSlots().get(0).getImpressionId());
        requestGroupIds.add(feedback.getRequestGroupId());
      }

      assertEquals(6, impressionIds.size());
      assertEquals(6, requestGroupIds.size());

      return true;
    }));
  }

  private <T> void assertOnlyNSatisfy(
      Collection<T> elements,
      int expectedNumberOfValidatedElements,
      Consumer<T> validation) {
    List<T> validatedElements = new ArrayList<>();
    for (T element : elements) {
      try {
        validation.accept(element);
        validatedElements.add(element);
      } catch (AssertionError e) {
        // ignored
      }
    }

    assertEquals(expectedNumberOfValidatedElements, validatedElements.size());
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
    assertNotNull(feedback.getRequestGroupId());
    assertEquals(1, feedback.getSlots().size());
    assertTrue(feedback.getSlots().get(0).getCachedBidUsed());
  }

  private void assertItRepresentsExpiredConsumedBid(MetricRequestFeedback feedback) {
    assertEquals(0, feedback.getCdbCallStartElapsed());
    assertNotNull(feedback.getCdbCallEndElapsed());
    assertNull(feedback.getElapsed());
    assertFalse(feedback.isTimeout());
    assertNotNull(feedback.getRequestGroupId());
    assertEquals(1, feedback.getSlots().size());
    assertTrue(feedback.getSlots().get(0).getCachedBidUsed());
  }

  private void assertItRepresentsNoBid(MetricRequestFeedback feedback) {
    assertEquals(0, feedback.getCdbCallStartElapsed());
    assertNotNull(feedback.getCdbCallEndElapsed());
    assertNull(feedback.getElapsed());
    assertFalse(feedback.isTimeout());
    assertNotNull(feedback.getRequestGroupId());
    assertEquals(1, feedback.getSlots().size());
    assertFalse(feedback.getSlots().get(0).getCachedBidUsed());
  }

  private void assertItRepresentsNetworkError(MetricRequestFeedback feedback) {
    assertEquals(0, feedback.getCdbCallStartElapsed());
    assertNull(feedback.getCdbCallEndElapsed());
    assertNull(feedback.getElapsed());
    assertFalse(feedback.isTimeout());
    assertNotNull(feedback.getRequestGroupId());
    assertEquals(1, feedback.getSlots().size());
    assertFalse(feedback.getSlots().get(0).getCachedBidUsed());
  }

  private void assertItRepresentsTimeoutError(MetricRequestFeedback feedback) {
    assertEquals(0, feedback.getCdbCallStartElapsed());
    assertNull(feedback.getCdbCallEndElapsed());
    assertNull(feedback.getElapsed());
    assertTrue(feedback.isTimeout());
    assertNotNull(feedback.getRequestGroupId());
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
