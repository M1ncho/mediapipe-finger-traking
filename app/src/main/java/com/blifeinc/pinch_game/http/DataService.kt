package com.blifeinc.pinch_game.http

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface DataService {

    // 목록 가져오기
    @POST("api/finger_data/upload")
    fun uploadTapData(@Body tapData: FingerData): Call<SendResult>

    // 해당 멤버 데이터 가져오기
    @GET("api/finger_data/list/{member_id}")
    fun getMemberData(@Path("member_id") member_id: Int): Call<List<MemberFingerData>>

    // 데이터 업로드 - 모든 위치
    @POST("api/finger_3d_data/upload")
    fun upload3DData(@Body data: Finger3DData): Call<SendResult>

}