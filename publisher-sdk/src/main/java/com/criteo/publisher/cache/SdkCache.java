package com.criteo.publisher.cache;

import android.util.Pair;

import com.criteo.publisher.model.Slot;

import java.util.HashMap;
import java.util.List;

public class SdkCache {
    private HashMap<Pair<String, String>, Slot> slotMap;

    public SdkCache() {
        slotMap = new HashMap<>();
    }

    public void add(Slot slot) {
        slotMap.put(new Pair<>(slot.getPlacementId(),
                slot.getFormattedSize()), slot);
    }

    public void addAll(List<Slot> slots) {
        if (slots == null) {
            return;
        }
        for (Slot slot : slots) {
            this.add(slot);
        }
    }

    public void setAdUnits(List<Slot> slots) {
        this.slotMap.clear();
        addAll(slots);
    }

    public Slot peekAdUnit(String placement, String formattedSize) {
        Pair<String, String> placementKey = new Pair<String, String>(placement, formattedSize);
        if (!slotMap.containsKey(placementKey)) {
            return null;
        }
        return slotMap.get(placementKey);
    }

    public Slot getAdUnit(String placement, String formattedSize) {
        Pair<String, String> placementKey = new Pair<String, String>(placement, formattedSize);
        if (!slotMap.containsKey(placementKey)) {
            return null;
        }
        Slot slot = slotMap.get(placementKey);
        slotMap.remove(placementKey);
        return slot;
    }

    public void remove(String placement, String formattedSize) {
        Pair<String, String> placementKey = new Pair<String, String>(placement, formattedSize);
        slotMap.remove(placementKey);
    }

    public int getItemCount() {
        return slotMap.size();
    }

}
