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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.util.DisplayMetrics;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class DeviceUtilTest {

  private static final String DEVICE_ID_LIMITED = "00000000-0000-0000-0000-000000000000";

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private Context context;

  @Mock
  private AdvertisingInfo info;

  private DeviceUtil deviceUtil;

  @Before
  public void setup() throws Exception {
    MockitoAnnotations.initMocks(this);
    deviceUtil = new DeviceUtil(context, info);
  }

  @Test
  public void isLimitAdTrackingEnabled_GivenLimitedAdTrackingEnabled_Return1() throws Exception {
    when(info.isLimitAdTrackingEnabled()).thenReturn(true);

    int isLimited = deviceUtil.isLimitAdTrackingEnabled();

    assertEquals(1, isLimited);
  }

  @Test
  public void isLimitAdTrackingEnabled_GivenNotLimitedAdTrackingEnabled_Return0() throws Exception {
    when(info.isLimitAdTrackingEnabled()).thenReturn(false);

    int isLimited = deviceUtil.isLimitAdTrackingEnabled();

    assertEquals(0, isLimited);
  }

  @Test
  public void getAdvertisingId_GivenLimitedAdTracking_ReturnLimitedDeviceId() {
    when(info.isLimitAdTrackingEnabled()).thenReturn(true);

    String advertisingId = deviceUtil.getAdvertisingId();

    assertEquals(DEVICE_ID_LIMITED, advertisingId);
  }

  @Test
  public void getAdvertisingId_GivenNotLimitedAdTracking_ReturnFetchedDeviceId() {
    when(info.isLimitAdTrackingEnabled()).thenReturn(false);
    when(info.getAdvertisingId()).thenReturn("expected");

    String advertisingId = deviceUtil.getAdvertisingId();

    assertEquals("expected", advertisingId);
  }

  @Test
  public void getAdvertisingId_GivenErrorWhenCheckingLimitedAdTracking_ReturnNull()
      throws Exception {
    when(info.isLimitAdTrackingEnabled()).thenThrow(RuntimeException.class);

    String advertisingId = deviceUtil.getAdvertisingId();

    assertNull(advertisingId);
  }

  @Test
  public void getAdvertisingId_GivenErrorWhenFetchingDeviceId_ReturnNull() throws Exception {
    when(info.getAdvertisingId()).thenThrow(RuntimeException.class);

    String advertisingId = deviceUtil.getAdvertisingId();

    assertNull(advertisingId);
  }

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
