package com.criteo.publisher.advancednative;

import static com.criteo.publisher.CriteoUtil.givenInitializedCriteo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import android.app.Activity;
import android.support.test.rule.ActivityTestRule;
import com.criteo.publisher.TestAdUnits;
import com.criteo.publisher.activity.TestNativeActivity;
import com.criteo.publisher.memory.GarbageCollector;
import com.criteo.publisher.mock.MockedDependenciesRule;
import java.lang.ref.WeakReference;
import org.junit.Rule;
import org.junit.Test;

public class CriteoNativeLoaderMemoryTest {

  @Rule
  public ActivityTestRule<TestNativeActivity> activityRule = new ActivityTestRule<>(
      TestNativeActivity.class);

  @Rule
  public MockedDependenciesRule mockedDependenciesRule = new MockedDependenciesRule();

  @Test
  public void givenLoadedAdAndFinishedActivity_DoNotRetainAnyState() throws Exception {
    givenInitializedCriteo(TestAdUnits.NATIVE);
    mockedDependenciesRule.waitForIdleState();

    activityRule.getActivity().loadStandaloneAd();
    mockedDependenciesRule.waitForIdleState();

    WeakReference<Activity> activityRef = new WeakReference<>(activityRule.getActivity());

    // Activity is running, so it should not be GC
    forceGc();
    assertNotNull(activityRef.get());

    activityRule.finishActivity();

    // Activity is finished, nothing should hold it
    forceGc();
    assertNull(activityRef.get());
  }

  private void forceGc() throws InterruptedException {
    GarbageCollector.forceGc(5);
  }

}