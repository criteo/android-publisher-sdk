package com.criteo.publisher.integration;

import static com.criteo.publisher.CriteoUtil.givenInitializedCriteo;
import static com.criteo.publisher.ThreadingUtil.runOnMainThreadAndWait;
import static com.criteo.publisher.ThreadingUtil.waitForAllThreads;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import com.criteo.publisher.CriteoErrorCode;
import com.criteo.publisher.CriteoInterstitial;
import com.criteo.publisher.CriteoInterstitialAdListener;
import com.criteo.publisher.DependencyProvider;
import com.criteo.publisher.TestAdUnits;
import com.criteo.publisher.Util.MockedDependenciesRule;
import com.criteo.publisher.model.Cdb;
import com.criteo.publisher.model.InterstitialAdUnit;
import com.criteo.publisher.network.PubSdkApi;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class StandaloneTest {

  @Rule
  public MockedDependenciesRule mockedDependenciesRule  = new MockedDependenciesRule();

  private final InterstitialAdUnit interstitialAdUnit = TestAdUnits.INTERSTITIAL;

  private PubSdkApi api;

  private Context context;

  @Captor
  private ArgumentCaptor<Cdb> requestCaptor;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    api = spy(PubSdkApi.getInstance());

    DependencyProvider dependencyProvider = mockedDependenciesRule.getDependencyProvider();
    when(dependencyProvider.providePubSdkApi()).thenReturn(api);

    context = InstrumentationRegistry.getContext();
  }

  @Test
  public void whenLoadingAnInterstitial_GivenInitializedSdk_ShouldSetInterstitialFlagInTheRequest()
      throws Exception {
    givenInitializedSdk();
    Mockito.clearInvocations(api);

    runOnMainThreadAndWait(() -> {
      CriteoInterstitial interstitial = new CriteoInterstitial(context, interstitialAdUnit);
      interstitial.loadAd();
    });
    waitForBids();

    verify(api).loadCdb(any(), requestCaptor.capture(), anyString());
    Cdb request = requestCaptor.getValue();

    boolean interstitialFlag = request.toJson()
        .getJSONArray("slots")
        .getJSONObject(0)
        .getBoolean("interstitial");

    assertTrue(interstitialFlag);
  }

  @Test
  public void whenLoadingAnInterstitial_GivenDeviceInPortrait_NotifyListenerForSuccessOnNextCall() throws Exception {
    // FIXME This test assume that the screen orientation is already set to PORTRAIT

    givenInitializedSdk();

    AtomicReference<CriteoInterstitial> interstitial = new AtomicReference<>();
    CriteoInterstitialAdListener listener = mock(CriteoInterstitialAdListener.class);

    runOnMainThreadAndWait(() -> {
      interstitial.set(new CriteoInterstitial(context, interstitialAdUnit));
      interstitial.get().setCriteoInterstitialAdListener(listener);
    });

    // Given a first bid (that should do a cache miss)
    runOnMainThreadAndWait(interstitial.get()::loadAd);
    waitForBids();

    // Given a second bid (that should success)
    runOnMainThreadAndWait(interstitial.get()::loadAd);
    waitForBids();

    InOrder inOrder = inOrder(listener);
    inOrder.verify(listener).onAdFailedToReceive(CriteoErrorCode.ERROR_CODE_NO_FILL);
    inOrder.verify(listener).onAdReceived();
    inOrder.verifyNoMoreInteractions();
  }

  private void givenInitializedSdk() throws Exception {
    givenInitializedCriteo();
    waitForBids();
  }

  private void waitForBids() {
    waitForAllThreads(mockedDependenciesRule.getTrackingCommandsExecutor());
  }

}
