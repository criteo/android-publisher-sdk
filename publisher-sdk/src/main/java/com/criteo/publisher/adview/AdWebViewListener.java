package com.criteo.publisher.adview;

public interface AdWebViewListener {

  /**
   * Callback notified when the user click on the ad view, and is then redirected to the ad.
   */
  void onUserRedirectedToAd();

  /**
   * Callback notified when the user is back from an ad. This happens generally when user press the
   * back button after being redirected to an ad.
   */
  void onUserBackFromAd();
}
