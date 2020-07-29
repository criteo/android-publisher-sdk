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

package com.criteo.publisher.activity;

import static com.criteo.publisher.CriteoUtil.givenInitializedCriteo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import androidx.test.rule.ActivityTestRule;
import com.criteo.publisher.concurrent.ThreadingUtil;
import com.criteo.publisher.mock.MockedDependenciesRule;
import com.criteo.publisher.test.activity.DummyActivity;
import com.criteo.publisher.view.WebViewLookup;
import javax.inject.Inject;
import org.junit.Rule;
import org.junit.Test;

public class TopActivityFinderTest {

  @Rule
  public MockedDependenciesRule mockedDependenciesRule = new MockedDependenciesRule();

  @Rule
  public ActivityTestRule<DummyActivity> activityRule = new ActivityTestRule<>(
      DummyActivity.class,
      false,
      false
  );

  private final WebViewLookup lookup = new WebViewLookup();

  @Inject
  private TopActivityFinder topActivityFinder;

  @Inject
  private Context context;

  @Test
  public void topActivityName_GivenSdkInitializedAfterActivityHasStarted_ReturnTheActivity() throws Exception {
    activityRule.launchActivity(new Intent());
    givenInitializedCriteo();

    ComponentName topActivityName = topActivityFinder.getTopActivityName();

    assertEquals(activityRule.getActivity().getComponentName(), topActivityName);
  }

  @Test
  public void topActivityName_GivenSdkInitializedBeforeActivityHasStarted_ReturnTheActivity() throws Exception {
    givenInitializedCriteo();
    activityRule.launchActivity(new Intent());

    ComponentName topActivityName = topActivityFinder.getTopActivityName();

    assertEquals(activityRule.getActivity().getComponentName(), topActivityName);
  }

  @Test
  public void topActivityName_GivenFinishedActivityWithoutPredecessor_ReturnNull() throws Exception {
    givenInitializedCriteo();
    activityRule.launchActivity(new Intent());
    activityRule.getActivity().finish();

    ThreadingUtil.waitForMessageQueueToBeIdle();

    ComponentName topActivityName = topActivityFinder.getTopActivityName();

    assertNull(topActivityName);
  }

  @Test
  public void topActivityName_GivenAnotherActivityOpened_ReturnIt() throws Exception {
    givenInitializedCriteo();
    activityRule.launchActivity(new Intent());
    Activity activity = openAnotherActivity(DummyAdActivity.class);

    ComponentName topActivityName = topActivityFinder.getTopActivityName();

    assertEquals(activity.getComponentName(), topActivityName);
  }

  @Test
  public void topActivityName_GivenFinishedActivityWithPredecessor_ReturnPredecessor() throws Exception {
    givenInitializedCriteo();

    Activity predecessor = activityRule.launchActivity(new Intent());
    Activity activity = openAnotherActivity(DummyAdActivity.class);
    lookup.lookForResumedActivity(activity::finish).get();

    ComponentName topActivityName = topActivityFinder.getTopActivityName();

    assertEquals(predecessor.getComponentName(), topActivityName);
  }

  private Activity openAnotherActivity(Class<? extends Activity> activityClass) throws Exception {
    return lookup.lookForResumedActivity(() -> {
      // The injected context represents an application context and require flag to open a new
      // activity. It would be possible to not flag the intent by using directly the activity
      // context, but the SDK use application context, then those tests would not represent anymore
      // what is done in prod.
      Intent intent = new Intent(context, activityClass).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      context.startActivity(intent);
    }).get();
  }

}