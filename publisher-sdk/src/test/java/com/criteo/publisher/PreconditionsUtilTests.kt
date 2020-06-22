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

package com.criteo.publisher

import com.criteo.publisher.util.BuildConfigWrapper
import com.criteo.publisher.util.PreconditionsUtil
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import org.junit.After
import org.junit.Test

class PreconditionsUtilTests {

    @After
    fun tearDown() {
        DependencyProvider.setInstance(null)
    }

    @Test(expected = RuntimeException::class)
    fun givenDebug_RuntimeExceptionShouldBeThrown() {
        givenMockedDependencyProvider(true)
        PreconditionsUtil.throwOrLog(Exception(""))
    }

    @Test
    fun givenNotDebug_RuntimeExceptionShouldNotBeThrown() {
        givenMockedDependencyProvider(false)
        PreconditionsUtil.throwOrLog(Exception(""))
    }

    private fun givenMockedDependencyProvider(isDebugMode: Boolean) {
        val buildConfigWrapper = mock<BuildConfigWrapper> {
            on { preconditionThrowsOnException() } doReturn isDebugMode
        }

        val dependencyProvider = mock<DependencyProvider> {
            on { provideBuildConfigWrapper() } doReturn buildConfigWrapper
        }

        DependencyProvider.setInstance(dependencyProvider)
    }
}
