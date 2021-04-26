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

package com.criteo.publisher.headerbidding

import android.os.Bundle
import com.google.android.gms.ads.doubleclick.PublisherAdRequest
import java.util.function.Consumer

class Dfp19HeaderBiddingRetroCompatTest: AbstractDfpHeaderBiddingTest() {

  override fun versionName(): String = "AdMob19"

  override fun newBuilder(): Any = PublisherAdRequest.Builder()

  override fun customTargetingFrom(action: Consumer<Any>): Bundle {
    val builder = PublisherAdRequest.Builder()
    action.accept(builder)
    return builder.build().customTargeting
  }

}