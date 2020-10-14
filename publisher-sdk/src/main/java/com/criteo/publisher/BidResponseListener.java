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

package com.criteo.publisher;

import androidx.annotation.Keep;
import androidx.annotation.Nullable;
import com.criteo.publisher.advancednative.CriteoNativeLoader;
import com.criteo.publisher.model.AdUnit;
import java.lang.ref.WeakReference;

@Keep
public interface BidResponseListener {

  /**
   * Callback invoked when a response for a bid is given to the publisher.
   * <p>
   * In case of no bid, the given response is <code>null</code>. In case of successful bid, the given response is not
   * <code>null</code> and its CPM is accessible through {@link Bid#getPrice()}.
   *
   * <h1>InHouse integration</h1>
   * When using the In-House integration, the response can be given to one of these methods, to display the Ad:
   * <ul>
   *   <li>{@link CriteoBannerView#loadAd(Bid)}: display a banner</li>
   *   <li>{@link CriteoInterstitial#loadAd(Bid)}: display an interstitial</li>
   *   <li>{@link CriteoNativeLoader#loadAd(Bid)}: display a native ad</li>
   * </ul>
   * <p>
   * Please note that the <code>loadAd</code> method should match the kind of {@link AdUnit} the bid was asked for.
   *
   * <h1>AppBidding integration</h1>
   * When using the AppBidding integration, the response can be given to the
   * {@link Criteo#enrichAdObjectWithBid(Object, Bid)} method, to enrich your ad server object.
   *
   * <h1>Memory management</h1>
   * This listener can be retained few seconds until it is notified. As an effect, if this listener holds a reference
   * to an activity, when the activity is closed, then it can still be retained briefly in memory.
   *
   * If drastic memory management is required, few actions can be applied:
   * <ul>
   *   <li>Prefer lambda expression or static inner class over anonymous class: anonymous classes implicitly hold the
   *   outer class</li>
   *   <li>Use {@link WeakReference} to capture {@link android.app.Activity} or {@link android.view.View} (or any big
   *   object)</li>
   * </ul>
   *
   * Here is a sample code showing this:
   * <pre><code>
   *   class MyActivity extends Activity {
   *     private CriteoBannerView bannerView;
   *
   *     void sample() {
   *       // This is an anonymous class, "this" is implicitly hold and is retained until listener is notified
   *       Criteo.getInstance().loadBid(adUnit, new BidResponseListener() {
   *       });
   *
   *       // "this" is explicitly held and is retained until listener is notified
   *       Criteo.getInstance().loadBid(adUnit, bid -> {
   *         this.bannerView.loadAd(bid);
   *       });
   *
   *       // "this" is weakly held and is not retained
   *       WeakReference&lg;MyActivity&gt; weakThis = new WeakReference(this);
   *       Criteo.getInstance().loadBid(adUnit, bid -> {
   *         MyActivity activity = weakThis.get();
   *         if (activity != null) {
   *           activity.bannerView.loadAd(bid);
   *         }
   *       });
   *     }
   *   }
   * </code></pre>
   *
   * @param bid <code>null</code> in case of no bid, or a bid object that can be used to display an Ad
   * @see <a href="https://publisherdocs.criteotilt.com/app/android/app-bidding/inhouse/">InHouse documentation</a>
   * @see <a href="https://publisherdocs.criteotilt.com/app/android/app-bidding/">AppBidding documentation</a>
   * @see Criteo#loadBid(AdUnit, BidResponseListener)
   */
  void onResponse(@Nullable Bid bid);
}
