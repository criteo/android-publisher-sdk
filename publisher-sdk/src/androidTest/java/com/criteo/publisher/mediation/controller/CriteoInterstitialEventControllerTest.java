package com.criteo.publisher.mediation.controller;

import com.criteo.publisher.CriteoInterstitialEventController;
import com.criteo.publisher.Util.CriteoErrorCode;
import com.criteo.publisher.listener.CriteoInterstitialAdListener;
import com.criteo.publisher.model.AdUnit;
import com.criteo.publisher.model.Config;
import com.criteo.publisher.model.InterstitialAdUnit;
import com.criteo.publisher.model.WebViewData;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class CriteoInterstitialEventControllerTest {

    private CriteoInterstitialEventController criteoInterstitialEventController;

    private InterstitialAdUnit interstitialAdUnit;

    private WebViewData webViewData;

    @Mock
    private CriteoInterstitialAdListener criteoInterstitialAdListener;

    @Mock
    private WebViewDownloader mockWebViewDownloader;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        interstitialAdUnit = new InterstitialAdUnit("/140800857/Endeavour_320x50");
        JSONObject configJson = new JSONObject();
        Config config = new Config(configJson);
        webViewData = new WebViewData();
        webViewData.setContent("html content", criteoInterstitialAdListener);
        WebViewDownloader webViewDownloader = new WebViewDownloader(webViewData);
        criteoInterstitialEventController = new CriteoInterstitialEventController(criteoInterstitialAdListener,
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

        Mockito.verify(criteoInterstitialAdListener, Mockito.times(0)).onAdLoaded();
        Mockito.verify(criteoInterstitialAdListener, Mockito.times(1))
                .onAdFailedToLoad(CriteoErrorCode.ERROR_CODE_NO_FILL);
    }

}