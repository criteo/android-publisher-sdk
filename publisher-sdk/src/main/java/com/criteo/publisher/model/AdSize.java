package com.criteo.publisher.model;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.Objects;

public class AdSize implements Parcelable {

    private int height;
    private int width;

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public String getFormattedSize() {
        return width + "x" + height;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public String toString() {
        return "AdSize{" +
                "height=" + height +
                ", width=" + width +
                '}';
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.height);
        dest.writeInt(this.width);
    }

    public AdSize() {
    }

    protected AdSize(Parcel in) {
        this.height = in.readInt();
        this.width = in.readInt();
    }

    public AdSize(int height, int width) {
        this.height = height;
        this.width = width;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AdSize adSize = (AdSize) o;
        return height == adSize.height &&
                width == adSize.width;
    }

    @Override
    public int hashCode() {
        return Objects.hash(height, width);
    }
}
