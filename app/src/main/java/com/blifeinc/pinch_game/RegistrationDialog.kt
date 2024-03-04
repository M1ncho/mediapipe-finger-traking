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
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blifeinc.pinch_game.http.*
import com.blifeinc.pinch_game.util.SaveSettingUtil
import org.w3c.dom.Text
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegistrationDialog(val context: Context) {

    val dialog = Dialog(context)

    var number: String = ""
    var patientNum: String = ""
    var leftCm: Float = 0f
    var rightCm: Float = 0f
    var severityGrade = 0


    fun showDialog() {
        dialog.setContentView(R.layout.registration_dialog)
        dialog.window!!.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT)

        dialog.setCanceledOnTouchOutside(true)
        dialog.setCancelable(true)
        dialog.show()


        val btnNext = dialog.findViewById<Button>(R.id.btn_next)
        val edNumber = dialog.findViewById<EditText>(R.id.ed_number)
        val edPatient = dialog.findViewById<EditText>(R.id.ed_patient)
        val closeArea = dialog.findViewById<RelativeLayout>(R.id.rl_close)

        val gradeZero = dialog.findViewById<TextView>(R.id.tv_grade_0)
        val gradeOne = dialog.findViewById<TextView>(R.id.tv_grade_1)
        val gradeTwo = dialog.findViewById<TextView>(R.id.tv_grade_2)
        val gradeThree = dialog.findViewById<TextView>(R.id.tv_grade_3)
        val gradeFour = dialog.findViewById<TextView>(R.id.tv_grade_4)
        val gradeFive = dialog.findViewById<TextView>(R.id.tv_grade_5)

        val btnRegister = dialog.findViewById<Button>(R.id.btn_register)
        val edLeftCm = dialog.findViewById<EditText>(R.id.ed_cm_left)
        val edRightCm = dialog.findViewById<EditText>(R.id.ed_cm_right)


        closeArea.setOnClickListener {
            dialog.dismiss()
        }

        btnNext.setOnClickListener {
            number = edNumber.text.toString()
            patientNum = edPatient.text.toString()

            checkNumber(number)
        }


        gradeZero.setOnClickListener {
            selectGrade(0, gradeZero, gradeOne, gradeTwo, gradeThree, gradeFour, gradeFive)
        }

        gradeOne.setOnClickListener {
            selectGrade(1, gradeZero, gradeOne, gradeTwo, gradeThree, gradeFour, gradeFive)
        }

        gradeTwo.setOnClickListener {
            selectGrade(2, gradeZero, gradeOne, gradeTwo, gradeThree, gradeFour, gradeFive)
        }

        gradeThree.setOnClickListener {
            selectGrade(3, gradeZero, gradeOne, gradeTwo, gradeThree, gradeFour, gradeFive)
        }

        gradeFour.setOnClickListener {
            selectGrade(4, gradeZero, gradeOne, gradeTwo, gradeThree, gradeFour, gradeFive)
        }

        gradeFive.setOnClickListener {
            selectGrade(5, gradeZero, gradeOne, gradeTwo, gradeThree, gradeFour, gradeFive)
        }


        btnRegister.setOnClickListener {
            leftCm = edLeftCm.text.toString().toFloat()
            rightCm = edRightCm.text.toString().toFloat()

            addMember(number, patientNum)
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

                    if (!result) {
                        Log.d("중복 체크", "$result")

                        val numberLayout = dialog.findViewById<ConstraintLayout>(R.id.cl_register_num)
                        val dataLayout = dialog.findViewById<ConstraintLayout>(R.id.cl_register_data)

                        numberLayout.visibility = View.INVISIBLE
                        dataLayout.visibility = View.VISIBLE
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
                patient_number = patient,
                severity_level = severityGrade.toString(),
                right_finger_length = rightCm,
                left_finger_length = leftCm
        )

        val server = FingertappingClient.instance().create(MemberService::class.java)
        val addsMember = server.addMember(saveData)

        addsMember.enqueue(object : Callback<ResultResponse> {
            override fun onResponse(call: Call<ResultResponse>, response: Response<ResultResponse>) {
                if (response.isSuccessful) {
                    val id = response.body()!!.member_id
                    SaveSettingUtil.setMemberId(dialog.context, id)

                    val intent = Intent(dialog.context, RoundSelectActivity::class.java)
                    dialog.context.startActivity(intent)

                    dialog.dismiss()
                }
            }
            override fun onFailure(call: Call<ResultResponse>, t: Throwable) {
                Log.d("Member Add Failed", "${t.message}")
                dialog.dismiss()
            }
        })
    }



    // 등급 선택 표시 함수
    fun selectGrade(grade: Int, tvZero: TextView, tvOne: TextView, tvTwo: TextView, tvThree: TextView, tvFour: TextView, tvFive: TextView) {
        severityGrade = grade

        if (grade == 0) {
            tvZero.setBackgroundResource(R.drawable.round_small_blue_btn)
            tvZero.setTextColor(ContextCompat.getColor(context, R.color.white))
            tvOne.setBackgroundResource(R.drawable.round_small_skyblue_btn)
            tvOne.setTextColor(ContextCompat.getColor(context, R.color.black))
            tvTwo.setBackgroundResource(R.drawable.round_small_skyblue_btn)
            tvTwo.setTextColor(ContextCompat.getColor(context, R.color.black))
            tvThree.setBackgroundResource(R.drawable.round_small_skyblue_btn)
            tvThree.setTextColor(ContextCompat.getColor(context, R.color.black))
            tvFour.setBackgroundResource(R.drawable.round_small_skyblue_btn)
            tvFour.setTextColor(ContextCompat.getColor(context, R.color.black))
            tvFive.setBackgroundResource(R.drawable.round_small_skyblue_btn)
            tvFive.setTextColor(ContextCompat.getColor(context, R.color.black))
        }

        else if (grade == 1) {
            tvZero.setBackgroundResource(R.drawable.round_small_skyblue_btn)
            tvZero.setTextColor(ContextCompat.getColor(context, R.color.black))
            tvOne.setBackgroundResource(R.drawable.round_small_blue_btn)
            tvOne.setTextColor(ContextCompat.getColor(context, R.color.white))
            tvTwo.setBackgroundResource(R.drawable.round_small_skyblue_btn)
            tvTwo.setTextColor(ContextCompat.getColor(context, R.color.black))
            tvThree.setBackgroundResource(R.drawable.round_small_skyblue_btn)
            tvThree.setTextColor(ContextCompat.getColor(context, R.color.black))
            tvFour.setBackgroundResource(R.drawable.round_small_skyblue_btn)
            tvFour.setTextColor(ContextCompat.getColor(context, R.color.black))
            tvFive.setBackgroundResource(R.drawable.round_small_skyblue_btn)
            tvFive.setTextColor(ContextCompat.getColor(context, R.color.black))
        }
        else if (grade == 2) {
            tvZero.setBackgroundResource(R.drawable.round_small_skyblue_btn)
            tvZero.setTextColor(ContextCompat.getColor(context, R.color.black))
            tvOne.setBackgroundResource(R.drawable.round_small_skyblue_btn)
            tvOne.setTextColor(ContextCompat.getColor(context, R.color.black))
            tvTwo.setBackgroundResource(R.drawable.round_small_blue_btn)
            tvTwo.setTextColor(ContextCompat.getColor(context, R.color.white))
            tvThree.setBackgroundResource(R.drawable.round_small_skyblue_btn)
            tvThree.setTextColor(ContextCompat.getColor(context, R.color.black))
            tvFour.setBackgroundResource(R.drawable.round_small_skyblue_btn)
            tvFour.setTextColor(ContextCompat.getColor(context, R.color.black))
            tvFive.setBackgroundResource(R.drawable.round_small_skyblue_btn)
            tvFive.setTextColor(ContextCompat.getColor(context, R.color.black))
        }
        else if (grade == 3) {
            tvZero.setBackgroundResource(R.drawable.round_small_skyblue_btn)
            tvZero.setTextColor(ContextCompat.getColor(context, R.color.black))
            tvOne.setBackgroundResource(R.drawable.round_small_skyblue_btn)
            tvOne.setTextColor(ContextCompat.getColor(context, R.color.black))
            tvTwo.setBackgroundResource(R.drawable.round_small_skyblue_btn)
            tvTwo.setTextColor(ContextCompat.getColor(context, R.color.black))
            tvThree.setBackgroundResource(R.drawable.round_small_blue_btn)
            tvThree.setTextColor(ContextCompat.getColor(context, R.color.white))
            tvFour.setBackgroundResource(R.drawable.round_small_skyblue_btn)
            tvFour.setTextColor(ContextCompat.getColor(context, R.color.black))
            tvFive.setBackgroundResource(R.drawable.round_small_skyblue_btn)
            tvFive.setTextColor(ContextCompat.getColor(context, R.color.black))
        }
        else if (grade == 4) {
            tvZero.setBackgroundResource(R.drawable.round_small_skyblue_btn)
            tvZero.setTextColor(ContextCompat.getColor(context, R.color.black))
            tvOne.setBackgroundResource(R.drawable.round_small_skyblue_btn)
            tvOne.setTextColor(ContextCompat.getColor(context, R.color.black))
            tvTwo.setBackgroundResource(R.drawable.round_small_skyblue_btn)
            tvTwo.setTextColor(ContextCompat.getColor(context, R.color.black))
            tvThree.setBackgroundResource(R.drawable.round_small_skyblue_btn)
            tvThree.setTextColor(ContextCompat.getColor(context, R.color.black))
            tvFour.setBackgroundResource(R.drawable.round_small_blue_btn)
            tvFour.setTextColor(ContextCompat.getColor(context, R.color.white))
            tvFive.setBackgroundResource(R.drawable.round_small_skyblue_btn)
            tvFive.setTextColor(ContextCompat.getColor(context, R.color.black))
        }
        else {
            tvZero.setBackgroundResource(R.drawable.round_small_skyblue_btn)
            tvZero.setTextColor(ContextCompat.getColor(context, R.color.black))
            tvOne.setBackgroundResource(R.drawable.round_small_skyblue_btn)
            tvOne.setTextColor(ContextCompat.getColor(context, R.color.black))
            tvTwo.setBackgroundResource(R.drawable.round_small_skyblue_btn)
            tvTwo.setTextColor(ContextCompat.getColor(context, R.color.black))
            tvThree.setBackgroundResource(R.drawable.round_small_skyblue_btn)
            tvThree.setTextColor(ContextCompat.getColor(context, R.color.black))
            tvFour.setBackgroundResource(R.drawable.round_small_skyblue_btn)
            tvFour.setTextColor(ContextCompat.getColor(context, R.color.black))
            tvFive.setBackgroundResource(R.drawable.round_small_blue_btn)
            tvFive.setTextColor(ContextCompat.getColor(context, R.color.white))
        }
    }


}