package com.blifeinc.pinch_game.http

import java.time.LocalDateTime
import java.util.*

data class FingerDataDetail (
        var time: String,
        var thumb_x: Double,
        var thumb_y: Double,
        var thumb_z: Double,
        var index_x: Double,
        var index_y: Double,
        var index_z: Double
)