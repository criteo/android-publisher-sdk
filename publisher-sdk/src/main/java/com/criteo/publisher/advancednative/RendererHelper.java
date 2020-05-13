package com.criteo.publisher.advancednative;

import android.support.annotation.NonNull;
import android.widget.ImageView;
import com.criteo.publisher.SafeRunnable;
import com.criteo.publisher.annotation.Incubating;
import com.criteo.publisher.concurrent.RunOnUiThreadExecutor;
import java.net.URL;

@Incubating(Incubating.NATIVE)
public class RendererHelper {

  @NonNull
  private final ImageLoader imageLoader;

  @NonNull
  private final RunOnUiThreadExecutor uiExecutor;

  public RendererHelper(
      @NonNull ImageLoader imageLoader,
      @NonNull RunOnUiThreadExecutor uiExecutor
  ) {
    this.imageLoader = imageLoader;
    this.uiExecutor = uiExecutor;
  }

  public void setMediaInView(CriteoMedia mediaContent, ImageView imageView) {
    setMediaInView(mediaContent.getImageUrl(), imageView);
  }

  void setMediaInView(@NonNull URL url, @NonNull ImageView imageView) {
    uiExecutor.execute(new SafeRunnable() {
      @Override
      public void runSafely() throws Throwable {
        imageLoader.loadImageInto(url, imageView);
      }
    });
  }

}
