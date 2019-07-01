package com.criteo.publisher.mediation.controller;

import android.content.Context;
import com.criteo.publisher.CriteoInterstitialEventController;
import com.criteo.publisher.Util.WebViewLoadStatus;
import com.criteo.publisher.listener.CriteoInterstitialAdListener;
import com.criteo.publisher.model.Config;
import com.criteo.publisher.model.InterstitialAdUnit;
import com.criteo.publisher.model.WebViewData;
import org.json.JSONObject;
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
       //TODO change code later, Config creation
        JSONObject configJson = new JSONObject();
        Config config = new Config(configJson);
        WebViewData webViewData = new WebViewData();
        webViewData.setContent("html content", criteoInterstitialAdListener);
        WebViewDownloader webViewDownloader = new WebViewDownloader(webViewData);
        criteoInterstitialEventController = new CriteoInterstitialEventController(criteoInterstitialAdListener,
                webViewDownloader);
        criteoInterstitialEventController.refresh();
        Assert.assertEquals("", webViewData.getContent());
        Assert.assertEquals(false, webViewData.isLoaded());
    }

}