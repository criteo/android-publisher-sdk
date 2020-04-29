package com.criteo.publisher.model.nativeads;

import android.support.annotation.NonNull;
import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import java.net.URL;

@AutoValue
public abstract class NativeProduct {

  public static TypeAdapter<NativeProduct> typeAdapter(Gson gson) {
    return new AutoValue_NativeProduct.GsonTypeAdapter(gson);
  }

  @NonNull
  public abstract String getTitle();

  @NonNull
  public abstract String getDescription();

  @NonNull
  public abstract String getPrice();

  @NonNull
  public abstract URL getClickUrl();

  @NonNull
  public abstract String getCallToAction();

  @NonNull
  abstract NativeImage getImage();

  @NonNull
  public URL getImageUrl() {
    return getImage().getUrl();
  }

}
