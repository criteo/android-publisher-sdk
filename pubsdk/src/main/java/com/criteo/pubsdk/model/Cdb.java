package com.criteo.pubsdk.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.criteo.pubsdk.Util.SlotDeserializer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

public class Cdb implements Parcelable {
    private static final String PUBLISHER = "publisher";
    private static final String USER = "user";
    private static final String SDK_VERSION = "sdkVersion";
    private static final String PROFILE_ID = "profileId";
    private static final String GDPR_CONSENT = "gdprConsent";
    public static final String SLOTS = "slots";
    private List<Slot> slots;
    private List<AdUnit> adUnits;
    private Publisher publisher;
    private User user;
    private String sdkVersion;
    private int profileId;
    private JsonObject gdprConsent;

    public Cdb() {
        slots = new ArrayList<>();
    }

    public Cdb(JsonObject json) {
        if (json != null && json.has(SLOTS)) {
            try {
                GsonBuilder gsonBuilder = new GsonBuilder();
                gsonBuilder.registerTypeAdapter(Slot.class, new SlotDeserializer());
                TypeToken<ArrayList<Slot>> token = new TypeToken<ArrayList<Slot>>() {
                };
                String slotStr = json.get(SLOTS).toString();
                Gson gson = gsonBuilder.create();
                slots = gson.fromJson(slotStr, token.getType());
            } catch (JsonParseException ex) {
                slots = new ArrayList<>();
            }
        }
    }

    public JsonObject getGdprConsent() {
        return gdprConsent;
    }

    public void setGdprConsent(JsonObject gdprConsent) {
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

    public List<AdUnit> getAdUnits() {
        return adUnits;
    }

    public void setAdUnits(ArrayList<AdUnit> adUnits) {
        this.adUnits = adUnits;
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

    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        if (user != null) {
            json.add(USER, user.toJson());
        }
        if (publisher != null) {
            json.add(PUBLISHER, publisher.toJson());
        }
        json.addProperty(SDK_VERSION, sdkVersion);
        json.addProperty(PROFILE_ID, profileId);

        JsonArray jsonAdUnits = new JsonArray();
        for (AdUnit adUnit : adUnits) {
            jsonAdUnits.add(adUnit.toJson());
        }
        if (jsonAdUnits.size() > 0) {
            json.add(SLOTS, jsonAdUnits);
        }
        if (gdprConsent != null) {
            json.add(GDPR_CONSENT, gdprConsent);
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
        dest.writeTypedList(this.adUnits);
        dest.writeParcelable(this.publisher, flags);
        dest.writeParcelable(this.user, flags);
        dest.writeString(this.sdkVersion);
        dest.writeInt(this.profileId);
    }

    protected Cdb(Parcel in) {
        this.slots = in.createTypedArrayList(Slot.CREATOR);
        this.adUnits = in.createTypedArrayList(AdUnit.CREATOR);
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
