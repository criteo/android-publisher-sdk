package com.criteo.publisher.Util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class URLUtilTest {

  @Test
  public void isValidUrl_GivenNullUrl_ReturnFalse() throws Exception {
    assertThat(URLUtil.isValidUrl(null)).isFalse();
  }

  @Test
  public void isValidUrl_GivenEmptyUrl_ReturnFalse() throws Exception {
    assertThat(URLUtil.isValidUrl("")).isFalse();
  }

  @Test
  public void isValidUrl_GivenUrlWithoutScheme_ReturnFalse() throws Exception {
    assertThat(URLUtil.isValidUrl("urlWithoutScheme")).isFalse();
  }

  @Test
  public void isValidUrl_GivenUrlWithSchemeOtherThanHttpOrHttps_ReturnFalse() throws Exception {
    assertThat(URLUtil.isValidUrl("file://url")).isFalse();
  }

  @Test
  public void isValidUrl_GivenUrlWithHttpScheme_ReturnTrue() throws Exception {
    assertThat(URLUtil.isValidUrl("http://url")).isTrue();
  }

  @Test
  public void isValidUrl_GivenUrlWithHttpsScheme_ReturnTrue() throws Exception {
    assertThat(URLUtil.isValidUrl("https://url")).isTrue();
  }

}