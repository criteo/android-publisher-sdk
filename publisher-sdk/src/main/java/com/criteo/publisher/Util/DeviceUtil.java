package com.criteo.publisher.Util;

import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Base64;
import android.webkit.WebView;

import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public final class DeviceUtil {
    private static final String CRITEO_LOGGING="CRITEO_LOGGING";
    private DeviceUtil() {
    }

    public static String getUserAgent(Context context) {
        return new WebView(context).getSettings().getUserAgentString();
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
        AdvertisingIdClient.Info adInfo = null;
        try {
            adInfo = AdvertisingIdClient.getAdvertisingIdInfo(context);
            return adInfo.getId();
        } catch (IOException | GooglePlayServicesNotAvailableException | IllegalStateException
                | GooglePlayServicesRepairableException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static int isLimitAdTrackingEnabled(Context context) {
        try {
            AdvertisingIdClient.Info adInfo = AdvertisingIdClient.getAdvertisingIdInfo(context);
            return adInfo.isLimitAdTrackingEnabled() ? 1 : 0;
        } catch (IOException | GooglePlayServicesNotAvailableException
                | GooglePlayServicesRepairableException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static String createDfpCompatibleDisplayUrl(String displayUrl) {
        byte[] byteUrl = displayUrl.getBytes(StandardCharsets.UTF_8);
        String base64Url = Base64.encodeToString(byteUrl, Base64.NO_WRAP);
        String utf8 = StandardCharsets.UTF_8.name();
        try {
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
