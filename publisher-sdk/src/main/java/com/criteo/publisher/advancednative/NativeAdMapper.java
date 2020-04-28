package com.criteo.publisher.advancednative;

import android.support.annotation.NonNull;
import com.criteo.publisher.model.Slot;

public class NativeAdMapper {

  @NonNull
  CriteoNativeAd map(@NonNull Slot slot) {
    return new CriteoNativeAd(); // Dummy implementation for now
  }

}
