/*
 *    Copyright 2020 Criteo
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.criteo.publisher.activity;

import static android.widget.LinearLayout.VERTICAL;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import com.criteo.publisher.Bid;
import com.criteo.publisher.TestAdUnits;
import com.criteo.publisher.advancednative.CriteoMediaView;
import com.criteo.publisher.advancednative.CriteoNativeAd;
import com.criteo.publisher.advancednative.CriteoNativeAdListener;
import com.criteo.publisher.advancednative.CriteoNativeLoader;
import com.criteo.publisher.advancednative.CriteoNativeRenderer;
import com.criteo.publisher.advancednative.RendererHelper;
import com.criteo.publisher.context.ContextData;
import com.criteo.publisher.tests.R;
import java.util.ArrayList;
import java.util.List;

/**
 * This tries to generate memory leaks by having inner class with strong references to the parent
 * class, ... This challenge the memory management in the native module.
 */
public class TestNativeActivity extends Activity {

  public static final Object AD_LAYOUT_TAG = new Object();
  public static final Object RECYCLER_VIEW_TAG = new Object();
  public static final Object TITLE_TAG = new Object();
  public static final Object DESCRIPTION_TAG = new Object();
  public static final Object PRICE_TAG = new Object();
  public static final Object CALL_TO_ACTION_TAG = new Object();
  public static final Object PRODUCT_IMAGE_TAG = new Object();
  public static final Object ADVERTISER_DOMAIN_TAG = new Object();
  public static final Object ADVERTISER_DESCRIPTION_TAG = new Object();
  public static final Object ADVERTISER_LOGO_TAG = new Object();
  public static final Object PRIVACY_LEGAL_TEXT_TAG = new Object();

  private CriteoNativeLoader standaloneNativeLoaderInAdLayout;
  private CriteoNativeLoader inHouseNativeLoaderInAdLayout;
  private ViewGroup adLayout;

  private CriteoNativeLoader standaloneNativeLoaderInRecyclerView;
  private CriteoNativeLoader inHouseNativeLoaderInRecyclerView;
  private Adapter adapter;

  private Drawable defaultDrawable;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    adLayout = new FrameLayout(this);
    adLayout.setTag(AD_LAYOUT_TAG);

    adapter = new Adapter();
    RecyclerView recyclerView = new RecyclerView(this);
    recyclerView.setTag(RECYCLER_VIEW_TAG);
    recyclerView.setLayoutManager(new LinearLayoutManager(this));
    recyclerView.setAdapter(adapter);

    ViewGroup layout = new FrameLayout(this);
    layout.addView(adLayout);
    layout.addView(recyclerView);
    setContentView(layout);

    initDefaultDrawable();

    standaloneNativeLoaderInAdLayout = new CriteoNativeLoader(
        TestAdUnits.NATIVE,
        new AdLayoutNativeAdListener(),
        new NativeRenderer()
    );

    standaloneNativeLoaderInRecyclerView = new CriteoNativeLoader(
        TestAdUnits.NATIVE,
        new RecyclerViewNativeAdListener(),
        new NativeRenderer()
    );

    inHouseNativeLoaderInAdLayout = new CriteoNativeLoader(
        new AdLayoutNativeAdListener(),
        new NativeRenderer()
    );

    inHouseNativeLoaderInRecyclerView = new CriteoNativeLoader(
        new RecyclerViewNativeAdListener(),
        new NativeRenderer()
    );
  }

  public void loadStandaloneAdInAdLayout() {
    standaloneNativeLoaderInAdLayout.loadAd(new ContextData());
  }

  public void loadInHouseAdInAdLayout(@Nullable Bid bid) {
    inHouseNativeLoaderInAdLayout.loadAd(bid);
  }

  public void loadStandaloneAdInRecyclerView() {
    standaloneNativeLoaderInRecyclerView.loadAd(new ContextData());
  }

  public void loadInHouseAdInRecyclerView(@Nullable Bid bid) {
    inHouseNativeLoaderInRecyclerView.loadAd(bid);
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

  private class Adapter extends RecyclerView.Adapter<ViewHolder> {

    private final List<CriteoNativeAd> dataset = new ArrayList<>();

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
      View nativeView = standaloneNativeLoaderInRecyclerView.createEmptyNativeView(
          parent.getContext(),
          parent
      );

      return new ViewHolder(nativeView) {};
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
      dataset.get(position).renderNativeView(holder.itemView);
    }

    @Override
    public int getItemCount() {
      return dataset.size();
    }

    void addNativeAd(CriteoNativeAd nativeAd) {
      dataset.add(nativeAd);
      notifyItemInserted(dataset.size() - 1);
    }

  }

  private class AdLayoutNativeAdListener implements CriteoNativeAdListener {

    @Override
    public void onAdReceived(@NonNull CriteoNativeAd nativeAd) {
      View nativeView = nativeAd.createNativeRenderedView(TestNativeActivity.this, adLayout);
      adLayout.addView(nativeView);
    }
  }

  private class RecyclerViewNativeAdListener implements CriteoNativeAdListener {

    @Override
    public void onAdReceived(@NonNull CriteoNativeAd nativeAd) {
      adapter.addNativeAd(nativeAd);
    }
  }

  private class NativeRenderer implements CriteoNativeRenderer {

    @NonNull
    @Override
    public View createNativeView(@NonNull Context context, @Nullable ViewGroup parent) {
      LinearLayout layout = new LinearLayout(context);
      layout.setOrientation(VERTICAL);
      layout.addView(createTextView(context, TITLE_TAG));
      layout.addView(createTextView(context, DESCRIPTION_TAG));
      layout.addView(createTextView(context, PRICE_TAG));
      layout.addView(createTextView(context, CALL_TO_ACTION_TAG));
      layout.addView(createMediaView(context, PRODUCT_IMAGE_TAG));
      layout.addView(createTextView(context, ADVERTISER_DOMAIN_TAG));
      layout.addView(createTextView(context, ADVERTISER_DESCRIPTION_TAG));
      layout.addView(createMediaView(context, ADVERTISER_LOGO_TAG));
      layout.addView(createTextView(context, PRIVACY_LEGAL_TEXT_TAG));
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
      ((CriteoMediaView) layout.getChildAt(4)).setPlaceholder(getDefaultDrawable());
      helper.setMediaInView(nativeAd.getProductMedia(), (CriteoMediaView) layout.getChildAt(4));
      ((TextView) layout.getChildAt(5)).setText(nativeAd.getAdvertiserDomain());
      ((TextView) layout.getChildAt(6)).setText(nativeAd.getAdvertiserDescription());
      ((CriteoMediaView) layout.getChildAt(7)).setPlaceholder(getDefaultDrawable());
      helper.setMediaInView(nativeAd.getAdvertiserLogoMedia(), (CriteoMediaView) layout.getChildAt(7));
      ((TextView) layout.getChildAt(8)).setText(nativeAd.getLegalText());
    }

    private TextView createTextView(@NonNull Context context, @NonNull Object tag) {
      TextView view = new TextView(context);
      view.setTag(tag);
      return view;
    }

    private CriteoMediaView createMediaView(@NonNull Context context, @NonNull Object tag) {
      CriteoMediaView view = new CriteoMediaView(context);
      view.setTag(tag);
      return view;
    }
  }

}
