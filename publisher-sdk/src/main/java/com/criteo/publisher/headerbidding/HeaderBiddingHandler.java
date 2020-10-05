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

package com.criteo.publisher.headerbidding;

import androidx.annotation.NonNull;
import com.criteo.publisher.integration.Integration;
import com.criteo.publisher.model.CdbResponseSlot;
import com.criteo.publisher.util.AdUnitType;

public interface HeaderBiddingHandler {

  /**
   * Indicate if this handler can handle the given object.
   * <p>
   * This means that this handler is ready to accept this object for all other methods in this
   * interface.
   *
   * @param object object to test
   * @return <code>true</code> if the object is supported
   */
  boolean canHandle(@NonNull Object object);

  /**
   * Indicate which kind of integration is this handler doing.
   */
  @NonNull
  Integration getIntegration();

  /**
   * Remove previous state that may have been stored in the object.
   * <p>
   * This cancels any modification done by a {@link #enrichBid(Object, AdUnitType, CdbResponseSlot)} call.
   * <p>
   * This method is only called on {@linkplain #canHandle(Object) handled objects}. If there is a bid, but also if there
   * is no bid to avoid having a third-party considering that Criteo bids again.
   *
   * @param object bid object to clean
   */
  void cleanPreviousBid(@NonNull Object object);

  /**
   * Enrich the given bid object with a bid for the given ad unit.
   * <p>
   * This method is only called on {@linkplain #canHandle(Object) handled objects}.
   *  @param object bid object to fill
   * @param adUnitType ad unit representing the requested bid
   * @param slot bid to use
   */
  void enrichBid(@NonNull Object object, @NonNull AdUnitType adUnitType, @NonNull CdbResponseSlot slot);
}
