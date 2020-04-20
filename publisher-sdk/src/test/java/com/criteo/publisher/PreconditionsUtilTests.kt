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
