package com.criteo.publisher.tasks;

import android.test.UiThreadTest;
import com.criteo.publisher.CriteoErrorCode;
import com.criteo.publisher.CriteoInterstitialAdDisplayListener;
import com.criteo.publisher.model.DeviceInfo;
import com.criteo.publisher.model.WebViewData;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class WebViewDataTaskIntegrationTest {

    private static final String INVALID_URL = "!!!!";

    @Mock
    private WebViewData webViewData;

    @Mock
    private DeviceInfo deviceInfo;

    @Mock
    private CriteoInterstitialAdDisplayListener adDisplayListener;

    private WebViewDataTask webViewDataTask;
    ;

    @Before
    @UiThreadTest
    public void setup() {
        MockitoAnnotations.initMocks(this);
        webViewDataTask = new WebViewDataTask(webViewData, deviceInfo, adDisplayListener);
    }

    @Test
    public void testWithData() {
        webViewDataTask.onPostExecute("<html></html>");

        Mockito.verify(webViewData, Mockito.times(1)).downloadSucceeeded();
        Mockito.verify(webViewData, Mockito.times(0)).downloadFailed();
        Mockito.verify(adDisplayListener, Mockito.times(1)).onAdReadyToDisplay();
        Mockito.verify(adDisplayListener, Mockito.times(0))
                .onAdFailedToDisplay(CriteoErrorCode.ERROR_CODE_NETWORK_ERROR);
    }

    @Test
    public void testWithInvalidUrl() {
        webViewDataTask.execute(INVALID_URL);

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Assert.assertFalse(webViewData.isLoaded());
        Mockito.verify(webViewData, Mockito.times(0)).downloadSucceeeded();
        Mockito.verify(webViewData, Mockito.times(1)).downloadFailed();
        Mockito.verify(adDisplayListener, Mockito.times(0)).onAdReadyToDisplay();
        Mockito.verify(adDisplayListener, Mockito.times(1))
                .onAdFailedToDisplay(CriteoErrorCode.ERROR_CODE_NETWORK_ERROR);
    }

}