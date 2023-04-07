package com.blifeinc.pinch_game.http

data class Member(
    var member_id: Int,
    var number: String,
    var patient_number: String,
    var gender: String,
    var birth_date: String,
    var data_count: Int
)