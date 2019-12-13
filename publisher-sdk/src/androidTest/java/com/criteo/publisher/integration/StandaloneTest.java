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
import android.content.res.Configuration;
import android.support.test.InstrumentationRegistry;
import com.criteo.publisher.CriteoErrorCode;
import com.criteo.publisher.CriteoInterstitial;
import com.criteo.publisher.CriteoInterstitialAdListener;
import com.criteo.publisher.DependencyProvider;
import com.criteo.publisher.TestAdUnits;
import com.criteo.publisher.Util.AndroidUtil;
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
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class StandaloneTest {

  @Rule
  public MockedDependenciesRule mockedDependenciesRule  = new MockedDependenciesRule();

  private final InterstitialAdUnit interstitialAdUnit = TestAdUnits.INTERSTITIAL;

  private PubSdkApi api;

  @Mock
  private AndroidUtil androidUtil;

  private Context context;

  @Captor
  private ArgumentCaptor<Cdb> requestCaptor;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    context = InstrumentationRegistry.getContext();

    DependencyProvider dependencyProvider = mockedDependenciesRule.getDependencyProvider();

    api = spy(dependencyProvider.providePubSdkApi(context));
    when(dependencyProvider.providePubSdkApi(any())).thenReturn(api);
    when(dependencyProvider.provideAndroidUtil(any())).thenReturn(androidUtil);
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

    verify(api).loadCdb(requestCaptor.capture(), anyString());
    Cdb request = requestCaptor.getValue();

    boolean interstitialFlag = request.toJson()
        .getJSONArray("slots")
        .getJSONObject(0)
        .getBoolean("interstitial");

    assertTrue(interstitialFlag);
  }

  @Test
  public void whenLoadingAnInterstitial_GivenDeviceInPortrait_NotifyListenerForSuccessOnNextCall() throws Exception {
    givenDeviceInPortrait();

    whenLoadingAnInterstitial_NotifyListenerForSuccessOnNextCall();
  }

  @Test
  public void whenLoadingAnInterstitial_GivenDeviceInLandscape_NotifyListenerForSuccessOnNextCall() throws Exception {
    givenDeviceInLandscape();

    whenLoadingAnInterstitial_NotifyListenerForSuccessOnNextCall();
  }

  private void whenLoadingAnInterstitial_NotifyListenerForSuccessOnNextCall() throws Exception {
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

  private void givenDeviceInPortrait() {
    when(androidUtil.getOrientation()).thenReturn(Configuration.ORIENTATION_PORTRAIT);
  }

  private void givenDeviceInLandscape() {
    when(androidUtil.getOrientation()).thenReturn(Configuration.ORIENTATION_LANDSCAPE);
  }

  private void givenInitializedSdk() throws Exception {
    givenInitializedCriteo();
    waitForBids();
  }

  private void waitForBids() {
    waitForAllThreads(mockedDependenciesRule.getTrackingCommandsExecutor());
  }

}
