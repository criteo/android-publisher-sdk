package com.criteo.publisher.model;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

public class Publisher implements Parcelable {

    private static final String BUNDLE_ID = "bundleId";
    // Not changing this string to "cpId" that CDB expects for now
    // this should be done as part of https://jira.criteois.com/browse/EE-235
    private static final String NETWORK_ID = "networkId";
    private String bundleId;
    private int criteoPublisherId;

    public Publisher(Context context) {
        bundleId = context.getApplicationContext().getPackageName();
    }

    public String getBundleId() {
        return bundleId;
    }

    public void setBundleId(String bundleId) {
        this.bundleId = bundleId;
    }

    public int getCriteoPublisherId() {
        return criteoPublisherId;
    }

    public void setCriteoPublisherId(int criteoPublisherId) {
        this.criteoPublisherId = criteoPublisherId;
    }

    public JSONObject toJson() throws JSONException {
        JSONObject json = new JSONObject();
        json.put(BUNDLE_ID, bundleId);
        if (criteoPublisherId > 0) {
            json.put(NETWORK_ID, criteoPublisherId);
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
        dest.writeInt(this.criteoPublisherId);
    }

    protected Publisher(Parcel in) {
        this.bundleId = in.readString();
        this.criteoPublisherId = in.readInt();
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
