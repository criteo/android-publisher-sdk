package com.criteo.publisher.advancednative;

import android.support.annotation.NonNull;
import com.criteo.publisher.annotation.Incubating;
import com.google.auto.value.AutoValue;
import java.net.URL;

@AutoValue
@Incubating(Incubating.NATIVE)
public abstract class CriteoMedia {

  static CriteoMedia create(@NonNull URL imageUrl) {
    return new AutoValue_CriteoMedia(imageUrl);
  }

  @NonNull
  abstract URL getImageUrl();

}
