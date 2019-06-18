package com.criteo.publisher.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Slot implements Parcelable {

    private static final String TAG = Slot.class.getSimpleName();
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
    private double cpmValue;

    public Slot() {
        sizes = new ArrayList<>();
        timeOfDownload = System.currentTimeMillis();
    }

    public Slot(JSONObject json) {
        placementId = json.optString(PLACEMENT_ID, null);
        impId = json.optString(IMP_ID, null);
        slotId = json.optString(SLOT_ID, null);
        if (json.has(CPM)) {
            try {
                cpm = json.getString(CPM);
            } catch (JSONException e) {
                Log.d(TAG, "Unable to parse CPM " + e.getMessage());
                double cpmInt = json.optDouble(CPM, 0.0);
                cpm = String.valueOf(cpmInt);
            }
        } else {
            cpm = "0.0";
        }
        currency = json.optString(CURRENCY, null);
        width = json.optInt(WIDTH, 0);
        height = json.optInt(HEIGHT, 0);
        creative = json.optString(CREATIVE, null);
        displayUrl = json.optString(DISPLAY_URL, null);
        sizes = new ArrayList<>();
        ttl = json.optInt(TTL, DEFAULT_TTL);
        if (getCpmAsNumber() == null) {
            cpmValue = 0.0;
        }
        if (cpmValue > 0.0 && ttl == 0) {
            ttl = DEFAULT_TTL;
        }
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

    public String getFormattedSize() {
        return width + "x" + height;
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

    public JSONObject toJson() throws JSONException {
        JSONObject json = new JSONObject();
        if (sizes.size() > 0) {
            JSONArray array = new JSONArray();
            for (String size : sizes) {
                array.put(size);
            }
            json.put(SIZES, array);
        }
        json.put(PLACEMENT_ID, placementId);
        if (nativeImpression) {
            json.put(NATIVE, nativeImpression);
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

    /// Checks if displayUrl is not null/empty
    /// and if cpm is a valid number > 0
    public boolean isValid() {
        // Check display Url
        if (this.displayUrl == null || (this.displayUrl.length() == 0)) {
            return false;
        }

        //check cpm
        Double testCpm = this.getCpmAsNumber();
        if (testCpm == null || testCpm < 0.0d) {
            return false;
        }
        return true;
    }

    // Get the cpm as a double
    // Returns null if conversion fails
    // use isValid() to check if Slot is a valid slot
    public Double getCpmAsNumber() {
        try {
            this.cpmValue = Double.parseDouble(getCpm());
        } catch (Exception ex) {
            Log.d(TAG, "CPM is not a valid double " + ex.getMessage());
            return null;
        }
        return this.cpmValue;
    }
}