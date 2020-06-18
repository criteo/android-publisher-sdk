package com.criteo.publisher.advancednative;

import static com.criteo.publisher.annotation.Internal.ADMOB_ADAPTER;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import com.criteo.publisher.annotation.Internal;
import com.google.auto.value.AutoValue;
import java.net.URL;

@AutoValue
@Keep
public abstract class CriteoMedia {

  static CriteoMedia create(@NonNull URL imageUrl) {
    return new AutoValue_CriteoMedia(imageUrl);
  }

  @NonNull
  @Internal(ADMOB_ADAPTER)
  abstract URL getImageUrl();

}
