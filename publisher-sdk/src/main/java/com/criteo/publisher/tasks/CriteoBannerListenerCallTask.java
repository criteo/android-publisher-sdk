package com.criteo.publisher.tasks;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.criteo.publisher.CriteoBannerAdListener;
import com.criteo.publisher.CriteoBannerView;
import com.criteo.publisher.CriteoErrorCode;
import com.criteo.publisher.CriteoListenerCode;
import java.lang.ref.Reference;

public class CriteoBannerListenerCallTask implements Runnable {

  @Nullable
  private final CriteoBannerAdListener listener;

  @NonNull
  private final Reference<CriteoBannerView> bannerViewRef;

  @NonNull
  private final CriteoListenerCode code;

  /**
   * Task that calls the relevant callback in the {@link CriteoBannerAdListener} based on the {@link
   * CriteoListenerCode} passed to execute. Passes the {@link CriteoBannerView} as a parameter to
   * the onAdReceived callback if the CriteoListenerCode is valid.
   */
  public CriteoBannerListenerCallTask(
      @Nullable CriteoBannerAdListener listener,
      @NonNull Reference<CriteoBannerView> bannerViewRef,
      @NonNull CriteoListenerCode code) {
    this.listener = listener;
    this.bannerViewRef = bannerViewRef;
    this.code = code;
  }

  @Override
  public void run() {
    if (listener == null) {
      return;
    }

    switch (code) {
      case INVALID:
        listener.onAdFailedToReceive(CriteoErrorCode.ERROR_CODE_NO_FILL);
        break;
      case VALID:
        listener.onAdReceived(bannerViewRef.get());
        break;
      case CLICK:
        listener.onAdClicked();
        listener.onAdLeftApplication();
        listener.onAdOpened();
        break;
      case CLOSE:
        listener.onAdClosed();
        break;
    }
  }

}
