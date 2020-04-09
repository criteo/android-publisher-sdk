package com.criteo.publisher.adview;

public interface AdWebViewListener {

  /**
   * Callback notified when the user click on the ad view, and is then redirected to the ad.
   */
  void onUserRedirectedToAd();
}
