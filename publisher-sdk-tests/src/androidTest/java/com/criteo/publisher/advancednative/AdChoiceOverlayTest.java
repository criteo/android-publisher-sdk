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

package com.criteo.publisher.advancednative;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.test.rule.ActivityTestRule;
import com.criteo.publisher.mock.MockedDependenciesRule;
import com.criteo.publisher.mock.SpyBean;
import com.criteo.publisher.test.activity.DummyActivity;
import com.criteo.publisher.util.BuildConfigWrapper;
import java.util.Arrays;
import java.util.Collection;
import javax.inject.Inject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;

@RunWith(Parameterized.class)
public class AdChoiceOverlayTest {

  @Rule
  public ActivityTestRule<DummyActivity> activityRule = new ActivityTestRule<>(DummyActivity.class);

  @Rule
  public MockedDependenciesRule mockedDependenciesRule = new MockedDependenciesRule();

  @SuppressWarnings("rawtypes")
  // FIXME Gordon runner does not support named parameterized test => they appear as IGNORED.
  //  Issue come from Android tools not supporting this neither.
  //  See https://github.com/Banno/Gordon/issues/47
  // @Parameterized.Parameters(name = "{index}: {0}")
  @Parameterized.Parameters
  public static Collection consents() {
    return Arrays.asList(new Object[][]{
        { new SimpleViewFactory() },
        { new FrameLayoutHierarchyFactory() },
        { new RelativeLayoutHierarchyFactory() }
    });
  }

  @Parameter
  public ViewFactory viewFactory;

  private UiHelper uiHelper;

  @SpyBean
  private BuildConfigWrapper buildConfigWrapper;

  @Inject
  private AdChoiceOverlay adChoiceOverlay;

  @Before
  public void setUp() throws Exception {
    uiHelper = new UiHelper(activityRule);
  }

  @Test
  public void getInitialView_GivenViewWithoutOverlay_ReturnNull() throws Exception {
    View view = viewFactory.create(this);

    View initialView = adChoiceOverlay.getInitialView(view);

    assertNull(initialView);
  }

  @Test
  public void getAdChoiceView_GivenViewWithoutOverlay_ReturnNull() throws Exception {
    View view = viewFactory.create(this);

    View adChoiceView = adChoiceOverlay.getAdChoiceView(view);

    assertNull(adChoiceView);
  }

  @Test
  public void addOverlay_GivenView_WrapItInViewGroupWithThePlaceholder() throws Exception {
    View viewWrappedInOverlay = viewFactory.create(this);

    ViewGroup viewWithOverlay = adChoiceOverlay.addOverlay(viewWrappedInOverlay);
    View adChoiceView = adChoiceOverlay.getAdChoiceView(viewWithOverlay);
    View initialView = adChoiceOverlay.getInitialView(viewWithOverlay);

    assertEquals(viewWithOverlay.getChildCount(), 2);
    assertEquals(viewWithOverlay.getChildAt(0), viewWrappedInOverlay);
    assertEquals(viewWithOverlay.getChildAt(1), adChoiceView);
    assertEquals(viewWrappedInOverlay, initialView);
  }

  @Test
  public void addOverlay_GivenView_ShouldSizeViewAsIfThereIsNoOverlay() {
    View viewWithoutOverlay = viewFactory.create(this);
    View viewWrappedInOverlay = viewFactory.create(this);

    ViewGroup viewWithOverlay = adChoiceOverlay.addOverlay(viewWrappedInOverlay);
    addDummyAdChoiceIcon(viewWithOverlay);

    uiHelper.drawViews(viewWithoutOverlay);
    uiHelper.drawViews(viewWithOverlay);

    assertEquals(viewWithoutOverlay.getWidth(), viewWrappedInOverlay.getWidth());
    assertEquals(viewWithoutOverlay.getHeight(), viewWrappedInOverlay.getHeight());
    assertEquals(viewWrappedInOverlay.getWidth(), viewWithOverlay.getWidth());
    assertEquals(viewWrappedInOverlay.getHeight(), viewWithOverlay.getHeight());
  }

