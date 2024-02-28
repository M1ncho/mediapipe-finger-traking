package com.blifeinc.pinch_game

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract.Data
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.blifeinc.pinch_game.databinding.ActivityRoundSelectBinding
import com.blifeinc.pinch_game.http.DataService
import com.blifeinc.pinch_game.http.FingertappingClient
import com.blifeinc.pinch_game.http.MemberFingerData
import com.blifeinc.pinch_game.util.SaveSettingUtil
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.create

class RoundSelectActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRoundSelectBinding
    private lateinit var dataService: DataService

    var roundOneYn = false
    var roundTwoYn = false
    var roundThreeYn = false
    var roundFourYn = false

    var selectLevel = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRoundSelectBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar!!.hide()


        dataService = FingertappingClient.instance().create(DataService::class.java)

        getMemberData()



        binding.tvOneRound.setOnClickListener {
            saveRound(1)
        }

        binding.tvTwoRound.setOnClickListener {
            saveRound(2)
        }

        binding.tvThreeRound.setOnClickListener {
            saveRound(3)
        }

        binding.tvFourRound.setOnClickListener {
            saveRound(4)
        }


        binding.tvCheck.setOnClickListener {
            if (selectLevel == 0) {
                Toast.makeText(this, "회차를 선택 해주세요.", Toast.LENGTH_SHORT).show()
            }
            else {
                val intent = Intent(this, HandTrackingActivity::class.java)
                startActivity(intent)
                finish()
            }
        }

    }




    // 데이터 가져오기
    fun getMemberData() {
        val id = SaveSettingUtil.getMemberId(this)
        val memberData = dataService.getMemberData(id)

        // api 통신
        memberData.enqueue(object : Callback<List<MemberFingerData>> {
            override fun onResponse(call: Call<List<MemberFingerData>>, response: Response<List<MemberFingerData>>) {
                if (response.isSuccessful) {
                    val MemberData = response.body()!!

                    Log.d("Success GET DATA ", "$MemberData")

                    // data 수만큼 반복
                    for (data in MemberData) {
                        val checkRound = data.round
                        val date = data.created_at.split("T")

                        if (checkRound == 1) {
                            roundOneYn = true
                            binding.tvOneRound.text = "1회차  (${date[0]})"
                            binding.tvOneRound.setBackgroundResource(R.drawable.round_line_blue_darkgreyblue_layout)
                            binding.tvOneRound.setTextColor(ContextCompat.getColor(this@RoundSelectActivity, R.color.white))
                        }
                        else if (checkRound == 2) {
                            roundTwoYn = true
                            binding.tvTwoRound.text = "2회차  (${date[0]})"
                            binding.tvTwoRound.setBackgroundResource(R.drawable.round_line_blue_darkgreyblue_layout)
                            binding.tvTwoRound.setTextColor(ContextCompat.getColor(this@RoundSelectActivity, R.color.white))
                        }
                        else if (checkRound == 3) {
                            roundThreeYn = true
                            binding.tvThreeRound.text = "3회차  (${date[0]})"
                            binding.tvThreeRound.setBackgroundResource(R.drawable.round_line_blue_darkgreyblue_layout)
                            binding.tvThreeRound.setTextColor(ContextCompat.getColor(this@RoundSelectActivity, R.color.white))
                        }
                        else if (checkRound == 4) {
                            roundFourYn = true
                            binding.tvFourRound.text = "4회차  (${date[0]})"
                            binding.tvFourRound.setBackgroundResource(R.drawable.round_line_blue_darkgreyblue_layout)
                            binding.tvFourRound.setTextColor(ContextCompat.getColor(this@RoundSelectActivity, R.color.white))
                        }
                    }
                }
            }
            override fun onFailure(call: Call<List<MemberFingerData>>, t: Throwable) {
                Log.d("Failed GET DATA ", "${t.message}")
            }
        })
    }


    // round 저장
    fun saveRound(value: Int) {
        SaveSettingUtil.setRound(this, value)

        if (value == 1) {
            selectLevel = if (!roundOneYn) {
                binding.tvOneRound.setBackgroundResource(R.drawable.round_line_blue_skyblue_layout)
                binding.tvOneRound.setTextColor(ContextCompat.getColor(this, R.color.black))
                1
            } else {
                0
            }

            if (roundTwoYn) {
                binding.tvTwoRound.setBackgroundResource(R.drawable.round_line_blue_darkgreyblue_layout)
                binding.tvTwoRound.setTextColor(ContextCompat.getColor(this, R.color.white))
            }
            else {
                binding.tvTwoRound.setBackgroundResource(R.drawable.round_line_blue_layout)
                binding.tvTwoRound.setTextColor(ContextCompat.getColor(this, R.color.black))
            }
            if (roundThreeYn) {
                binding.tvThreeRound.setBackgroundResource(R.drawable.round_line_blue_darkgreyblue_layout)
                binding.tvThreeRound.setTextColor(ContextCompat.getColor(this, R.color.white))
            }
            else {
                binding.tvThreeRound.setBackgroundResource(R.drawable.round_line_blue_layout)
                binding.tvThreeRound.setTextColor(ContextCompat.getColor(this, R.color.black))
            }
            if (roundFourYn) {
                binding.tvFourRound.setBackgroundResource(R.drawable.round_line_blue_darkgreyblue_layout)
                binding.tvFourRound.setTextColor(ContextCompat.getColor(this, R.color.white))
            }
            else {
                binding.tvFourRound.setBackgroundResource(R.drawable.round_line_blue_layout)
                binding.tvFourRound.setTextColor(ContextCompat.getColor(this, R.color.black))
            }
        }

        else if (value == 2) {
            selectLevel = if (!roundTwoYn) {
                binding.tvTwoRound.setBackgroundResource(R.drawable.round_line_blue_skyblue_layout)
                binding.tvTwoRound.setTextColor(ContextCompat.getColor(this, R.color.black))
                2
            } else {
                0
            }

            if (roundOneYn) {
                binding.tvOneRound.setBackgroundResource(R.drawable.round_line_blue_darkgreyblue_layout)
                binding.tvOneRound.setTextColor(ContextCompat.getColor(this, R.color.white))
            }
            else {
                binding.tvOneRound.setBackgroundResource(R.drawable.round_line_blue_layout)
                binding.tvOneRound.setTextColor(ContextCompat.getColor(this, R.color.black))
            }
            if (roundThreeYn) {
                binding.tvThreeRound.setBackgroundResource(R.drawable.round_line_blue_darkgreyblue_layout)
                binding.tvThreeRound.setTextColor(ContextCompat.getColor(this, R.color.white))
            }
            else {
                binding.tvThreeRound.setBackgroundResource(R.drawable.round_line_blue_layout)
                binding.tvThreeRound.setTextColor(ContextCompat.getColor(this, R.color.black))
            }
            if (roundFourYn) {
                binding.tvFourRound.setBackgroundResource(R.drawable.round_line_blue_darkgreyblue_layout)
                binding.tvFourRound.setTextColor(ContextCompat.getColor(this, R.color.white))
            }
            else {
                binding.tvFourRound.setBackgroundResource(R.drawable.round_line_blue_layout)
                binding.tvFourRound.setTextColor(ContextCompat.getColor(this, R.color.black))
            }
        }
        else if (value == 3) {
            selectLevel = if (!roundThreeYn) {
                binding.tvThreeRound.setBackgroundResource(R.drawable.round_line_blue_skyblue_layout)
                binding.tvThreeRound.setTextColor(ContextCompat.getColor(this, R.color.black))
                3
            } else {
                0
            }

            if (roundOneYn) {
                binding.tvOneRound.setBackgroundResource(R.drawable.round_line_blue_darkgreyblue_layout)
                binding.tvOneRound.setTextColor(ContextCompat.getColor(this, R.color.white))
            }
            else {
                binding.tvOneRound.setBackgroundResource(R.drawable.round_line_blue_layout)
                binding.tvOneRound.setTextColor(ContextCompat.getColor(this, R.color.black))
            }
            if (roundTwoYn) {
                binding.tvTwoRound.setBackgroundResource(R.drawable.round_line_blue_darkgreyblue_layout)
                binding.tvTwoRound.setTextColor(ContextCompat.getColor(this, R.color.white))
            }
            else {
                binding.tvTwoRound.setBackgroundResource(R.drawable.round_line_blue_layout)
                binding.tvTwoRound.setTextColor(ContextCompat.getColor(this, R.color.black))
            }
            if (roundFourYn) {
                binding.tvFourRound.setBackgroundResource(R.drawable.round_line_blue_darkgreyblue_layout)
                binding.tvFourRound.setTextColor(ContextCompat.getColor(this, R.color.white))
            }
            else {
                binding.tvFourRound.setBackgroundResource(R.drawable.round_line_blue_layout)
                binding.tvFourRound.setTextColor(ContextCompat.getColor(this, R.color.black))
            }
        }
        else {
            selectLevel = if (!roundFourYn) {
                binding.tvFourRound.setBackgroundResource(R.drawable.round_line_blue_skyblue_layout)
                binding.tvFourRound.setTextColor(ContextCompat.getColor(this, R.color.black))
                4
            } else {
                0
            }

            if (roundOneYn) {
                binding.tvOneRound.setBackgroundResource(R.drawable.round_line_blue_darkgreyblue_layout)
                binding.tvOneRound.setTextColor(ContextCompat.getColor(this, R.color.white))
            }
            else {
                binding.tvOneRound.setBackgroundResource(R.drawable.round_line_blue_layout)
                binding.tvOneRound.setTextColor(ContextCompat.getColor(this, R.color.black))
            }
            if (roundTwoYn) {
                binding.tvTwoRound.setBackgroundResource(R.drawable.round_line_blue_darkgreyblue_layout)
                binding.tvTwoRound.setTextColor(ContextCompat.getColor(this, R.color.white))
            }
            else {
                binding.tvTwoRound.setBackgroundResource(R.drawable.round_line_blue_layout)
                binding.tvTwoRound.setTextColor(ContextCompat.getColor(this, R.color.black))
            }
            if (roundThreeYn) {
                binding.tvThreeRound.setBackgroundResource(R.drawable.round_line_blue_darkgreyblue_layout)
                binding.tvThreeRound.setTextColor(ContextCompat.getColor(this, R.color.white))
            }
            else {
                binding.tvThreeRound.setBackgroundResource(R.drawable.round_line_blue_layout)
                binding.tvThreeRound.setTextColor(ContextCompat.getColor(this, R.color.black))
            }
        }
    }



}