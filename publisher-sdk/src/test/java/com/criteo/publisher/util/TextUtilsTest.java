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