  @Test
  public void addOverlay_GivenViewWrappedInLayout_ShouldSizeViewAsIfThereIsNoOverlay() {
    View viewWithoutOverlay = viewFactory.create(this);
    View viewWrappedInOverlay = viewFactory.create(this);

    ViewGroup viewWithOverlay = adChoiceOverlay.addOverlay(viewWrappedInOverlay);
    addDummyAdChoiceIcon(viewWithOverlay);

    ViewGroup wrappedViewWithoutOverlay = uiHelper.createFrameLayout();
    ViewGroup wrappedViewWithOverlay = uiHelper.createFrameLayout();

    wrappedViewWithoutOverlay.addView(viewWithoutOverlay);
    wrappedViewWithOverlay.addView(viewWithOverlay);

    uiHelper.drawViews(wrappedViewWithoutOverlay);
    uiHelper.drawViews(wrappedViewWithOverlay);

    assertEquals(viewWithoutOverlay.getWidth(), viewWrappedInOverlay.getWidth());
    assertEquals(viewWithoutOverlay.getHeight(), viewWrappedInOverlay.getHeight());
    assertEquals(viewWrappedInOverlay.getWidth(), viewWithOverlay.getWidth());
    assertEquals(viewWrappedInOverlay.getHeight(), viewWithOverlay.getHeight());
  }

  @Test
  public void addOverlay_GivenSimpleViewAndFixSizeForAdChoice_AdChoiceShouldAppearAtTopRightCorner() throws Exception {
    when(buildConfigWrapper.getAdChoiceIconWidthInDp()).thenReturn(19);
    when(buildConfigWrapper.getAdChoiceIconHeightInDp()).thenReturn(15);

    View viewWrappedInOverlay = viewFactory.create(this);

    ViewGroup viewWithOverlay = adChoiceOverlay.addOverlay(viewWrappedInOverlay);
    ImageView adChoiceView = adChoiceOverlay.getAdChoiceView(viewWithOverlay);
    addDummyAdChoiceIcon(viewWithOverlay);

    uiHelper.drawViews(viewWithOverlay);

    View viewInside1 = uiHelper.findViewAt(viewWithOverlay, -1, 0);
    View viewInside2 = uiHelper.findViewAt(viewWithOverlay, -19, 15);
    View viewOutside = uiHelper.findViewAt(viewWithOverlay, -20, 16);

    assertEquals(adChoiceView, viewInside1);
    assertEquals(adChoiceView, viewInside2);

    // On small devices, we may hit the content inside the viewWrappedInOverlay
    assertEqualsOrIsParent(viewWrappedInOverlay, viewOutside);
  }

  private void assertEqualsOrIsParent(@NonNull View parent, @Nullable View other) {
    if (parent.equals(other)) {
      return;
    }
    assertTrue(uiHelper.isParent(parent, other));
  }

  /**
   * Set dummy icon in ad choice view for debugging purpose
   */
  private void addDummyAdChoiceIcon(ViewGroup viewWithOverlay) {
    ImageView adChoiceView = adChoiceOverlay.getAdChoiceView(viewWithOverlay);
    adChoiceView.setImageResource(android.R.drawable.ic_delete);
  }

  private interface ViewFactory {
    View create(AdChoiceOverlayTest test);
  }

  private static class SimpleViewFactory implements ViewFactory {
    @Override
    public View create(AdChoiceOverlayTest test) {
      return test.uiHelper.createView();
    }
  }

  private static class FrameLayoutHierarchyFactory implements ViewFactory {
    @Override
    public View create(AdChoiceOverlayTest test) {
      View view1 = test.uiHelper.createView();
      View view2 = test.uiHelper.createView();

      FrameLayout layout = test.uiHelper.createFrameLayout();
      layout.addView(view1);
      layout.addView(view2);

      view1.getLayoutParams().width = 400;
      view1.getLayoutParams().height = 200;

      view2.getLayoutParams().width = 300;
      view2.getLayoutParams().height = 100;

      return layout;
    }
  }

  private static class RelativeLayoutHierarchyFactory implements ViewFactory {
    @Override
    public View create(AdChoiceOverlayTest test) {
      View view1 = test.uiHelper.createView();
      View view2 = test.uiHelper.createView();

      view1.setId(View.generateViewId());

      RelativeLayout layout = new RelativeLayout(test.activityRule.getActivity());
      layout.setLayoutParams(new ViewGroup.LayoutParams(WRAP_CONTENT, WRAP_CONTENT));
      layout.setPadding(50, 20, 200, 20);
      layout.addView(view1);
      layout.addView(view2);

      view1.getLayoutParams().width = 300;
      view1.getLayoutParams().height = 100;

      view2.getLayoutParams().width = 400;
      view2.getLayoutParams().height = 200;
      ((RelativeLayout.LayoutParams) view2.getLayoutParams()).addRule(RelativeLayout.BELOW, view1.getId());

      return layout;
    }
  }

}