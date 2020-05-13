package com.criteo.publisher.activity;

import static android.widget.LinearLayout.VERTICAL;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.criteo.publisher.BidToken;
import com.criteo.publisher.TestAdUnits;
import com.criteo.publisher.advancednative.CriteoNativeAd;
import com.criteo.publisher.advancednative.CriteoNativeAdListener;
import com.criteo.publisher.advancednative.CriteoNativeLoader;
import com.criteo.publisher.advancednative.CriteoNativeRenderer;
import com.criteo.publisher.advancednative.RendererHelper;
import com.criteo.publisher.testutils.R;

/**
 * This tries to generate memory leaks by having inner class with strong references to the parent
 * class, ... This challenge the memory management in the native module.
 */
public class TestNativeActivity extends Activity {

  public static final Object AD_LAYOUT_TAG = new Object();
  public static final Object TITLE_TAG = new Object();
  public static final Object DESCRIPTION_TAG = new Object();
  public static final Object PRICE_TAG = new Object();
  public static final Object CALL_TO_ACTION_TAG = new Object();
  public static final Object PRODUCT_IMAGE_TAG = new Object();
  public static final Object ADVERTISER_DOMAIN_TAG = new Object();
  public static final Object ADVERTISER_DESCRIPTION_TAG = new Object();
  public static final Object ADVERTISER_LOGO_TAG = new Object();

  private CriteoNativeLoader nativeLoader;
  private ViewGroup adLayout;
  private Drawable defaultDrawable;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    adLayout = new FrameLayout(this);
    adLayout.setTag(AD_LAYOUT_TAG);
    setContentView(adLayout);
    initDefaultDrawable();

    nativeLoader = new CriteoNativeLoader(
        TestAdUnits.NATIVE,
        new CriteoNativeAdListener() {
          @Override
          public void onAdReceived(@NonNull CriteoNativeAd nativeAd) {
            View nativeView = nativeAd.createNativeRenderedView(TestNativeActivity.this, adLayout);
            adLayout.addView(nativeView);
          }

          @Override
          public void onAdClicked() {
            loadStandaloneAd();
          }
        },
        new CriteoNativeRenderer() {
          @NonNull
          @Override
          public View createNativeView(@NonNull Context context, @Nullable ViewGroup parent) {
            LinearLayout layout = new LinearLayout(context);
            layout.setOrientation(VERTICAL);
            layout.addView(createTextView(context, TITLE_TAG));
            layout.addView(createTextView(context, DESCRIPTION_TAG));
            layout.addView(createTextView(context, PRICE_TAG));
            layout.addView(createTextView(context, CALL_TO_ACTION_TAG));
            layout.addView(createImageView(context, PRODUCT_IMAGE_TAG));
            layout.addView(createTextView(context, ADVERTISER_DOMAIN_TAG));
            layout.addView(createTextView(context, ADVERTISER_DESCRIPTION_TAG));
            layout.addView(createImageView(context, ADVERTISER_LOGO_TAG));
            return layout;
          }

          @Override
          public void renderNativeView(
              @NonNull RendererHelper helper,
              @NonNull View nativeView,
              @NonNull CriteoNativeAd nativeAd
          ) {
            // Casts and index are used to check that this views is the same than the one that was
            // created above.
            LinearLayout layout = (LinearLayout) nativeView;
            ((TextView) layout.getChildAt(0)).setText(nativeAd.getTitle());
            ((TextView) layout.getChildAt(1)).setText(nativeAd.getDescription());
            ((TextView) layout.getChildAt(2)).setText(nativeAd.getPrice());
            ((TextView) layout.getChildAt(3)).setText(nativeAd.getCallToAction());
            ((ImageView) layout.getChildAt(4)).setImageDrawable(getDefaultDrawable());
            helper.setMediaInView(nativeAd.getProductMedia(), (ImageView) layout.getChildAt(4));
            ((TextView) layout.getChildAt(5)).setText(nativeAd.getAdvertiserDomain());
            ((TextView) layout.getChildAt(6)).setText(nativeAd.getAdvertiserDescription());
            ((ImageView) layout.getChildAt(7)).setImageDrawable(getDefaultDrawable());
            helper.setMediaInView(nativeAd.getAdvertiserLogoMedia(), (ImageView) layout.getChildAt(7));
          }

          private TextView createTextView(@NonNull Context context, @NonNull Object tag) {
            TextView view = new TextView(context);
            view.setTag(tag);
            return view;
          }

          private ImageView createImageView(@NonNull Context context, @NonNull Object tag) {
            ImageView view = new ImageView(context);
            view.setTag(tag);
            return view;
          }
        }
    );
  }

  public void loadStandaloneAd() {
    nativeLoader.loadAd();
  }

  public void loadInHouseAd(@Nullable BidToken bidToken) {
    nativeLoader.loadAd(bidToken);
  }

  public Drawable getDefaultDrawable() {
    return defaultDrawable;
  }

  private void initDefaultDrawable() {
    if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
      defaultDrawable = getResources().getDrawable(R.drawable.closebtn, getTheme());
    } else {
      //noinspection deprecation
      defaultDrawable = getResources().getDrawable(R.drawable.closebtn);
    }
  }
}
