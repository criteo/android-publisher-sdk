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

import static com.criteo.publisher.CriteoUtil.givenInitializedCriteo;
import static com.criteo.publisher.concurrent.ThreadingUtil.runOnMainThreadAndWait;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import androidx.test.InstrumentationRegistry;
import com.criteo.publisher.mock.MockedDependenciesRule;
import com.criteo.publisher.model.BannerAdUnit;
import java.util.UUID;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class CriteoBannerViewIntegrationTest {

  @Rule
  public MockedDependenciesRule mockedDependenciesRule = new MockedDependenciesRule();

  @Mock
  private CriteoBannerAdListener criteoBannerAdListener;

  private CriteoBannerView criteoBannerView;

  private BannerAdUnit bannerAdUnit = TestAdUnits.BANNER_320_50;

  @Before
  public void setup() throws Exception {
    MockitoAnnotations.initMocks(this);

    runOnMainThreadAndWait(() -> {
      criteoBannerView = new CriteoBannerView(
          InstrumentationRegistry.getContext(),
          bannerAdUnit);
    });

    criteoBannerView.setCriteoBannerAdListener(criteoBannerAdListener);
  }

  @Test
  public void loadAdInHouse_GivenSelfMadeToken_NotifyListenerForFailure() throws Exception {
    givenInitializedCriteo(bannerAdUnit);
    waitForIdleState();

    // This should not be possible since BidToken is not part of the public API.
    // But just in case, we may check that no publisher can attempt this.
    BidToken token = new BidToken(UUID.randomUUID(), bannerAdUnit);

    criteoBannerView.loadAd(token);
    waitForIdleState();

    verify(criteoBannerAdListener, never()).onAdReceived(criteoBannerView);
    verify(criteoBannerAdListener).onAdFailedToReceive(CriteoErrorCode.ERROR_CODE_NO_FILL);
  }

  private void waitForIdleState() {
    mockedDependenciesRule.waitForIdleState();
  }

}