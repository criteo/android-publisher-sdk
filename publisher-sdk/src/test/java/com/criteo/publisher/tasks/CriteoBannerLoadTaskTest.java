package com.criteo.publisher.tasks;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import android.support.annotation.NonNull;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.criteo.publisher.model.Config;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class CriteoBannerLoadTaskTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private WebView webView;

    private Reference<WebView> webViewRef;

    @Mock
    private Config config;

    @Mock
    private WebViewClient webViewClient;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        webViewRef = new WeakReference<>(webView);
    }

    @Test
    public void execute_GivenDisplayUrlAndConfig_InjectItInsideConfigMacrosAndLoadItOnWebView() throws InterruptedException {
        String displayUrl = "https://www.criteo.com";

        when(config.getDisplayUrlMacro()).thenReturn("%macro%");
        when(config.getAdTagUrlMode()).thenReturn("myDisplayUrl: %macro%");

        CriteoBannerLoadTask criteoBannerLoadTask = createTask(displayUrl);
        criteoBannerLoadTask.run();

        verify(webView.getSettings()).setJavaScriptEnabled(true);
        verify(webView).setWebViewClient(webViewClient);
        verify(webView).loadDataWithBaseURL(
            "",
            "myDisplayUrl: https://www.criteo.com",
            "text/html",
            "UTF-8",
            "");
    }

    @Test
    public void execute_GivenExpiredReference_DoesNothing() throws Exception {
        webViewRef = new WeakReference<>(null);

        CriteoBannerLoadTask criteoBannerLoadTask = createTask("anything");
        criteoBannerLoadTask.run();

        verifyZeroInteractions(config);
    }

    @NonNull
    private CriteoBannerLoadTask createTask(String displayUrl) {
        return new CriteoBannerLoadTask(webViewRef, webViewClient, config, displayUrl);
    }

}