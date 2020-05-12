package com.criteo.publisher.activity;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import com.criteo.publisher.TestAdUnits;
import com.criteo.publisher.advancednative.CriteoNativeAd;
import com.criteo.publisher.advancednative.CriteoNativeAdListener;
import com.criteo.publisher.advancednative.CriteoNativeLoader;
import com.criteo.publisher.advancednative.CriteoNativeRenderer;

/**
 * This tries to generate memory leaks by having inner class with strong references to the parent
 * class, ... This challenge the memory management in the native module.
 */
public class MemoryTestNativeActivity extends Activity {

  private CriteoNativeLoader nativeLoader;
  private ViewGroup adLayout;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    adLayout = new FrameLayout(this);
    setContentView(adLayout);

    nativeLoader = new CriteoNativeLoader(
        TestAdUnits.NATIVE,
        new CriteoNativeAdListener() {
          @Override
          public void onAdReceived(@NonNull CriteoNativeAd nativeAd) {
            View nativeView = nativeAd.createNativeRenderedView(MemoryTestNativeActivity.this, adLayout);
            adLayout.addView(nativeView);
          }

          @Override
          public void onAdClicked() {
            loadAd();
          }
        },
        new CriteoNativeRenderer() {
          @NonNull
          @Override
          public View createNativeView(@NonNull Context context, @Nullable ViewGroup parent) {
            return new Button(context);
          }

          @Override
          public void renderNativeView(@NonNull View nativeView, @NonNull CriteoNativeAd nativeAd) {
          }
        }
    );

    loadAd();
  }

  private void loadAd() {
    nativeLoader.loadAd();
  }
}
