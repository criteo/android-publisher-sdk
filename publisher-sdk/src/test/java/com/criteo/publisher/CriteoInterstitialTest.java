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

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import android.app.Application;
import com.criteo.publisher.integration.Integration;
import com.criteo.publisher.integration.IntegrationRegistry;
import com.criteo.publisher.mock.MockBean;
import com.criteo.publisher.mock.MockedDependenciesRule;
import com.criteo.publisher.model.InterstitialAdUnit;
import kotlin.jvm.JvmField;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class CriteoInterstitialTest {

  @Rule
  @JvmField
  public MockedDependenciesRule mockedDependenciesRule = new MockedDependenciesRule();

  @Mock
  private Criteo criteo;

  @MockBean
  private IntegrationRegistry integrationRegistry;

  private InterstitialAdUnit adUnit = new InterstitialAdUnit("mock");

  private CriteoInterstitial interstitial;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    interstitial = spy(new CriteoInterstitial(adUnit, criteo));
  }

  @Test
  public void loadAdStandalone_GivenControllerAndLoadTwice_DelegateToItTwice() throws Exception {
    CriteoInterstitialEventController controller = givenMockedController();

    interstitial.loadAd();
    interstitial.loadAd();

    verify(controller, times(2)).fetchAdAsync(adUnit);
    verify(integrationRegistry, times(2)).declare(Integration.STANDALONE);
  }

  @Test
  public void loadAdInHouse_GivenControllerAndLoadTwice_DelegateToItTwice() throws Exception {
    Bid bid = mock(Bid.class);
    CriteoInterstitialEventController controller = givenMockedController();

    interstitial.loadAd(bid);
    interstitial.loadAd(bid);

    verify(controller, times(2)).fetchAdAsync(bid);
    verify(integrationRegistry, times(2)).declare(Integration.IN_HOUSE);
  }

  @Test
  public void show_GivenController_DelegateToIt() throws Exception {
    CriteoInterstitialEventController controller = givenMockedController();

    interstitial.show();

    verify(controller).show();
  }

  @Test
  public void show_GivenThrowingController_DoesNotThrow() throws Exception {
    CriteoInterstitialEventController controller = givenMockedController();
    doThrow(RuntimeException.class).when(controller).show();

    assertThatCode(interstitial::show).doesNotThrowAnyException();
  }

  @Test
  public void displayAd_GivenController_DelegateToIt() throws Exception {
    CriteoInterstitialEventController controller = givenMockedController();
    interstitial.loadAdWithDisplayData("fake_display_data");
    verify(controller).fetchCreativeAsync("fake_display_data");
  }

  @Test
  public void loadAd_GivenNullApplication_ReturnImmediately() throws Exception {
    CriteoInterstitialEventController controller = givenMockedController();
    Application application = givenNullApplication();

    interstitial.loadAd();

    verifyNoMoreInteractions(controller);
    DependencyProvider.getInstance().setApplication(application);
  }

  @Test
  public void loadAdWithBidToken_GivenNullApplication_ReturnImmediately() throws Exception {
    CriteoInterstitialEventController controller = givenMockedController();
    Application application = givenNullApplication();
    Bid bid = mock(Bid.class);

    interstitial.loadAd(bid);

    verifyNoMoreInteractions(controller);
    DependencyProvider.getInstance().setApplication(application);
  }

  @Test
  public void loadAdWithDisplayData_GivenNullApplication_ReturnImmediately() throws Exception {
    CriteoInterstitialEventController controller = givenMockedController();
    Application application = givenNullApplication();

    interstitial.loadAdWithDisplayData("");

    verifyNoMoreInteractions(controller);
    DependencyProvider.getInstance().setApplication(application);
  }


  @Test
  public void show_GivenNullApplication_ReturnImmediately() throws Exception {
    CriteoInterstitialEventController controller = givenMockedController();
    Application application = givenNullApplication();

    interstitial.show();

    verifyNoMoreInteractions(controller);
    DependencyProvider.getInstance().setApplication(application);
  }

  private Application givenNullApplication() {
    Application application = DependencyProvider.getInstance().provideApplication();
    try {
      DependencyProvider.getInstance().setApplication(null);
    } catch (Exception e) {
    }
    return application;
  }

  private CriteoInterstitialEventController givenMockedController() {
    CriteoInterstitialEventController controller = mock(CriteoInterstitialEventController.class);
    doReturn(controller).when(interstitial).getOrCreateController();
    return controller;
  }

}