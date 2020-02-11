package com.criteo.publisher.cache;

import static com.criteo.publisher.Util.AdUnitType.CRITEO_BANNER;
import static com.criteo.publisher.Util.AdUnitType.CRITEO_CUSTOM_NATIVE;
import static com.criteo.publisher.Util.AdUnitType.CRITEO_INTERSTITIAL;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import com.criteo.publisher.Util.AdUnitType;
import com.criteo.publisher.Util.DeviceUtil;
import com.criteo.publisher.model.AdSize;
import com.criteo.publisher.model.CacheAdUnit;
import com.criteo.publisher.model.Slot;
import java.util.HashMap;
import java.util.Map;

public class SdkCache {

  private final Map<CacheAdUnit, Slot> slotMap;
  private final DeviceUtil deviceUtil;

  public SdkCache(@NonNull DeviceUtil deviceUtil) {
    slotMap = new HashMap<>();
    this.deviceUtil = deviceUtil;
  }

  public void add(@NonNull Slot slot) {
    AdUnitType adUnitType = findAdUnitType(slot);
    CacheAdUnit key = new CacheAdUnit(new AdSize(slot.getWidth(), slot.getHeight())
        , slot.getPlacementId(), adUnitType);
    slotMap.put(key, slot);
  }

  // FIXME: this kind of method should not exist:
  //  The CacheAdUnit are used as slot description in the request sent to CDB.
  //  This means that ad unit type is known before sending the CDB request.
  //  When receiving slots from CDB, then this information is forgotten, and a new CacheAdUnit
  //  is created from the Slot received from CDB but with fuzzy methods like this one.
  //  Instead, we could generate a random slotid (not same concept as adunit id, see
  //  http://review.criteois.lan/gitweb?p=publisher/direct-bidder.git;a=blob;f=directbidder-app/src/main/scala/com/criteo/directbidder/models/Types.scala;h=e50edc2d7d3916b91a746b43674d76aafd0e9521;hb=HEAD#l27),
  //  give it to CDB, and reread it from CDB response.
  //  Note that the AdUnitId are not necessary unique (for instance if a publisher ask for same
  //  banner but in different size). So it could not be used as a key between request and
  //  response.
  //  See https://jira.criteois.com/browse/EE-608
  private AdUnitType findAdUnitType(Slot slot) {
    if (slot.isNative()) {
      return CRITEO_CUSTOM_NATIVE;
    }

    if ((deviceUtil.getSizePortrait().getHeight() == slot.getHeight()
        && deviceUtil.getSizePortrait().getWidth() == slot.getWidth())
        || deviceUtil.getSizeLandscape().getHeight() == slot.getHeight()
        && deviceUtil.getSizeLandscape().getWidth() == slot.getWidth()) {
      return CRITEO_INTERSTITIAL;
    }

    return CRITEO_BANNER;
  }

  /**
   * Get the slot corresponding to the given key.
   * <p>
   * If no slot match the given key, then <code>null</code> is returned.
   *
   * @param key of the slot to look for
   * @return found slot or null if not found
   */
  @Nullable
  public Slot peekAdUnit(CacheAdUnit key) {
    return slotMap.get(key);
  }

  public void remove(CacheAdUnit key) {
    slotMap.remove(key);
  }

  @VisibleForTesting
  int getItemCount() {
    return slotMap.size();
  }
}
