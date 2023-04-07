package com.blifeinc.pinch_game.http

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface MemberService {

    // 목록 가져오기
    @GET("api/member/list")
    fun getMemberList(): Call<MemberList>

    // 등록
    @POST("api/member/add")
    fun addMember(@Body number: NewMember): Call<ResultResponse>

    // 중복 여부 확인
    @GET("api/member/check_number")
    fun checkNumber(@Query("number") checkNum: String): Call<SendResult>

}