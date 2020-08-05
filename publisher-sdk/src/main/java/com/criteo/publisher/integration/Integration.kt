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

package com.criteo.publisher.integration

enum class Integration(val profileId: Int) {
  FALLBACK(235),

  STANDALONE(295),
  IN_HOUSE(296),

  // Mediation
  MOPUB_MEDIATION(297),
  ADMOB_MEDIATION(298),

  // AppBidding
  MOPUB_APP_BIDDING(299),
  GAM_APP_BIDDING(300),
  CUSTOM_APP_BIDDING(301)
}
