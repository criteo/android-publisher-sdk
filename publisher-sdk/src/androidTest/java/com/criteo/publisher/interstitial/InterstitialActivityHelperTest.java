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

package com.criteo.publisher.interstitial;

import static com.criteo.publisher.interstitial.InterstitialActivityHelper.CALLING_ACTIVITY;
import static com.criteo.publisher.interstitial.InterstitialActivityHelper.RESULT_RECEIVER;
import static com.criteo.publisher.interstitial.InterstitialActivityHelper.WEB_VIEW_DATA;
import static com.criteo.publisher.view.WebViewClicker.waitUntilWebViewIsLoaded;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.view.View;
import android.webkit.WebView;
import androidx.annotation.NonNull;
import androidx.test.rule.ActivityTestRule;
import com.criteo.publisher.CriteoInterstitialActivity;
import com.criteo.publisher.activity.TopActivityFinder;
import com.criteo.publisher.mock.MockedDependenciesRule;
import com.criteo.publisher.tasks.InterstitialListenerNotifier;
import com.criteo.publisher.test.activity.DummyActivity;
import com.criteo.publisher.util.CriteoResultReceiver;
import com.criteo.publisher.view.WebViewLookup;
import javax.inject.Inject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class InterstitialActivityHelperTest {

  @Rule
  public MockedDependenciesRule mockedDependenciesRule = new MockedDependenciesRule();

  @Rule
  public ActivityTestRule<DummyActivity> activityRule = new ActivityTestRule<>(DummyActivity.class);

  private Context context;

  @Inject
  private TopActivityFinder topActivityFinder;

  @Mock
  private InterstitialListenerNotifier listenerNotifier;

  private InterstitialActivityHelper helper;

  private WebViewLookup webViewLookup;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    context = activityRule.getActivity().getApplicationContext();
    topActivityFinder.registerActivityLifecycleFor(activityRule.getActivity().getApplication());
    helper = createHelper();
    webViewLookup = new WebViewLookup();
  }

  @Test
  public void isAvailable_GivenNormalIntegration_ReturnTrue() throws Exception {
    // given nothing special

    boolean isAvailable = helper.isAvailable();

    assertTrue(isAvailable);
  }

  @Test
  public void openActivity_GivenListenerAndContent_StartActivityWithThem() throws Exception {
    context = mock(Context.class);
    helper = spy(createHelper());

    doReturn(true).when(helper).isAvailable();

    when(context.getPackageName()).thenReturn("myPackage");
    ComponentName expectedComponent = new ComponentName(context, CriteoInterstitialActivity.class);

    ComponentName expectedCallingActivity = activityRule.getActivity().getComponentName();

    CriteoResultReceiver expectedReceiver = mock(CriteoResultReceiver.class);
    doReturn(expectedReceiver).when(helper).createReceiver(listenerNotifier);
    doReturn(true).when(helper).isAvailable();

    helper.openActivity("myContent", listenerNotifier, -1);

    verify(context).startActivity(argThat(intent -> {
      assertEquals(expectedComponent, intent.getComponent());
      assertEquals("myContent", intent.getStringExtra(WEB_VIEW_DATA));
      assertEquals(expectedReceiver, intent.getParcelableExtra(RESULT_RECEIVER));
      assertEquals(expectedCallingActivity, intent.getParcelableExtra(CALLING_ACTIVITY));
      return true;
    }));
  }

  @Test
  public void openActivity_GivenTwoOpening_OpenItTwice() throws Exception {
    String html1 = openInterstitialAndGetHtml("myContent1");
    String html2 = openInterstitialAndGetHtml("myContent2");

    assertTrue(html1.contains("myContent1"));
    assertTrue(html2.contains("myContent2"));
  }

  private String openInterstitialAndGetHtml(String content) throws Exception {
    Activity activity = webViewLookup.lookForResumedActivity(() -> {
      helper.openActivity(content, listenerNotifier, -1);
    }).get();

    View root = activity.getWindow().getDecorView();
    waitForWebViewToBeReady(root);

    return webViewLookup.lookForHtmlContent(root).get();
  }

  private void waitForWebViewToBeReady(@NonNull View root) throws Exception {
    for (WebView webView : webViewLookup.lookForWebViews(root)) {
      waitUntilWebViewIsLoaded(webView);
    }
  }

  @NonNull
  private InterstitialActivityHelper createHelper() {
    return new InterstitialActivityHelper(context, topActivityFinder);
  }

}