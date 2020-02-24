package com.criteo.publisher.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class WebViewDataTest {

  @Mock
  private Config config;

  private WebViewData webViewData;

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);

    webViewData = new WebViewData(config);
  }

  @Test
  public void setContent_GivenConfigMacro_ReplacePlaceholderByTagData() {
    when(config.getAdTagDataMacro()).thenReturn("%myMacro%");
    when(config.getAdTagDataMode()).thenReturn("myContent: %myMacro%");

    webViewData.setContent("myTagData");

    assertThat(webViewData.getContent()).isEqualTo("myContent: myTagData");
  }

  @Test
  public void refresh_GivenLoading_CleanContentAndState() throws Exception {
    webViewData.downloadLoading();

    webViewData.refresh();

    assertCleanContentAndState();
  }

  @Test
  public void refresh_GivenLoaded_CleanContentAndState() throws Exception {
    webViewData.downloadSucceeded();

    webViewData.refresh();

    assertCleanContentAndState();
  }

  private void assertCleanContentAndState() {
    assertThat(webViewData.isLoading()).isFalse();
    assertThat(webViewData.isLoaded()).isFalse();
    assertThat(webViewData.getContent()).isEmpty();
  }
}
