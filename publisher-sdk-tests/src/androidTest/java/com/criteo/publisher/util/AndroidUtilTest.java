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

package com.criteo.publisher.util;

import static org.junit.Assert.assertEquals;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import androidx.test.rule.ActivityTestRule;
import com.criteo.publisher.test.activity.DummyActivity;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class AndroidUtilTest {

  @Rule
  public ActivityTestRule<DummyActivity> activityRule = new ActivityTestRule<>(DummyActivity.class);

  private AndroidUtil androidUtil;

  @Before
  public void setUp() throws Exception {
    androidUtil = new AndroidUtil(activityRule.getActivity().getApplicationContext());
  }

  @Test
  public void getOrientation_GivenDeviceInPortrait_ReturnPortrait() throws Exception {
    activityRule.getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    waitForOrientation();

    int orientation = androidUtil.getOrientation();

    assertEquals(Configuration.ORIENTATION_PORTRAIT, orientation);
  }

  @Test
  public void getOrientation_GivenDeviceInReversePortrait_ReturnPortrait() throws Exception {
    activityRule.getActivity()
        .setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
    waitForOrientation();

    int orientation = androidUtil.getOrientation();

    assertEquals(Configuration.ORIENTATION_PORTRAIT, orientation);
  }

  @Test
  public void getOrientation_GivenDeviceInLandscape_ReturnLandscape() throws Exception {
    activityRule.getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    waitForOrientation();

    int orientation = androidUtil.getOrientation();

    assertEquals(Configuration.ORIENTATION_LANDSCAPE, orientation);
  }

  @Test
  public void getOrientation_GivenDeviceInReverseLandscape_ReturnLandscape() throws Exception {
    activityRule.getActivity()
        .setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
    waitForOrientation();

    int orientation = androidUtil.getOrientation();

    assertEquals(Configuration.ORIENTATION_LANDSCAPE, orientation);
  }

  private void waitForOrientation() throws InterruptedException {
    Thread.sleep(1000); // FIXME EE-657
  }

}