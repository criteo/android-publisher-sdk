package com.criteo.publisher

import com.criteo.publisher.logging.Logger
import com.criteo.publisher.logging.LoggerFactory
import com.criteo.publisher.mock.MockedDependenciesRule
import com.criteo.publisher.mock.SpyBean
import com.criteo.publisher.util.BuildConfigWrapper
import com.nhaarman.mockitokotlin2.*
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatCode
import org.junit.Rule
import org.junit.Test
import java.io.IOException

class SafeRunnableTest {
    @Rule
    @JvmField
    var mockedDependenciesRule = MockedDependenciesRule()

    @SpyBean
    private lateinit var buildConfigWrapper: BuildConfigWrapper

    @SpyBean
    private lateinit var loggerFactory: LoggerFactory

    @Test
    fun dontThrowInProduction() {
        doReturn(false).whenever(buildConfigWrapper).preconditionThrowsOnException()

        val safeRunnable = createThrowingRunnable(RuntimeException())

        assertThatCode { safeRunnable.run() }.doesNotThrowAnyException()
    }

    @Test
    fun givenCheckedException_DontThrowInDebugButLogIt() {
        val logger = mock<Logger>()
        doReturn(logger).whenever(loggerFactory).createLogger(any())
        doReturn(true).whenever(buildConfigWrapper).preconditionThrowsOnException()

        val throwable = IOException()
        val safeRunnable = createThrowingRunnable(IOException())

        assertThatCode { safeRunnable.run() }.doesNotThrowAnyException()

        verify(logger).error(check {
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