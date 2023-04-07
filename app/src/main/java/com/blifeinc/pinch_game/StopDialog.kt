package com.blifeinc.pinch_game

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.view.WindowManager
import android.widget.TextView


class StopDialog(context: Context) {

    val dialog = Dialog(context)
    private lateinit var onClickListener: OnDialogClickListener


    fun setOnClickListener(listener: OnDialogClickListener)
    {
        onClickListener = listener
    }


    fun showDialog() {
        dialog.setContentView(R.layout.stop_dialog)
        dialog.window!!.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT)

        dialog.setCanceledOnTouchOutside(true)
        dialog.setCancelable(true)
        dialog.show()


        val tv_cancel = dialog.findViewById<TextView>(R.id.tv_cancel)
        val tv_stop = dialog.findViewById<TextView>(R.id.tv_stop)


        tv_cancel.setOnClickListener {
            onClickListener.onCancelClicked()
            dialog.dismiss()
        }

        tv_stop.setOnClickListener {
            onClickListener.onStopClicked()
            dialog.dismiss()
        }
    }



    interface OnDialogClickListener {
        fun onCancelClicked()
        fun onStopClicked()
    }


}