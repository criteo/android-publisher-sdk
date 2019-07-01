package com.criteo.publisher.Util;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Base64;
import android.webkit.WebView;
import com.criteo.publisher.model.AdSize;
import com.criteo.publisher.model.ScreenSize;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;

public final class DeviceUtil {

    private static final String CRITEO_LOGGING = "CRITEO_LOGGING";
    private static final String DEVICE_ID_LIMITED = "00000000-0000-0000-0000-000000000000";

    private static AdSize sizePortrait;
    private static AdSize sizeLandscape;

    private DeviceUtil() {
    }

    public static String getUserAgent(Context context) {
        WebView webView = new WebView(context);
        String userAgent = webView.getSettings().getUserAgentString();
        webView.destroy();
        webView = null;
        return userAgent;
    }

    public static void setScreenSize(int screenWidth, int screenHeight,
            ArrayList<ScreenSize> supportedPortraitScreenSizes,
            ArrayList<ScreenSize> supportedLandscapeScreenSizes) {

        Collections.sort(supportedPortraitScreenSizes);
        sizePortrait = getNearestAdSize(supportedPortraitScreenSizes, Configuration.ORIENTATION_PORTRAIT, screenWidth);

        Collections.sort(supportedLandscapeScreenSizes);
        sizeLandscape = getNearestAdSize(supportedLandscapeScreenSizes, Configuration.ORIENTATION_LANDSCAPE,
                screenHeight);
    }

    public static AdSize getSizePortrait() {
        return sizePortrait;
    }

    public static AdSize getSizeLandscape() {
        return sizeLandscape;
    }

    private static AdSize getNearestAdSize(ArrayList<ScreenSize> screenSizes, int orientation, int screenWidth) {

        AdSize adSize = new AdSize();

        if (screenWidth <= screenSizes.get(0).getWidth()) {
            adSize = useThisSize(screenSizes.get(0));
        } else if (screenWidth >= screenSizes.get(screenSizes.size() - 1).getWidth()) {
            adSize = useThisSize(screenSizes.get(screenSizes.size() - 1));
        } else {
            for (int i = 1; i < screenSizes.size(); i++) {
                if (screenWidth < screenSizes.get(i).getWidth() && screenWidth >= screenSizes.get(i - 1).getWidth()) {
                    adSize = useThisSize(screenSizes.get(i - 1));
                    break;
                }
            }

        }

        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            sizePortrait = adSize;
        } else if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            sizeLandscape = adSize;
        }

        return adSize;
    }

    private static AdSize useThisSize(ScreenSize screenSize) {
        AdSize adSize = new AdSize(screenSize.getWidth(), screenSize.getHeight());
        return adSize;
    }

    public static String getDeviceModel() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.toLowerCase().startsWith(manufacturer.toLowerCase())) {
            return model;
        } else {
            return manufacturer + " " + model;
        }
    }

    public static boolean isAeroplaneModeOn(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return Settings.System.getInt(context.getContentResolver(),
                    Settings.System.AIRPLANE_MODE_ON, 0) != 0;
        } else {
            return Settings.Global.getInt(context.getContentResolver(),
                    Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
        }
    }

    public static boolean hasPlayServices(Context context) {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int status = googleApiAvailability.isGooglePlayServicesAvailable(context);
        return status == ConnectionResult.SUCCESS;
    }

    public static String getAdvertisingId(Context context) {
        if (AdvertisingInfo.getInstance().isLimitAdTrackingEnabled(context)) {
            return DEVICE_ID_LIMITED;
        }
        return AdvertisingInfo.getInstance().getAdvertisingId(context);
    }

    public static int isLimitAdTrackingEnabled(Context context) {
        return AdvertisingInfo.getInstance().isLimitAdTrackingEnabled(context) ? 1 : 0;
    }

    public static String createDfpCompatibleDisplayUrl(String displayUrl) {
        if (TextUtils.isEmpty(displayUrl)) {
            return null;
        }
        try {
            byte[] byteUrl = displayUrl.getBytes(StandardCharsets.UTF_8);
            String base64Url = Base64.encodeToString(byteUrl, Base64.NO_WRAP);
            String utf8 = StandardCharsets.UTF_8.name();
            return URLEncoder.encode(URLEncoder.encode(base64Url, utf8), utf8).toString();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean isLoggingEnabled() {
        String log = System.getenv(CRITEO_LOGGING);
        return TextUtils.isEmpty(log) ? false : Boolean.parseBoolean(log);
    }

}
