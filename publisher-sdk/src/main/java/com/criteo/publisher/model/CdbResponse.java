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

import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CdbResponse {

  private static final String TAG = CdbResponse.class.getSimpleName();
  private static final String TIME_TO_NEXT_CALL = "timeToNextCall";
  private static final String SLOTS = "slots";

  @NonNull
  private final List<CdbResponseSlot> slots;

  private final int timeToNextCall;

  public CdbResponse(
      @NonNull List<CdbResponseSlot> slots,
      int timeToNextCall
  ) {
    this.slots = slots;
    this.timeToNextCall = timeToNextCall;
  }

  @NonNull
  public static CdbResponse fromJson(@NonNull JSONObject json) {
    int timeToNextCall = 0;
    List<CdbResponseSlot> slots = new ArrayList<>();

    if (json.has(TIME_TO_NEXT_CALL)) {
      try {
        timeToNextCall = json.getInt(TIME_TO_NEXT_CALL);
      } catch (JSONException ex) {
        Log.d(TAG, "Exception while reading cdb time to next call" + ex.getMessage());
      }
    }

    if (json.has(SLOTS)) {
      JSONArray array = new JSONArray();
      try {
        array = json.getJSONArray(SLOTS);
      } catch (JSONException ex) {
        Log.d(TAG, "Exception while reading slots array" + ex.getMessage());
      }
      for (int i = 0; i < array.length(); i++) {
        try {
          JSONObject slotStr = array.getJSONObject(i);
          slots.add(CdbResponseSlot.fromJson(slotStr));
        } catch (Exception ex) {
          Log.d(TAG, "Exception while reading slot from slots array" + ex.getMessage());
        }
      }
    }

    return new CdbResponse(slots, timeToNextCall);
  }

  @NonNull
  public List<CdbResponseSlot> getSlots() {
    return slots;
  }

  public int getTimeToNextCall() {
    return timeToNextCall;
  }

  @Nullable
  public CdbResponseSlot getSlotByImpressionId(@NonNull String impressionId) {
    for (CdbResponseSlot slot : slots) {
      if (impressionId.equals(slot.getImpressionId())) {
        return slot;
      }
    }
    return null;
  }

  @NonNull
  @Override
  public String toString() {
    return "CdbResponse{" +
        "slots=" + slots +
        ", timeToNextCall=" + timeToNextCall +
        '}';
  }
}
