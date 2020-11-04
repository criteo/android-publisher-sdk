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

import androidx.annotation.Keep
import java.security.MessageDigest
import java.util.Locale

object EmailHasher {

  /**
   * Helper function to hash emails for [UserData.HASHED_EMAIL]
   *
   * ## Hashing Format
   * The hashing should be the users’ email address:
   * - Encoded in UTF-8
   * - Trimmed of any white space (eg: “test@criteo.com “ should become “test@criteo.com”)
   * - Converted to lower case
   * - Hashed with MD5 & output as ASCII text
   * - Hashed with SHA256 and output as ASCII text
   */
  @Keep
  @JvmStatic
  fun hash(email: String): String {
    return email.trim().toLowerCase(Locale.ROOT).toHash("MD5").toHash("SHA-256")
  }

  private fun String.toHash(type: String): String {
    return MessageDigest.getInstance(type)
        .digest(toByteArray())
        .joinToString("") {
          "%02x".format(it)
        }
  }
}
