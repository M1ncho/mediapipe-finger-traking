package com.blifeinc.pinch_game.http

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface DataService {

    // 목록 가져오기
    @POST("api/finger_data/upload")
    fun uploadTapData(@Body tapData: FingerData): Call<SendResult>

}