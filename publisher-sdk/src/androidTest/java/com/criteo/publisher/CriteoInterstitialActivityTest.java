package com.criteo.publisher;

import static android.support.test.runner.lifecycle.Stage.DESTROYED;
import static android.support.test.runner.lifecycle.Stage.RESUMED;
import static com.criteo.publisher.CriteoInterstitial.RESULT_RECEIVER;
import static com.criteo.publisher.CriteoInterstitial.WEB_VIEW_DATA;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import android.content.Intent;
import android.os.Bundle;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.criteo.publisher.Util.CriteoResultReceiver;
import java.util.Collection;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(AndroidJUnit4.class)
public class CriteoInterstitialActivityTest {

    @Rule
    public ActivityTestRule<CriteoInterstitialActivity> activityRule = new ActivityTestRule<>(
            CriteoInterstitialActivity.class, true, false);

    @Test
    public void whenDeeplinkIsLoaded_GivenTargetAppIsNotInstalled_DontThrowActivityNotFound() {
        CriteoResultReceiver criteoResultReceiver = mock(CriteoResultReceiver.class);
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putString(WEB_VIEW_DATA, "");
        bundle.putParcelable(RESULT_RECEIVER, criteoResultReceiver);
        intent.putExtras(bundle);
        activityRule.launchActivity(intent);

        CriteoInterstitialActivity activity = activityRule.getActivity();
        WebView webView = activity.getWebView();
        WebViewClient webViewClient  = webView.getWebViewClient();
        webViewClient.shouldOverrideUrlLoading(webView, "fake_deeplink://fakeappdispatch");
    }

    @Test
    public void testAppearAndDoNotDismiss() {
        CriteoResultReceiver criteoResultReceiver = mock(CriteoResultReceiver.class);
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putString(WEB_VIEW_DATA, "html content");
        bundle.putParcelable(RESULT_RECEIVER, criteoResultReceiver);
        intent.putExtras(bundle);
        activityRule.launchActivity(intent);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        InstrumentationRegistry.getInstrumentation().runOnMainSync(new Runnable() {
            public void run() {
                Collection resumedActivities = ActivityLifecycleMonitorRegistry.getInstance()
                        .getActivitiesInStage(RESUMED);
                Collection closedActivities = ActivityLifecycleMonitorRegistry.getInstance()
                        .getActivitiesInStage(DESTROYED);
                assertTrue(resumedActivities.contains(activityRule.getActivity()));
                assertFalse(closedActivities.contains(activityRule.getActivity()));
            }
        });

        try {
            Thread.sleep(8000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        InstrumentationRegistry.getInstrumentation().runOnMainSync(new Runnable() {
            public void run() {
                Collection closedActivities = ActivityLifecycleMonitorRegistry.getInstance()
                        .getActivitiesInStage(DESTROYED);
                assertFalse(closedActivities.contains(activityRule.getActivity()));
            }
        });


    }

}