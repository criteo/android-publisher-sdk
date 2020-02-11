package com.criteo.publisher;

import android.support.annotation.Nullable;

public final class BidResponse {

  private final double price;

  @Nullable
  private final BidToken token;

  private final boolean valid;

  protected BidResponse(double price, @Nullable BidToken token, boolean valid) {
    this.price = price;
    this.token = token;
    this.valid = valid;
  }

  protected BidResponse() {
    this.price = 0;
    this.token = null;
    this.valid = false;
  }

  public double getPrice() {
    return price;
  }

  @Nullable
  public BidToken getBidToken() {
    return token;
  }

  public boolean isBidSuccess() {
    return valid;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    BidResponse that = (BidResponse) o;

    if (Double.compare(that.price, price) != 0) {
      return false;
    }
    if (valid != that.valid) {
      return false;
    }
    return token != null ? token.equals(that.token) : that.token == null;
  }

  @Override
  public int hashCode() {
    int result;
    long temp;
    temp = Double.doubleToLongBits(price);
    result = (int) (temp ^ (temp >>> 32));
    result = 31 * result + (token != null ? token.hashCode() : 0);
    result = 31 * result + (valid ? 1 : 0);
    return result;
  }
}
