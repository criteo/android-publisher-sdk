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
import com.criteo.publisher.mock.MockedDependenciesRule
import com.criteo.publisher.mock.SpyBean
import com.criteo.publisher.util.BuildConfigWrapper
import com.nhaarman.mockitokotlin2.check
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatCode
import org.junit.Rule
import org.junit.Test
import java.net.SocketException

class SafeRunnableTest {
    @Rule
    @JvmField
    var mockedDependenciesRule = MockedDependenciesRule().withSpiedLogger()

    @SpyBean
    private lateinit var buildConfigWrapper: BuildConfigWrapper

    @SpyBean
    private lateinit var logger: Logger

    @Test
    fun dontThrowInProduction() {
        doReturn(false).whenever(buildConfigWrapper).preconditionThrowsOnException()

        val safeRunnable = createThrowingRunnable(RuntimeException())

        assertThatCode { safeRunnable.run() }.doesNotThrowAnyException()
    }

    @Test
    fun givenCheckedException_DontThrowInDebugButLogItInError() {
        doReturn(true).whenever(buildConfigWrapper).preconditionThrowsOnException()

        val throwable = Exception()
        val safeRunnable = createThrowingRunnable(throwable)

        assertThatCode { safeRunnable.run() }.doesNotThrowAnyException()

        verify(logger).log(check {
            assertThat(it.throwable).hasCause(throwable)
        })
    }

    @Test
    fun givenSocketException_LogItInDebug() {
        doReturn(true).whenever(buildConfigWrapper).preconditionThrowsOnException()

        val throwable = SocketException()
        val safeRunnable = createThrowingRunnable(throwable)

        assertThatCode { safeRunnable.run() }.doesNotThrowAnyException()

        verify(logger).debug(check<Throwable> {
            assertThat(it).hasCause(throwable)
        })
    }

    private fun createThrowingRunnable(throwable: Throwable): SafeRunnable {
        return object : SafeRunnable() {
            override fun runSafely() {
                throw throwable
            }
        }
    }
}
