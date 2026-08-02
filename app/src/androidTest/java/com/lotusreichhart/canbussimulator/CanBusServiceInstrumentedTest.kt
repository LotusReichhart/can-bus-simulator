package com.lotusreichhart.canbussimulator

import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ServiceTestRule
import com.lotusreichhart.canbussimulator.service.CanBusService
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeoutException

@RunWith(AndroidJUnit4::class)
class CanBusServiceInstrumentedTest {

    @get:Rule
    val serviceRule = ServiceTestRule()

    @Test
    @Throws(TimeoutException::class)
    fun testServiceBindingAndStreaming() {
        val serviceIntent = Intent(
            ApplicationProvider.getApplicationContext<Context>(),
            CanBusService::class.java
        )

        val binder: IBinder = serviceRule.bindService(serviceIntent)
        val service = (binder as CanBusService.LocalBinder).getService()
        assertNotNull(service)

        runBlocking {
            withTimeout(3000) {
                val frame = service.canFrameFlow.first()
                assertNotNull(frame)
                assertTrue(frame.canId in listOf(0x101, 0x102, 0x103))
                assertTrue(frame.data.isNotEmpty())
            }
        }
    }
}
