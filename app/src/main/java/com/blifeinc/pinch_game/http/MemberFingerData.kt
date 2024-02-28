package com.blifeinc.pinch_game.http

data class MemberFingerData (
    var finger_data_id: Int,
    var member_id: Int,
    var tapping_number: Int,
    var tapping_max_height: Int,
    var hand_type: Int,
    var times: String,
    var heights: String,
    var round: Int,
    var created_at: String,
    var update_at: String
)