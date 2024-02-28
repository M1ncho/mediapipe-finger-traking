package com.blifeinc.pinch_game.http

data class NewMember (
        var number: String,
        var patient_number: String,
        var severity_level: String,
        var right_finger_length: Float,
        var left_finger_length: Float
)