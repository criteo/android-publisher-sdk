package com.criteo.publisher.adview;

import static com.criteo.publisher.concurrent.ThreadingUtil.runOnMainThreadAndWait;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import android.content.Context;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.criteo.publisher.mock.MockedDependenciesRule;
import com.criteo.publisher.mock.SpyBean;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

public class AdWebViewClientTest {

  @Rule
  public MockitoRule mockitoRule = MockitoJUnit.rule();

  @Rule
  public MockedDependenciesRule mockedDependenciesRule = new MockedDependenciesRule();

  @SpyBean
  private Context context;

  @Mock
  private AdWebViewListener listener;

  @Test
  public void whenUrlIsLoaded_GivenBrowserAppIsInstalled_OpenActivityAndNotifyListener() {
    // We assume that there is a browser installed on the test device.

    whenUrlIsLoaded("https://criteo.com");

    verify(context).startActivity(any());
    verify(listener).onUserRedirectedToAd();
  }

  @Test
  public void whenDeeplinkIsLoaded_GivenTargetAppIsNotInstalled_DontThrowActivityNotFound() {
    whenUrlIsLoaded("fake_deeplink://fakeappdispatch");

    // nothing to assert, no thrown exception means success
  }

  private void whenUrlIsLoaded(String url) {
    runOnMainThreadAndWait(() -> {
      WebViewClient webViewClient = new AdWebViewClient(listener);
      WebView webView = new WebView(context);
      webViewClient.shouldOverrideUrlLoading(webView, url);
    });
  }

}