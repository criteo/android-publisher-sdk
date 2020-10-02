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

package com.criteo.publisher.degraded;


import static com.criteo.publisher.CriteoUtil.givenInitializedCriteo;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.criteo.publisher.BidResponseListener;
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

  @Mock
  private BidResponseListener listener;

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
    criteo.loadBidResponse(adUnit, listener);
    waitForIdleState();

    verifyNoInteractions(api);
    verify(listener).onResponse(null);
  }

  @Test
  public void whenGettingABidResponseTwice_ShouldReturnANoBid() throws Exception {
    criteo.loadBidResponse(adUnit, listener);
    waitForIdleState();

    criteo.loadBidResponse(adUnit, listener);
    waitForIdleState();

    verify(listener, times(2)).onResponse(null);
  }

  private void waitForIdleState() {
    mockedDependenciesRule.waitForIdleState();
  }
}
