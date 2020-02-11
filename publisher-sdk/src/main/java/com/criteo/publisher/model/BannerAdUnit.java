package com.criteo.publisher.model;

import com.criteo.publisher.Util.AdUnitType;
import com.criteo.publisher.Util.ObjectsUtil;

public final class BannerAdUnit extends AdUnit {

  private final AdSize adSize;

  public BannerAdUnit(String adUnitId, AdSize size) {
    super(adUnitId, AdUnitType.CRITEO_BANNER);
    this.adSize = size;
  }

  public AdSize getSize() {
    return adSize;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    BannerAdUnit that = (BannerAdUnit) o;
    return ObjectsUtil.equals(adSize, that.adSize);
  }

  @Override
  public int hashCode() {
    return ObjectsUtil.hash(super.hashCode(), adSize);
  }
}
