package com.criteo.publisher.Util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import com.criteo.publisher.model.ScreenSize;
import java.lang.reflect.Field;
import java.util.ArrayList;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class DeviceUtilTest {

    private static final String DEVICE_ID_LIMITED = "00000000-0000-0000-0000-000000000000";

    private ArrayList<ScreenSize> screenSizesPortrait;

    private ArrayList<ScreenSize> screenSizesLandscape;

    private Context context;

    @Before
    public void setup() {
        context = InstrumentationRegistry.getContext();
        screenSizesPortrait = new ArrayList<>();
        screenSizesPortrait.add(new ScreenSize(360, 540));
        screenSizesPortrait.add(new ScreenSize(100, 200));
        screenSizesPortrait.add(new ScreenSize(200, 350));
        screenSizesPortrait.add(new ScreenSize(320, 600));
        screenSizesPortrait.add(new ScreenSize(640, 960));
        screenSizesPortrait.add(new ScreenSize(250, 400));

        screenSizesLandscape = new ArrayList<>();
        screenSizesLandscape.add(new ScreenSize(200, 100));
        screenSizesLandscape.add(new ScreenSize(600, 320));
        screenSizesLandscape.add(new ScreenSize(400, 250));
        screenSizesLandscape.add(new ScreenSize(350, 200));
        screenSizesLandscape.add(new ScreenSize(540, 360));
        screenSizesLandscape.add(new ScreenSize(960, 640));

    }

    @Test
    public void createDfpCompatibleDisplayUrlTest() {
        String displayUrl = "https://ads.us.criteo.com/delivery/r/ajs.php?did=5c560a19383b7ad93bb37508deb03a00&u=%7CHX1eM0zpPitVbf0xT24vaM6U4AiY1TeYgfjDUVVbdu4%3D%7C&c1=eG9IAZIK2MKnlif_A3VZ1-8PEx5_bFVofQVrPPiKhda8JkCsKWBsD2zYvC_F9owWsiKQANPjzJs2iM3m5bCHei3w1zNKxtB3Cx_TBleNKtL5VK1aqyK68XTa0A43qlwLNaStT5NXB3Mz7kx6fDZ20Rh6eAGAW2F9SXVN_7xiLgP288-4OqtK-R7pziZDS04LRUhkL7ohLmAFFyVuwQTREHbpx-4NoonsiQRHKn7ZkuIqZR_rqEewHQ2YowxbI3EOowxo6OV50faWCc7QO5M388FHv8NxeOgOH03LHZT_a2PEKF1xh0-G_qdu5wiyGjJYyPEoNVxB0OaEnDaFVtM7cVaHDm4jrjKlfFhtIGuJb8mg2EeHN0mhUL_0eyv9xWUUQ6osYh3B-jiawHq4592kDDCpS2kYYeqR073IOoRNFNRCR7Fnl0yhIA";
        String encodedValue = "aHR0cHM6Ly9hZHMudXMuY3JpdGVvLmNvbS9kZWxpdmVyeS9yL2Fqcy5waHA%252FZGlkPTVjNTYwYTE5MzgzYjdhZDkzYmIzNzUwOGRlYjAzYTAwJnU9JTdDSFgxZU0wenBQaXRWYmYweFQyNHZhTTZVNEFpWTFUZVlnZmpEVVZWYmR1NCUzRCU3QyZjMT1lRzlJQVpJSzJNS25saWZfQTNWWjEtOFBFeDVfYkZWb2ZRVnJQUGlLaGRhOEprQ3NLV0JzRDJ6WXZDX0Y5b3dXc2lLUUFOUGp6SnMyaU0zbTViQ0hlaTN3MXpOS3h0QjNDeF9UQmxlTkt0TDVWSzFhcXlLNjhYVGEwQTQzcWx3TE5hU3RUNU5YQjNNejdreDZmRFoyMFJoNmVBR0FXMkY5U1hWTl83eGlMZ1AyODgtNE9xdEstUjdwemlaRFMwNExSVWhrTDdvaExtQUZGeVZ1d1FUUkVIYnB4LTROb29uc2lRUkhLbjdaa3VJcVpSX3JxRWV3SFEyWW93eGJJM0VPb3d4bzZPVjUwZmFXQ2M3UU81TTM4OEZIdjhOeGVPZ09IMDNMSFpUX2EyUEVLRjF4aDAtR19xZHU1d2l5R2pKWXlQRW9OVnhCME9hRW5EYUZWdE03Y1ZhSERtNGpyaktsZkZodElHdUpiOG1nMkVlSE4wbWhVTF8wZXl2OXhXVVVRNm9zWWgzQi1qaWF3SHE0NTkya0REQ3BTMmtZWWVxUjA3M0lPb1JORk5SQ1I3Rm5sMHloSUE%253D";
        assertEquals(DeviceUtil.createDfpCompatibleDisplayUrl(displayUrl), encodedValue);
    }

    @Test
    public void getAdvertisingIdAndLimitedTest() {
        assertNotNull(DeviceUtil.getAdvertisingId(context));
        assertNotNull(DeviceUtil.isLimitAdTrackingEnabled(context));
    }

    @Test
    public void getAdvertisingIdAdLimited() {
        AdvertisingInfo info = mock(AdvertisingInfo.class);
        setMock(info);
        when(info.isLimitAdTrackingEnabled(context)).thenReturn(true);
        Assert.assertTrue(info.isLimitAdTrackingEnabled(context));
        Assert.assertEquals(DEVICE_ID_LIMITED, DeviceUtil.getAdvertisingId(context));
    }

    private void setMock(AdvertisingInfo mock) {
        try {
            Field instance = AdvertisingInfo.class.getDeclaredField("advertisingInfo");
            instance.setAccessible(true);
            instance.set(instance, mock);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @Test
    public void getDeviceIdForAppEventTest() {
        Assert.assertNotEquals(DEVICE_ID_LIMITED,
                DeviceUtil.getAdvertisingId(context));
    }

    //TODO Create Intrumentation Test , change settings as Limited and test


    @Test
    public void getDeviceModelTest() {
        assertNotNull(DeviceUtil.getDeviceModel());
    }

    @Test
    public void testgetNearestAdSizeLandscape() {
        DeviceUtil.setScreenSize(360, 692, screenSizesPortrait, screenSizesLandscape);
        Assert.assertEquals(DeviceUtil.getSizePortrait().getWidth(), 360);
        Assert.assertEquals(DeviceUtil.getSizePortrait().getHeight(), 540);
        Assert.assertEquals(DeviceUtil.getSizeLandscape().getWidth(), 600);
        Assert.assertEquals(DeviceUtil.getSizeLandscape().getHeight(), 320);

        DeviceUtil.setScreenSize(120, 350, screenSizesPortrait, screenSizesLandscape);
        Assert.assertEquals(DeviceUtil.getSizePortrait().getWidth(), 100);
        Assert.assertEquals(DeviceUtil.getSizePortrait().getHeight(), 200);
        Assert.assertEquals(DeviceUtil.getSizeLandscape().getWidth(), 350);
        Assert.assertEquals(DeviceUtil.getSizeLandscape().getHeight(), 200);

        DeviceUtil.setScreenSize(600, 900, screenSizesPortrait, screenSizesLandscape);
        Assert.assertEquals(DeviceUtil.getSizePortrait().getWidth(), 360);
        Assert.assertEquals(DeviceUtil.getSizePortrait().getHeight(), 540);
        Assert.assertEquals(DeviceUtil.getSizeLandscape().getWidth(), 600);
        Assert.assertEquals(DeviceUtil.getSizeLandscape().getHeight(), 320);
    }
}

