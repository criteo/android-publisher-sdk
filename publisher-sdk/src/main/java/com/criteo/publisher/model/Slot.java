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

package com.criteo.publisher.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.criteo.publisher.Clock;
import com.criteo.publisher.model.nativeads.NativeAssets;
import org.json.JSONObject;

public class Slot {

  @NonNull
  private final CdbResponseSlot cdbResponseSlot;

  public Slot(JSONObject json) {
    cdbResponseSlot = CdbResponseSlot.fromJson(json);
  }

  @Nullable
  public String getImpressionId() {
    return cdbResponseSlot.getImpressionId();
  }

  public boolean isNative() {
    return cdbResponseSlot.isNative();
  }

  public String getPlacementId() {
    return cdbResponseSlot.getPlacementId();
  }

  public String getCpm() {
    return cdbResponseSlot.getCpm();
  }

  public int getWidth() {
    return cdbResponseSlot.getWidth();
  }

  public int getHeight() {
    return cdbResponseSlot.getHeight();
  }

  public String getCurrency() {
    return cdbResponseSlot.getCurrency();
  }

  /**
   * Returns the TTL in seconds for this bid response.
   */
  public int getTtl() {
    return cdbResponseSlot.getTtl();
  }

  public void setTtl(int ttl) {
    cdbResponseSlot.setTtl(ttl);
  }

  public void setTimeOfDownload(long timeOfDownload) {
    cdbResponseSlot.setTimeOfDownload(timeOfDownload);
  }

  /**
   * Returns the URL of the AJS creative to load for displaying the ad.
   * <p>
   * Non null after validation through {@link #isValid()}
   *
   * @return display URL
   */
  public String getDisplayUrl() {
    return cdbResponseSlot.getDisplayUrl();
  }

  @Nullable
  public NativeAssets getNativeAssets() {
    return cdbResponseSlot.getNativeAssets();
  }

  public boolean isExpired(@NonNull Clock clock) {
    return cdbResponseSlot.isExpired(clock);
  }

  @NonNull
  @Override
  public String toString() {
    return cdbResponseSlot.toString();
  }

  public boolean isValid() {
    return cdbResponseSlot.isValid();
  }

  public Double getCpmAsNumber() {
    return cdbResponseSlot.getCpmAsNumber();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Slot) {
      Slot other = (Slot) obj;
      return cdbResponseSlot.equals(other.cdbResponseSlot);
    }
    return false;
  }

}
