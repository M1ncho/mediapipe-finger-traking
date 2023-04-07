package com.blifeinc.pinch_game

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blifeinc.pinch_game.http.*
import com.blifeinc.pinch_game.util.SaveSettingUtil
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegistrationDialog(context: Context) {

    val dialog = Dialog(context)

    var number: String = ""
    var patientNum: String = ""


    fun showDialog() {
        dialog.setContentView(R.layout.registration_dialog)
        dialog.window!!.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT)

        dialog.setCanceledOnTouchOutside(true)
        dialog.setCancelable(true)
        dialog.show()


        val btnRegistar = dialog.findViewById<Button>(R.id.btn_registra)
        val edNumber = dialog.findViewById<EditText>(R.id.ed_number)
        val edPatient = dialog.findViewById<EditText>(R.id.ed_patient)
        val closeArea = dialog.findViewById<RelativeLayout>(R.id.rl_close)


        closeArea.setOnClickListener {
            dialog.dismiss()
        }


        btnRegistar.setOnClickListener {
            number = edNumber.text.toString()
            patientNum = edPatient.text.toString()

            checkNumber(number)
        }

    }




    // 중복여부 api
    fun checkNumber(num: String) {
        val server = FingertappingClient.instance().create(MemberService::class.java)
        val numChecked= server.checkNumber(num)

        numChecked.enqueue(object : Callback<SendResult> {
            override fun onResponse(call: Call<SendResult>, response: Response<SendResult>) {
                if (response.isSuccessful) {
                    val result = response.body()!!.result
                    Log.d("중복 체크", "$result")

                    if (!result) {
                        addMember(number, patientNum)
                    }
                    else {
                        val tvWarning = dialog.findViewById<TextView>(R.id.tv_warning)
                        tvWarning.visibility = View.VISIBLE
                    }
                }
            }

            override fun onFailure(call: Call<SendResult>, t: Throwable) {
                Log.d("Member Add Failed", "${t.message}")
                dialog.dismiss()
            }
        })
    }


    // 등록 api
    fun addMember(num: String, patient: String) {
        var saveData = NewMember(
                number = num,
                patient_number = patient
        )

        val server = FingertappingClient.instance().create(MemberService::class.java)
        val addsMember = server.addMember(saveData)

        addsMember.enqueue(object : Callback<ResultResponse> {
            override fun onResponse(call: Call<ResultResponse>, response: Response<ResultResponse>) {
                if (response.isSuccessful) {
                    val id = response.body()!!.member_id
                    SaveSettingUtil.setMemberId(dialog.context, id)

                    val intent = Intent(dialog.context, HandTrackingActivity::class.java)
                    dialog.context.startActivity(intent)
                }
            }
            override fun onFailure(call: Call<ResultResponse>, t: Throwable) {
                Log.d("Member Add Failed", "${t.message}")
                dialog.dismiss()
            }
        })
    }



}