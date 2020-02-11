package com.criteo.publisher;

public interface CriteoAdListener {

  /**
   * Called when an ad fetch fails.
   *
   * @param code The reason the fetch failed.
   */
  void onAdFailedToReceive(CriteoErrorCode code);

  /**
   * Called when an ad is clicked.
   */
  void onAdLeftApplication();

  /**
   * Called when Ad link clicked.
   */
  void onAdClicked();

  /**
   * Called when browser opened
   */
  void onAdOpened();

  /**
   * Called when browser closed
   */
  void onAdClosed();

}
