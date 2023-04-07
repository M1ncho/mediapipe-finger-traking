package com.blifeinc.pinch_game

import android.app.Dialog
import android.content.Context
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blifeinc.pinch_game.http.Member
import com.blifeinc.pinch_game.util.SaveSettingUtil

class UserListDialog(context: Context, var memberList: ArrayList<Member>) {

    val dialog = Dialog(context)

    private lateinit var viewManager: RecyclerView.LayoutManager
    private lateinit var recyclerAdapter: RecyclerListAdapter


    fun showDialog() {
        dialog.setContentView(R.layout.list_dialog)
        dialog.window!!.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT)

        dialog.setCanceledOnTouchOutside(true)
        dialog.setCancelable(true)
        dialog.show()


        viewManager = LinearLayoutManager(dialog.context)
        //recyclerAdapter = RecyclerListAdapter(memberList)

//        recyclerAdapter.itemClick = object : RecyclerListAdapter.ItemClick {
//            override fun onClick(view: View, position: Int) {
//                val id = memberList[position].member_id
//                SaveSettingUtil.setMemberId(dialog.context, id)
//
//                Log.d("Select Id ", "${SaveSettingUtil.getMemberId(dialog.context)}")
//
//                dialog.dismiss()
//            }
//        }


        val btnUserRegistar = dialog.findViewById<Button>(R.id.btn_user_post)
        val userListView = dialog.findViewById<RecyclerView>(R.id.list_member)

//        userListView.apply {
//            layoutManager = viewManager
//            adapter = recyclerAdapter
//        }

        // 등록팝업 띄우기
        btnUserRegistar.setOnClickListener {
            val regisDialog = RegistrationDialog(dialog.context)
            regisDialog.showDialog()

            dialog.dismiss()
        }

    }



}