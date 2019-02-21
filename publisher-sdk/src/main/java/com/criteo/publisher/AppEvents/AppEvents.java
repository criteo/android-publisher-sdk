package com.criteo.publisher.AppEvents;

import android.content.Context;
import android.os.AsyncTask;

import com.criteo.publisher.Util.AppEventResponseListener;
import com.criteo.publisher.Util.ApplicationStoppedListener;
import com.criteo.publisher.network.AppEventTask;

public class AppEvents implements AppEventResponseListener, ApplicationStoppedListener {
    private static final String EVENT_INACTIVE = "Inactive";
    private static final String EVENT_ACTIVE = "Active";
    private static final String EVENT_LAUNCH = "Launch";

    private AppEventTask eventTask;
    private Context mContext;
    private int appEventThrottle = -1;
    private long throttleSetTime = 0;

    public AppEvents (Context context) {
        this.mContext = context;
        this.eventTask = new AppEventTask(mContext, this);
    }

    private void postAppEvent(String eventType) {
        if (appEventThrottle > 0 &&
                System.currentTimeMillis() - throttleSetTime < appEventThrottle * 1000) {
            return;
        }
        if (eventTask.getStatus() == AsyncTask.Status.FINISHED) {
            eventTask = new AppEventTask(mContext, this);
        }
        if (eventTask.getStatus() != AsyncTask.Status.RUNNING) {
            eventTask.execute(eventType);
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
        if (eventTask.getStatus() == AsyncTask.Status.RUNNING) {
            eventTask.cancel(false);
        }
    }
}
