package com.criteo.pubsdk.model;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

public class Slot implements Parcelable {

    private static final String SLOT_ID = "slotId";
    private static final String IMP_ID = "impId";
    private static final String CPM = "cpm";
    private static final String CURRENCY = "currency";
    private static final String CREATIVE = "creative";
    private static final String HEIGHT = "height";
    private static final String WIDTH = "width";
    private static final String PLACEMENT_ID = "placementId";
    private static final String SIZES = "sizes";
    private static final String NATIVE = "isNative";
    private static final String TTL = "ttl";
    private static final int DEFAULT_TTL = 15 * 60 * 1000;
    private static final String DISPLAY_URL = "displayUrl";

    private String slotId;
    private String impId;
    private String cpm;
    private String currency;
    private String creative;
    private int width;
    private int height;
    private String placementId;
    private List<String> sizes;
    private boolean nativeImpression;
    private String displayUrl;
    private int ttl;
    //require for cache
    private long timeOfDownload;

    public Slot() {
        sizes = new ArrayList<>();
        timeOfDownload = System.currentTimeMillis();
    }

    public Slot(JsonObject json) {
        placementId = json.has(PLACEMENT_ID) ? json.get(PLACEMENT_ID).getAsString() : null;
        impId = json.has(IMP_ID) ? json.get(IMP_ID).getAsString() : null;
        slotId = json.has(SLOT_ID) ? json.get(SLOT_ID).getAsString() : null;
        if (json.has(CPM)) {
            JsonPrimitive cpmPrimitive = json.get(CPM).getAsJsonPrimitive();
            if (cpmPrimitive.isString()) {
                cpm = cpmPrimitive.getAsString();
            } else {
                cpm = String.valueOf(cpmPrimitive.getAsFloat());
            }
        } else {
            cpm = "0.0";
        }
        currency = json.has(CURRENCY) ? json.get(CURRENCY).getAsString() : null;
        width = json.has(WIDTH) ? json.get(WIDTH).getAsInt() : 0;
        height = json.has(HEIGHT) ? json.get(HEIGHT).getAsInt() : 0;
        creative = json.has(CREATIVE) ? json.get(CREATIVE).getAsString() : null;
        displayUrl = json.has(DISPLAY_URL) ? json.get(DISPLAY_URL).getAsString() : null;
        sizes = new ArrayList<>();
        ttl = json.has(TTL) ? json.get(TTL).getAsInt() : DEFAULT_TTL;
        timeOfDownload = System.currentTimeMillis();
    }

    public String getPlacementId() {
        return placementId;
    }

    public boolean isNativeImpression() {
        return nativeImpression;
    }

    public void setNativeImpression(boolean nativeImpression) {
        this.nativeImpression = nativeImpression;
    }

    public void setPlacementId(String placementId) {
        this.placementId = placementId;
    }

    public String getImpId() {
        return impId;
    }

    public void setImpId(String impId) {
        this.impId = impId;
    }

    public String getCpm() {
        return cpm;
    }

    public void setCpm(String cpm) {
        this.cpm = cpm;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public List<String> getSizes() {
        return sizes;
    }

    public void setSizes(List<String> sizes) {
        this.sizes = sizes;
    }

    public void addSize(String size) {
        this.sizes.add(size);
    }

    public String getSlotId() {
        return slotId;
    }

    public void setSlotId(String slotId) {
        this.slotId = slotId;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getCreative() {
        return creative;
    }

    public void setCreative(String creative) {
        this.creative = creative;
    }

    public int getTtl() {
        return ttl;
    }

    public void setTtl(int ttl) {
        this.ttl = ttl;
    }

    public long getTimeOfDownload() {
        return timeOfDownload;
    }

    public void setTimeOfDownload(long timeOfDownload) {
        this.timeOfDownload = timeOfDownload;
    }

    public String getDisplayUrl() {
        return displayUrl;
    }

    public void setDisplayUrl(String displayUrl) {
        this.displayUrl = displayUrl;
    }

    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        if (sizes.size() > 0) {
            TypeToken<ArrayList<String>> token = new TypeToken<ArrayList<String>>() {
            };
            Gson gson = new Gson();
            json.addProperty(SIZES, gson.toJson(sizes, token.getType()));
        }
        json.addProperty(PLACEMENT_ID, placementId);
        if (nativeImpression) {
            json.addProperty(NATIVE, nativeImpression);
        }
        return json;
    }

    @Override
    public String toString() {
        return "Slot{" +
                "slotId='" + slotId + '\'' +
                ", impId='" + impId + '\'' +
                ", cpm=" + cpm +
                ", currency='" + currency + '\'' +
                ", creative='" + creative + '\'' +
                ", width=" + width +
                ", height=" + height +
                ", placementId='" + placementId + '\'' +
                ", sizes=" + sizes +
                ", nativeImpression=" + nativeImpression +
                ", displayUrl='" + displayUrl + '\'' +
                ", ttl=" + ttl +
                ", timeOfDownload=" + timeOfDownload +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.slotId);
        dest.writeString(this.impId);
        dest.writeString(this.cpm);
        dest.writeString(this.currency);
        dest.writeString(this.creative);
        dest.writeInt(this.width);
        dest.writeInt(this.height);
        dest.writeString(this.placementId);
        dest.writeStringList(this.sizes);
        dest.writeByte(this.nativeImpression ? (byte) 1 : (byte) 0);
        dest.writeString(this.displayUrl);
        dest.writeInt(this.ttl);
        dest.writeLong(this.timeOfDownload);
    }

    protected Slot(Parcel in) {
        this.slotId = in.readString();
        this.impId = in.readString();
        this.cpm = in.readString();
        this.currency = in.readString();
        this.creative = in.readString();
        this.width = in.readInt();
        this.height = in.readInt();
        this.placementId = in.readString();
        this.sizes = in.createStringArrayList();
        this.nativeImpression = in.readByte() != 0;
        this.displayUrl = in.readString();
        this.ttl = in.readInt();
        this.timeOfDownload = in.readLong();
    }

    public static final Creator<Slot> CREATOR = new Creator<Slot>() {
        @Override
        public Slot createFromParcel(Parcel source) {
            return new Slot(source);
        }

        @Override
        public Slot[] newArray(int size) {
            return new Slot[size];
        }
    };
}