package com.criteo.publisher.model;

import static com.criteo.publisher.model.Config.WEBVIEW_DATA_MACRO;

import android.text.TextUtils;
import com.criteo.publisher.Util.WebViewLoadStatus;
import com.criteo.publisher.listener.CriteoInterstitialAdListener;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class WebviewDataTest {

    private String data;

    @Mock
    private CriteoInterstitialAdListener criteoInterstitialAdListener;

    private WebViewData webviewData;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        webviewData = new WebViewData(WebViewLoadStatus.STATUS_NONE);
    }

    @Test
    public void testSetContentWithData() {
        data = "html";
        webviewData.setContent(data, criteoInterstitialAdListener);

        Assert.assertTrue(!TextUtils.isEmpty(webviewData.getContent()));
        Assert.assertFalse(webviewData.getContent().contains(WEBVIEW_DATA_MACRO));
        Assert.assertTrue(webviewData.isLoaded());

    }
}