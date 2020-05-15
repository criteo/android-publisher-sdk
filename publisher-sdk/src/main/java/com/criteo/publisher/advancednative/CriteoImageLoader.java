package com.criteo.publisher.advancednative;

import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.widget.ImageView;
import com.squareup.picasso.Picasso;
import java.net.URL;

public class CriteoImageLoader implements ImageLoader {

  private final Picasso picasso;

  public CriteoImageLoader(@NonNull Picasso picasso) {
    this.picasso = picasso;
  }

  @Override
  public void preload(@NonNull URL imageUrl) {
    picasso.load(imageUrl.toString()).fetch();
  }

  @UiThread
  @Override
  public void loadImageInto(
      @NonNull URL imageUrl,
      @NonNull ImageView imageView,
      @Nullable Drawable placeholder
  ) {
    picasso.load(imageUrl.toString()).placeholder(placeholder).into(imageView);
  }
}