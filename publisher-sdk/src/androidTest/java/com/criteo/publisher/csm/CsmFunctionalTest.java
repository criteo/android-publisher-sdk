/*
 *    Copyright 2020 Criteo
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.criteo.publisher.csm;

import static com.criteo.publisher.CriteoUtil.givenInitializedCriteo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import androidx.annotation.NonNull;
import androidx.core.util.Consumer;
import androidx.test.filters.FlakyTest;
import com.criteo.publisher.Clock;
import com.criteo.publisher.Criteo;
import com.criteo.publisher.LiveCdbCallListener;
import com.criteo.publisher.TestAdUnits;
import com.criteo.publisher.context.ContextData;
import com.criteo.publisher.csm.MetricRequest.MetricRequestFeedback;
import com.criteo.publisher.integration.Integration;
import com.criteo.publisher.integration.IntegrationRegistry;
import com.criteo.publisher.mock.MockedDependenciesRule;
import com.criteo.publisher.mock.SpyBean;
import com.criteo.publisher.model.AdUnit;
import com.criteo.publisher.network.CdbMock;
import com.criteo.publisher.network.LiveBidRequestSender;
import com.criteo.publisher.network.PubSdkApi;
import com.criteo.publisher.privacy.ConsentData;
import com.criteo.publisher.util.BuildConfigWrapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import javax.inject.Inject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

@FlakyTest(detail = "CSM are triggered concurrently of bid")
public class CsmFunctionalTest {

  @Rule
  public MockedDependenciesRule mockedDependenciesRule = new MockedDependenciesRule();

  @Rule
  public MockitoRule mockitoRule = MockitoJUnit.rule();

  @Inject
  private IntegrationRegistry integrationRegistry;

  @SpyBean
  private BuildConfigWrapper buildConfigWrapper;

  @SpyBean
  private PubSdkApi api;

  @SpyBean
  private Clock clock;

  @Inject
  private CdbMock cdbMock;

  @Inject
  private ConsentData consentData;

  @Inject
  private MetricSendingQueueConsumer metricSendingQueueConsumer;

  @SpyBean
  private LiveBidRequestSender liveBidRequestSender;

  @Before
  public void setUp() throws Exception {
    integrationRegistry.declare(Integration.IN_HOUSE);
    consentData.setConsentGiven(true);
  }

  @Test
  public void givenConsumedLiveBids_CallApiWithCsmOfConsumedBid() throws Exception {
    givenInitializedCriteo();
    waitForIdleState();

    loadBid(TestAdUnits.BANNER_320_50);
    waitForIdleState();

    sendMetricBatch();

    AtomicReference<String> firstImpressionId = new AtomicReference<>();
    AtomicReference<String> firstRequestGroupId = new AtomicReference<>();

    verify(api).postCsm(argThat(request -> {
      assertRequestHeaderIsExpected(request);

      // There is only one expected because the second one was not ready when sending was triggered.
      assertEquals(1, request.getFeedbacks().size());
      MetricRequestFeedback feedback = request.getFeedbacks().get(0);
      assertItRepresentsConsumedLiveBid(feedback);
      firstImpressionId.set(feedback.getSlots().get(0).getImpressionId());
      firstRequestGroupId.set(feedback.getRequestGroupId());

      return true;
    }));

    clearInvocations(api);
    loadBid(TestAdUnits.BANNER_320_50);
    waitForIdleState();

    sendMetricBatch();

    verify(api).postCsm(argThat(request -> {
      assertRequestHeaderIsExpected(request);

      assertEquals(1, request.getFeedbacks().size());
      MetricRequestFeedback feedback = request.getFeedbacks().get(0);
      assertItRepresentsConsumedLiveBid(feedback);
      assertNotEquals(firstImpressionId.get(), feedback.getSlots().get(0).getImpressionId());

      // This two metrics come from the different CDB requests and should have the different ID
      assertNotEquals(firstRequestGroupId.get(), feedback.getRequestGroupId());

      return true;
    }));
  }

  @Test
  public void givenConsumedExpiredBid_CallApiWithCsmOfConsumedExpiredBid() throws Exception {
    givenTimeBudgetExceededWhenFetchingLiveBids();
    givenInitializedCriteo();
    waitForIdleState();

    when(clock.getCurrentTimeInMillis()).thenReturn(0L);
    loadBid(TestAdUnits.INTERSTITIAL); // fetched but not consumed
    waitForIdleState();

    when(clock.getCurrentTimeInMillis()).thenReturn(Long.MAX_VALUE); // bid is expired
    loadBid(TestAdUnits.INTERSTITIAL); // consumed but expired
    waitForIdleState();

    sendMetricBatch();

    verify(api).postCsm(argThat(request -> {
      assertRequestHeaderIsExpected(request);

      assertEquals(1, request.getFeedbacks().size());
      MetricRequestFeedback feedback = request.getFeedbacks().get(0);
      assertItRepresentsExpiredConsumedLiveBid(feedback);

      return true;
    }));
  }

  @Test
  public void givenNoBidFromCdb_CallApiWithCsmOfNoBid() throws Exception {
    givenInitializedCriteo();
    waitForIdleState();

    loadBid(TestAdUnits.INTERSTITIAL_UNKNOWN);
    waitForIdleState();

    sendMetricBatch();

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

    givenInitializedCriteo();
    waitForIdleState();

    loadBid(TestAdUnits.INTERSTITIAL_UNKNOWN);
    waitForIdleState();

    sendMetricBatch();

    verify(api).postCsm(argThat(request -> {
      assertRequestHeaderIsExpected(request);

      assertEquals(1, request.getFeedbacks().size());
      MetricRequestFeedback feedback = request.getFeedbacks().get(0);
      assertItRepresentsNetworkError(feedback);

      return true;
    }));
  }

  @Test
  public void givenTimeoutErrorFromCdb_CallApiWithCsmOfTimeoutError() throws Exception {
    givenInitializedCriteo();
    waitForIdleState();

    when(buildConfigWrapper.getNetworkTimeoutInMillis()).thenReturn(1);

    loadBid(TestAdUnits.INTERSTITIAL_UNKNOWN);
    loadBid(TestAdUnits.BANNER_320_480);
    waitForIdleState();

    when(buildConfigWrapper.getNetworkTimeoutInMillis()).thenCallRealMethod();
    sendMetricBatch();

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

      assertNotEquals(feedback1.getRequestGroupId(), feedback2.getRequestGroupId());

      return true;
    }));
  }

  @Test
  public void givenErrorWhenSendingCsm_QueueMetricsUntilCsmRequestWorksAgain() throws Exception {
    when(buildConfigWrapper.preconditionThrowsOnException()).thenReturn(false);
    doThrow(IOException.class).when(api).postCsm(any());

    when(clock.getCurrentTimeInMillis()).thenReturn(0L);
    givenInitializedCriteo();
    waitForIdleState();

    // Consumed and not expired -> CSM
    loadBid(TestAdUnits.BANNER_320_50);
    waitForIdleState();

    // No bid consumed, and bid (1) is cached -> no CSM
    givenTimeBudgetExceededWhenFetchingLiveBids();
    loadBid(TestAdUnits.INTERSTITIAL);
    waitForIdleState();

    // Consumed and not expired -> CSM
    givenTimeBudgetRespectedWhenFetchingLiveBids();
    loadBid(TestAdUnits.INTERSTITIAL);
    waitForIdleState();

    // Bid (1) is consumed but expired -> CSM of (1)
    // a bid (2) is cached but never consumed -> no CSM
    when(clock.getCurrentTimeInMillis()).thenCallRealMethod();
    givenTimeBudgetExceededWhenFetchingLiveBids();
    loadBid(TestAdUnits.INTERSTITIAL);
    waitForIdleState();

    // No bid -> CSM
    givenTimeBudgetRespectedWhenFetchingLiveBids();
    loadBid(TestAdUnits.INTERSTITIAL_UNKNOWN);
    waitForIdleState();

    // Timeout -> CSM
    cdbMock.simulatorSlowNetworkOnNextRequest();
    when(buildConfigWrapper.getNetworkTimeoutInMillis()).thenReturn(1);
    loadBid(TestAdUnits.BANNER_320_50);
    waitForIdleState();
    when(buildConfigWrapper.getNetworkTimeoutInMillis()).thenCallRealMethod();

    // Network error -> CSM
    doThrow(IOException.class).when(api).loadCdb(any(), any());
    loadBid(TestAdUnits.BANNER_320_50);
    waitForIdleState();
    doCallRealMethod().when(api).loadCdb(any(), any());

    // CSM endpoint works again
    clearInvocations(api);
    doCallRealMethod().when(api).postCsm(any());
    sendMetricBatch();

    verify(api).postCsm(argThat(request -> {
      assertRequestHeaderIsExpected(request);

      List<MetricRequestFeedback> feedbacks = request.getFeedbacks();
      assertEquals(6, feedbacks.size());
      assertOnlyNSatisfy(feedbacks, 2, this::assertItRepresentsConsumedLiveBid);
      assertOnlyNSatisfy(feedbacks, 1, this::assertItRepresentsExpiredConsumedLiveBid);
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

    assertThat(validatedElements).describedAs(
        "%d elements should match given validation among %s",
        expectedNumberOfValidatedElements,
        elements
    ).hasSize(expectedNumberOfValidatedElements);
  }

  private void assertRequestHeaderIsExpected(MetricRequest request) {
    assertEquals(integrationRegistry.getProfileId(), request.getProfileId());
    assertEquals(buildConfigWrapper.getSdkVersion(), request.getWrapperVersion());
  }

  private void assertItRepresentsConsumedLiveBid(MetricRequestFeedback feedback) {
    assertEquals(0, feedback.getCdbCallStartElapsed());
    assertNotNull(feedback.getCdbCallEndElapsed());
    assertNotNull(feedback.getElapsed());
    assertFalse(feedback.isTimeout());
    assertNotNull(feedback.getRequestGroupId());
    assertEquals(1, feedback.getSlots().size());
    assertFalse(feedback.getSlots().get(0).getCachedBidUsed());
    assertNotNull(feedback.getSlots().get(0).getZoneId());
  }

  private void assertItRepresentsExpiredConsumedLiveBid(MetricRequestFeedback feedback) {
    assertEquals(0, feedback.getCdbCallStartElapsed());
    assertNotNull(feedback.getCdbCallEndElapsed());
    assertNull(feedback.getElapsed());
    assertFalse(feedback.isTimeout());
    assertNotNull(feedback.getRequestGroupId());
    assertEquals(1, feedback.getSlots().size());
    assertTrue(feedback.getSlots().get(0).getCachedBidUsed());
    assertNotNull(feedback.getSlots().get(0).getZoneId());
  }

  private void assertItRepresentsNoBid(MetricRequestFeedback feedback) {
    assertEquals(0, feedback.getCdbCallStartElapsed());
    assertNotNull(feedback.getCdbCallEndElapsed());
    assertNull(feedback.getElapsed());
    assertFalse(feedback.isTimeout());
    assertNotNull(feedback.getRequestGroupId());
    assertEquals(1, feedback.getSlots().size());
    assertFalse(feedback.getSlots().get(0).getCachedBidUsed());
    assertNull(feedback.getSlots().get(0).getZoneId());
  }

  private void assertItRepresentsNetworkError(MetricRequestFeedback feedback) {
    assertEquals(0, feedback.getCdbCallStartElapsed());
    assertNull(feedback.getCdbCallEndElapsed());
    assertNull(feedback.getElapsed());
    assertFalse(feedback.isTimeout());
    assertNotNull(feedback.getRequestGroupId());
    assertEquals(1, feedback.getSlots().size());
    assertFalse(feedback.getSlots().get(0).getCachedBidUsed());
    assertNull(feedback.getSlots().get(0).getZoneId());
  }

  private void assertItRepresentsTimeoutError(MetricRequestFeedback feedback) {
    assertEquals(0, feedback.getCdbCallStartElapsed());
    assertNull(feedback.getCdbCallEndElapsed());
    assertNull(feedback.getElapsed());
    assertTrue(feedback.isTimeout());
    assertNotNull(feedback.getRequestGroupId());
    assertEquals(1, feedback.getSlots().size());
    assertFalse(feedback.getSlots().get(0).getCachedBidUsed());
    assertNull(feedback.getSlots().get(0).getZoneId());
  }

  private void waitForIdleState() {
    mockedDependenciesRule.waitForIdleState();
  }

  private void loadBid(@NonNull AdUnit adUnit) {
    Criteo.getInstance().loadBid(adUnit, new ContextData(), ignored -> { /* no op */ });
  }

  private void sendMetricBatch() {
    metricSendingQueueConsumer.sendMetricBatch();
    waitForIdleState();
  }

  private void givenTimeBudgetRespectedWhenFetchingLiveBids() {
    doNothing().when(liveBidRequestSender).scheduleTimeBudgetExceeded$publisher_sdk_debug(any());
  }

  private void givenTimeBudgetExceededWhenFetchingLiveBids() {
    doAnswer(invocation -> {
      invocation.getArgument(0, LiveCdbCallListener.class).onTimeBudgetExceeded();
      return null;
    }).when(liveBidRequestSender).scheduleTimeBudgetExceeded$publisher_sdk_debug(any());
  }
}
