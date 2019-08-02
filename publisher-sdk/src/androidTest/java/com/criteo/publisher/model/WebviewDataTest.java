package com.criteo.publisher.model;

import android.text.TextUtils;
import com.criteo.publisher.CriteoInterstitialAdListener;
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
        webviewData = new WebViewData();
    }

    @Test
    public void testSetContentWithData() {

        data = "html";
        webviewData.setContent(data);

        Assert.assertTrue(!TextUtils.isEmpty(webviewData.getContent()));
        Assert.assertFalse(webviewData.getContent().contains(Config.getAdTagDataMode()));

    }
}