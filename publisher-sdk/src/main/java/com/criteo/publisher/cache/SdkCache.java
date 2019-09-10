package com.criteo.publisher.cache;

import com.criteo.publisher.model.AdSize;
import com.criteo.publisher.model.CacheAdUnit;
import com.criteo.publisher.model.Slot;

import java.util.HashMap;
import java.util.List;

public class SdkCache {

    private HashMap<CacheAdUnit, Slot> slotMap;

    public SdkCache() {
        slotMap = new HashMap<>();
    }

    public void add(Slot slot) {
        CacheAdUnit key = new CacheAdUnit(new AdSize(slot.getWidth(), slot.getHeight())
                        , slot.getPlacementId(), slot.isNative());
        slotMap.put(key, slot);
    }

    public void addAll(List<Slot> slots) {
        if (slots == null) {
            return;
        }
        for (Slot slot : slots) {
            this.add(slot);
        }
    }

    public Slot peekAdUnit(CacheAdUnit key) {
        if (!slotMap.containsKey(key)) {
            return null;
        }
        return slotMap.get(key);
    }

    public Slot getAdUnit(CacheAdUnit key) {
        if (!slotMap.containsKey(key)) {
            return null;
        }
        Slot slot = slotMap.get(key);
        slotMap.remove(key);
        return slot;
    }

    public void remove(CacheAdUnit key) {
        slotMap.remove(key);
    }

    public int getItemCount() {
        return slotMap.size();
    }

}
