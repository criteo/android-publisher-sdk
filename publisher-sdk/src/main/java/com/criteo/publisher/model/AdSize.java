/*
 *    Copyright 2020 Criteo
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.criteo.publisher.model;

import android.os.Parcel;
import android.os.Parcelable;

public class AdSize implements Parcelable {

  private int width;
  private int height;

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
        "width=" + width +
        ", height=" + height +
        '}';
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeInt(this.width);
    dest.writeInt(this.height);
  }

  public AdSize() {
  }

  protected AdSize(Parcel in) {
    this.width = in.readInt();
    this.height = in.readInt();
  }

  public AdSize(int width, int height) {
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
    int result = width;
    result = 31 * result + height;
    return result;
  }
}
