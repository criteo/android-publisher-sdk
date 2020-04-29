package com.criteo.publisher.model.nativeads;

import android.support.annotation.NonNull;
import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;
import java.net.URL;

@AutoValue
public abstract class NativePrivacy {

  public static TypeAdapter<NativePrivacy> typeAdapter(Gson gson) {
    return new AutoValue_NativePrivacy.GsonTypeAdapter(gson);
  }

  @NonNull
  @SerializedName("optoutClickUrl")
  abstract URL getClickUrl();

  @NonNull
  @SerializedName("optoutImageUrl")
  abstract URL getImageUrl();

  @NonNull
  @SerializedName("longLegalText")
  abstract String getLegalText();

}
