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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;
import android.view.WindowMetrics;
import com.criteo.publisher.model.AdSize;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import org.junit.Before;
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

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private WindowManager windowManager;

  @InjectMocks
  private DeviceUtil deviceUtil;

  private DisplayMetrics metrics;

  @Before
  public void setUp() {
    metrics = new DisplayMetrics();
    when(context.getResources().getDisplayMetrics()).thenReturn(metrics);

    when(context.getSystemService(Context.WINDOW_SERVICE)).thenReturn(windowManager);
  }

  @Test
  public void isTablet_GivenDeviceInPortraitAndWidthBelow600dp_ReturnFalse() throws Exception {
    metrics.density = 1.25f;
    metrics.widthPixels = 749; // 600dp is 750pixel
    metrics.heightPixels = 1000;

    boolean isTablet = deviceUtil.isTablet();

    assertThat(isTablet).isFalse();
  }

  @Test
  public void isTablet_GivenDeviceInPortraitAndWidthAboveOrEqualTo600dp_ReturnTrue() throws Exception {
    metrics.density = 1.25f;
    metrics.widthPixels = 750; // 600dp is 750pixel
    metrics.heightPixels = 1000;

    boolean isTablet = deviceUtil.isTablet();

    assertThat(isTablet).isTrue();
  }

  @Test
  public void isTablet_GivenDeviceInLandscapeAndHeightBelow600dp_ReturnFalse() throws Exception {
    metrics.density = 1.25f;
    metrics.widthPixels = 1000;
    metrics.heightPixels = 749; // 600dp is 750pixel

    boolean isTablet = deviceUtil.isTablet();

    assertThat(isTablet).isFalse();
  }

  @Test
  public void isTablet_GivenDeviceInLandscapeAndHeightAboveOrEqualTo600dp_ReturnTrue() throws Exception {
    metrics.density = 1.25f;
    metrics.widthPixels = 1000;
    metrics.heightPixels = 750; // 600dp is 750pixel

    boolean isTablet = deviceUtil.isTablet();

    assertThat(isTablet).isTrue();
  }

  @Test
  public void getRealScreenSize_GivenApiLevel29_ShouldReturnScreenSizeInDp()
      throws Exception {
    setSdkIntVersion(29);
    metrics.density = 2f;

    Display display = windowManager.getDefaultDisplay();
    doAnswer(invocation -> {
      Object[] args = invocation.getArguments();
      Point point = (Point) args[0];
      point.x = 100;
      point.y = 100;
      return null;
    }).when(display).getRealSize(any());

    AdSize screenSize = deviceUtil.getRealScreenSize();
    assertThat(screenSize.getWidth()).isEqualTo(50);
    assertThat(screenSize.getHeight()).isEqualTo(50);
  }

  @Test
  public void getRealScreenSize_GivenApiLevel30_ShouldReturnScreenSizeInDp()
      throws Exception {
    setSdkIntVersion(30);
    metrics.density = 2f;

    WindowMetrics windowMetrics = mock(WindowMetrics.class, RETURNS_DEEP_STUBS);
    Rect bounds = windowMetrics.getBounds();
    when(bounds.height()).thenReturn(100);
    when(bounds.width()).thenReturn(100);
    when(windowManager.getMaximumWindowMetrics()).thenReturn(windowMetrics);

    AdSize screenSize = deviceUtil.getRealScreenSize();
    assertThat(screenSize.getWidth()).isEqualTo(50);
    assertThat(screenSize.getHeight()).isEqualTo(50);
  }

  static void setSdkIntVersion(int newValue) throws Exception {
    Field sdkIntField = Build.VERSION.class.getField("SDK_INT");
    sdkIntField.setAccessible(true);

    Field modifiersField = Field.class.getDeclaredField("modifiers");
    modifiersField.setAccessible(true);
    modifiersField.setInt(sdkIntField, sdkIntField.getModifiers() & ~Modifier.FINAL);

    sdkIntField.set(null, newValue);
  }

}
