package com.criteo.pubsdk.model;

import android.os.Parcel;
import android.os.Parcelable;

public class AdUnit implements Parcelable {
    private String placementId;
    private AdSize size;

    public String getPlacementId() {
        return placementId;
    }

    public void setPlacementId(String placementId) {
        this.placementId = placementId;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.placementId);
        dest.writeParcelable(this.size, flags);
    }

    public AdUnit() {
    }

    protected AdUnit(Parcel in) {
        this.placementId = in.readString();
        this.size = in.readParcelable(AdSize.class.getClassLoader());
    }

    public static final Parcelable.Creator<AdUnit> CREATOR = new Parcelable.Creator<AdUnit>() {
        @Override
        public AdUnit createFromParcel(Parcel source) {
            return new AdUnit(source);
        }

        @Override
        public AdUnit[] newArray(int size) {
            return new AdUnit[size];
        }
    };
}
