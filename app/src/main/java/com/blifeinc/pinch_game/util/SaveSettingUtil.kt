package com.blifeinc.pinch_game.util

import android.content.Context
import com.blifeinc.pinch_game.R

class SaveSettingUtil {

    companion object {
        const val MEMBER_ID = "member_id"
        const val TAPPING_COUNT = "tapping_count"
        const val HAND_TYPE = "hand_type"


        // 선택한 멤버 아이디 저장
        fun setMemberId(context: Context, value: Int) {
            val pref = context.getSharedPreferences(context.getString(R.string.shared_name), Context.MODE_PRIVATE)

            val editor = pref.edit()
            editor.putInt(MEMBER_ID, value)
            editor.apply()
        }

        fun getMemberId(context: Context): Int {
            val pref = context.getSharedPreferences(context.getString(R.string.shared_name), Context.MODE_PRIVATE)

            return pref.getInt(MEMBER_ID, -1)
        }



        // 선택한 손의 위치 저장
        fun setHandType(context: Context, value: Int) {
            val pref = context.getSharedPreferences(context.getString(R.string.shared_name), Context.MODE_PRIVATE)

            val editor = pref.edit()
            editor.putInt(HAND_TYPE, value)
            editor.apply()
        }

        fun getHandType(context: Context): Int {
            val pref = context.getSharedPreferences(context.getString(R.string.shared_name), Context.MODE_PRIVATE)

            return pref.getInt(HAND_TYPE, 0)
        }


    }

}