package com.criteo.pubsdk_android.cdb;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.support.annotation.NonNull;

import com.criteo.pubsdk.model.Publisher;
import com.criteo.pubsdk.model.Slot;
import com.criteo.pubsdk.model.User;

import java.util.ArrayList;

public class CdbViewModel extends AndroidViewModel {

    private CdbLiveData mCbdLiveData;
    private static final String PROFILE_ID = "217";

    public CdbViewModel(@NonNull Application application) {
        super(application);

    }

    public CdbLiveData getDataFromCbd(Publisher publisher) {
        if (mCbdLiveData == null) {
            mCbdLiveData = new CdbLiveData(getApplication());
            mCbdLiveData.loadCbdData(PROFILE_ID, new User(),
                    publisher, getTestSlots());
        }
        return mCbdLiveData;
    }

    private ArrayList<Slot> getTestSlots() {
        ArrayList<Slot> slots = new ArrayList<>();

        Slot slot = new Slot();
        slot.setImpId("ad-unit-1");
        slot.setPlacementId("adunitid");
        slots.add(slot);

        Slot slot1 = new Slot();
        slot1.setImpId("ad-unit-2");
        slot1.setNativeImpression(true);
        slot1.setPlacementId("adunitid");
        slots.add(slot1);
        return slots;
    }
}
