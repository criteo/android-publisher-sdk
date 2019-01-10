package com.criteo.pubsdk.model;

import android.os.Parcel;
import android.os.Parcelable;

public class AdSize implements Parcelable {
    private int hight;
    private int width;

    public int getHight() {
        return hight;
    }

    public void setHight(int hight) {
        this.hight = hight;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.hight);
        dest.writeInt(this.width);
    }

    public AdSize() {
    }

    protected AdSize(Parcel in) {
        this.hight = in.readInt();
        this.width = in.readInt();
    }

    public static final Parcelable.Creator<AdSize> CREATOR = new Parcelable.Creator<AdSize>() {
        @Override
        public AdSize createFromParcel(Parcel source) {
            return new AdSize(source);
        }

        @Override
        public AdSize[] newArray(int size) {
            return new AdSize[size];
        }
    };
}
