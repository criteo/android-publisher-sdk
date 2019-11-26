package com.criteo.publisher.Util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

import android.content.Context;
import com.criteo.publisher.DependencyProvider;
import java.lang.reflect.Field;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class DeviceUtilTest {
  private static final String DEVICE_ID_LIMITED = "00000000-0000-0000-0000-000000000000";

  @Mock
  private Context context;

  @Mock
  AdvertisingInfo info;

  @Before
  public void setup() throws Exception {
    MockitoAnnotations.initMocks(this);
    setAdvertisingInfoMock();
  }

  @After
  public void tearDown() throws Exception {
    removeAdvertisingInfoMock();
  }

  private void setAdvertisingInfoMock() throws Exception {
    setAdvertisingInfoSingletonInstance(info);

    assertEquals(info, DependencyProvider.getInstance().provideAdvertisingInfo());
  }

  private void removeAdvertisingInfoMock() throws Exception {
    setAdvertisingInfoSingletonInstance(null);
  }

  private void setAdvertisingInfoSingletonInstance(AdvertisingInfo newInstance)
      throws NoSuchFieldException, IllegalAccessException {
    Field instance = AdvertisingInfo.class.getDeclaredField("advertisingInfo");
    instance.setAccessible(true);
    instance.set(instance, newInstance);
  }

  @Test
  public void isLimitAdTrackingEnabled_GivenLimitedAdTrackingEnabled_Return1() throws Exception {
    when(info.isLimitAdTrackingEnabled(context)).thenReturn(true);

    int isLimited = DeviceUtil.isLimitAdTrackingEnabled(context);

    assertEquals(1, isLimited);
  }

  @Test
  public void isLimitAdTrackingEnabled_GivenNotLimitedAdTrackingEnabled_Return0() throws Exception {
    when(info.isLimitAdTrackingEnabled(context)).thenReturn(false);

    int isLimited = DeviceUtil.isLimitAdTrackingEnabled(context);

    assertEquals(0, isLimited);
  }

  @Test
  public void getAdvertisingId_GivenLimitedAdTracking_ReturnLimitedDeviceId() {
    when(info.isLimitAdTrackingEnabled(context)).thenReturn(true);

    String advertisingId = DeviceUtil.getAdvertisingId(context, info);

    assertEquals(DEVICE_ID_LIMITED, advertisingId);
  }

  @Test
  public void getAdvertisingId_GivenNotLimitedAdTracking_ReturnFetchedDeviceId() {
    when(info.isLimitAdTrackingEnabled(context)).thenReturn(false);
    when(info.getAdvertisingId(context)).thenReturn("expected");

    String advertisingId = DeviceUtil.getAdvertisingId(context, info);

    assertEquals("expected", advertisingId);
  }

  @Test
  public void getAdvertisingId_GivenErrorWhenCheckingLimitedAdTracking_ReturnNull() throws Exception {
    when(info.isLimitAdTrackingEnabled(context)).thenThrow(RuntimeException.class);

    String advertisingId = DeviceUtil.getAdvertisingId(context, info);

    assertNull(advertisingId);
  }

  @Test
  public void getAdvertisingId_GivenErrorWhenFetchingDeviceId_ReturnNull() throws Exception {
    when(info.getAdvertisingId(context)).thenThrow(RuntimeException.class);

    String advertisingId = DeviceUtil.getAdvertisingId(context, info);

    assertNull(advertisingId);
  }
}
