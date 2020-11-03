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

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class EmailHasherTest {

  @Test
  fun hash_GivenExampleWithSpaceAndCaps_ReturnsSameThanWhenTrimmedAndLowered() {
    val expected = "000e3171a5110c35c69d060112bd0ba55d9631c7c2ec93f1840e4570095b263a"

    val hash1 = EmailHasher.hash("john.doe@gmail.com")
    val hash2 = EmailHasher.hash(" john.doe@gmail.com ")
    val hash3 = EmailHasher.hash("John.Doe@gmail.com")
    val hash4 = EmailHasher.hash(" John.Doe@gmail.com ")

    assertThat(hash1).isEqualTo(expected)
    assertThat(hash2).isEqualTo(expected)
    assertThat(hash3).isEqualTo(expected)
    assertThat(hash4).isEqualTo(expected)
  }

  @Test
  fun hash_GivenGermanEmailAddresses_ReturnsSameThanWhenTrimmedAndLowered() {
    val hash1 = EmailHasher.hash("Dörte@Sörensen.example.com")
    val hash2 = EmailHasher.hash(" dörte@sÖrensen.example.com ")

    assertThat(hash1).isEqualTo(hash2)
  }

  @Test
  fun hash_GivenRussianEmailAddresses_ReturnsSameThanWhenTrimmedAndLowered() {
    val hash1 = EmailHasher.hash("коля@пример.рф")
    val hash2 = EmailHasher.hash(" КОЛЯ@ПРИМЕР.РФ ")

    assertThat(hash1).isEqualTo(hash2)
  }

  @Test
  fun hash_GivenGreekEmailAddresses_ReturnsSameThanWhenTrimmedAndLowered() {
    val hash1 = EmailHasher.hash("χρήστης@παράδειγμα.ελ")
    val hash2 = EmailHasher.hash(" ΧΡήστΗς@πΑράδειγμΑ.ελ ")

    assertThat(hash1).isEqualTo(hash2)
  }
}
