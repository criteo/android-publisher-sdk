package com.criteo.publisher.advancednative;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import com.google.auto.value.AutoValue;
import java.net.URL;

@AutoValue
@Keep
public abstract class CriteoMedia {

  static CriteoMedia create(@NonNull URL imageUrl) {
    return new AutoValue_CriteoMedia(imageUrl);
  }

  @NonNull
  abstract URL getImageUrl();

}
