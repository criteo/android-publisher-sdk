package com.criteo.publisher.advancednative;

import static org.junit.Assert.assertEquals;

import android.support.test.rule.ActivityTestRule;
import android.view.ViewGroup;
import com.criteo.publisher.memory.GarbageCollector;
import com.criteo.publisher.mock.MockedDependenciesRule;
import com.criteo.publisher.test.activity.DummyActivity;
import javax.inject.Inject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class AdChoiceMemoryTest {

  @Rule
  public ActivityTestRule<DummyActivity> activityRule = new ActivityTestRule<>(DummyActivity.class);

  @Rule
  public MockedDependenciesRule mockedDependenciesRule = new MockedDependenciesRule();

  private UiHelper uiHelper;

  @Inject
  private AdChoiceOverlay adChoiceOverlay;

  @Before
  public void setUp() throws Exception {
    uiHelper = new UiHelper(activityRule);
  }

  @SuppressWarnings("UnusedAssignment")
  @Test
  public void getAdChoiceCount_GivenActivityDestroyed_ReleaseAdChoicesAttachedToActivity() throws Exception {
    ViewGroup viewWithOverlay = adChoiceOverlay.addOverlay(uiHelper.createView());

    // AdChoice is protected while overlay is strongly held
    forceGc();
    assertEquals(1, adChoiceOverlay.getAdChoiceCount());

    uiHelper.drawViews(viewWithOverlay);
    viewWithOverlay = null;

    // AdChoice is protected because activity is leaving
    forceGc();
    assertEquals(1, adChoiceOverlay.getAdChoiceCount());

    activityRule.finishActivity();

    // AdChoice should be released, nothing holds it anymore
    forceGc();
    assertEquals(0, adChoiceOverlay.getAdChoiceCount());
  }

  private void forceGc() throws InterruptedException {
    GarbageCollector.forceGc(5);
  }

}
