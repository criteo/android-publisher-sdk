package com.criteo.publisher.advancednative;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.ImageView;
import com.criteo.publisher.util.AndroidUtil;
import com.criteo.publisher.util.BuildConfigWrapper;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;

public class AdChoiceOverlay {

  // This is used instead of View.setTag, which causes a memory leak in 2.3
  // and earlier: https://code.google.com/p/android/issues/detail?id=18273
  @NonNull
  private final Map<View, WeakReference<ImageView>> adChoicePerView = new WeakHashMap<>();

  @NonNull
  private final BuildConfigWrapper buildConfigWrapper;

  @NonNull
  private final AndroidUtil androidUtil;

  public AdChoiceOverlay(
      @NonNull BuildConfigWrapper buildConfigWrapper,
      @NonNull AndroidUtil androidUtil
  ) {
    this.buildConfigWrapper = buildConfigWrapper;
    this.androidUtil = androidUtil;
  }

  /**
   * Add an overlay on the given view, with a placeholder for AdChoice icon.
   * <p>
   * The placeholder is left empty, but after calling this method, as long as the given view is
   * alive, {@link #getAdChoiceView(View)} should return non null result, and you can then
   * fill the placeholder view.
   * <p>
   * It is recommended to call this method only once per view. Or you may end with multiple overlays
   * on a view. Also, it is recommended to throw reference to the given view and only keep the
   * returned one:
   * <pre><code>
   *   View myView = new Button(context);
   *   myView = adChoiceOverlay.addOverlay(myView);
   * </code></pre>
   *
   * @param view view to wrap with AdChoice
   * @return view with the overlay
   */
  @SuppressLint("RtlHardcoded") // AdChoice is at top-right corner, no matter RTL locales
  @NonNull
  ViewGroup addOverlay(@NonNull View view) {
    Context context = view.getContext();

    ImageView adChoiceImageView = new ImageView(context);
    FrameLayout overlayFrameLayout = new FrameLayout(context);

    LayoutParams inheritedLayoutParams = view.getLayoutParams();
    if (inheritedLayoutParams != null) {
      overlayFrameLayout.setLayoutParams(inheritedLayoutParams);
    }

    overlayFrameLayout.addView(view);
    overlayFrameLayout.addView(adChoiceImageView);

    // Put the AdChoice at the top right corner
    FrameLayout.LayoutParams adChoiceLayoutParams = (FrameLayout.LayoutParams) adChoiceImageView.getLayoutParams();
    adChoiceLayoutParams.gravity = Gravity.RIGHT;
    adChoiceLayoutParams.width = androidUtil.dpToPixel(buildConfigWrapper.getAdChoiceIconWidthInDp());
    adChoiceLayoutParams.height = androidUtil.dpToPixel(buildConfigWrapper.getAdChoiceIconHeightInDp());

    if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
      // From Android 21, Buttons and some other views have an elevation above 0 so they can cast
      // shadows. Because of this, they are also drawn above other elements (see
      // https://developer.android.com/reference/android/view/View.html#Drawing).
      // To keep the AdChoice icon above given view, its elevation is set really high, and its
      // shadow is removed.
      adChoiceImageView.setElevation(1000.f);
      adChoiceImageView.setOutlineProvider(null);
    }

    adChoicePerView.put(overlayFrameLayout, new WeakReference<>(adChoiceImageView));

    return overlayFrameLayout;
  }

  /**
   * Return the AdChoice placeholder injected by {@link #addOverlay(View)}.
   * <p>
   * You can call this method multiple times on the same view.
   *
   * @param overlappedView view to get the AdChoice from
   * @return AdChoice view
   */
  @Nullable
  ImageView getAdChoiceView(@NonNull View overlappedView) {
    WeakReference<ImageView> adChoiceViewRef = adChoicePerView.get(overlappedView);
    if (adChoiceViewRef == null) {
      return null;
    }
    return adChoiceViewRef.get();
  }

  /**
   * Return the initial view that was wrapped by {@link #addOverlay(View)}.
   * <p>
   * You can call this method multiple times on the same view.
   *
   * @param overlappedView view to get the initial view from
   * @return initial view, or null if this view was not wrapped
   */
  @Nullable
  View getInitialView(@NonNull View overlappedView) {
    if (getAdChoiceView(overlappedView) == null) {
      return null;
    }

    ViewGroup viewGroup = (ViewGroup) overlappedView;
    return viewGroup.getChildAt(0);
  }

  @VisibleForTesting
  int getAdChoiceCount() {
    return adChoicePerView.size();
  }

}
