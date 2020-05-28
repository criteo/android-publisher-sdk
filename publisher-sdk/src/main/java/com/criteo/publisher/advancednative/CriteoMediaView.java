package com.criteo.publisher.advancednative;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION_CODES;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.ImageView;
import androidx.annotation.RequiresApi;
import com.criteo.publisher.annotation.Incubating;

/**
 * Displays {@linkplain CriteoMedia Ad media} such as product image or advertiser logo.
 * <p>
 * The following XML snippet is a common example of using an CriteoMediaView with a placeholder:
 * <pre><code>
 * &lt;LinearLayout
 *     xmlns:android="http://schemas.android.com/apk/res/android"
 *     android:layout_width="match_parent"
 *     android:layout_height="match_parent"&gt;
 *     &lt;com.criteo.publisher.advancednative.CriteoMediaView
 *         android:layout_width="wrap_content"
 *         android:layout_height="wrap_content"
 *         android:src="@mipmap/ic_launcher"
 *         /&gt;
 * &lt;/LinearLayout&gt;
 * </code></pre>
 * <p>
 * The <code>android:src</code> attribute let you define image placeholder while Ad's media are not
 * loaded (either because of slow network or even network timeout).
 *
 * @attr ref android.R.styleable#ImageView_src
 */
@Incubating(Incubating.NATIVE)
public class CriteoMediaView extends FrameLayout {

  @NonNull
  private final ImageView imageView;

  @Nullable
  private Drawable placeholder;

  public CriteoMediaView(Context context) {
    super(context);
    imageView = initImageView(context);
  }

  public CriteoMediaView(
      Context context,
      @Nullable AttributeSet attrs
  ) {
    this(context, attrs, 0);
  }

  public CriteoMediaView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    imageView = initImageView(context, attrs, defStyleAttr, 0);
  }

  @RequiresApi(api = VERSION_CODES.LOLLIPOP)
  public CriteoMediaView(
      Context context,
      @Nullable AttributeSet attrs,
      int defStyleAttr,
      int defStyleRes
  ) {
    super(context, attrs, defStyleAttr, defStyleRes);
    imageView = initImageView(context, attrs, defStyleAttr, defStyleRes);
  }

  private ImageView initImageView(Context context) {
    ImageView imageView = new ImageView(context);
    addView(imageView);
    return imageView;
  }

  private ImageView initImageView(
      Context context,
      @Nullable AttributeSet attrs,
      int defStyleAttr,
      int defStyleRes) {
    ImageView imageView = initImageView(context);

    int[] values = new int[]{android.R.attr.src};
    TypedArray a = context.obtainStyledAttributes(attrs, values, defStyleAttr, defStyleRes);

    try {
      Drawable d = a.getDrawable(0);
      if (d != null) {
        // The image is really set in the image view, so that the Layout Editor of Android Studio
        // displays the drawable as expected.
        imageView.setImageDrawable(d);
        placeholder = d;
      }
    } finally {
      a.recycle();
    }

    return imageView;
  }

  public void setPlaceholder(@NonNull Drawable placeholder) {
    this.placeholder = placeholder;
  }

  @Nullable
  public Drawable getPlaceholder() {
    return placeholder;
  }

  @NonNull
  ImageView getImageView() {
    return imageView;
  }

}
