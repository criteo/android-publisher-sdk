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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import android.content.Context;
import android.webkit.WebViewClient;
import com.criteo.publisher.adview.CriteoMraidController;
import com.criteo.publisher.adview.MraidController;
import com.criteo.publisher.adview.MraidPlacementType;
import com.criteo.publisher.adview.MraidState;
import com.criteo.publisher.context.ContextData;
import com.criteo.publisher.integration.Integration;
import com.criteo.publisher.integration.IntegrationRegistry;
import com.criteo.publisher.logging.Logger;
import com.criteo.publisher.mock.MockBean;
import com.criteo.publisher.mock.MockedDependenciesRule;
import com.criteo.publisher.mock.SpyBean;
import com.criteo.publisher.model.AdSize;
import com.criteo.publisher.model.AdUnit;
import com.criteo.publisher.model.BannerAdUnit;
import java.util.Arrays;
import java.util.List;
import kotlin.jvm.JvmField;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

public class CriteoBannerAdWebViewTest {

  @Rule
  @JvmField
  public MockedDependenciesRule mockedDependenciesRule = new MockedDependenciesRule().withSpiedLogger();

  @Rule
  public MockitoRule mockitoRule = MockitoJUnit.rule();

  @Mock
  private Context context;

  @Mock
  private CriteoBannerEventController controller;

  @Mock
  private Criteo criteo;

  @MockBean
  private IntegrationRegistry integrationRegistry;

  private CriteoBannerAdWebView bannerView;

  @Mock
  private CriteoBannerView parentContainer;

  private BannerAdUnit bannerAdUnit;

  @Mock
  private ContextData contextData;

  @Mock
  private Bid bid;

  @Mock
  private CriteoMraidController criteoMraidController;

  @SpyBean
  private Logger logger;

  @Before
  public void setUp() throws Exception {
    doReturn(criteoMraidController).when(DependencyProvider.getInstance()).provideMraidController(
        eq(MraidPlacementType.INLINE),
        any()
    );
    bannerAdUnit = new BannerAdUnit("mock", new AdSize(320, 50));

    CriteoBannerAdWebView view = new CriteoBannerAdWebView(
        context,
        null,
        bannerAdUnit,
        criteo,
        parentContainer
    );
    view.setWebViewClient(mock(WebViewClient.class));
    bannerView = spy(view);
    doReturn(controller).when(criteo).createBannerController(view);
  }

  @Test
  public void loadAdStandalone_GivenNoContext_UseEmptyContext() throws Exception {
    bannerView.loadAd();

    verify(bannerView).loadAd(eq(new ContextData()));
  }

  @Test
  public void loadAdStandalone_GivenController_DelegateToIt() throws Exception {
    bannerView.loadAd(contextData);

    verify(controller).fetchAdAsync(bannerAdUnit, contextData);
    verifyNoMoreInteractions(controller);
    verify(integrationRegistry).declare(Integration.STANDALONE);
  }

  @Test
  public void loadAdStandalone_GivenAdIsNotExpanded_DelegateToController() {
    List<MraidState> notExpandedStates = getNotExpandedStates();

    for (MraidState state : notExpandedStates) {
      clearInvocations(controller, integrationRegistry);
      doReturn(state).when(criteoMraidController).getCurrentState();

      bannerView.loadAd(contextData);

      verify(controller).fetchAdAsync(bannerAdUnit, contextData);
      verifyNoMoreInteractions(controller);
      verify(integrationRegistry).declare(Integration.STANDALONE);
    }
  }

  @Test
  public void loadAdStandalone_GivenAdIsExpanded_DoNotLoadAd() {
    doReturn(MraidState.EXPANDED).when(criteoMraidController).getCurrentState();

    bannerView.loadAd();

    verifyNoMoreInteractions(controller);
    verifyNoMoreInteractions(integrationRegistry);
    verify(logger).log(BannerLogMessage.onBannerViewFailedToReloadDuringExpandedState());
  }

  @Test
  public void loadAdStandalone_GivenControllerAndLoadTwice_DelegateToItTwice() throws Exception {
    bannerView.loadAd(contextData);
    bannerView.loadAd(contextData);

    verify(controller, times(2)).fetchAdAsync(bannerAdUnit, contextData);
    verifyNoMoreInteractions(controller);
    verify(integrationRegistry, times(2)).declare(Integration.STANDALONE);
  }

