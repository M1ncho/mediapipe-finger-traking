package com.blifeinc.pinch_game

import android.app.Dialog
import android.content.Context
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.blifeinc.pinch_game.util.SaveSettingUtil

class HandSelectDialog(val context: Context, val leftYn: Boolean, val rightYn: Boolean) {

    val dialog = Dialog(context)
    var selectHand = -1


    fun showDialog() {
        dialog.setContentView(R.layout.handselect_dialog)
        dialog.window!!.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT)

        dialog.setCanceledOnTouchOutside(true)
        dialog.setCancelable(true)
        dialog.show()

        val btnCheck = dialog.findViewById<Button>(R.id.btn_select_hand)
        val tvLeft = dialog.findViewById<TextView>(R.id.tv_left_hand)
        val tvRight = dialog.findViewById<TextView>(R.id.tv_right_hand)
        val closeArea = dialog.findViewById<RelativeLayout>(R.id.rl_close)


        // data 유무 표시
        if (leftYn) {
            tvLeft.setBackgroundResource(R.drawable.round_line_blue_darkgreyblue_layout)
            tvLeft.setTextColor(ContextCompat.getColor(context, R.color.white))
        }
        if (rightYn) {
            tvRight.setBackgroundResource(R.drawable.round_line_blue_darkgreyblue_layout)
            tvRight.setTextColor(ContextCompat.getColor(context, R.color.white))
        }


        // click event
        tvLeft.setOnClickListener {
            if (!leftYn) {
                selectHand = 1
                SaveSettingUtil.setHandType(context, 1)
                handSelect(tvLeft, tvRight)
            }
        }

        tvRight.setOnClickListener {
            if (!rightYn) {
                selectHand = 0
                SaveSettingUtil.setHandType(context, 0)
                handSelect(tvLeft, tvRight)
            }
        }

        btnCheck.setOnClickListener {
            if (selectHand != -1) {
                SaveSettingUtil.setSelectYn(context, 1)

                // activity 가져오기
                val trackingActivity = HandTrackingActivity.getInstance()
                trackingActivity!!.changeBtnText()

                dialog.dismiss()
            }
        }

        closeArea.setOnClickListener {
            SaveSettingUtil.setSelectYn(context, 0)
            dialog.dismiss()
        }
    }



    // 왼손 - 1, 오른손 - 0
    fun handSelect(tvLeft: TextView, tvRight: TextView) {
        if (selectHand == 1) {
            tvLeft.setBackgroundResource(R.drawable.round_line_blue_skyblue_layout)
            tvLeft.setTextColor(ContextCompat.getColor(context, R.color.black))

            if (rightYn) {
                tvRight.setBackgroundResource(R.drawable.round_line_blue_darkgreyblue_layout)
                tvRight.setTextColor(ContextCompat.getColor(context, R.color.white))
            }
            else {
                tvRight.setBackgroundResource(R.drawable.round_line_blue_layout)
                tvRight.setTextColor(ContextCompat.getColor(context, R.color.black))
            }
        }
        else if (selectHand == 0) {
            tvRight.setBackgroundResource(R.drawable.round_line_blue_skyblue_layout)
            tvRight.setTextColor(ContextCompat.getColor(context, R.color.black))

            if (leftYn) {
                tvLeft.setBackgroundResource(R.drawable.round_line_blue_darkgreyblue_layout)
                tvLeft.setTextColor(ContextCompat.getColor(context, R.color.white))
            }
            else {
                tvLeft.setBackgroundResource(R.drawable.round_line_blue_layout)
                tvLeft.setTextColor(ContextCompat.getColor(context, R.color.black))
            }
        }
    }



}