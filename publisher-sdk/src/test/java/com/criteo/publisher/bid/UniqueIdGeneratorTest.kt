package com.criteo.publisher.bid

import com.criteo.publisher.EpochClock
import com.nhaarman.mockitokotlin2.mock
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.util.*

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