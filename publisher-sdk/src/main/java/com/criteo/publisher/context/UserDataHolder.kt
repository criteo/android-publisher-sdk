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

package com.criteo.publisher.context

import java.util.concurrent.atomic.AtomicReference

/**
 * Indirect reference to the user data to use.
 *
 * The [UserData] can be injected by a publisher. So callers of user data should not retain a direct
 * reference, instead they should always ask this holder for the user data, as it may change
 * during runtime.
 */
internal class UserDataHolder {

  private val valueRef = AtomicReference(UserData())

  fun get(): UserData = valueRef.get()

  fun set(userData: UserData) {
    valueRef.set(userData)
  }
}
