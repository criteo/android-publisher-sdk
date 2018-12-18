package com.criteo.pubsdk.model;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;

public class Cdb {
    private static final String PUBLISHER = "publisher";
    private static final String USER = "user";
    private static final String SDK_VERSION = "sdkVersion";
    private static final String PROFILE_ID = "profileId";
    public static final String SLOTS = "slots";
    private ArrayList<Slot> slots;
    private Publisher publisher;
    private User user;
    private String sdkVersion;
    private String profileId;

    public Cdb() {
        slots = new ArrayList<Slot>();
    }

    public Cdb(JsonObject json) {
        if (json != null && json.has(SLOTS)) {
            TypeToken<ArrayList<Slot>> token = new TypeToken<ArrayList<Slot>>() {
            };
            String slotStr = json.get(SLOTS).toString();
            slots = new Gson().fromJson(slotStr, token.getType());
        }
    }

    public void addSlot(Slot slot) {
        this.slots.add(slot);
    }

    public ArrayList<Slot> getSlots() {
        return slots;
    }

    public void setSlots(ArrayList<Slot> slots) {
        this.slots = slots;
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

    public String getProfileId() {
        return profileId;
    }

    public void setProfileId(String profileId) {
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

        JsonArray array = new JsonArray();
        for (Slot slot : slots) {
            array.add(slot.toJson());
        }
        if (array.size() > 0) {
            json.add(SLOTS, array);
        }

        return json;

    }
}
