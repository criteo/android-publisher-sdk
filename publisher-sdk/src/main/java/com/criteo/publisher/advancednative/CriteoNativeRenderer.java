package com.criteo.publisher.advancednative;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

@Keep
public interface CriteoNativeRenderer {

  /**
   * Create a new empty native view that may be filled later.
   * <p>
   * Generally this method returns inflated XML Android Layout resource like:
   * <pre><code>
   *   LayoutInflater inflater = LayoutInflater.from(context);
   *   return inflater.inflate(R.layout.my_native_ad, parent, false);
   * </code></pre>
   * <p>
   * Note that parent is given so that inflated views are setup with the {@link
   * android.view.ViewGroup.LayoutParams} corresponding to its parent. But you should not attach yet
   * your view to the parent. Your expected to attach it after the view is filled.
   *
   * @param context android context
   * @param parent optional parent to get layout params from
   * @return new empty native view to fill later
   */
  @NonNull
  View createNativeView(@NonNull Context context, @Nullable ViewGroup parent);

  /**
   * Fill the given native view with Ad payload.
   * <p>
   * The native view is a one created by the {@link #createNativeView(Context, ViewGroup)} method.
   * <p>
   * Note that, if you're using a recycler view, or any other mechanism reusing views, you may
   * expect that for a same view this method can be called with different Ad payload. To avoid
   * having residual data, you should clear the view. If you're not setting values conditionally,
   * you should not be worried about this warning.
   *
   * @param helper helper class providing extension features, such as image loading
   * @param nativeView the view to fill with data
   * @param nativeAd Ad payload that you can use
   */
  void renderNativeView(
      @NonNull RendererHelper helper,
      @NonNull View nativeView,
      @NonNull CriteoNativeAd nativeAd
  );

}
