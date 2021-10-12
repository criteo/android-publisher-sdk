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
import static org.mockito.Mockito.when;

import android.content.Context;
import android.util.DisplayMetrics;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

public class DeviceUtilTest {

  @Rule
  public MockitoRule mockitoRule = MockitoJUnit.rule();

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private Context context;

  @InjectMocks
  private DeviceUtil deviceUtil;

  @Test
  public void isTablet_GivenDeviceInPortraitAndWidthBelow600dp_ReturnFalse() throws Exception {
    DisplayMetrics metrics = new DisplayMetrics();
    when(context.getResources().getDisplayMetrics()).thenReturn(metrics);

    metrics.density = 1.25f;
    metrics.widthPixels = 749; // 600dp is 750pixel
    metrics.heightPixels = 1000;

    boolean isTablet = deviceUtil.isTablet();

    assertThat(isTablet).isFalse();
  }

  @Test
  public void isTablet_GivenDeviceInPortraitAndWidthAboveOrEqualTo600dp_ReturnTrue() throws Exception {
    DisplayMetrics metrics = new DisplayMetrics();
    when(context.getResources().getDisplayMetrics()).thenReturn(metrics);

    metrics.density = 1.25f;
    metrics.widthPixels = 750; // 600dp is 750pixel
    metrics.heightPixels = 1000;

    boolean isTablet = deviceUtil.isTablet();

    assertThat(isTablet).isTrue();
  }

  @Test
  public void isTablet_GivenDeviceInLandscapeAndHeightBelow600dp_ReturnFalse() throws Exception {
    DisplayMetrics metrics = new DisplayMetrics();
    when(context.getResources().getDisplayMetrics()).thenReturn(metrics);

    metrics.density = 1.25f;
    metrics.widthPixels = 1000;
    metrics.heightPixels = 749; // 600dp is 750pixel

    boolean isTablet = deviceUtil.isTablet();

    assertThat(isTablet).isFalse();
  }

  @Test
  public void isTablet_GivenDeviceInLandscapeAndHeightAboveOrEqualTo600dp_ReturnTrue() throws Exception {
    DisplayMetrics metrics = new DisplayMetrics();
    when(context.getResources().getDisplayMetrics()).thenReturn(metrics);

    metrics.density = 1.25f;
    metrics.widthPixels = 1000;
    metrics.heightPixels = 750; // 600dp is 750pixel

    boolean isTablet = deviceUtil.isTablet();

    assertThat(isTablet).isTrue();
  }

}
