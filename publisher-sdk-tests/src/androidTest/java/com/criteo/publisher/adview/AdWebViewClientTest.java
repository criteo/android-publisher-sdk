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

package com.criteo.publisher.adview;

import static com.criteo.publisher.concurrent.ThreadingUtil.callOnMainThreadAndWait;
import static com.criteo.publisher.concurrent.ThreadingUtil.runOnMainThreadAndWait;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import android.app.Activity;
import android.content.Context;
import android.webkit.WebView;
import androidx.annotation.NonNull;
import androidx.test.rule.ActivityTestRule;
import com.criteo.publisher.mock.MockedDependenciesRule;
import com.criteo.publisher.mock.SpyBean;
import com.criteo.publisher.test.activity.DummyActivity;
import com.criteo.publisher.view.WebViewClicker;
import com.criteo.publisher.view.WebViewLookup;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

public class AdWebViewClientTest {

  @Rule
  public MockitoRule mockitoRule = MockitoJUnit.rule();

  @Rule
  public ActivityTestRule<DummyActivity> activityRule = new ActivityTestRule<>(DummyActivity.class);

  @Rule
  public MockedDependenciesRule mockedDependenciesRule = new MockedDependenciesRule();

  private WebViewLookup lookup = new WebViewLookup();

  private WebViewClicker clicker = new WebViewClicker();

  @SpyBean
  private Context context;

  @Mock
  private RedirectionListener listener;

  private WebView webView;

  private AdWebViewClient webViewClient;

  @Before
  public void setUp() throws Exception {
    webViewClient = spy(new AdWebViewClient(
        listener,
        activityRule.getActivity().getComponentName()
    ));

    webView = callOnMainThreadAndWait(() -> {
      WebView view = new WebView(context);
      view.getSettings().setJavaScriptEnabled(true);
      view.setWebViewClient(webViewClient);
      return view;
    });
  }

  @Test
  public void whenUserClickOnAd_GivenHttpUrl_OpenActivityAndNotifyListener() throws Exception {
    // We assume that there is a browser installed on the test device.

    whenUserClickOnAd("https://criteo.com");

    verify(context).startActivity(any());
    verify(listener).onUserRedirectedToAd();
    verifyNoMoreInteractions(listener);
  }

  @Test
  public void whenUserClickOnAd_GivenDeepLinkAndInstalledAppToHandleIt_OpenActivityAndNotifyListener() throws Exception {
    whenUserClickOnAd("criteo-test://dummy-ad-activity");

    verify(context).startActivity(any());
    verify(listener).onUserRedirectedToAd();
    verifyNoMoreInteractions(listener);
  }

  @Test
  public void whenUserClickOnAd_GivenTargetAppIsNotInstalled_DontThrowActivityNotFoundAndDoNotRedirectUser() throws Exception {
    // We assume that no application can handle such URL.

    whenUserClickOnAd("fake-deeplink://fakeappdispatch");

    verify(context, never()).startActivity(any());
    verifyNoMoreInteractions(listener);
  }

  @Test
  public void whenUserClickOnAdAndGoBack_GivenDeepLinkAndInstalledAppToHandleIt_NotifyListener() throws Exception {
    Activity activity = lookup.lookForResumedActivity(() -> {
      whenUserClickOnAd("criteo-test://dummy-ad-activity");
    }).get();

    lookup.lookForResumedActivity(() -> {
      runOnMainThreadAndWait(activity::onBackPressed);
    }).get();

    InOrder inOrder = inOrder(listener);
    inOrder.verify(listener).onUserRedirectedToAd();
    inOrder.verify(listener).onUserBackFromAd();
    inOrder.verifyNoMoreInteractions();
  }

  private void whenUserClickOnAd(@NonNull String url) throws Exception {
    clicker.loadHtmlAndSimulateClickOnAd(webView, url);
  }

}