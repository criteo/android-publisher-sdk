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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.criteo.publisher.model.AbstractTokenValue;
import com.criteo.publisher.model.AdUnit;
import com.criteo.publisher.util.AdUnitType;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TokenCache {

  private final Map<BidResponse, AbstractTokenValue> tokenMap;

  public TokenCache() {
    tokenMap = new ConcurrentHashMap<>();
  }

  public void add(@NonNull BidResponse bidResponse, @NonNull AbstractTokenValue tokenValue) {
    tokenMap.put(bidResponse, tokenValue);
  }

  @Nullable
  public AbstractTokenValue getTokenValue(@NonNull BidResponse bidResponse, @NonNull AdUnitType expectedType) {
    if (bidResponse.getAdUnitType() != expectedType) {
      return null;
    }

    AbstractTokenValue tokenValue = tokenMap.remove(bidResponse);
    if (tokenValue == null || tokenValue.isExpired()) {
      return null;
    }

    return tokenValue;
  }

}
