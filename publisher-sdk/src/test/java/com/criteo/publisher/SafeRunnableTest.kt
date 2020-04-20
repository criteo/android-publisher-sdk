package com.criteo.publisher

import com.criteo.publisher.mock.MockedDependenciesRule
import com.criteo.publisher.mock.SpyBean
import com.criteo.publisher.util.BuildConfigWrapper
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThatCode
import org.junit.Rule
import org.junit.Test

class SafeRunnableTest {
    @Rule
    @JvmField
    var mockedDependenciesRule = MockedDependenciesRule()

    @SpyBean
    private lateinit var buildConfigWrapper: BuildConfigWrapper

    @Test
    fun dontThrowInProduction() {
        doReturn(false).whenever(buildConfigWrapper).preconditionThrowsOnException()

        val safeRunnable = object: SafeRunnable() {
            override fun runSafely() {
                throw RuntimeException()
            }
        }

        assertThatCode { safeRunnable.run() }.doesNotThrowAnyException()
    }
}