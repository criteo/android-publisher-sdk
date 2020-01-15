package com.criteo.publisher.model;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import org.json.JSONException;
import org.json.JSONObject;

public class Publisher implements Parcelable {

    private static final String BUNDLE_ID = "bundleId";
    private static final String CRITEO_PUBLISHER_ID = "cpId";

    @NonNull
    private final String bundleId;

    @NonNull
    private final String criteoPublisherId;

    public Publisher(@NonNull Context context, @NonNull String criteoPublisherId) {
        this.bundleId = context.getPackageName();
        this.criteoPublisherId = criteoPublisherId;
    }

    @NonNull
    public String getBundleId() {
        return bundleId;
    }

    @NonNull
    public String getCriteoPublisherId() {
        return criteoPublisherId;
    }

    public JSONObject toJson() throws JSONException {
        JSONObject json = new JSONObject();
        json.put(BUNDLE_ID, bundleId);
        if (!TextUtils.isEmpty(criteoPublisherId)) {
            json.put(CRITEO_PUBLISHER_ID, criteoPublisherId);
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
        dest.writeString(this.criteoPublisherId);
    }

    protected Publisher(Parcel in) {
        this.bundleId = in.readString();
        this.criteoPublisherId = in.readString();
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
