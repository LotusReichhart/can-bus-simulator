package com.lotusreichhart.canbussimulator.data.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "can_frames",
    indices = [
        Index(value = ["can_id"]),
        Index(value = ["timestamp"])
    ]
)
data class CanFrameEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "can_id") val canId: Int,
    @ColumnInfo(name = "data", typeAffinity = ColumnInfo.BLOB) val data: ByteArray,
    @ColumnInfo(name = "timestamp") val timestamp: Long
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CanFrameEntity

        if (id != other.id) return false
        if (canId != other.canId) return false
        if (!data.contentEquals(other.data)) return false
        if (timestamp != other.timestamp) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + canId
        result = 31 * result + data.contentHashCode()
        result = 31 * result + timestamp.hashCode()
        return result
    }
}
