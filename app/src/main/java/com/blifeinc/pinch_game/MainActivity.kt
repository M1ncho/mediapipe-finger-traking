package com.blifeinc.pinch_game

import android.media.SoundPool
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity(){

    var countView: TextView? = null
    var timerView: TextView? = null
    var timer_bar: ProgressBar? = null

    var lefthand_view: TextView? = null
    var righthand_view: TextView? = null
    var stopBtn: Button? = null
    var start_countView: TextView? = null

    var pointer1: ImageView? = null
    var pointer2: ImageView? = null

    var iv_guid1: ImageView? = null
    var iv_guid2: ImageView? = null
    var iv_guid3: ImageView? = null
    var iv_guid4: ImageView? = null


    var count = 0
    var now_hand = "right"

    var isPlay = false
    var isPaused = false
    var isCancelled = false

    var resumeFromMillis:Long = 0
    val millisInFuture:Long = 5000
    val countDownInterval:Long = 1000

    // 초기 image view 위치 저장 변수
    var reset_p1X = 0.0f
    var reset_p1Y = 0.0f
    var reset_p2X = 0.0f
    var reset_p2Y = 0.0f


    // 효과음 관련
    val soundPool = SoundPool.Builder().build()
    var tap_soundId: Int? = null
    var count_soundId: Int? = null


    //timer 설정
    private val sCountDown: CountDownTimer = object : CountDownTimer(3000, 1000) {
        override fun onTick(p0: Long) {
            start_countView!!.text = "${(p0.toFloat() / 1000.0f).roundToInt()}"
        }

        override fun onFinish() {
            soundPool.play(count_soundId!!, 1.0f, 1.0f, 0, 0, 1.0f)
            start_countView!!.visibility = View.GONE

            Handler(Looper.getMainLooper()).postDelayed({
                isPlay = true
                timer(millisInFuture, countDownInterval).start()
            }, 200)
        }
    }




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar!!.hide()

        countView = findViewById(R.id.tv_counts)
        timerView = findViewById(R.id.tv_timer)
        timer_bar = findViewById(R.id.bar_timer)
        lefthand_view = findViewById(R.id.tv_left)
        righthand_view = findViewById(R.id.tv_right)
        stopBtn = findViewById(R.id.btn_stop)
        start_countView = findViewById(R.id.tv_start_count)

        pointer1 = findViewById(R.id.iv_pointer_one)
        pointer2 = findViewById(R.id.iv_pointer_two)

        iv_guid1 = findViewById(R.id.iv_guid1)
        iv_guid2 = findViewById(R.id.iv_guid2)
        iv_guid3 = findViewById(R.id.iv_guid3)
        iv_guid4 = findViewById(R.id.iv_guid4)

        pointer1!!.viewTreeObserver.addOnGlobalLayoutListener(ImageViewGlobalLayoutListener(pointer1!!))
        pointer2!!.viewTreeObserver.addOnGlobalLayoutListener(ImageViewGlobalLayoutListener(pointer2!!))


        tap_soundId = soundPool.load(this, R.raw.blop_sound, 1)
        count_soundId = soundPool.load(this, R.raw.start_count3, 1)


        lefthand_view!!.setOnClickListener {
            now_hand = "left"
            changeHand()
        }

        righthand_view!!.setOnClickListener {
            now_hand = "right"
            changeHand()
        }



        stopBtn!!.setOnClickListener {

            // 시작 지점
            tappingVisibility()

            count = 0
            countView!!.text = "$count"

            stopBtn!!.text = "중지"
            stopBtn!!.visibility = View.INVISIBLE

            start_countView!!.visibility = View.VISIBLE
            sCountDown.start()


//            if (!isPlay) {
//                tappingVisibility()
//
//                count = 0
//                countView!!.text = "$count"
//
//                stopBtn!!.text = "중지"
//                stopBtn!!.visibility = View.INVISIBLE
//
//                start_countView!!.visibility = View.VISIBLE
//                sCountDown.start()
//            }
//            else {
//                isPaused = true
//                isCancelled = false
//                isPlay = false
//
//                val dialog = StopDialog(this)
//                dialog.showDialog()
//                dialog.setOnClickListener(object : StopDialog.OnDialogClickListener {
//                    override fun onCancelClicked() {
//                        timer(resumeFromMillis, countDownInterval).start()
//
//                        isPaused = false
//                        isCancelled = false
//                        isPlay = true
//                    }
//
//                    override fun onStopClicked() {
//                        isPaused = false
//                        isCancelled = true
//                        isPlay = false
//
//                        timerView!!.text = "5"
//                        timer_bar!!.progress = 0
//                        stopBtn!!.text = "시작"
//                    }
//                })
//            }

        }


    }





    // 손 바꾸기
    fun changeHand() {
        if (now_hand == "right") {
            righthand_view!!.setBackgroundResource(R.color.ash_blue)
            righthand_view!!.setTextColor(ContextCompat.getColor(this, R.color.deep_blue))

            lefthand_view!!.setBackgroundResource(R.color.ash_yellow)
            lefthand_view!!.setTextColor(ContextCompat.getColor(this, R.color.deep_yellow))

            tappingVisibility()
        }
        else {
            lefthand_view!!.setBackgroundResource(R.color.ash_blue)
            lefthand_view!!.setTextColor(ContextCompat.getColor(this, R.color.deep_blue))

            righthand_view!!.setBackgroundResource(R.color.ash_yellow)
            righthand_view!!.setTextColor(ContextCompat.getColor(this, R.color.deep_yellow))

            tappingVisibility()
        }

        countReset()

        // 만약 동작중인 timer 존재 시, timer 멈추기
        if (isPlay) {
            isPaused = false
            isCancelled = true
            isPlay = false
        }

    }



    // count 관련
    // timer 작동시에만 세도록??
    fun countPlus() {
        if (isPlay) {
            soundPool.play(tap_soundId!!, 1.0f, 1.0f, 0, 0, 1.0f)
            count++
        }
        countView!!.text = "$count"
    }

    fun countReset() {
        count = 0
        countView!!.text = "$count"
    }


    // 선택한 손 위치 반환
    fun handReturn(): String {
        return now_hand
    }


    // tap icon 정렬 및 보이기
    fun tappingVisibility() {
        pointer1!!.x = reset_p1X
        pointer1!!.y = reset_p1Y
        pointer2!!.x = reset_p2X
        pointer2!!.y = reset_p2Y
    }



    // guid image change
    fun changeInOut(inout: String) {
        if (now_hand == "right" && inout == "in") {
            iv_guid1!!.setImageResource(R.drawable.ic_baseline_north_east_24)
            iv_guid2!!.setImageResource(R.drawable.ic_baseline_south_west_24)
        }
        else if (now_hand == "right" && inout == "out") {
            iv_guid1!!.setImageResource(R.drawable.ic_baseline_south_west_24)
            iv_guid2!!.setImageResource(R.drawable.ic_baseline_north_east_24)
        }

        else if (now_hand == "left" && inout == "in") {
            iv_guid3!!.setImageResource(R.drawable.ic_baseline_north_west_24)
            iv_guid4!!.setImageResource(R.drawable.ic_baseline_south_east_24)
        }
        else if (now_hand == "left" && inout == "out") {
            iv_guid3!!.setImageResource(R.drawable.ic_baseline_south_east_24)
            iv_guid4!!.setImageResource(R.drawable.ic_baseline_north_west_24)
        }
    }




    // tap play - count timer 함수
    fun timer(millis: Long, countInterval: Long): CountDownTimer {
        return object : CountDownTimer(millis, countInterval) {
            override fun onTick(p0: Long) {
                if (isPaused) {
                    resumeFromMillis = p0
                    cancel()
                }
                else if (isCancelled) {
                    cancel()
                }
                else {
                    timerView!!.text = "${(p0.toFloat() / 1000.0f).roundToInt()}"

                    var timebar_value = 5 - ((p0.toFloat() / 1000.0f).roundToInt())
                    timer_bar!!.progress = timebar_value
                }
            }

            override fun onFinish() {
                timerView!!.text = "0"
                timer_bar!!.progress = 5
                stopBtn!!.text = "시작"
                stopBtn!!.visibility = View.VISIBLE

                isPlay = false
            }
        }
    }




    // tree observer
    inner class ImageViewGlobalLayoutListener(private val iv: ImageView) : ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            if (iv == pointer1) {
                reset_p1X = iv.x
                reset_p1Y = iv.y
            }
            else if (iv == pointer2) {
                reset_p2X = iv.x
                reset_p2Y = iv.y
            }

            iv.viewTreeObserver.removeOnGlobalLayoutListener(this)
        }
    }


}
