package com.criteo.publisher.model.nativeads;

import android.support.annotation.NonNull;
import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import java.net.URL;

@AutoValue
public abstract class NativeImpressionPixel {

  public static TypeAdapter<NativeImpressionPixel> typeAdapter(Gson gson) {
    return new AutoValue_NativeImpressionPixel.GsonTypeAdapter(gson);
  }

  @NonNull
  abstract URL getUrl();

}
