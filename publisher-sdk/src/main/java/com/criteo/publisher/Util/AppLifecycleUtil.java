package com.criteo.publisher.Util;

import android.app.Application;
import android.app.Activity;
import android.os.Bundle;

import com.criteo.publisher.AppEvents.AppEvents;
import com.criteo.publisher.BidManager;

public class AppLifecycleUtil implements Application.ActivityLifecycleCallbacks {
    private final AppEvents appEvents;
    private final BidManager bidManager;
    private int started;
    private int resumed;
    private boolean transitionPossible;
    public AppLifecycleUtil(Application application, AppEvents appEvents, BidManager bidmanager) {
        this.appEvents = appEvents;
        this.bidManager = bidmanager;
        application.registerActivityLifecycleCallbacks(this);
        started = 0;
        resumed = 0;
        transitionPossible = false;
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle bundle) {
        // intentionally left blank
    }

    @Override
    public void onActivityStarted(Activity activity) {
        if (started == 0) {
            appEvents.sendLaunchEvent();
        }
        started += 1;
    }

    @Override
    public void onActivityResumed(Activity activity) {
        if(resumed == 0 && !transitionPossible) {
            appEvents.sendActiveEvent();
        }
        transitionPossible = false;
        resumed += 1;
    }

    @Override
    public void onActivityPaused(Activity activity) {
        transitionPossible = true;
        resumed -= 1;
    }

    @Override
    public void onActivityStopped(Activity activity) {
        if(started == 1) {
            // All transitions pause and stop activities
            if(transitionPossible && resumed == 0) {
                appEvents.sendInactiveEvent();
            }
            appEvents.onApplicationStopped();
            bidManager.onApplicationStopped();
        }
        transitionPossible = false;
        started -= 1;
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {
        // intentionally left blank
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        // intentionally left blank
    }
}
