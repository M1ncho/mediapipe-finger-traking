package com.blifeinc.pinch_game

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.blifeinc.pinch_game.databinding.ActivityRegistrationBinding
import com.blifeinc.pinch_game.http.FingertappingClient
import com.blifeinc.pinch_game.http.Member
import com.blifeinc.pinch_game.http.MemberList
import com.blifeinc.pinch_game.http.MemberService
import com.blifeinc.pinch_game.util.SaveSettingUtil
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.normal.TedPermission
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegistrationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegistrationBinding

    private lateinit var memberService: MemberService
    private var memberList = ArrayList<Member>()
    private var useDataList = ArrayList<Int>()


    private val REQUIRED_PERMISSIONS = mutableListOf(Manifest.permission.INTERNET, Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_NETWORK_STATE).toTypedArray()


    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar!!.hide()


        // 퍼미션 체크
        val permissionListener = object : PermissionListener {
            override fun onPermissionGranted() {
                Log.d("Success", "권한 존재.")
            }

            override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {
                Log.d("Error", "권한이 없습니다.")
            }
        }

        TedPermission.create().setPermissionListener(permissionListener).setPermissions(*REQUIRED_PERMISSIONS).check()

        memberService = FingertappingClient.instance().create(MemberService::class.java)
        getMemberList()


        // 등록팝업 띄우기
        binding.btnUserPost.setOnClickListener {
            val dialog = RegistrationDialog(this)
            dialog.showDialog()
        }
    }





    fun setAdapter(list: ArrayList<Member>, countList: ArrayList<Int>) {
        val mAdapter = RecyclerListAdapter(list, countList)

        binding.listMember.adapter = mAdapter
        binding.listMember.layoutManager = LinearLayoutManager(this)

        mAdapter.itemClick = object : RecyclerListAdapter.ItemClick {
            override fun onClick(view: View, position: Int) {
                val id = memberList[position].member_id
                SaveSettingUtil.setMemberId(this@RegistrationActivity, id)

                //val intent = Intent(this@RegistrationActivity, HandTrackingActivity::class.java)
                val intent = Intent(this@RegistrationActivity, RoundSelectActivity::class.java)

                startActivity(intent)
            }
        }
    }


    fun getMemberList() {
        val memberDt = memberService.getMemberList()

        memberDt.enqueue(object : Callback<MemberList> {
            override fun onResponse(call: Call<MemberList>, response: Response<MemberList>) {
                if (response.isSuccessful) {
                    val memberData = response.body()!!.member_list
                    val size = memberData.size

                    for (data in memberData) {
                        memberList.add(data)

                        if (data.data_count > 0) {
                            useDataList.add(memberData.indexOf(data))
                        }
                    }

                    setAdapter(memberList, useDataList)

                    Log.d("DATA CHECK ", "$memberList  $size")
                    Log.d("DATA CHECK 22 ", "$useDataList")
                }
            }
            override fun onFailure(call: Call<MemberList>, t: Throwable) {
                Log.d("Failed GET DATA ", "${t.message}")
            }
        })
    }


}