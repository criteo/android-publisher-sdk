/*
 *    Copyright 2020 Criteo
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.criteo.publisher.tasks;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.annotation.NonNull;
import com.criteo.publisher.model.Config;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

public class CriteoBannerLoadTaskTest {

  @Rule
  public MockitoRule mockitoRule = MockitoJUnit.rule();

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private WebView webView;

  private Reference<WebView> webViewRef;

  @Mock
  private Config config;

  @Mock
  private WebViewClient webViewClient;

  @Before
  public void setup() {
    webViewRef = new WeakReference<>(webView);
  }

  @Test
  public void execute_GivenDisplayUrlAndConfig_InjectItInsideConfigMacrosAndLoadItOnWebView()
      throws InterruptedException {
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

    verifyNoInteractions(config);
  }

  @NonNull
  private CriteoBannerLoadTask createTask(String displayUrl) {
    return new CriteoBannerLoadTask(webViewRef, webViewClient, config, displayUrl);
  }

}