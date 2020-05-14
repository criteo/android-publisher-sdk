package com.criteo.publisher.advancednative;

import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.widget.ImageView;
import java.net.URL;

public class CriteoImageLoader implements ImageLoader {

  @UiThread
  @Override
  public void loadImageInto(
      @NonNull URL imageUrl,
      @NonNull ImageView imageView,
      @Nullable Drawable placeholder
  ) {
    // TODO EE-1052 or EE-921
  }

}
