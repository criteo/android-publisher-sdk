package com.criteo.publisher.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Cdb implements Parcelable {
    private static final String TAG = Cdb.class.getSimpleName();
    private static final String PUBLISHER = "publisher";
    private static final String USER = "user";
    private static final String SDK_VERSION = "sdkVersion";
    private static final String PROFILE_ID = "profileId";
    private static final String GDPR_CONSENT = "gdprConsent";
    private static final String TIME_TO_NEXT_CALL = "timeToNextCall";
    private static final String SLOTS = "slots";
    private List<Slot> slots;
    private List<CacheAdUnit> cacheAdUnits;
    private Publisher publisher;
    private User user;
    private String sdkVersion;
    private int profileId;
    private int timeToNextCall;
    private JSONObject gdprConsent;

    public Cdb() {
        slots = new ArrayList<>();
    }

    public Cdb(JSONObject json) {
        if (json != null && json.has(SLOTS)) {
            JSONArray array = new JSONArray();
            try {
                array = json.getJSONArray(SLOTS);
            } catch (JSONException ex) {
                Log.d(TAG, "Exception while reading slots array" + ex.getMessage());
            }
            slots = new ArrayList<>();
            for (int i = 0; i < array.length(); i++) {
                try {
                    JSONObject slotStr = array.getJSONObject(i);
                    slots.add(new Slot(slotStr));
                } catch (JSONException ex) {
                    Log.d(TAG, "Exception while reading slot from slots array" + ex.getMessage());
                    ex.printStackTrace();
                }
            }
        }
    }

    public JSONObject getGdprConsent() {
        return gdprConsent;
    }

    public void setGdprConsent(JSONObject gdprConsent) {
        this.gdprConsent = gdprConsent;
    }

    public void addSlot(Slot slot) {
        this.slots.add(slot);
    }

    public List<Slot> getSlots() {
        return slots;
    }

    public void setSlots(ArrayList<Slot> slots) {
        this.slots = slots;
    }

    public List<CacheAdUnit> getCacheAdUnits() {
        return cacheAdUnits;
    }

    public void setCacheAdUnits(ArrayList<CacheAdUnit> cacheAdUnits) {
        this.cacheAdUnits = cacheAdUnits;
    }

    public Publisher getPublisher() {
        return publisher;
    }

    public void setPublisher(Publisher publisher) {
        this.publisher = publisher;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getSdkVersion() {
        return sdkVersion;
    }

    public void setSdkVersion(String sdkVersion) {
        this.sdkVersion = sdkVersion;
    }

    public int getProfileId() {
        return profileId;
    }

    public void setProfileId(int profileId) {
        this.profileId = profileId;
    }


    public int getTimeToNextCall() {
        return timeToNextCall;
    }

    public void setTimeToNextCall(int timeToNextCall) {
        this.timeToNextCall = timeToNextCall;
    }


    public JSONObject toJson() throws JSONException {
        JSONObject json = new JSONObject();
        if (user != null) {
            json.put(USER, user.toJson());
        }
        if (publisher != null) {
            json.put(PUBLISHER, publisher.toJson());
        }
        json.put(SDK_VERSION, sdkVersion);
        json.put(PROFILE_ID, profileId);

        JSONArray jsonAdUnits = new JSONArray();
        for (CacheAdUnit cacheAdUnit : cacheAdUnits) {
            jsonAdUnits.put(cacheAdUnit.toJson());
        }
        if (jsonAdUnits.length() > 0) {
            json.put(SLOTS, jsonAdUnits);
        }
        if (gdprConsent != null) {
            json.put(GDPR_CONSENT, gdprConsent);
        }
        return json;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(this.slots);
        dest.writeParcelable(this.publisher, flags);
        dest.writeParcelable(this.user, flags);
        dest.writeString(this.sdkVersion);
        dest.writeInt(this.profileId);
    }

    protected Cdb(Parcel in) {
        this.slots = in.createTypedArrayList(Slot.CREATOR);
        this.publisher = in.readParcelable(Publisher.class.getClassLoader());
        this.user = in.readParcelable(User.class.getClassLoader());
        this.sdkVersion = in.readString();
        this.profileId = in.readInt();
    }

    public static final Creator<Cdb> CREATOR = new Creator<Cdb>() {
        @Override
        public Cdb createFromParcel(Parcel source) {
            return new Cdb(source);
        }

        @Override
        public Cdb[] newArray(int size) {
            return new Cdb[size];
        }
    };
}
