package com.criteo.publisher.tasks;

import static org.mockito.Mockito.mock;

import android.test.UiThreadTest;

import com.criteo.publisher.CriteoInterstitialAdListener;
import com.criteo.publisher.CriteoErrorCode;
import com.criteo.publisher.model.WebViewData;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class WebViewDataTaskTest {

    private static final String TEST_CREATIVE = "https://rdi.us.criteo.com/delivery/r/ajs.php?did=5c87fcdb7cc0d71b24ee2ee6454eb810&u=%7CvsLBMQ0Ek4IxXQb0B5n7RyCAQymjqwh29YhNM9EzK9Q%3D%7C&c1=fYGSyyN4O4mkT2ynhzfwbdpiG7v0SMGpms6Tk24GWc957HzbzgL1jw-HVL5D0BjRx5ef3wBVfDXXmh9StLy8pf5kDJtrQLTLQrexjq5CZt9tEDx9mY8Y-eTV19PWOQoNjXkJ4_mhKqV0IfwHDIfLVDBWmsizVCoAtU1brQ2weeEkUU5-mDfn3qzTX3jPXszef5bC3pbiLJAK3QamQlglD1dkWYOkUwLAXxMjr2MXeBQk2YK-_qYz0fMVJG0xWJ-jVmsqdOw9A9rkGIgToRoUewB0VAu5eSkjSBoGs4yEbsnJ5Ssq5fquJMNvm6T77b8fzQI-eXgwoEfKkdAuCbj3gNrPBgzGZAJPGO-TYvJgs22Bljy-hNCk1E0E030zLtKo-XvAVRvZ5PswtwoccPSl6u1wiV8fMCXHx9QW9-fdXaVxzZe9AZB6w7pHxKUwiRK9";
    private static final String INVALID_URL = "!!!!";
    @Mock
    private CriteoInterstitialAdListener criteoInterstitialAdListener;

    @Mock
    private  WebViewData webViewData;

    private WebViewDataTask webViewDataTask;
    ;

    @Before
    @UiThreadTest
    public void setup() {
        MockitoAnnotations.initMocks(this);
        webViewDataTask = new WebViewDataTask(webViewData,
                criteoInterstitialAdListener);
    }

    @Test
    public void testWithData() {
        webViewDataTask.onPostExecute("<html></html>");

        Mockito.verify(webViewData, Mockito.times(1)).downloadSucceeeded();
        Mockito.verify(webViewData, Mockito.times(0)).downloadFailed();
        Mockito.verify(criteoInterstitialAdListener, Mockito.times(1)).onAdLoaded();
        Mockito.verify(criteoInterstitialAdListener, Mockito.times(0)).onAdFailedToLoad(CriteoErrorCode.ERROR_CODE_NETWORK_ERROR);
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
        Mockito.verify(criteoInterstitialAdListener, Mockito.times(0)).onAdLoaded();
        Mockito.verify(criteoInterstitialAdListener, Mockito.times(1)).onAdFailedToLoad(CriteoErrorCode.ERROR_CODE_NETWORK_ERROR);
    }

}