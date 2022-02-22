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

package com.criteo.publisher;

import static androidx.test.runner.lifecycle.Stage.RESUMED;
import static com.criteo.publisher.CriteoUtil.givenInitializedCriteo;
import static com.criteo.publisher.concurrent.ThreadingUtil.callOnMainThreadAndWait;
import static com.criteo.publisher.concurrent.ThreadingUtil.runOnMainThreadAndWait;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atMostOnce;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import android.app.Activity;
import android.app.Application;
import android.app.Application.ActivityLifecycleCallbacks;
import android.content.Context;
import android.webkit.WebView;
import androidx.annotation.NonNull;
import androidx.test.filters.FlakyTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.lifecycle.ActivityLifecycleMonitor;
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import androidx.test.runner.lifecycle.Stage;
import com.criteo.publisher.activity.TopActivityFinder;
import com.criteo.publisher.concurrent.RunOnUiThreadExecutor;
import com.criteo.publisher.interstitial.InterstitialActivityHelper;
import com.criteo.publisher.mock.MockedDependenciesRule;
import com.criteo.publisher.mock.SpyBean;
import com.criteo.publisher.tasks.InterstitialListenerNotifier;
import com.criteo.publisher.test.activity.DummyActivity;
import com.criteo.publisher.view.WebViewClicker;
import com.criteo.publisher.view.WebViewLookup;
import java.util.Collection;
import javax.inject.Inject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;


public class CriteoInterstitialActivityTest {

  @Rule
  public ActivityTestRule<DummyActivity> activityRule = new ActivityTestRule<>(DummyActivity.class);

  @Rule
  public MockedDependenciesRule mockedDependenciesRule = new MockedDependenciesRule();

  @Rule
  public MockitoRule mockitoRule = MockitoJUnit.rule();

  private final WebViewLookup lookup = new WebViewLookup();

  private final WebViewClicker clicker = new WebViewClicker();

  @SpyBean
  private Context context;

  @Inject
  private InterstitialActivityHelper interstitialActivityHelper;

  @Inject
  private RunOnUiThreadExecutor runOnUiThreadExecutor;

  @Mock
  private CriteoInterstitialAdListener listener;

  @Before
  public void setUp() throws Exception {
    givenInitializedCriteo();
  }

  @Test
  @FlakyTest(detail = "Device takes time for creating and destroying the activity")
  public void openActivity_GivenActivityStartedBySystemWithoutSdkBeingInitialized_CloseImmediatelyWithoutCrashing()
      throws Exception {
    CriteoUtil.clearCriteo();
    MockableDependencyProvider.setInstance(null);
    activityRule.finishActivity();

    assertThat(DependencyProvider.getInstance().isApplicationSet())
        .describedAs("Verify that all dependencies are empty like on a fresh start")
        .isFalse();

    ActivityLifecycleCallbacks lifecycle = mock(ActivityLifecycleCallbacks.class);
    Application application = (Application) context.getApplicationContext();
    application.registerActivityLifecycleCallbacks(lifecycle);

    InterstitialActivityHelper helper = new InterstitialActivityHelper(
        context,
        mock(TopActivityFinder.class)
    );

    helper.openActivity("content", mock(InterstitialListenerNotifier.class));

    InOrder inOrder = inOrder(lifecycle);
    inOrder.verify(lifecycle, timeout(2000))
        .onActivityCreated(any(CriteoInterstitialActivity.class), any());
    inOrder.verify(lifecycle, never()).onActivityStarted(any(CriteoInterstitialActivity.class));
    inOrder.verify(lifecycle, timeout(2000))
        .onActivityDestroyed(any(CriteoInterstitialActivity.class));
  }

  @Test
  public void whenUserClickOnAd_GivenHtmlWithHttpUrl_RedirectUserAndNotifyListener()
      throws Exception {
    Activity activity = whenUserClickOnAd("https://criteo.com");

    assertFalse(getActivitiesInStage(RESUMED).contains(activity));
    verify(listener).onAdClicked();
    verify(listener).onAdLeftApplication();
    verify(listener, atMostOnce()).onAdClosed();
    verifyNoMoreInteractions(listener);
  }

  @Test
  @FlakyTest(detail = "Flakiness comes from UI")
  public void whenUserClickOnAd_GivenHtmlWithNotHandledDeepLink_DoNothing() throws Exception {
    // We assume that no application can handle such URL.

    whenUserClickOnAd("fake-deeplink://fakeappdispatch");

    verify(context, never()).startActivity(any());
    verifyNoMoreInteractions(listener);
  }

  @Test
  @FlakyTest(detail = "Flakiness comes from UI")
  public void whenUserClickOnAd_GivenHtmlWithHandledDeepLink_RedirectUserAndNotifyListener() throws Exception {
    Activity activity = whenUserClickOnAd("criteo-test://dummy-ad-activity");

    assertFalse(getActivitiesInStage(RESUMED).contains(activity));
    verify(listener).onAdClicked();
    verify(listener).onAdLeftApplication();
    verify(listener, atMostOnce()).onAdClosed();
    verifyNoMoreInteractions(listener);
  }

  @Test
  @FlakyTest(detail = "Flakiness comes from UI")
  public void whenUserClickOnAdAndGoBack_GivenHtmlWithHandledDeepLink_NotifyListener() throws Exception {
    String html = clicker.getAdHtmlWithClickUrl("criteo-test://dummy-ad-activity");
    CriteoInterstitialActivity activity = givenOpenedInterstitialActivity(html);
    WebView webView = activity.getWebView();

    Activity dummyAdActivity = lookup.lookForResumedActivity(() -> {
      clicker.simulateClickOnAd(webView);
    }).get();

    Activity afterGoingBackActivity = lookup.lookForResumedActivity(() -> {
      runOnMainThreadAndWait(dummyAdActivity::onBackPressed);
    }).get();

    waitForIdleState();

    assertEquals(activityRule.getActivity().getComponentName(), afterGoingBackActivity.getComponentName());

    InOrder inOrder = inOrder(listener);
    inOrder.verify(listener).onAdClicked();
    inOrder.verify(listener).onAdLeftApplication();
    inOrder.verify(listener).onAdClosed();
    verifyNoMoreInteractions(listener);
  }

  private CriteoInterstitialActivity whenUserClickOnAd(String url) throws Exception {
    String html = clicker.getAdHtmlWithClickUrl(url);
    CriteoInterstitialActivity activity = givenOpenedInterstitialActivity(html);
    WebView webView = activity.getWebView();

    clicker.simulateClickOnAd(webView);
    waitForIdleState();

    return activity;
  }

  @NonNull
  private CriteoInterstitialActivity givenOpenedInterstitialActivity(@NonNull String html) throws Exception {
    Activity activity = lookup.lookForResumedActivity(() -> {
      InterstitialListenerNotifier listenerNotifier = new InterstitialListenerNotifier(
          mock(CriteoInterstitial.class),
          listener,
          runOnUiThreadExecutor
      );

      interstitialActivityHelper.openActivity(html, listenerNotifier);
    }).get();

    clearInvocations(context);

    return (CriteoInterstitialActivity) activity;
  }

  private Collection<Activity> getActivitiesInStage(Stage stage) {
    ActivityLifecycleMonitor activityMonitor = ActivityLifecycleMonitorRegistry.getInstance();
    return callOnMainThreadAndWait(() -> activityMonitor.getActivitiesInStage(stage));
  }

  private void waitForIdleState() {
    mockedDependenciesRule.waitForIdleState();
  }

}