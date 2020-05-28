package com.criteo.publisher.model.nativeads;

import androidx.annotation.NonNull;
import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import java.net.URI;

@AutoValue
public abstract class NativeAdvertiser {

  public static TypeAdapter<NativeAdvertiser> typeAdapter(Gson gson) {
    return new AutoValue_NativeAdvertiser.GsonTypeAdapter(gson);
  }

  @NonNull
  abstract String getDomain();

  @NonNull
  abstract String getDescription();

  /**
   * This is an {@link URI} and not an {@link java.net.URL}, because deeplink are acceptable.
   */
  @NonNull
  abstract URI getLogoClickUrl();

  @NonNull
  abstract NativeImage getLogo();

}
