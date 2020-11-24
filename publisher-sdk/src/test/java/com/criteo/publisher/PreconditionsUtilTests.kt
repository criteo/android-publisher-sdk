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

import com.criteo.publisher.logging.Logger
import com.criteo.publisher.mock.MockBean
import com.criteo.publisher.mock.MockedDependenciesRule
import com.criteo.publisher.util.BuildConfigWrapper
import com.criteo.publisher.util.PreconditionsUtil
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.stub
import com.nhaarman.mockitokotlin2.verify
import org.assertj.core.api.Assertions.assertThatCode
import org.junit.Rule
import org.junit.Test

class PreconditionsUtilTests {

    @Rule
    @JvmField
    var mockedDependenciesRule = MockedDependenciesRule().withSpiedLogger()

    @MockBean
    private lateinit var buildConfigWrapper: BuildConfigWrapper

    private val logger: Logger get() = mockedDependenciesRule.spiedLogger!!

    @Test
    fun givenDebug_RuntimeExceptionShouldBeThrown() {
        val exception = Exception("")
        givenDebugMode(true)

        assertThatCode {
            PreconditionsUtil.throwOrLog(exception)
        }.isInstanceOf(RuntimeException::class.java)

        verify(logger).error(exception)
    }

    @Test
    fun givenNotDebug_RuntimeExceptionShouldNotBeThrown() {
        val exception = Exception("")
        givenDebugMode(false)

        PreconditionsUtil.throwOrLog(exception)

        verify(logger).error(exception)
    }

    private fun givenDebugMode(isDebugMode: Boolean) {
        buildConfigWrapper.stub {
            on { preconditionThrowsOnException() } doReturn isDebugMode
        }
    }
}
