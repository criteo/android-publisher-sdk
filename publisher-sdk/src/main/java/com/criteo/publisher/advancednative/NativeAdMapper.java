package com.criteo.publisher.advancednative;

import android.support.annotation.NonNull;
import com.criteo.publisher.model.nativeads.NativeAssets;

public class NativeAdMapper {

  @NonNull
  CriteoNativeAd map(@NonNull NativeAssets nativeAssets) {
    return new CriteoNativeAd(); // Dummy implementation for now
  }

}
