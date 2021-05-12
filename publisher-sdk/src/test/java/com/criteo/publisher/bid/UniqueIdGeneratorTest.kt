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

package com.criteo.publisher.bid

import com.criteo.publisher.EpochClock
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.kotlin.mock
import java.util.UUID

class UniqueIdGeneratorTest {

  /**
   * Those are values taken from CDB unit test on their generator.
   * They are reused to confirm that our generator yield the same output.
   */
  @Test
  fun generateId_GivenDeterministicInputFromCdb_ReturnExpectedOutputFromCdb() {
    val uuid = UUID.fromString("c60e5638-ce73-4c42-a7a1-33c2fff509e4")
    val timestamp = 1234567890L

    val generator = UniqueIdGenerator(mock())
    val id = generator.generateId(uuid, timestamp)

    assertThat(id).isEqualTo("499602d2ce73cc4267a133c2fff509e4");
  }

  @Test
  fun generateId_GivenRealClockAndManyGenerated_AllAreUnique() {
    repeat(1000) {
      val expectedSize = 1000

      val generator = UniqueIdGenerator(EpochClock())

      val ids = (0 until expectedSize)
          .map { generator.generateId() }
          .toSet()

      assertThat(ids).hasSize(expectedSize)
    }
  }

}