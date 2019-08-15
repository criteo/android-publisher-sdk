package com.criteo.publisher;

import com.criteo.publisher.controller.WebViewDownloader;
import com.criteo.publisher.model.AdUnit;
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

    @Mock
    private CriteoInterstitialAdListener criteoInterstitialAdListener;

    @Mock
    private CriteoInterstitialAdDisplayListener adDisplayListener;

    @Mock
    private WebViewDownloader mockWebViewDownloader;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        webViewData = new WebViewData();
        webViewData.setContent("html content");
        WebViewDownloader webViewDownloader = new WebViewDownloader(webViewData);
        criteoInterstitialEventController = new CriteoInterstitialEventController(criteoInterstitialAdListener,adDisplayListener,
                webViewDownloader);
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

        Thread.sleep(100);

        Mockito.verify(criteoInterstitialAdListener, Mockito.times(0)).onAdReceived();
        Mockito.verify(criteoInterstitialAdListener, Mockito.times(1))
                .onAdFailedToReceive(CriteoErrorCode.ERROR_CODE_NO_FILL);
    }

}