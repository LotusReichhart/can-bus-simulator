package com.lotusreichhart.canbussimulator

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.lotusreichhart.canbussimulator.data.database.CanFrameDao
import com.lotusreichhart.canbussimulator.data.database.CanFrameDatabase
import com.lotusreichhart.canbussimulator.data.database.CanFrameEntity
import com.lotusreichhart.canbussimulator.data.jni.NativeCalculator
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class DatabaseAndJniInstrumentedTest {

    private lateinit var db: CanFrameDatabase
    private lateinit var dao: CanFrameDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, CanFrameDatabase::class.java).build()
        dao = db.canFrameDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun testWriteAndReadCanFrames() = runBlocking {
        val frame1 = CanFrameEntity(
            canId = 0x1A0,
            data = byteArrayOf(10, 20, 30),
            timestamp = 1000L
        )
        val frame2 = CanFrameEntity(
            canId = 0x1B0,
            data = byteArrayOf(40, 50),
            timestamp = 2000L
        )

        dao.insert(frame1)
        dao.insert(frame2)

        val frames = dao.getCanFrames().first()
        assertEquals(2, frames.size)
        assertEquals(0x1B0, frames[0].canId)
        assertArrayEquals(byteArrayOf(40, 50), frames[0].data)
        assertEquals(0x1A0, frames[1].canId)
        assertArrayEquals(byteArrayOf(10, 20, 30), frames[1].data)
    }

    @Test
    fun testNativeCalculatorChecksum() {
        val nativeCalculator = NativeCalculator()
        val data = byteArrayOf(0x01, 0x02, 0x03, 0x04)
        
        val checksum = nativeCalculator.calculate(data)
        
        assertNotNull(checksum)
    }
}
