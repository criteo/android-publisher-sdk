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

package com.criteo.publisher.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.criteo.publisher.config.Config;
import com.criteo.publisher.network.PubSdkApi;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

public class WebViewDataTest {

  @Rule
  public MockitoRule mockitoRule = MockitoJUnit.rule();

  @Mock
  private Config config;

  @Mock
  private PubSdkApi api;

  @InjectMocks
  private WebViewData webViewData;

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
