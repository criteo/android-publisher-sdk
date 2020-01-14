package com.criteo.publisher.Util;

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

}