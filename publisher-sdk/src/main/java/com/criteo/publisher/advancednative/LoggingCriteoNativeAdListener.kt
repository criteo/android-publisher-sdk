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
package com.criteo.publisher.advancednative

import com.criteo.publisher.CriteoErrorCode
import com.criteo.publisher.advancednative.NativeLogMessage.onNativeClicked
import com.criteo.publisher.advancednative.NativeLogMessage.onNativeFailedToLoad
import com.criteo.publisher.advancednative.NativeLogMessage.onNativeImpressionRegistered
import com.criteo.publisher.advancednative.NativeLogMessage.onNativeLoaded
import com.criteo.publisher.logging.LoggerFactory
import java.lang.ref.Reference

class LoggingCriteoNativeAdListener(
    private val delegate: CriteoNativeAdListener,
    private val nativeLoaderRef: Reference<CriteoNativeLoader>
) : CriteoNativeAdListener by delegate {

  private val logger = LoggerFactory.getLogger(javaClass)

  override fun onAdReceived(nativeAd: CriteoNativeAd) {
    logger.log(onNativeLoaded(nativeLoaderRef.get()))
    delegate.onAdReceived(nativeAd)
  }

  override fun onAdFailedToReceive(errorCode: CriteoErrorCode) {
    logger.log(onNativeFailedToLoad(nativeLoaderRef.get()))
    delegate.onAdFailedToReceive(errorCode)
  }

  override fun onAdImpression() {
    logger.log(onNativeImpressionRegistered(nativeLoaderRef.get()))
    delegate.onAdImpression()
  }
  override fun onAdClicked() {
    logger.log(onNativeClicked(nativeLoaderRef.get()))
    delegate.onAdClicked()
  }
}
