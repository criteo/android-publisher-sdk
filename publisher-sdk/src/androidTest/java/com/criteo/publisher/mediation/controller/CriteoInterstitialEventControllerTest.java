package com.criteo.publisher.mediation.controller;

import android.support.test.InstrumentationRegistry;
import android.test.UiThreadTest;
import com.criteo.publisher.mediation.listeners.CriteoInterstitialAdListener;
import com.criteo.publisher.mediation.view.CriteoInterstitialView;
import com.criteo.publisher.model.WebviewData;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class CriteoInterstitialEventControllerTest {

    private static final String TEST_CREATIVE = "https://rdi.us.criteo.com/delivery/r/ajs.php?did=5c87fcdb7cc0d71b24ee2ee6454eb810&u=%7CvsLBMQ0Ek4IxXQb0B5n7RyCAQymjqwh29YhNM9EzK9Q%3D%7C&c1=fYGSyyN4O4mkT2ynhzfwbdpiG7v0SMGpms6Tk24GWc957HzbzgL1jw-HVL5D0BjRx5ef3wBVfDXXmh9StLy8pf5kDJtrQLTLQrexjq5CZt9tEDx9mY8Y-eTV19PWOQoNjXkJ4_mhKqV0IfwHDIfLVDBWmsizVCoAtU1brQ2weeEkUU5-mDfn3qzTX3jPXszef5bC3pbiLJAK3QamQlglD1dkWYOkUwLAXxMjr2MXeBQk2YK-_qYz0fMVJG0xWJ-jVmsqdOw9A9rkGIgToRoUewB0VAu5eSkjSBoGs4yEbsnJ5Ssq5fquJMNvm6T77b8fzQI-eXgwoEfKkdAuCbj3gNrPBgzGZAJPGO-TYvJgs22Bljy-hNCk1E0E030zLtKo-XvAVRvZ5PswtwoccPSl6u1wiV8fMCXHx9QW9-fdXaVxzZe9AZB6w7pHxKUwiRK9";
    private static final String INVALID_URL = "?@#$#$";

    @Mock
    private CriteoInterstitialAdListener criteoInterstitialAdListener;

    @Mock
    private CriteoInterstitialView criteoInterstitialView;

    private CriteoInterstitialEventController criteoInterstitialEventController;

    @Before
    @UiThreadTest
    public void setup() {
        MockitoAnnotations.initMocks(this);
        criteoInterstitialEventController = new CriteoInterstitialEventController(InstrumentationRegistry.getContext(),
                criteoInterstitialView,
                criteoInterstitialAdListener,
                new WebViewDownloader(new WebviewData("html", true)));
    }

    @Test
    public void TestWebViewData() {
        criteoInterstitialEventController.getWebviewDataAsync(TEST_CREATIVE);

        String data = criteoInterstitialEventController.getWebViewDataContent();
        Assert.assertFalse(data.equals(""));
        Assert.assertTrue(criteoInterstitialEventController.isAdLoaded());
    }

    @Test
    public void TestWebViewDataWithInvalidUrl() {
        criteoInterstitialEventController.getWebviewDataAsync(INVALID_URL);

        String data = criteoInterstitialEventController.getWebViewDataContent();
        Assert.assertFalse(data.equals(""));
        Assert.assertFalse(criteoInterstitialEventController.isAdLoaded());
    }

    @Test
    public void TestWebViewDataWithEmptyUrl() {
        criteoInterstitialEventController.getWebviewDataAsync("");

        String data = criteoInterstitialEventController.getWebViewDataContent();
        Assert.assertFalse(data.equals(""));
        Assert.assertFalse(criteoInterstitialEventController.isAdLoaded());
    }

    @Test
    public void TestWebViewDataWithNullUrl() {
        String displayUrl = null;
        criteoInterstitialEventController.getWebviewDataAsync(displayUrl);

        String data = criteoInterstitialEventController.getWebViewDataContent();
        Assert.assertFalse(data.equals(""));
        Assert.assertFalse(criteoInterstitialEventController.isAdLoaded());
    }
}