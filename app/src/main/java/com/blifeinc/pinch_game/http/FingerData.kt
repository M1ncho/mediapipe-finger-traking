package com.blifeinc.pinch_game.http

data class FingerData (
    var member_id: Int,
    var tapping_number: Int,
    var hand_type: Int,
    var finger_max_height: Int,
    var finger_data_details: List<FingerDataDetail>,
    var round: Int
)