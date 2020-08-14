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

package com.criteo.publisher.advancednative

import androidx.test.rule.ActivityTestRule
import com.criteo.publisher.test.activity.DummyActivity
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class UIHelperTest {

  @Rule
  @JvmField
  var activityRule = ActivityTestRule<DummyActivity>(DummyActivity::class.java)

  private lateinit var uiHelper: UiHelper

  @Before
  fun setUp() {
    uiHelper = UiHelper(activityRule)
  }

  @Test
  fun isParent_GivenUnrelatedViews_ReturnsFalse() {
    val view1 = uiHelper.createView()
    val view2 = uiHelper.createView()

    val isParent = uiHelper.isParent(view1, view2)

    assertThat(isParent).isFalse()
  }

  @Test
  fun isParent_GivenViewAndNull_ReturnsFalse() {
    val view = uiHelper.createView()

    val isParent = uiHelper.isParent(view, null)

    assertThat(isParent).isFalse()
  }

  @Test
  fun isParent_GivenSiblingViews_ReturnsFalse() {
    val view1 = uiHelper.createView()
    val view2 = uiHelper.createView()
    val parent = uiHelper.createFrameLayout()
    parent.addView(view1)
    parent.addView(view2)

    val isParent = uiHelper.isParent(view1, view2)

    assertThat(isParent).isFalse()
  }

  @Test
  fun isParent_GivenParentAndChild_ReturnsTrue() {
    val view = uiHelper.createView()
    val parent = uiHelper.createFrameLayout()
    parent.addView(view)

    val isParent = uiHelper.isParent(parent, view)

    assertThat(isParent).isTrue()
  }

  @Test
  fun isParent_GivenChildAndParent_ReturnsFalse() {
    val view = uiHelper.createView()
    val parent = uiHelper.createFrameLayout()
    parent.addView(view)

    val isParent = uiHelper.isParent(view, parent)

    assertThat(isParent).isFalse()
  }

  @Test
  fun isParent_GivenGrandParentAndChildren_ReturnsTrue() {
    val view1 = uiHelper.createFrameLayout()
    val view2 = uiHelper.createFrameLayout()
    val view3 = uiHelper.createFrameLayout()
    val view4 = uiHelper.createFrameLayout()
    val parent = uiHelper.createFrameLayout()

    parent.addView(view1)
    parent.addView(view2)
    view1.addView(view3)
    view3.addView(view4)

    assertThat(uiHelper.isParent(parent, view1)).isTrue()
    assertThat(uiHelper.isParent(parent, view2)).isTrue()
    assertThat(uiHelper.isParent(parent, view3)).isTrue()
    assertThat(uiHelper.isParent(parent, view4)).isTrue()
  }

}