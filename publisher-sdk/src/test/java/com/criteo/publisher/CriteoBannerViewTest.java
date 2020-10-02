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
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import android.content.Context;
import com.criteo.publisher.integration.Integration;
import com.criteo.publisher.integration.IntegrationRegistry;
import com.criteo.publisher.mock.MockBean;
import com.criteo.publisher.mock.MockedDependenciesRule;
import com.criteo.publisher.model.AdSize;
import com.criteo.publisher.model.AdUnit;
import com.criteo.publisher.model.BannerAdUnit;
import kotlin.jvm.JvmField;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class CriteoBannerViewTest {

  @Rule
  @JvmField
  public MockedDependenciesRule mockedDependenciesRule = new MockedDependenciesRule();

  @Mock
  private Context context;

  @Mock
  private CriteoBannerEventController controller;

  @Mock
  private Criteo criteo;

  @MockBean
  private IntegrationRegistry integrationRegistry;

  private CriteoBannerView bannerView;

  private BannerAdUnit bannerAdUnit;

  @Mock
  private Bid bid;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    bannerAdUnit = new BannerAdUnit("mock", new AdSize(320, 50));

    bannerView = spy(new CriteoBannerView(context, bannerAdUnit, criteo));
    doReturn(controller).when(criteo).createBannerController(bannerView);
  }

  @Test
  public void loadAdStandalone_GivenController_DelegateToIt() throws Exception {
    bannerView.loadAd();

    verify(controller).fetchAdAsync(bannerAdUnit);
    verifyNoMoreInteractions(controller);
    verify(integrationRegistry).declare(Integration.STANDALONE);
  }

  @Test
  public void loadAdStandalone_GivenControllerAndLoadTwice_DelegateToItTwice() throws Exception {
    bannerView.loadAd();
    bannerView.loadAd();

    verify(controller, times(2)).fetchAdAsync(bannerAdUnit);
    verifyNoMoreInteractions(controller);
    verify(integrationRegistry, times(2)).declare(Integration.STANDALONE);
  }

  @Test
  public void loadAdStandalone_GivenNullAdUnitController_DelegateToIt() throws Exception {
    bannerView = spy(new CriteoBannerView(context, null, criteo));
    doReturn(controller).when(criteo).createBannerController(bannerView);

    bannerView.loadAd();

    verify(controller).fetchAdAsync((AdUnit) null);
    verifyNoMoreInteractions(controller);
    verify(integrationRegistry).declare(Integration.STANDALONE);
  }

  @Test
  public void loadAdStandalone_GivenControllerThrowing_DoNotThrow() throws Exception {
    doThrow(RuntimeException.class).when(controller).fetchAdAsync(any(AdUnit.class));

    assertThatCode(bannerView::loadAd).doesNotThrowAnyException();
  }

  @Test
  public void loadAdInStandalone_GivenNonInitializedSdk_DoesNotThrow() throws Exception {
    bannerView = givenBannerUsingNonInitializedSdk();

    assertThatCode(bannerView::loadAd).doesNotThrowAnyException();
  }

  @Test
  public void loadAdInHouse_GivenController_DelegateToIt() throws Exception {
    bannerView.loadAd(bid);

    verify(controller).fetchAdAsync(bid);
    verifyNoMoreInteractions(controller);
    verify(integrationRegistry, never()).declare(any());
  }

  @Test
  public void loadAdInHouse_GivenControllerAndLoadTwice_DelegateToItTwice() throws Exception {
    bannerView.loadAd(bid);
    bannerView.loadAd(bid);

    verify(controller, times(2)).fetchAdAsync(bid);
    verifyNoMoreInteractions(controller);
    verify(integrationRegistry, never()).declare(any());
  }

  @Test
  public void loadAdInHouse_GivenControllerThrowing_DoNotThrow() throws Exception {
    doThrow(RuntimeException.class).when(controller).fetchAdAsync(any(Bid.class));

    assertThatCode(() -> bannerView.loadAd(bid)).doesNotThrowAnyException();
  }

  @Test
  public void loadAdInHouse_GivenNonInitializedSdk_DoesNotThrow() throws Exception {
    bannerView = givenBannerUsingNonInitializedSdk();

    assertThatCode(() -> bannerView.loadAd(bid)).doesNotThrowAnyException();
  }

  @Test
  public void getOrCreateController_CalledTwice_ReturnTheSameInstance() throws Exception {
    CriteoBannerEventController controller1 = bannerView.getOrCreateController();
    CriteoBannerEventController controller2 = bannerView.getOrCreateController();

    assertThat(controller1).isSameAs(controller2);
  }

  @Test
  public void new_GivenNonInitializedSdk_DoesNotThrowException() throws Exception {
    givenBannerUsingNonInitializedSdk();

    assertThatCode(() -> new CriteoBannerView(context, bannerAdUnit)).doesNotThrowAnyException();
  }

  @Test
  public void displayAd_GivenController_DelegateToIt() throws Exception {
    bannerView.loadAdWithDisplayData("fake_display_data");
    verify(controller).notifyFor(CriteoListenerCode.VALID);
    verify(controller).displayAd("fake_display_data");
  }

  private CriteoBannerView givenBannerUsingNonInitializedSdk() {
    Criteo.setInstance(null);
    return new CriteoBannerView(context, bannerAdUnit);
  }

}