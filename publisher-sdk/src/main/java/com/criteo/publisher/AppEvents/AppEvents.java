package com.criteo.publisher.AppEvents;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import com.criteo.publisher.DependencyProvider;
import com.criteo.publisher.Util.AdvertisingInfo;
import com.criteo.publisher.Util.AppEventResponseListener;
import com.criteo.publisher.Util.ApplicationStoppedListener;
import com.criteo.publisher.network.AppEventTask;
import java.util.concurrent.Executor;

public class AppEvents implements AppEventResponseListener, ApplicationStoppedListener {

    private static final String EVENT_INACTIVE = "Inactive";
    private static final String EVENT_ACTIVE = "Active";
    private static final String EVENT_LAUNCH = "Launch";

    private AppEventTask eventTask;
    private Context mContext;
    private int appEventThrottle = -1;
    private long throttleSetTime = 0;

    private final AdvertisingInfo advertisingInfo;

    public AppEvents(@NonNull Context context, @NonNull AdvertisingInfo advertisingInfo) {
        this.mContext = context;
        this.advertisingInfo = advertisingInfo;
        this.eventTask = new AppEventTask(mContext, this, advertisingInfo);
    }

    private void postAppEvent(String eventType) {
        if (appEventThrottle > 0 &&
                System.currentTimeMillis() - throttleSetTime < appEventThrottle * 1000) {
            return;
        }
        if (eventTask.getStatus() == AsyncTask.Status.FINISHED) {
            eventTask = new AppEventTask(mContext, this, advertisingInfo);
        }
        if (eventTask.getStatus() != AsyncTask.Status.RUNNING) {
            Executor threadPoolExecutor = DependencyProvider.getInstance().provideThreadPoolExecutor();
            eventTask.executeOnExecutor(threadPoolExecutor, eventType);
        }
    }

    @Override
    public void setThrottle(int throttle) {
        this.appEventThrottle = throttle;
        this.throttleSetTime = System.currentTimeMillis();
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
