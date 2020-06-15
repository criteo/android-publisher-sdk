package com.criteo.publisher.model

import com.criteo.publisher.mock.MockedDependenciesRule
import com.criteo.publisher.util.JsonSerializer
import com.criteo.publisher.util.writeIntoString
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

class RemoteConfigRequestTest {

  @Rule
  @JvmField
  val mockedDependenciesRule = MockedDependenciesRule()

  @Inject
  private lateinit var serializer: JsonSerializer

  @Test
  fun write_GivenData_ReturnSerializedJson() {
    val request = RemoteConfigRequest.create(
        "myCpId",
        "my.bundle.id",
        "1.2.3"
    )

    val json = serializer.writeIntoString(request)

    assertThat(json).isEqualToIgnoringWhitespace("""
      {
        "cpId" : "myCpId",
        "bundleId" : "my.bundle.id",
        "sdkVersion" : "1.2.3"
      }
    """.trimIndent())
  }

}