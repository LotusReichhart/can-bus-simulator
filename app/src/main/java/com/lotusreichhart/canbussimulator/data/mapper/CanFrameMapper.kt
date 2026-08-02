package com.lotusreichhart.canbussimulator.data.mapper

import com.lotusreichhart.canbussimulator.data.database.CanFrameEntity
import com.lotusreichhart.canbussimulator.domain.model.CanFrame

fun CanFrameEntity.toDomain(): CanFrame {
    return CanFrame(
        canId = this.canId,
        data = this.data,
        timestamp = this.timestamp
    )
}

fun CanFrame.toEntity(): CanFrameEntity {
    return CanFrameEntity(
        canId = this.canId,
        data = this.data,
        timestamp = this.timestamp
    )
}
