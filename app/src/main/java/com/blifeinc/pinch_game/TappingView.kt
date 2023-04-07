package com.blifeinc.pinch_game

import android.animation.ObjectAnimator
import android.content.Context
import android.media.SoundPool
import android.os.Looper
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewTreeObserver
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import java.lang.Math.abs
import android.os.Handler
import android.widget.TextView


class TappingView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
): LinearLayout(context, attrs){

    var parentView: ConstraintLayout

    var iv_pointer_one: ImageView
    var iv_pointer_two: ImageView


    private var pointer_one: Int? = null
    private var pointer_two: Int? = null

    var id_pointer1: Int? = null
    var id_pointer2: Int? = null


    private var prev_interval_X = 0.0f     // X 터치 간격 - 비교 용도
    private var prev_interval_Y = 0.0f     // Y 터치 간격0

    private var now_state = "out"
    private var select_hand = "right"

    //val soundPool = SoundPool.Builder().build()
    //val soundId = soundPool.load(context, R.raw.pop_up, 1)


    // view 의 범위에 들오왔는지 판단할 함수
    var p1_left = 0.0f
    var p1_top = 0.0f
    var p1_right = 0.0f
    var p1_bottom = 0.0f

    var p2_left = 0.0f
    var p2_top = 0.0f
    var p2_right = 0.0f
    var p2_bottom = 0.0f


    var imageSize = 0     // image view 의 크기를 저장시킬 변수

    // imageview 가 움직인 위치를 저장할 변수
    var p1_X = 0.0f
    var p1_Y = 0.0f
    var p2_X = 0.0f
    var p2_Y = 0.0f


    // 초기 image view 위치 저장 변수
    var reset_p1X = 0.0f
    var reset_p1Y = 0.0f
    var reset_p2X = 0.0f
    var reset_p2Y = 0.0f


    // 화면 밖 이동을 막기 위한 전체 뷰 크기 변수
    var full_width = 0.0f
    var full_height = 0.0f




    init {
        val view = LayoutInflater.from(context).inflate(R.layout.tapping_view, this, false)
        addView(view)

        parentView = findViewById(R.id.tapping_layout)
        iv_pointer_one = findViewById(R.id.iv_pointer_one)
        iv_pointer_two = findViewById(R.id.iv_pointer_two)


        iv_pointer_one.viewTreeObserver.addOnGlobalLayoutListener(ImageViewGlobalLayoutListener(iv_pointer_one))
        iv_pointer_two.viewTreeObserver.addOnGlobalLayoutListener(ImageViewGlobalLayoutListener(iv_pointer_two))
    }




    // 멀티 터치 처리
    override fun onTouchEvent(event: MotionEvent): Boolean = try {
        full_width = parentView.width.toFloat()
        full_height = parentView.height.toFloat()

        //select_hand = (context as MainActivity).handReturn()


        when (event.actionMasked) {

            MotionEvent.ACTION_DOWN -> {
                pointer_one = event.getPointerId(0)

                p1_X = event.getX(pointer_one!!)
                p1_Y = event.getY(pointer_one!!)

                getViewSize()

                if (p1_X in p1_left..p1_right && p1_Y >= p1_top && p1_Y <= p1_bottom) {
                    id_pointer1 = 1
                }
                else if (p1_X in p2_left..p2_right && p1_Y >= p2_top && p1_Y <= p2_bottom) {
                    id_pointer1 = 2
                }
            }

            MotionEvent.ACTION_POINTER_DOWN -> {
                pointer_two = event.getPointerId(event.actionIndex)
                prev_interval_Y = abs(event.getX(pointer_one!!) - event.getX(pointer_two!!))

                p2_X = event.getX(pointer_two!!)
                p2_Y = event.getY(pointer_two!!)

                getViewSize()

                if (p2_X in p1_left..p1_right && p2_Y >= p1_top && p2_Y <= p1_bottom) {
                    id_pointer2 = 1
                }
                else if (p2_X in p2_left..p2_right && p2_Y >= p2_top && p2_Y <= p2_bottom) {
                    id_pointer2 = 2
                }


                // image 변환
                iv_pointer_one.setImageResource(R.drawable.ic_tap_yellow)
                iv_pointer_two.setImageResource(R.drawable.ic_tap_yellow)
            }


            MotionEvent.ACTION_MOVE -> {
                if (event.pointerCount == 2) {

                    // 좌표 얻어오기
                    // 두 터치 포인트 간의 절대적 거리
                    val interval_x = abs(event.getX(pointer_one!!) - event.getX(pointer_two!!))
                    val interval_y = abs(event.getY(pointer_one!!) - event.getY(pointer_two!!))

                    // 본래 위치보다 얼마나 움직였는지를 저장 - 움직인 위치 저장
                    val p1_move_y = event.getY(pointer_one!!) - p1_Y
                    val p2_move_y = event.getY(pointer_two!!) - p2_Y


                    // image view 이동
                    if (id_pointer1 == 1) {
                        if (iv_pointer_one.y < 0) {
                            iv_pointer_one.y = 0.0f
                        }
                        if (iv_pointer_one.y + imageSize > full_height) {
                            iv_pointer_one.y = full_height - imageSize
                        }

                        if (iv_pointer_two.y < 0) {
                            iv_pointer_two.y = 0.0f
                        }
                        if (iv_pointer_two.y + imageSize > full_height) {
                            iv_pointer_two.y = full_height - imageSize
                        }


                        iv_pointer_one.y = iv_pointer_one.y + p1_move_y
                        iv_pointer_two.y = iv_pointer_two.y + p2_move_y
                    }

                    else if (id_pointer1 == 2) {
                        if (iv_pointer_one.y < 0) {
                            iv_pointer_one.y = 0.0f
                        }
                        if (iv_pointer_one.y + imageSize > full_height) {
                            iv_pointer_one.y = full_height - imageSize
                        }


                        if (iv_pointer_two.y < 0) {
                            iv_pointer_two.y = 0.0f
                        }
                        if (iv_pointer_two.y + imageSize > full_height) {
                            iv_pointer_two.y = full_height - imageSize
                        }


                        iv_pointer_two.y = iv_pointer_two.y + p1_move_y
                        iv_pointer_one.y = iv_pointer_one.y + p2_move_y
                    }


                    // 축소 기능
                    if (prev_interval_Y > interval_y && interval_y <= 300) {
                        now_state = "in"
                    }

                    // 확대 기능
                    if (prev_interval_Y < interval_y && interval_y >= 860 && now_state == "in") {
                        now_state = "out"
                        upCount()
                    }


                    // 좌표 다시 저장
                    prev_interval_X = interval_x
                    prev_interval_Y = interval_y

                    p1_X = event.getX(pointer_one!!)
                    p1_Y = event.getY(pointer_one!!)
                    p2_X = event.getX(pointer_two!!)
                    p2_Y = event.getY(pointer_two!!)

                    Log.d("현 좌표 차이", "x = $interval_x , y = $interval_y")
                }
            }

            MotionEvent.ACTION_POINTER_UP -> {
                pointer_one = null
                pointer_two = null

                iv_pointer_one.setImageResource(R.drawable.ic_tap_black)
                iv_pointer_two.setImageResource(R.drawable.ic_tap_black)


                // 자동 리셋???
                if (now_state == "in") {
                    now_state = "out"

                    Handler(Looper.getMainLooper()).postDelayed({
                        iv_pointer_one.x = reset_p1X
                        iv_pointer_one.y = reset_p1Y

                        iv_pointer_two.x = reset_p2X
                        iv_pointer_two.y = reset_p2Y

                        upCount()
                    }, 300)
                }

                Log.d("Now", "손가락 떨어짐")
            }
        }

        true

    } catch (e: Exception) {
        false
    }




    // view 크기 가져오기
    fun getViewSize() {
        imageSize = iv_pointer_one.width                       // 4개가 같은 정사각형이니까 한 부분만 받아도 ok

        select_hand = (context as MainActivity).handReturn()


        p1_left = iv_pointer_one.x
        p1_top = iv_pointer_one.y
        p1_right = p1_left + imageSize
        p1_bottom = p1_top + imageSize

        p2_left = iv_pointer_two.x
        p2_top = iv_pointer_two.y
        p2_right = p2_left + imageSize
        p2_bottom = p2_top + imageSize
    }



//    fun imageMove(iv: ImageView, posix: Float, posiy: Float) {
//        ObjectAnimator.ofFloat(iv, "translationX", posix).apply {
//            duration = 400
//            start()
//        }
//
//        ObjectAnimator.ofFloat(iv, "translationY", posiy).apply {
//            duration = 400
//            start()
//        }
//    }




    fun upCount() {
        (context as MainActivity).countPlus()
    }

    fun inout_change() {
        (context as MainActivity).changeInOut(now_state)
    }




    // tree observer
    inner class ImageViewGlobalLayoutListener(private val iv: ImageView) : ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            if (iv == iv_pointer_one) {
                reset_p1X = iv.x
                reset_p1Y = iv.y
            }
            else if (iv == iv_pointer_two) {
                reset_p2X = iv.x
                reset_p2Y = iv.y
            }

            iv.viewTreeObserver.removeOnGlobalLayoutListener(this)
        }
    }

}
