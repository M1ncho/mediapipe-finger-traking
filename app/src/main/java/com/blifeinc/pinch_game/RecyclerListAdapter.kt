package com.blifeinc.pinch_game

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.blifeinc.pinch_game.http.Member


class RecyclerListAdapter(private val mData: ArrayList<Member>, private val datacountList: ArrayList<Int>) : RecyclerView.Adapter<RecyclerListAdapter.myviewholder>() {

    var itemClick: ItemClick? = null

    interface ItemClick {
        fun onClick(view: View, position: Int)
    }

    class myviewholder(val linearView: LinearLayout): RecyclerView.ViewHolder(linearView)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): myviewholder {
        val linearView = LayoutInflater.from(parent.context).inflate(R.layout.list_item, parent, false) as LinearLayout
        return myviewholder(linearView)
    }


    override fun onBindViewHolder(holder: myviewholder, position: Int) {
        val userId: TextView = holder.linearView.findViewById(R.id.tv_user_id)
        val userNumber: TextView = holder.linearView.findViewById(R.id.tv_user_number)
        val userGender: TextView = holder.linearView.findViewById(R.id.tv_user_gender)
        val userBirth: TextView = holder.linearView.findViewById(R.id.tv_user_birth)
        val userPatient: TextView = holder.linearView.findViewById(R.id.tv_patient_num)


        userId.text = "${mData[position].member_id}"
        userNumber.text = mData[position].number
        userBirth.text = mData[position].birth_date
        userPatient.text = mData[position].patient_number


        if (mData[position].gender == "1") {
            userGender.text = "남"
        }
        else {
            userGender.text = "여"
        }

        val dataCount = mData[position].data_count


        if (datacountList.contains(position)) {
            holder.linearView.setBackgroundResource(R.color.powder_blue)
        }
        else {
            holder.linearView.setBackgroundResource(R.color.white)
        }




        Log.d("DATA COUNT CHECK", "$dataCount")
        Log.d("DATA CHECK", "${mData[position]}")


        if (itemClick != null) {
            holder.itemView.setOnClickListener { v -> itemClick!!.onClick(v,position) }
        }
    }


    override fun getItemCount(): Int = mData.size

}