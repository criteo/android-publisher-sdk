package com.criteo.publisher.advancednative;

import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import android.widget.ImageView;
import java.net.URL;

public class CriteoImageLoader implements ImageLoader {

  @UiThread
  @Override
  public void loadImageInto(@NonNull URL imageUrl, @NonNull ImageView imageView) {
    // TODO EE-1052 or EE-921
  }

}
