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
package com.criteo.publisher.adview

interface RedirectionListener {
  /**
   * Callback notified when the user click on the ad view, and is then redirected to the ad.
   */
  fun onUserRedirectedToAd()

  /**
   * Callback notified when the user is back from an ad. This happens generally when user press the
   * back button after being redirected to an ad.
   */
  fun onUserBackFromAd()
}