package com.criteo.publisher.degraded;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.test.InstrumentationRegistry;
import com.criteo.publisher.Criteo;
import com.criteo.publisher.CriteoBannerAdListener;
import com.criteo.publisher.CriteoBannerView;
import com.criteo.publisher.CriteoErrorCode;
import com.criteo.publisher.model.AdSize;
import com.criteo.publisher.model.BannerAdUnit;
import com.criteo.publisher.network.PubSdkApi;
import com.criteo.publisher.network.PubSdkApiHelper;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class StandaloneDegradedTest {

  private final BannerAdUnit bannerAdUnit = new BannerAdUnit("banner", new AdSize(1, 2));

  private Context context;

  @Mock
  private PubSdkApi api;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    DegradedUtil.assumeIsDegraded();

    context = InstrumentationRegistry.getContext();

    Application app = (Application) InstrumentationRegistry.getTargetContext()
        .getApplicationContext();

    Criteo.init(app, "B-056946", Collections.emptyList());
  }

  @Test
  public void whenLoadingABanner_ShouldNotDoAnyCallToCdb() throws Exception {
    PubSdkApiHelper.withApi(api, () -> {
      runOnMainThreadAndWait(() -> {
        CriteoBannerView bannerView = new CriteoBannerView(context, bannerAdUnit);

        bannerView.loadAd();
      });
    });

    Thread.sleep(200);

    verifyZeroInteractions(api);
  }

  @Test
  public void whenLoadingTwiceABanner_ShouldCallBackListenerWithErrorNoFill() throws Exception {
    CriteoBannerAdListener listener = mock(CriteoBannerAdListener.class);
    AtomicReference<CriteoBannerView> bannerView = new AtomicReference<>();

    runOnMainThreadAndWait(() -> {
      bannerView.set(new CriteoBannerView(context, bannerAdUnit));
    });

    bannerView.get().setCriteoBannerAdListener(listener);

    runOnMainThreadAndWait(bannerView.get()::loadAd);
    Thread.sleep(200);

    // Load twice, because first one is a cache miss
    runOnMainThreadAndWait(bannerView.get()::loadAd);
    Thread.sleep(200);

    verify(listener, never()).onAdReceived(any());
    verify(listener, times(2)).onAdFailedToReceive(CriteoErrorCode.ERROR_CODE_NO_FILL);
  }

  private static void runOnMainThreadAndWait(Runnable runnable) {
    CountDownLatch latch = new CountDownLatch(1);

    new Handler(Looper.getMainLooper()).post(() -> {
      runnable.run();
      latch.countDown();
    });

    try {
      latch.await();
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

}
