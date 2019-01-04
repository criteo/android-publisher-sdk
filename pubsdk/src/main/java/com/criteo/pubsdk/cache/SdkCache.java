package com.criteo.pubsdk.cache;

import android.util.Pair;

import com.criteo.pubsdk.model.Slot;

import java.util.HashMap;
import java.util.List;

public class SdkCache {
    //act as a delimiter to the CDB call.
    private static final String X = "x";
    private static final int SECOND_TO_MILLI = 1000;
    private HashMap<Pair<String, String>, Slot> slotMap;
    private int ttl;

    public SdkCache() {
        slotMap = new HashMap<>();
    }

    public void add(Slot slot) {
        slotMap.put(new Pair<String, String>(slot.getPlacementId(),
                slot.getWidth() + X + slot.getHeight()), slot);
    }

    public void addAll(List<Slot> slots) {
        for (Slot slot : slots) {
            this.add(slot);
        }
    }

    public void setAdUnits(List<Slot> slots) {
        this.slotMap.clear();
        addAll(slots);
    }

    public Slot getAdUnit(Pair<String, String> placementKey) {
        if (!slotMap.containsKey(placementKey)) {
            return null;
        }
        Slot slot = this.slotMap.get(placementKey);
        this.slotMap.remove(placementKey);
        if (slot.getTtl() * SECOND_TO_MILLI + slot.getTimeOfDownload() < System.currentTimeMillis()) {
            return null;
        }
        return slot;
    }

    public Slot getAdUnit(String placement, int width, int height) {
        return getAdUnit(new Pair<String, String>(placement,
                width + X + height));
    }

    public int getTtl() {
        return ttl;
    }

    public void setTtl(int ttl) {
        this.ttl = ttl;
    }

    public int getItemCount() {
        return slotMap.size();
    }

}
