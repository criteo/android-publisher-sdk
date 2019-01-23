package com.criteo.pubsdk;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;
import android.arch.lifecycle.ProcessLifecycleOwner;
import android.content.Context;
import android.text.TextUtils;

import com.criteo.pubsdk.model.AdUnit;
import com.criteo.pubsdk.model.Slot;
import com.google.android.gms.ads.doubleclick.PublisherAdRequest;

import java.util.ArrayList;
import java.util.List;

public final class Criteo implements LifecycleObserver {

    private static Criteo criteo;
    private Context mContext;
    private BidManager bidManager;

    public static Criteo init(Context context, List<AdUnit> adUnits, int networkId) {
        synchronized (Criteo.class) {
            if (criteo == null) {
                criteo = new Criteo(context, adUnits, networkId);
            }
        }
        return criteo;
    }

    private Criteo(Context context, List<AdUnit> adUnits, int networkId) {
        if (context == null) throw new IllegalArgumentException("Application context requires.");
        if (adUnits == null || adUnits.size() == 0)
            throw new IllegalArgumentException("Add units require.");
        for (AdUnit adUnit : adUnits) {
            if (TextUtils.isEmpty(adUnit.getPlacementId()) || adUnit.getSize() == null
                    || adUnit.getSize().getWidth() <= 0 || adUnit.getSize().getHight() <= 0) {
                throw new IllegalArgumentException("Invalid add request");
            }
        }
        if (networkId == 0) throw new IllegalArgumentException("Network identity is require.");
        this.mContext = context;
        this.bidManager = new BidManager(context, networkId, adUnits);
        ProcessLifecycleOwner.get().getLifecycle()
                .addObserver(this);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void appStart() {
        bidManager.prefetch();

    }

    public PublisherAdRequest.Builder enrich(PublisherAdRequest.Builder request, AdUnit adUnit) {
        return bidManager.enrichBid(request, adUnit);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void appPause() {
        bidManager.cancelLoad();
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
