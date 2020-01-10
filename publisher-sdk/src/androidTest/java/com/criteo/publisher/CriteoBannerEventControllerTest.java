package com.criteo.publisher;

import static com.criteo.publisher.ThreadingUtil.runOnMainThreadAndWait;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import android.app.Application;
import android.support.test.InstrumentationRegistry;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.criteo.publisher.model.AdUnit;
import com.criteo.publisher.model.Config;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class CriteoBannerEventControllerTest {

    private CriteoBannerEventController criteoBannerEventController;

    @Mock
    private CriteoBannerView criteoBannerView;

    @Mock
    private CriteoBannerAdListener criteoBannerAdListener;

    @Mock
    private Config config;

    @Before
    public void Setup() {
        MockitoAnnotations.initMocks(this);
        criteoBannerEventController = new CriteoBannerEventController(criteoBannerView, criteoBannerAdListener, config);
    }

    @Test
    public void testWithNullAdUnit() throws Exception {
        AdUnit adUnit = null;
        criteoBannerEventController.fetchAdAsync(adUnit);

        Thread.sleep(100);

        verify(criteoBannerAdListener, times(0)).onAdReceived(criteoBannerView);
        verify(criteoBannerAdListener, times(1)).onAdFailedToReceive(CriteoErrorCode.ERROR_CODE_NO_FILL);
    }

    @Test
    public void whenDeeplinkIsLoaded_GivenTargetAppIsNotInstalled_DontThrowActivityNotFound() {
        runOnMainThreadAndWait(() -> {
            WebViewClient webViewClient  = criteoBannerEventController.createWebViewClient();
            Application app = (Application) InstrumentationRegistry.getTargetContext().getApplicationContext();
            webViewClient.shouldOverrideUrlLoading(new WebView(app.getApplicationContext()), "fake_deeplink://fakeappdispatch");
        });
    }
}
