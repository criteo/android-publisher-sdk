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

package com.criteo.publisher.config

import com.criteo.publisher.concurrent.DirectMockExecutor
import com.criteo.publisher.network.PubSdkApi
import org.assertj.core.api.Assertions.assertThatCode
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.junit.MockitoJUnit
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.io.IOException
import java.util.concurrent.Executor

class ConfigManagerTest {

  @Rule
  @JvmField
  val mockitoRule = MockitoJUnit.rule()

  @Mock
  private lateinit var remoteConfigRequestFactory: RemoteConfigRequestFactory

  @Mock
  private lateinit var api: PubSdkApi

  @Mock
  private lateinit var config: Config

  private var executor = Executor(Runnable::run)

  private lateinit var manager: ConfigManager

  @Before
  fun setUp() {
    givenNewManager()
  }

  @Test
  fun onSdkInitialized_GivenSuccessfulResponse_RefreshConfig() {
    val request = givenMockedCreatedRequest()
    val response = mock<RemoteConfigResponse>()

    whenever(api.loadConfig(request)).doReturn(response)

    manager.onSdkInitialized()

    verify(config).refreshConfig(response)
  }

  @Test
  fun onSdkInitialized_GivenException_DoNotThrow() {
    givenMockedCreatedRequest()
    whenever(api.loadConfig(any())).doThrow(IOException::class)

    assertThatCode {
      manager.onSdkInitialized()
    }.doesNotThrowAnyException()
  }

  @Test
  fun onSdkInitialized_GivenExecutor_IsWorkingInExecutor() {
    givenMockedCreatedRequest()

    val executor = DirectMockExecutor()
    givenNewManager(executor = executor)

    doAnswer {
      executor.expectIsRunningInExecutor()
      mock<RemoteConfigResponse>()
    }.whenever(api).loadConfig(any())

    manager.onSdkInitialized()

    executor.verifyExpectations()
  }

  private fun givenNewManager(executor: Executor = this.executor) {
    manager = ConfigManager(
        config,
        remoteConfigRequestFactory,
        api,
        executor
    )
  }

  private fun givenMockedCreatedRequest(): RemoteConfigRequest {
    val request = mock<RemoteConfigRequest>()
    whenever(remoteConfigRequestFactory.createRequest()).doReturn(request)
    return request
  }
}
