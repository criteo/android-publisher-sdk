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
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import android.content.Context;
import com.criteo.publisher.application.InstrumentationUtil;
import com.criteo.publisher.mock.MockedDependenciesRule;
import com.criteo.publisher.model.BannerAdUnit;
import javax.inject.Inject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

public class CriteoBannerViewIntegrationTest {

  @Rule
  public MockedDependenciesRule mockedDependenciesRule = new MockedDependenciesRule();

  @Rule
  public MockitoRule mockitoRule = MockitoJUnit.rule();

  @Mock
  private CriteoBannerAdListener criteoBannerAdListener;

  @Inject
  private Context context;

  private CriteoBannerView criteoBannerView;

  private BannerAdUnit bannerAdUnit = TestAdUnits.BANNER_320_50;

  @Before
  public void setup() throws Exception {
    runOnMainThreadAndWait(() -> {
      criteoBannerView = new CriteoBannerView(
          InstrumentationUtil.getApplication().getApplicationContext(),
          bannerAdUnit);
    });

    criteoBannerView.setCriteoBannerAdListener(criteoBannerAdListener);
  }

  @Test
  public void instantiate_GivenContext_InitializeCorrectly() throws Exception {
    runOnMainThreadAndWait(() -> {
      CriteoBannerView criteoBannerView = new CriteoBannerView(context);
      assertThat(criteoBannerView.getContext()).isEqualTo(context);
    });
  }

  @Test
  public void loadAdInHouse_GivenSelfMadeToken_NotifyListenerForFailure() throws Exception {
    givenInitializedCriteo(bannerAdUnit);
    waitForIdleState();

    // This should not be possible since BidResponse constructor is not part of the public API.
    // But just in case, we may check that no publisher can attempt this.
    Bid bid = mock(Bid.class);

    criteoBannerView.loadAd(bid);
    waitForIdleState();

    verify(criteoBannerAdListener, never()).onAdReceived(criteoBannerView);
    verify(criteoBannerAdListener).onAdFailedToReceive(CriteoErrorCode.ERROR_CODE_NO_FILL);
  }

  private void waitForIdleState() {
    mockedDependenciesRule.waitForIdleState();
  }

}