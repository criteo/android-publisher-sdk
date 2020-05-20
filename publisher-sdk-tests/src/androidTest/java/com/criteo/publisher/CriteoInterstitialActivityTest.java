package com.criteo.publisher;

import static android.support.test.runner.lifecycle.Stage.RESUMED;
import static com.criteo.publisher.CriteoUtil.givenInitializedCriteo;
import static com.criteo.publisher.concurrent.ThreadingUtil.callOnMainThreadAndWait;
import static com.criteo.publisher.concurrent.ThreadingUtil.runOnMainThreadAndWait;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.lifecycle.ActivityLifecycleMonitor;
import android.support.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import android.support.test.runner.lifecycle.Stage;
import android.webkit.WebView;
import com.criteo.publisher.interstitial.InterstitialActivityHelper;
import com.criteo.publisher.mock.MockedDependenciesRule;
import com.criteo.publisher.mock.SpyBean;
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

  @Mock
  private CriteoInterstitialAdListener listener;

  @Before
  public void setUp() throws Exception {
    givenInitializedCriteo();
  }

  @Test
  public void whenUserClickOnAd_GivenHtmlWithHttpUrl_RedirectUserAndNotifyListener() throws Exception {
    Activity activity = whenUserClickOnAd("https://criteo.com");

    assertFalse(getActivitiesInStage(RESUMED).contains(activity));
    verify(listener).onAdClicked();
    verify(listener).onAdLeftApplication();
    verifyNoMoreInteractions(listener);
  }

  @Test
  public void whenUserClickOnAd_GivenHtmlWithNotHandledDeepLink_DoNothing() throws Exception {
    // We assume that no application can handle such URL.

    whenUserClickOnAd("fake-deeplink://fakeappdispatch");

    verify(context, never()).startActivity(any());
    verifyNoMoreInteractions(listener);
  }

  @Test
  public void whenUserClickOnAd_GivenHtmlWithHandledDeepLink_RedirectUserAndNotifyListener() throws Exception {
    Activity activity = whenUserClickOnAd("criteo-test://dummy-ad-activity");

    assertFalse(getActivitiesInStage(RESUMED).contains(activity));
    verify(listener).onAdClicked();
    verify(listener).onAdLeftApplication();
    verifyNoMoreInteractions(listener);
  }

  @Test
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

  @Test
  public void testAppearAndDoNotDismiss() throws Exception {
    Activity activity = givenOpenedInterstitialActivity("");

    assertTrue(getActivitiesInStage(RESUMED).contains(activity));

    Thread.sleep(2000);

    assertTrue(getActivitiesInStage(RESUMED).contains(activity));
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
      interstitialActivityHelper.openActivity(html, listener);
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