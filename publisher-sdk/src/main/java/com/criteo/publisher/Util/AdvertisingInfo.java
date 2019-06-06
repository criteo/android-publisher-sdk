package com.criteo.publisher.Util;

import android.content.Context;
import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import java.io.IOException;

public class AdvertisingInfo {

    private static AdvertisingInfo advertisingInfo;

    private AdvertisingInfo() {
    }

    public static AdvertisingInfo getInstance() {
        if (advertisingInfo == null) {
            advertisingInfo = new AdvertisingInfo();
        }
        return advertisingInfo;
    }

    public String getAdvertisingId(Context context) {
        try {
            return AdvertisingIdClient.getAdvertisingIdInfo(context).getId();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        } catch (GooglePlayServicesRepairableException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean isLimitAdTrackingEnabled(Context context) {
        try {
            return AdvertisingIdClient.getAdvertisingIdInfo(context).isLimitAdTrackingEnabled();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        } catch (GooglePlayServicesRepairableException e) {
            e.printStackTrace();
        }
        return true;
    }
}
