package com.criteo.publisher.model.nativeads;

import android.support.annotation.NonNull;
import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import java.net.URL;

@AutoValue
public abstract class NativeAdvertiser {

  public static TypeAdapter<NativeAdvertiser> typeAdapter(Gson gson) {
    return new AutoValue_NativeAdvertiser.GsonTypeAdapter(gson);
  }

  @NonNull
  abstract String getDomain();

  @NonNull
  abstract String getDescription();

  @NonNull
  abstract URL getLogoClickUrl();

  @NonNull
  abstract NativeImage getLogo();

}
