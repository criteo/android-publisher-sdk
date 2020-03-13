package com.criteo.publisher.Util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

import android.content.Context;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class DeviceUtilTest {

  private static final String DEVICE_ID_LIMITED = "00000000-0000-0000-0000-000000000000";

  @Mock
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
    when(info.isLimitAdTrackingEnabled(context)).thenReturn(true);

    int isLimited = deviceUtil.isLimitAdTrackingEnabled();

    assertEquals(1, isLimited);
  }

  @Test
  public void isLimitAdTrackingEnabled_GivenNotLimitedAdTrackingEnabled_Return0() throws Exception {
    when(info.isLimitAdTrackingEnabled(context)).thenReturn(false);

    int isLimited = deviceUtil.isLimitAdTrackingEnabled();

    assertEquals(0, isLimited);
  }

  @Test
  public void getAdvertisingId_GivenLimitedAdTracking_ReturnLimitedDeviceId() {
    when(info.isLimitAdTrackingEnabled(context)).thenReturn(true);

    String advertisingId = deviceUtil.getAdvertisingId();

    assertEquals(DEVICE_ID_LIMITED, advertisingId);
  }

  @Test
  public void getAdvertisingId_GivenNotLimitedAdTracking_ReturnFetchedDeviceId() {
    when(info.isLimitAdTrackingEnabled(context)).thenReturn(false);
    when(info.getAdvertisingId(context)).thenReturn("expected");

    String advertisingId = deviceUtil.getAdvertisingId();

    assertEquals("expected", advertisingId);
  }

  @Test
  public void getAdvertisingId_GivenErrorWhenCheckingLimitedAdTracking_ReturnNull()
      throws Exception {
    when(info.isLimitAdTrackingEnabled(context)).thenThrow(RuntimeException.class);

    String advertisingId = deviceUtil.getAdvertisingId();

    assertNull(advertisingId);
  }

  @Test
  public void getAdvertisingId_GivenErrorWhenFetchingDeviceId_ReturnNull() throws Exception {
    when(info.getAdvertisingId(context)).thenThrow(RuntimeException.class);

    String advertisingId = deviceUtil.getAdvertisingId();

    assertNull(advertisingId);
  }
}
