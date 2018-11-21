package com.criteo.pubsdk;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;
import android.arch.lifecycle.ProcessLifecycleOwner;
import android.content.Context;
import android.os.AsyncTask;
import com.criteo.pubsdk.model.Publisher;
import com.criteo.pubsdk.model.Slot;
import com.criteo.pubsdk.model.User;
import com.criteo.pubsdk.network.CdbDownloadTask;

import java.util.ArrayList;
import java.util.List;

public final class Criteo implements LifecycleObserver {

    private static final String PROFILE_ID = "217";
    private static Criteo criteo;
    private Context mContext;

    private CdbDownloadTask mTask;

    public static Criteo init(Context context) {
        synchronized (Criteo.class) {
            if (criteo == null) {
                criteo = new Criteo(context);
            }
        }
        return criteo;
    }

    private Criteo(Context context) {
        this.mContext = context;
        mTask = new CdbDownloadTask(context);
        ProcessLifecycleOwner.get().getLifecycle()
                .addObserver(this);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void appStart() {
        if (mTask.getStatus() != AsyncTask.Status.RUNNING) {
            mTask.execute(PROFILE_ID, new User(mContext), new Publisher(mContext), getTestSlots());
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void appPause() {
        if (mTask.getStatus() == AsyncTask.Status.RUNNING) {
            mTask.cancel(true);
        }
    }

    private List<Slot> getTestSlots() {
        List<Slot> slots = new ArrayList<>();

        Slot slot = new Slot();
        slot.setZoneId(775591);
        slot.setImpId("ad-unit-1");
        slots.add(slot);

        Slot slot1 = new Slot();
        slot1.setZoneId(497747);
        slot1.setImpId("ad-unit-2");
        slots.add(slot1);
        return slots;
    }
}
