package com.criteo.publisher;

public interface CriteoInterstitialAdListener extends CriteoAdListener {

  /**
   * Called when an ad is successfully fetched.
   */
  void onAdReceived();

}

