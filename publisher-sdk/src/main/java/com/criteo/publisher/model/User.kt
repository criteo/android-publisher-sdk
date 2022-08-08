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

package com.criteo.publisher.model

import com.criteo.publisher.annotation.OpenForTesting

@OpenForTesting
data class User(
    val deviceId: String?,

    /**
     * US Privacy consent IAB format (for CCPA)
     */
    val uspIab: String?,

    /**
     * US Privacy optout in binary format (for CCPA)
     */
    val uspOptout: String?,
    val ext: Map<String, Any>
){
    val deviceIdType: String = "gaid"
    val deviceOs: String = "android"
}
