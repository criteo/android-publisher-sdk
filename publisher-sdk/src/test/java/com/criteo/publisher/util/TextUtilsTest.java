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

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class TextUtilsTest {

  @Test
  public void isEmpty_GivenNull_ReturnTrue() throws Exception {
    boolean isEmpty = TextUtils.isEmpty(null);

    assertThat(isEmpty).isTrue();
  }

  @Test
  public void isEmpty_GivenEmpty_ReturnTrue() throws Exception {
    boolean isEmpty = TextUtils.isEmpty("");

    assertThat(isEmpty).isTrue();
  }

  @Test
  public void isEmpty_GivenNonEmpty_ReturnFalse() throws Exception {
    boolean isEmpty = TextUtils.isEmpty(" ");

    assertThat(isEmpty).isFalse();
  }

  @Test
  public void notEmptyOrNull_ReturnNonEmptyValue() throws Exception {
    assertThat(TextUtils.getNotEmptyOrNullValue("not_empty")).isEqualTo("not_empty");
  }

  @Test
  public void notEmptyOrNull_ReturnNullValue() throws Exception {
    assertThat(TextUtils.getNotEmptyOrNullValue("")).isNull();
  }
}
