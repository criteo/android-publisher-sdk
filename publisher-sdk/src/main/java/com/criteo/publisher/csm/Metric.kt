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

package com.criteo.publisher.csm

import com.criteo.publisher.annotation.OpenForTesting
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@OpenForTesting
@JsonClass(generateAdapter = true)
data class Metric(
    val cdbCallStartTimestamp: Long? = null,
    val cdbCallEndTimestamp: Long? = null,
    @Json(name = "cdbCallTimeout")
    val isCdbCallTimeout: Boolean = false,
    @Json(name = "cachedBidUsed")
    val isCachedBidUsed: Boolean = false,
    val elapsedTimestamp: Long? = null,
    /**
     * Uniquely identifies a slot from a CDB request.
     * <p>
     * This ID is generated on client side so we can track the life of this slot during a CDB call.
     * For CSM, this allows studies at slot level.
     */
    val impressionId: String,
    /**
     * Uniquely identifies a CDB bid request.
     * <p>
     * This ID is generated on client side so we can track the life of a request even after a CDB
     * call. All metrics coming from the same request should have the same request group ID. For CSM,
     * this allows studies at request level (for instance to determine the timeout rate vs the number
     * of slots in the same request).
     */
    val requestGroupId: String? = null,

    /**
     * Zone ID that was mapped during bidding to the CDB slot response.
     * <p>
     * This should be present in case of valid bid.
     */
    val zoneId: Int? = null,

    /**
     * Indicate from which integration comes this metric.
     *
     * @see Integration#getProfileId()
     */
    val profileId: Int? = null,
    @Json(name = "readyToSend")
    val isReadyToSend: Boolean = false
) {

  fun toBuilder(): Builder = Builder(this)

  @OpenForTesting
  @Suppress("TooManyFunctions")
  class Builder {
    private var impressionId: String? = null
    private var cdbCallStartTimestamp: Long? = null
    private var cdbCallEndTimestamp: Long? = null
    private var elapsedTimestamp: Long? = null
    private var requestGroupId: String? = null
    private var zoneId: Int? = null
    private var profileId: Int? = null
    private var isCachedBidUsed: Boolean = false
    private var isCdbCallTimeout: Boolean = false
    private var isReadyToSend: Boolean = false

    constructor()

    constructor(source: Metric) {
      this.cdbCallStartTimestamp = source.cdbCallStartTimestamp
      this.cdbCallEndTimestamp = source.cdbCallEndTimestamp
      this.isCdbCallTimeout = source.isCdbCallTimeout
      this.isCachedBidUsed = source.isCachedBidUsed
      this.elapsedTimestamp = source.elapsedTimestamp
      this.impressionId = source.impressionId
      this.requestGroupId = source.requestGroupId
      this.zoneId = source.zoneId
      this.profileId = source.profileId
      this.isReadyToSend = source.isReadyToSend
    }

    fun setCdbCallStartTimestamp(cdbCallStartTimestamp: Long?): Builder {
      this.cdbCallStartTimestamp = cdbCallStartTimestamp
      return this
    }

    fun setCdbCallEndTimestamp(cdbCallEndTimestamp: Long?): Builder {
      this.cdbCallEndTimestamp = cdbCallEndTimestamp
      return this
    }

    fun setCdbCallTimeout(cdbCallTimeout: Boolean): Builder {
      this.isCdbCallTimeout = cdbCallTimeout
      return this
    }

    fun setCachedBidUsed(cachedBidUsed: Boolean): Builder {
      this.isCachedBidUsed = cachedBidUsed
      return this
    }

    fun setElapsedTimestamp(elapsedTimestamp: Long?): Builder {
      this.elapsedTimestamp = elapsedTimestamp
      return this
    }

    fun setImpressionId(impressionId: String): Builder {
      this.impressionId = impressionId
      return this
    }

    fun setRequestGroupId(requestGroupId: String?): Builder {
      this.requestGroupId = requestGroupId
      return this
    }

    fun setZoneId(zoneId: Int?): Builder {
      this.zoneId = zoneId
      return this
    }

    fun setProfileId(profileId: Int?): Builder {
      this.profileId = profileId
      return this
    }

    fun setReadyToSend(readyToSend: Boolean): Builder {
      this.isReadyToSend = readyToSend
      return this
    }

    fun build(): Metric {
      check(this.impressionId != null) { "Missing required properties: impressionId" }

      return Metric(
          impressionId = impressionId!!,
          cdbCallStartTimestamp = cdbCallStartTimestamp,
          cdbCallEndTimestamp = cdbCallEndTimestamp,
          elapsedTimestamp = elapsedTimestamp,
          requestGroupId = requestGroupId,
          zoneId = zoneId,
          profileId = profileId,
          isCachedBidUsed = isCachedBidUsed,
          isCdbCallTimeout = isCdbCallTimeout,
          isReadyToSend = isReadyToSend,
      )
    }
  }

  companion object {
    @JvmStatic
    fun builder(): Builder = Builder()

    @JvmStatic
    fun builder(impressionId: String): Builder = builder().setImpressionId(impressionId)
  }
}
