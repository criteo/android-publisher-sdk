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

package com.criteo.publisher.headerbidding;

import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.gms.ads.admanager.AdManagerAdRequest;
import com.google.android.gms.ads.admanager.AdManagerAdRequest.Builder;
import java.util.function.Consumer;

/**
 * This is an instrumented test because DFP use Android objects
 */
public class DfpHeaderBiddingTest extends AbstractDfpHeaderBiddingTest {

  @Override
  @NonNull
  protected Object newBuilder() {
    return new AdManagerAdRequest.Builder();
  }

  @Override
  @NonNull
  protected Bundle customTargetingFrom(@NonNull Consumer<Object> action) {
    AdManagerAdRequest.Builder builder = new Builder();
    action.accept(builder);
    return builder.build().getCustomTargeting();
  }

}