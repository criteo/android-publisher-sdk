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

package com.criteo.publisher.adview

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class MraidResizeCustomClosePositionTest {

  @Test
  fun asCustomClosePosition_givenTopLeftString_shouldReturnTopLeftEnumValue() {
    assertThat("top-left".asCustomClosePosition()).isEqualTo(MraidResizeCustomClosePosition.TOP_LEFT)
  }

  @Test
  fun asCustomClosePosition_givenTopRightString_shouldReturnTopRightEnumValue() {
    assertThat("top-right".asCustomClosePosition()).isEqualTo(MraidResizeCustomClosePosition.TOP_RIGHT)
  }

  @Test
  fun asCustomClosePosition_givenCenterString_shouldReturnCenterEnumValue() {
    assertThat("center".asCustomClosePosition()).isEqualTo(MraidResizeCustomClosePosition.CENTER)
  }

  @Test
  fun asCustomClosePosition_givenBottomLeftString_shouldReturnBottomLeftEnumValue() {
    assertThat("bottom-left".asCustomClosePosition()).isEqualTo(MraidResizeCustomClosePosition.BOTTOM_LEFT)
  }

  @Test
  fun asCustomClosePosition_givenBottomRightString_shouldReturnBottomRightEnumValue() {
    assertThat("bottom-right".asCustomClosePosition()).isEqualTo(MraidResizeCustomClosePosition.BOTTOM_RIGHT)
  }

  @Test
  fun asCustomClosePosition_givenTopCenterString_shouldReturnTopCenterEnumValue() {
    assertThat("top-center".asCustomClosePosition()).isEqualTo(MraidResizeCustomClosePosition.TOP_CENTER)
  }

  @Test
  fun asCustomClosePosition_givenBottomCenterString_shouldReturnBottomCenterEnumValue() {
    assertThat("bottom-center".asCustomClosePosition()).isEqualTo(MraidResizeCustomClosePosition.BOTTOM_CENTER)
  }

  @Test
  fun asCustomClosePosition_givenRandomString_shouldReturnTopRightEnumValue() {
    assertThat("random".asCustomClosePosition()).isEqualTo(MraidResizeCustomClosePosition.TOP_RIGHT)
  }
}
