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

package com.criteo.publisher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.criteo.publisher.concurrent.DirectMockRunOnUiThreadExecutor;
import com.criteo.publisher.context.ContextData;
import com.criteo.publisher.model.AdUnit;
import com.criteo.publisher.model.CdbResponseSlot;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class ConsumableBidLoaderTest {

  @Mock
  private BidManager bidManager;

  @Mock
  private Clock clock;

  @Mock
  private BidResponseListener listener;

  private final DirectMockRunOnUiThreadExecutor runOnUiThreadExecutor = new DirectMockRunOnUiThreadExecutor();

  private ConsumableBidLoader consumableBidLoader;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    doAnswer(invocation -> {
      runOnUiThreadExecutor.expectIsRunningInExecutor();
      return null;
    }).when(listener).onResponse(any());

    consumableBidLoader = new ConsumableBidLoader(
        bidManager,
        clock,
        runOnUiThreadExecutor
    );
  }

  @After
  public void tearDown() throws Exception {
    runOnUiThreadExecutor.verifyExpectations();
  }

  @Test
  public void getBidResponse_GivenBidManagerYieldingNoBid_ReturnNoBid() throws Exception {
    AdUnit adUnit = mock(AdUnit.class);
    ContextData contextData = mock(ContextData.class);

    doAnswer(invocation -> {
      invocation.<BidListener>getArgument(2).onNoBid();
      return null;
    }).when(bidManager).getBidForAdUnit(eq(adUnit), eq(contextData), any());

    consumableBidLoader.loadBid(adUnit, contextData, listener);

    verify(listener).onResponse(null);
  }

  @Test
  public void getBidResponse_GivenBidManagerYieldingBid_ReturnBid() throws Exception {
    AdUnit adUnit = mock(AdUnit.class);
    ContextData contextData = mock(ContextData.class);
    CdbResponseSlot slot = mock(CdbResponseSlot.class);

    when(slot.getCpmAsNumber()).thenReturn(42.1337);

    doAnswer(invocation -> {
      invocation.<BidListener>getArgument(2).onBidResponse(slot);
      return null;
    }).when(bidManager).getBidForAdUnit(eq(adUnit), eq(contextData), any());

    consumableBidLoader.loadBid(adUnit, contextData, listener);

    verify(listener).onResponse(argThat(bidResponse -> {
      assertThat(bidResponse.getPrice()).isEqualTo(42.1337);
      return true;
    }));
  }

}