  @Test
  public void loadAdStandalone_GivenNullAdUnitController_DelegateToIt() throws Exception {
    CriteoBannerAdWebView view = new CriteoBannerAdWebView(
        context,
        null,
        null,
        criteo,
        parentContainer
    );
    bannerView = spy(view);
    doReturn(controller).when(criteo).createBannerController(view);

    bannerView.loadAd(contextData);

    verify(controller).fetchAdAsync(null, contextData);
    verifyNoMoreInteractions(controller);
    verify(integrationRegistry).declare(Integration.STANDALONE);
  }

  @Test
  public void loadAdStandalone_GivenControllerThrowing_DoNotThrow() throws Exception {
    doThrow(RuntimeException.class).when(controller).fetchAdAsync(
        any(AdUnit.class),
        any(ContextData.class)
    );

    assertThatCode(() -> bannerView.loadAd(mock(ContextData.class))).doesNotThrowAnyException();
  }

  @Test
  public void loadAdInStandalone_GivenNonInitializedSdk_DoesNotThrow() throws Exception {
    bannerView = givenBannerUsingNonInitializedSdk();

    assertThatCode(() -> bannerView.loadAd(mock(ContextData.class))).doesNotThrowAnyException();
  }

  @Test
  public void loadAdInHouse_GivenController_DelegateToIt() throws Exception {
    bannerView.loadAd(bid);

    verify(controller).fetchAdAsync(bid);
    verifyNoMoreInteractions(controller);
    verify(integrationRegistry).declare(Integration.IN_HOUSE);
  }

  @Test
  public void loadAdInHouse_GivenControllerAndLoadTwice_DelegateToItTwice() throws Exception {
    bannerView.loadAd(bid);
    bannerView.loadAd(bid);

    verify(controller, times(2)).fetchAdAsync(bid);
    verifyNoMoreInteractions(controller);
    verify(integrationRegistry, times(2)).declare(Integration.IN_HOUSE);
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
  public void loadAdInHouse_GivenAdIsNotExpanded_DelegateToController() {
    List<MraidState> notExpandedStates = getNotExpandedStates();

    for (MraidState state : notExpandedStates) {
      clearInvocations(controller, integrationRegistry);
      doReturn(state).when(criteoMraidController).getCurrentState();

      bannerView.loadAd(bid);

      verify(controller).fetchAdAsync(bid);
      verifyNoMoreInteractions(controller);
      verify(integrationRegistry).declare(Integration.IN_HOUSE);
    }
  }

  @Test
  public void loadAdInHouse_GivenAdIsExpanded_DoNotLoadAd() {
    doReturn(MraidState.EXPANDED).when(criteoMraidController).getCurrentState();

    bannerView.loadAd(bid);

    verifyNoMoreInteractions(controller);
    verifyNoMoreInteractions(integrationRegistry);
    verify(logger).log(BannerLogMessage.onBannerViewFailedToReloadDuringExpandedState());
  }

  @Test
  public void new_GivenNonInitializedSdk_DoesNotThrowException() throws Exception {
    givenBannerUsingNonInitializedSdk();

    assertThatCode(() -> new CriteoBannerAdWebView(
        context,
        null,
        bannerAdUnit,
        null,
        parentContainer
    )).doesNotThrowAnyException();
  }

  @Test
  public void displayAd_GivenController_DelegateToIt() throws Exception {
    bannerView.loadAdWithDisplayData("fake_display_data");
    verify(controller).notifyFor(CriteoListenerCode.VALID);
    verify(controller).displayAd("fake_display_data");
  }

  @Test
  public void provideMraidController_ShouldDelegateToDependencyProviderWithInlineType() {
    doReturn(mock(MraidController.class)).when(DependencyProvider.getInstance())
        .provideMraidController(MraidPlacementType.INLINE, bannerView);

    bannerView.provideMraidController();

    verify(DependencyProvider.getInstance()).provideMraidController(
        MraidPlacementType.INLINE,
        bannerView
    );
  }

  private CriteoBannerAdWebView givenBannerUsingNonInitializedSdk() {
    Criteo.setInstance(null);
    return new CriteoBannerAdWebView(context, null, bannerAdUnit, null, parentContainer);
  }

  private List<MraidState> getNotExpandedStates() {
    return Arrays.asList(MraidState.LOADING, MraidState.DEFAULT, MraidState.HIDDEN);
  }

}
