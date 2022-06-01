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

import com.criteo.publisher.SafeRunnable
import com.criteo.publisher.dependency.SdkServiceLifecycle
import com.criteo.publisher.network.PubSdkApi
import java.util.concurrent.Executor

class ConfigManager(
    private val config: Config,
    private val remoteConfigRequestFactory: RemoteConfigRequestFactory,
    private val api: PubSdkApi,
    private val executor: Executor
) : SdkServiceLifecycle {

  /**
   * Asynchronously send a remote config request and update the given config.
   * <p>
   * If no error occurs during the request, the given configuration is updated. Else, it is left
   * unchanged.
   */
  override fun onSdkInitialized() {
    executor.execute(RemoteConfigCall())
  }

  private inner class RemoteConfigCall : SafeRunnable() {
    override fun runSafely() {
      val request: RemoteConfigRequest = remoteConfigRequestFactory.createRequest()
      val response: RemoteConfigResponse = api.loadConfig(request)
      config.refreshConfig(response)
    }
  }
}
