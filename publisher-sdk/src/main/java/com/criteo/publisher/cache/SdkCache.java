package com.criteo.publisher.cache;

import static com.criteo.publisher.Util.AdUnitType.CRITEO_BANNER;
import static com.criteo.publisher.Util.AdUnitType.CRITEO_CUSTOM_NATIVE;
import static com.criteo.publisher.Util.AdUnitType.CRITEO_INTERSTITIAL;

import com.criteo.publisher.Util.AdUnitType;
import com.criteo.publisher.Util.DeviceUtil;
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
        if (slot != null && slot.isValid()) {
            AdUnitType adUnitType = findAdUnitType(slot);
            CacheAdUnit key = new CacheAdUnit(new AdSize(slot.getWidth(), slot.getHeight())
                    , slot.getPlacementId(), adUnitType);
            slotMap.put(key, slot);
        }
    }

    private AdUnitType findAdUnitType(Slot slot) {
        if (slot.isNative()) {
            return CRITEO_CUSTOM_NATIVE;
        }

        if ((DeviceUtil.getSizePortrait().getHeight() == slot.getHeight()
                && DeviceUtil.getSizePortrait().getWidth() == slot.getWidth())
                || DeviceUtil.getSizeLandscape().getHeight() == slot.getHeight()
                && DeviceUtil.getSizeLandscape().getWidth() == slot.getWidth()) {
            return CRITEO_INTERSTITIAL;
        }

        return CRITEO_BANNER;
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
