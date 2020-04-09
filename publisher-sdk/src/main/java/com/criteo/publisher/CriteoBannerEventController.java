package com.criteo.publisher;

import static com.criteo.publisher.CriteoListenerCode.CLICK;
import static com.criteo.publisher.CriteoListenerCode.INVALID;
import static com.criteo.publisher.CriteoListenerCode.VALID;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.webkit.WebViewClient;
import com.criteo.publisher.adview.AdWebViewClient;
import com.criteo.publisher.adview.AdWebViewListener;
import com.criteo.publisher.model.AdUnit;
import com.criteo.publisher.model.Slot;
import com.criteo.publisher.model.TokenValue;
import com.criteo.publisher.tasks.CriteoBannerListenerCallTask;
import com.criteo.publisher.tasks.CriteoBannerLoadTask;
import com.criteo.publisher.util.AdUnitType;
import com.criteo.publisher.util.RunOnUiThreadExecutor;
import java.lang.ref.WeakReference;


public class CriteoBannerEventController {

  @NonNull
  private final WeakReference<CriteoBannerView> view;

  @Nullable
  private final CriteoBannerAdListener adListener;

  @NonNull
  private final Criteo criteo;

  @NonNull
  private final RunOnUiThreadExecutor executor;

  public CriteoBannerEventController(
      @NonNull CriteoBannerView bannerView,
      @NonNull Criteo criteo) {
    this.view = new WeakReference<>(bannerView);
    this.adListener = bannerView.getCriteoBannerAdListener();
    this.criteo = criteo;
    this.executor = DependencyProvider.getInstance().provideRunOnUiThreadExecutor();
  }

  public void fetchAdAsync(@Nullable AdUnit adUnit) {
    Slot slot = criteo.getBidForAdUnit(adUnit);

    if (slot == null) {
      notifyFor(INVALID);
    } else {
      notifyFor(VALID);
      displayAd(slot.getDisplayUrl());
    }
  }

  public void fetchAdAsync(@Nullable BidToken bidToken) {
    TokenValue tokenValue = criteo.getTokenValue(bidToken, AdUnitType.CRITEO_BANNER);

    if (tokenValue == null) {
      notifyFor(INVALID);
    } else {
      notifyFor(VALID);
      displayAd(tokenValue.getDisplayUrl());
    }
  }

  private void notifyFor(@NonNull CriteoListenerCode code) {
    executor.executeAsync(new CriteoBannerListenerCallTask(adListener, view, code));
  }

  @VisibleForTesting
  void displayAd(@NonNull String displayUrl) {
    executor.executeAsync(new CriteoBannerLoadTask(
        view, createWebViewClient(), criteo.getConfig(), displayUrl));
  }

  // WebViewClient is created here to prevent passing the AdListener everywhere.
  // Setting this webViewClient to the WebView is done in the CriteoBannerLoadTask as all
  // WebView methods need to run in the same UI thread
  @VisibleForTesting
  WebViewClient createWebViewClient() {
    return new AdWebViewClient(new AdWebViewListener() {
      @Override
      public void onUserRedirectedToAd() {
        notifyFor(CLICK);
      }
    });
  }

}
