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

package com.criteo.publisher.manual;

import static com.criteo.publisher.CriteoUtil.givenInitializedCriteo;
import static com.criteo.publisher.concurrent.ThreadingUtil.runOnMainThreadAndWait;

import androidx.test.rule.ActivityTestRule;
import com.criteo.publisher.CriteoErrorCode;
import com.criteo.publisher.CriteoInterstitial;
import com.criteo.publisher.CriteoInterstitialAdDisplayListener;
import com.criteo.publisher.CriteoInterstitialAdListener;
import com.criteo.publisher.TestAdUnits;
import com.criteo.publisher.mock.MockedDependenciesRule;
import com.criteo.publisher.model.InterstitialAdUnit;
import com.criteo.publisher.test.activity.DummyActivity;
import com.criteo.publisher.util.CompletableFuture;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

@Ignore("Those are test that should only be run manually")
public class StandaloneInterstitialManualTest {

  private static final int INVESTIGATION_TIME_MS = 15_000;

  @Rule
  public ActivityTestRule<DummyActivity> activityRule = new ActivityTestRule<>(DummyActivity.class);

  @Rule
  public MockedDependenciesRule mockedDependenciesRule = new MockedDependenciesRule();

  private final InterstitialAdUnit interstitialDemo = TestAdUnits.INTERSTITIAL_DEMO;
  private final InterstitialAdUnit interstitialIbvDemo = TestAdUnits.INTERSTITIAL_IBV_DEMO;

  private CriteoInterstitial interstitial;
  private ShowingInterstitialListener listener;

  /**
   * Here is the list of test scenarios that could be verified through this test case.
   *
   * <h1>Scenario: Interstitial Ad should fit the size of the screen</h1>
   * <ul>
   *  <li>Given this test case</li>
   *  <li>Then interstitial ad's width matches the screen of the device (no scrolling)</li>
   *  <li>Then interstitial ad's height matches the screen of the device (no scrolling)</li>
   * </ul>
   *
   * <h1>Scenario: Interstitial Ad should hide the status and action bar but keep the home button
   * bar</h1>
   * <ul>
   *  <li>Given visible home button bar</li>
   *  <li>Given this test case</li>
   *  <li>Then action bar should be hidden</li>
   *  <li>Then status bar should be hidden</li>
   *  <li>Then home button bar should stay visible</li>
   * </ul>
   */
  @Test
  public void showingAnInterstitialDemoAd() throws Exception {
    showingAnInterstitialAd(interstitialDemo);
  }

  /**
   * Here is the list of test scenarios that could be verified through this test case.
   *
   * <h1>Scenario: IBV should be played automatically</h1>
   * <ul>
   *  <li>Given this test case</li>
   *  <li>Then video start automatically</li>
   * </ul>
   */
  @Test
  public void showingAnInterstitialIbvDemoAd() throws Exception {
    showingAnInterstitialAd(interstitialIbvDemo);
  }

  private void showingAnInterstitialAd(InterstitialAdUnit adUnit) throws Exception {
    givenInitializedCriteo(adUnit);
    waitForBids();

    runOnMainThreadAndWait(() -> {
      interstitial = new CriteoInterstitial(adUnit);

      listener = new ShowingInterstitialListener(interstitial);
      interstitial.setCriteoInterstitialAdListener(listener);
      interstitial.setCriteoInterstitialAdDisplayListener(listener);

      interstitial.loadAd();
    });

    listener.throwIfFailure();
    sleepForManualTesting();
  }

  private void sleepForManualTesting() throws InterruptedException {
    Thread.sleep(INVESTIGATION_TIME_MS);
  }

  private void waitForBids() {
    mockedDependenciesRule.waitForIdleState();
  }

  private static class ShowingInterstitialListener implements CriteoInterstitialAdListener,
      CriteoInterstitialAdDisplayListener {

    private final CriteoInterstitial interstitial;
    private final CompletableFuture<Void> failure;

    private ShowingInterstitialListener(CriteoInterstitial interstitial) {
      this.interstitial = interstitial;
      this.failure = new CompletableFuture<>();
    }

    void throwIfFailure() throws Exception {
      failure.get();
    }

    @Override
    public void onAdReadyToDisplay() {
      interstitial.show();
      failure.complete(null);
    }

    @Override
    public void onAdFailedToDisplay(CriteoErrorCode error) {
      failure.completeExceptionally(
          new IllegalStateException("Error while displaying the interstitial: " + error.name()));
    }

    @Override
    public void onAdFailedToReceive(CriteoErrorCode code) {
      failure.completeExceptionally(
          new IllegalStateException("Error while loading the interstitial: " + code.name()));
    }

    @Override
    public void onAdReceived() {
    }

    @Override
    public void onAdLeftApplication() {
    }

    @Override
    public void onAdClicked() {
    }

    @Override
    public void onAdOpened() {
    }

    @Override
    public void onAdClosed() {
    }
  }

}
