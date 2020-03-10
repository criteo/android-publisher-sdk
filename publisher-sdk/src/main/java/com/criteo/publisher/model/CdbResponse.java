package com.criteo.publisher.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
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
  private final List<Slot> slots;

  private final int timeToNextCall;

  public CdbResponse(@NonNull JSONObject json) {
    int timeToNextCall = 0;
    slots = new ArrayList<>();

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
          slots.add(new Slot(slotStr));
        } catch (Exception ex) {
          Log.d(TAG, "Exception while reading slot from slots array" + ex.getMessage());
        }
      }
    }

    this.timeToNextCall = timeToNextCall;
  }

  @Nullable
  public static CdbResponse fromJson(@Nullable JSONObject json) {
    if (json == null) {
      return null;
    }
    return new CdbResponse(json);
  }

  @NonNull
  public List<Slot> getSlots() {
    return slots;
  }

  public int getTimeToNextCall() {
    return timeToNextCall;
  }

}
