package com.criteo.publisher.model;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

public class Publisher implements Parcelable {

    private static final String BUNDLE_ID = "bundleId";
    private static final String NETWORK_ID = "networkId";
    private String bundleId;
    private int networkId;

    public Publisher(Context context) {
        bundleId = context.getApplicationContext().getPackageName();
    }

    public String getBundleId() {
        return bundleId;
    }

    public void setBundleId(String bundleId) {
        this.bundleId = bundleId;
    }

    public int getNetworkId() {
        return networkId;
    }

    public void setNetworkId(int networkId) {
        this.networkId = networkId;
    }

    public JSONObject toJson() throws JSONException {
        JSONObject json = new JSONObject();
        json.put(BUNDLE_ID, bundleId);
        if (networkId > 0) {
            json.put(NETWORK_ID, networkId);
        }
        return json;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.bundleId);
        dest.writeInt(this.networkId);
    }

    protected Publisher(Parcel in) {
        this.bundleId = in.readString();
        this.networkId = in.readInt();
    }

    public static final Parcelable.Creator<Publisher> CREATOR = new Parcelable.Creator<Publisher>() {
        @Override
        public Publisher createFromParcel(Parcel source) {
            return new Publisher(source);
        }

        @Override
        public Publisher[] newArray(int size) {
            return new Publisher[size];
        }
    };
}
