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
    val request = RemoteConfigRequest(
        "myCpId",
        null,
        "my.bundle.id",
        "1.2.3",
        456
    )

    val json = serializer.writeIntoString(request)

    assertThat(json).isEqualToIgnoringWhitespace(
        """
      {
        "cpId" : "myCpId",
        "bundleId" : "my.bundle.id",
        "sdkVersion" : "1.2.3",
        "rtbProfileId": 456,
        "deviceOs": "android"
      }
    """.trimIndent())
  }

  @Test
  fun write_GivenData_ReturnSerializedJsonWithInventoryGroupId() {
    val request = RemoteConfigRequest(
      "myCpId",
      "myInventoryGroupId",
      "my.bundle.id",
      "1.2.3",
      456
    )

    val json = serializer.writeIntoString(request)

    assertThat(json).isEqualToIgnoringWhitespace(
      """
      {
        "cpId" : "myCpId",
        "inventoryGroupId": "myInventoryGroupId",
        "bundleId" : "my.bundle.id",
        "sdkVersion" : "1.2.3",
        "rtbProfileId": 456,
        "deviceOs": "android"
      }
    """.trimIndent())
  }
}
