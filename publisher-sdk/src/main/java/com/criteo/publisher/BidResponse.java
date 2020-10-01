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

package com.criteo.publisher;

import androidx.annotation.Keep;
import androidx.annotation.Nullable;
import com.criteo.publisher.util.ObjectUtils;

public class BidResponse {

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

  @Keep
  public double getPrice() {
    return price;
  }

  @Nullable
  public BidToken getBidToken() {
    return token;
  }

  @Keep
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
    return ObjectUtils.equals(token, that.token);
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
