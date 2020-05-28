package com.criteo.publisher.model.nativeads;

import androidx.annotation.NonNull;
import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import java.net.URL;

@AutoValue
public abstract class NativeImage {

  public static TypeAdapter<NativeImage> typeAdapter(Gson gson) {
    return new AutoValue_NativeImage.GsonTypeAdapter(gson);
  }

  @NonNull
  abstract URL getUrl();

}
