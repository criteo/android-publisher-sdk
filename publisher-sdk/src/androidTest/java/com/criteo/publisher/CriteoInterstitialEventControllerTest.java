package com.criteo.publisher;

import android.app.Application;
import android.support.test.InstrumentationRegistry;
import com.criteo.publisher.controller.WebViewDownloader;
import com.criteo.publisher.model.AdUnit;
import com.criteo.publisher.model.Config;
import com.criteo.publisher.model.WebViewData;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class CriteoInterstitialEventControllerTest {

    private CriteoInterstitialEventController criteoInterstitialEventController;

    private WebViewData webViewData;

    private Config config;

    @Mock
    private CriteoInterstitialAdListener criteoInterstitialAdListener;

    @Mock
    private CriteoInterstitialAdDisplayListener adDisplayListener;

    @Mock
    private WebViewDownloader mockWebViewDownloader;

    @Before
    public void setup() throws CriteoInitException {
        MockitoAnnotations.initMocks(this);
        config = new Config(InstrumentationRegistry.getContext());
        webViewData = new WebViewData(config);
        webViewData.setContent("html content");
        Application app = (Application) InstrumentationRegistry.getTargetContext().getApplicationContext();
        Criteo.init(app, "B-056946", null);
        WebViewDownloader webViewDownloader = new WebViewDownloader(webViewData);

        criteoInterstitialEventController = new CriteoInterstitialEventController(
            criteoInterstitialAdListener,
            adDisplayListener,
            webViewDownloader,
            Criteo.getInstance()
        );
    }

    @Test
    public void testUnload() {

        criteoInterstitialEventController.refresh();
        Assert.assertEquals("", webViewData.getContent());
        Assert.assertEquals(false, webViewData.isLoaded());
    }

    @Test
    public void testWithNullAdUnit() throws Exception {
        AdUnit adUnit = null;
        criteoInterstitialEventController.fetchAdAsync(adUnit);

        Thread.sleep(500);

        Mockito.verify(criteoInterstitialAdListener, Mockito.times(0)).onAdReceived();
        Mockito.verify(criteoInterstitialAdListener, Mockito.times(1))
                .onAdFailedToReceive(CriteoErrorCode.ERROR_CODE_NO_FILL);
        Mockito.verify(adDisplayListener, Mockito.times(0))
                .onAdFailedToDisplay((CriteoErrorCode.ERROR_CODE_NO_FILL));
        Mockito.verify(adDisplayListener, Mockito.times(0))
                .onAdReadyToDisplay();
    }

}