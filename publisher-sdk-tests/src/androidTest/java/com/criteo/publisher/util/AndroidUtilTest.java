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