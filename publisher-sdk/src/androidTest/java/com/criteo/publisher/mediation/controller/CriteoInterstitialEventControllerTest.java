package com.criteo.publisher.mediation.controller;

import android.support.test.InstrumentationRegistry;
import android.test.UiThreadTest;
import com.criteo.publisher.mediation.listeners.CriteoInterstitialAdListener;
import com.criteo.publisher.mediation.utils.CriteoErrorCode;
import com.criteo.publisher.mediation.view.CriteoInterstitialView;
import com.criteo.publisher.model.WebViewData;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class CriteoInterstitialEventControllerTest {

    private static final String TEST_CREATIVE = "https://rdi.us.criteo.com/delivery/r/ajs.php?did=5c87fcdb7cc0d71b24ee2ee6454eb810&u=%7CvsLBMQ0Ek4IxXQb0B5n7RyCAQymjqwh29YhNM9EzK9Q%3D%7C&c1=fYGSyyN4O4mkT2ynhzfwbdpiG7v0SMGpms6Tk24GWc957HzbzgL1jw-HVL5D0BjRx5ef3wBVfDXXmh9StLy8pf5kDJtrQLTLQrexjq5CZt9tEDx9mY8Y-eTV19PWOQoNjXkJ4_mhKqV0IfwHDIfLVDBWmsizVCoAtU1brQ2weeEkUU5-mDfn3qzTX3jPXszef5bC3pbiLJAK3QamQlglD1dkWYOkUwLAXxMjr2MXeBQk2YK-_qYz0fMVJG0xWJ-jVmsqdOw9A9rkGIgToRoUewB0VAu5eSkjSBoGs4yEbsnJ5Ssq5fquJMNvm6T77b8fzQI-eXgwoEfKkdAuCbj3gNrPBgzGZAJPGO-TYvJgs22Bljy-hNCk1E0E030zLtKo-XvAVRvZ5PswtwoccPSl6u1wiV8fMCXHx9QW9-fdXaVxzZe9AZB6w7pHxKUwiRK9";
    private static final String INVALID_URL = "?@#$#$";

    @Mock
    private CriteoInterstitialAdListener criteoInterstitialAdListener;

    @Mock
    private CriteoInterstitialView criteoInterstitialView ;

    @Mock
    private WebViewData webViewData = new WebViewData("", false);

    @Mock
    private WebViewDownloader webViewDownloader = new WebViewDownloader(webViewData);

    private CriteoInterstitialEventController criteoInterstitialEventController;

    @Before
    @UiThreadTest
    public void setup() {
        MockitoAnnotations.initMocks(this);
        criteoInterstitialEventController = new CriteoInterstitialEventController(InstrumentationRegistry.getContext(),
                criteoInterstitialView,
                criteoInterstitialAdListener,
                webViewDownloader);
    }

    @Test
    public void TestWebViewData() {
        Mockito.doReturn(false).when(webViewData).isLoaded();
        criteoInterstitialEventController = new CriteoInterstitialEventController(InstrumentationRegistry.getContext(),
                criteoInterstitialView,
                criteoInterstitialAdListener,
                webViewDownloader);

        criteoInterstitialEventController.getWebviewDataAsync(TEST_CREATIVE, criteoInterstitialAdListener);

        String data = criteoInterstitialEventController.getWebViewDataContent();
        Assert.assertFalse(data.equals(""));
        Assert.assertTrue(criteoInterstitialEventController.isAdLoaded());
        Mockito.verify(criteoInterstitialAdListener, Mockito.times(1)).onAdFetchSucceededForInterstitial();
        Mockito.verify(criteoInterstitialAdListener, Mockito.times(0))
                .onAdFetchFailed(CriteoErrorCode.ERROR_CODE_NETWORK_ERROR);
    }

    @Test
    public void TestWebViewDataWithInvalidUrl() {
        criteoInterstitialEventController.getWebviewDataAsync(INVALID_URL, criteoInterstitialAdListener);

        String data = criteoInterstitialEventController.getWebViewDataContent();
        Assert.assertFalse(data.equals(""));
        Assert.assertFalse(criteoInterstitialEventController.isAdLoaded());
        Mockito.verify(criteoInterstitialAdListener, Mockito.times(0)).onAdFetchSucceededForInterstitial();
        Mockito.verify(criteoInterstitialAdListener, Mockito.times(1))
                .onAdFetchFailed(CriteoErrorCode.ERROR_CODE_INVALID_REQUEST);
    }

    @Test
    public void TestWebViewDataWithEmptyUrl() {
        criteoInterstitialEventController.getWebviewDataAsync("", criteoInterstitialAdListener);

        String data = criteoInterstitialEventController.getWebViewDataContent();
        Assert.assertFalse(data.equals(""));
        Assert.assertFalse(criteoInterstitialEventController.isAdLoaded());
        Mockito.verify(criteoInterstitialAdListener, Mockito.times(0)).onAdFetchSucceededForInterstitial();
        Mockito.verify(criteoInterstitialAdListener, Mockito.times(1))
                .onAdFetchFailed(CriteoErrorCode.ERROR_CODE_INVALID_REQUEST);
    }

    @Test
    public void TestWebViewDataWithNullUrl() {
        String displayUrl = null;
        criteoInterstitialEventController.getWebviewDataAsync(displayUrl, criteoInterstitialAdListener);

        String data = criteoInterstitialEventController.getWebViewDataContent();
        Assert.assertFalse(data.equals(""));
        Assert.assertFalse(criteoInterstitialEventController.isAdLoaded());
        Mockito.verify(criteoInterstitialAdListener, Mockito.times(0)).onAdFetchSucceededForInterstitial();
        Mockito.verify(criteoInterstitialAdListener, Mockito.times(1))
                .onAdFetchFailed(CriteoErrorCode.ERROR_CODE_INVALID_REQUEST);
    }
}