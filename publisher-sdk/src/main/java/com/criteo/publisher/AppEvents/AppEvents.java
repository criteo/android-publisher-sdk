package com.criteo.publisher.AppEvents;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import com.criteo.publisher.Clock;
import com.criteo.publisher.DependencyProvider;
import com.criteo.publisher.Util.AppEventResponseListener;
import com.criteo.publisher.Util.ApplicationStoppedListener;
import com.criteo.publisher.Util.DeviceUtil;
import com.criteo.publisher.Util.UserPrivacyUtil;
import com.criteo.publisher.network.AppEventTask;
import com.criteo.publisher.network.PubSdkApi;
import java.util.concurrent.Executor;

public class AppEvents implements AppEventResponseListener, ApplicationStoppedListener {

    private static final String EVENT_INACTIVE = "Inactive";
    private static final String EVENT_ACTIVE = "Active";
    private static final String EVENT_LAUNCH = "Launch";

    private AppEventTask eventTask;
    private final Context mContext;
    private int appEventThrottle = -1;
    private long throttleSetTime = 0;

    private final DeviceUtil deviceUtil;
    private final Clock clock;
    private final PubSdkApi api;
    private final UserPrivacyUtil userPrivacyUtil;

    public AppEvents(
        @NonNull Context context,
        @NonNull DeviceUtil deviceUtil,
        @NonNull Clock clock,
        @NonNull PubSdkApi api,
        @NonNull UserPrivacyUtil userPrivacyUtil
    ) {
        this.mContext = context;
        this.deviceUtil = deviceUtil;
        this.clock = clock;
        this.api = api;
        this.eventTask = new AppEventTask(mContext, this, deviceUtil, api);
        this.userPrivacyUtil = userPrivacyUtil;
    }

    private void postAppEvent(String eventType) {
        if (userPrivacyUtil.isCCPAConsentGiven()) {
            if (appEventThrottle > 0 &&
                clock.getCurrentTimeInMillis() - throttleSetTime < appEventThrottle * 1000) {
                return;
            }
            if (eventTask.getStatus() == AsyncTask.Status.FINISHED) {
                eventTask = new AppEventTask(mContext, this, deviceUtil, api);
            }
            if (eventTask.getStatus() != AsyncTask.Status.RUNNING) {
                Executor threadPoolExecutor = DependencyProvider.getInstance().provideThreadPoolExecutor();
                eventTask.executeOnExecutor(threadPoolExecutor, eventType);
            }
        }
    }

    @Override
    public void setThrottle(int throttle) {
        this.appEventThrottle = throttle;
        this.throttleSetTime = clock.getCurrentTimeInMillis();
    }

    public void sendLaunchEvent() {
        postAppEvent(EVENT_LAUNCH);
    }

    public void sendActiveEvent() {
        postAppEvent(EVENT_ACTIVE);
    }

    public void sendInactiveEvent() {
        postAppEvent(EVENT_INACTIVE);
    }

    @Override
    public void onApplicationStopped() {
    }
}
