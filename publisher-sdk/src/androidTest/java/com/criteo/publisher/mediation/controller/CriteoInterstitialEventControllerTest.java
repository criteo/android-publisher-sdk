package com.criteo.publisher.mediation.controller;

import com.criteo.publisher.CriteoInterstitialEventController;
import com.criteo.publisher.Util.WebViewLoadStatus;
import com.criteo.publisher.listener.CriteoInterstitialAdListener;
import com.criteo.publisher.model.InterstitialAdUnit;
import com.criteo.publisher.model.WebViewData;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class CriteoInterstitialEventControllerTest {

    private CriteoInterstitialEventController criteoInterstitialEventController;

    private InterstitialAdUnit interstitialAdUnit;

    @Mock
    private CriteoInterstitialAdListener criteoInterstitialAdListener;

    @Mock
    private WebViewDownloader mockWebViewDownloader;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        interstitialAdUnit = new InterstitialAdUnit("/140800857/Endeavour_320x50");
    }

    @Test
    public void testUnload() {
        WebViewData webViewData = new WebViewData("html content", true, WebViewLoadStatus.STATUS_LOADED);
        WebViewDownloader webViewDownloader = new WebViewDownloader(webViewData);
        criteoInterstitialEventController = new CriteoInterstitialEventController(criteoInterstitialAdListener,
                webViewDownloader);
        criteoInterstitialEventController.unLoad();
        Assert.assertEquals("", webViewData.getContent());
        Assert.assertEquals(false, webViewData.isLoaded());
        Assert.assertEquals(WebViewLoadStatus.STATUS_NONE, webViewData.getWebViewLoadStatus());
    }

}