package com.blifeinc.pinch_game

import android.Manifest
import android.os.*
import android.util.Log
import android.view.View
import android.webkit.CookieManager
import android.webkit.CookieSyncManager
import android.webkit.WebSettings
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.blifeinc.pinch_game.databinding.ActivityHandTrackingBinding
import com.blifeinc.pinch_game.http.*
import com.blifeinc.pinch_game.util.SaveSettingUtil
import com.google.mediapipe.solutioncore.CameraInput
import com.google.mediapipe.solutioncore.SolutionGlSurfaceView
import com.google.mediapipe.solutions.hands.Hands
import com.google.mediapipe.solutions.hands.HandsOptions
import com.google.mediapipe.solutions.hands.HandsResult
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.normal.TedPermission
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.math.roundToInt


class HandTrackingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHandTrackingBinding

    var count = 0
    var startAbsY = 0
    var leftPostYn = false
    var rightPostYn = false

    var cookieManager: CookieManager? = null
    var sendDataYN = false

    // count down 관련
    var isPlay = false
    var isPaused = false
    var isCancelled = false
    var is3sec = false

    var resumeFromMillis:Long = 0
    var millisInFuture:Long = 15000
    val countDownInterval:Long = 1000

    // 서버통신 관련
    private lateinit var dataService: DataService
    private var tappingList = mutableListOf<FingerDataDetail>()
    private var fingerDataList = mutableListOf<Finger3DDataDetail>()


    // mediapipe 에 들어가있는
    private lateinit var hands: Hands
    private lateinit var cameraInput: CameraInput
    private lateinit var glSurfaceView: SolutionGlSurfaceView<HandsResult>

    private val REQUIRED_PERMISSIONS = mutableListOf(Manifest.permission.INTERNET, Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_NETWORK_STATE).toTypedArray()


    // delay timer 설정
    private val sCountDown: CountDownTimer = object : CountDownTimer(3000, 1000) {
        override fun onTick(p0: Long) {
            binding.tvDelayCount.text = "${(p0.toFloat() / 1000.0f).roundToInt()}"
        }

        override fun onFinish() {
            binding.tvDelayCount.visibility = View.GONE
            is3sec = false

            Handler(Looper.getMainLooper()).postDelayed({
                isPlay = true
                timer(millisInFuture, countDownInterval).start()
            }, 200)
        }
    }


    // activity 객체를 넘기기 위한 함수
    init {
        instance = this
    }

    companion object {
        private var instance: HandTrackingActivity? = null
        fun getInstance(): HandTrackingActivity? {
            return instance
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHandTrackingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar!!.hide()


        // 퍼미션 체크
        val permissionListener = object : PermissionListener {
            override fun onPermissionGranted() {
                setupStreamingModePipeline()

                glSurfaceView.post { startCamera() }
                glSurfaceView.visibility = View.VISIBLE
            }

            override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {
                Log.d("Error", "권한이 없습니다.")
            }
        }

        TedPermission.create().setPermissionListener(permissionListener).setPermissions(*REQUIRED_PERMISSIONS).check()

        //
        dataService = FingertappingClient.instance().create(DataService::class.java)


        binding.btn15sec.setOnClickListener {
            changeTime(15)
        }

        binding.btn10sec.setOnClickListener {
            changeTime(10)
        }

        binding.btn5sec.setOnClickListener {
            changeTime(5)
        }


        // 처음 시작시 손 방향을 선택 하게끔 설정

        binding.btnStart.setOnClickListener {
            if (SaveSettingUtil.getSelectYn(this) == 0) {
                HandsResultGlRenderer().onePrint = false
                val dialog = HandSelectDialog(this, leftPostYn, rightPostYn)
                dialog.showDialog()
            }
            else {
                tappingStart()
            }
        }

        binding.ivCloseWeb.setOnClickListener {
            binding.layoutWeb.visibility = View.GONE
        }
    }


    override fun onDestroy() {
        super.onDestroy()

        SaveSettingUtil.setSelectYn(this, 0)
        HandsResultGlRenderer().release()
    }



    // 화면에 결과 띄우기??
    fun setupStreamingModePipeline() {
        hands = Hands(
                this@HandTrackingActivity,
                HandsOptions.builder()
                        .setStaticImageMode(false)
                        .setMaxNumHands(1)
                        .setRunOnGpu(true)
                        .build()
        )
        hands.setErrorListener { message, e -> Log.e("TAG", "MediaPipe Hands error: $message") }

        cameraInput = CameraInput(this)
        cameraInput.setNewFrameListener { hands.send(it) }

        glSurfaceView = SolutionGlSurfaceView(this@HandTrackingActivity, hands.glContext, hands.glMajorVersion)
        glSurfaceView.setSolutionResultRenderer(HandsResultGlRenderer())
        glSurfaceView.setRenderInputImage(true)

        hands.setResultListener {
            glSurfaceView.setRenderData(it)
            glSurfaceView.requestRender()
        }

        glSurfaceView.post(this::startCamera)


        binding.previewDisplayLayout.apply {
            removeAllViewsInLayout()
            addView(glSurfaceView)
            glSurfaceView.visibility = View.VISIBLE
            requestLayout()
        }
    }


    // 카메라 열기??
    fun startCamera() {
        cameraInput.start(
                this@HandTrackingActivity,
                hands.glContext,
                CameraInput.CameraFacing.BACK,
                glSurfaceView.width,
                glSurfaceView.height
        )
    }



    // time count 설정 - 10 추가
    fun changeTime(time: Int) {
        millisInFuture = (time * 1000).toLong()

        if (time == 15) {
            binding.btn15sec.setBackgroundResource(R.drawable.round_small_blue_btn)
            binding.btn15sec.setTextColor(ContextCompat.getColor(this, R.color.white))

            binding.btn10sec.setBackgroundResource(R.drawable.round_small_skyblue_btn)
            binding.btn10sec.setTextColor(ContextCompat.getColor(this, R.color.dark_grey_blue))
            binding.btn5sec.setBackgroundResource(R.drawable.round_small_skyblue_btn)
            binding.btn5sec.setTextColor(ContextCompat.getColor(this, R.color.dark_grey_blue))
        }
        else if (time == 10) {
            binding.btn10sec.setBackgroundResource(R.drawable.round_small_blue_btn)
            binding.btn10sec.setTextColor(ContextCompat.getColor(this, R.color.white))

            binding.btn15sec.setBackgroundResource(R.drawable.round_small_skyblue_btn)
            binding.btn15sec.setTextColor(ContextCompat.getColor(this, R.color.dark_grey_blue))
            binding.btn5sec.setBackgroundResource(R.drawable.round_small_skyblue_btn)
            binding.btn5sec.setTextColor(ContextCompat.getColor(this, R.color.dark_grey_blue))
        }
        else {
            binding.btn5sec.setBackgroundResource(R.drawable.round_small_blue_btn)
            binding.btn5sec.setTextColor(ContextCompat.getColor(this, R.color.white))

            binding.btn10sec.setBackgroundResource(R.drawable.round_small_skyblue_btn)
            binding.btn10sec.setTextColor(ContextCompat.getColor(this, R.color.dark_grey_blue))
            binding.btn15sec.setBackgroundResource(R.drawable.round_small_skyblue_btn)
            binding.btn15sec.setTextColor(ContextCompat.getColor(this, R.color.dark_grey_blue))
        }

        binding.tvTimer.text = "$time"
        binding.barTimer.max = time
        binding.barTimer.progress = 0
    }



    // count 함수
    fun setCount() {
        if (isPlay) {
            count++
        }

        runOnUiThread {
            binding.tvCount.text = "$count"
        }
    }



    // countdown 함수
    fun timer(millis: Long, countInterval: Long): CountDownTimer {
        return object : CountDownTimer(millis, countInterval) {
            override fun onTick(millisUntilFinished: Long) {
                if (isPaused){
                    resumeFromMillis = millisUntilFinished
                    cancel()
                }
                else if (isCancelled) {
                    cancel()
                }
                else {
                    binding.tvTimer.text = "${(millisUntilFinished.toFloat() / 1000.0f).roundToInt()}"

                    var timebar_value = (millisInFuture / 1000).toInt() - ((millisUntilFinished.toFloat() / 1000.0f).roundToInt())
                    binding.barTimer.progress = timebar_value
                }
            }

            override fun onFinish() {
                binding.tvTimer.text = "0"
                binding.barTimer.progress = (millisInFuture / 1000).toInt()
                binding.btnStart.text = "시작"
                binding.btnStart.visibility = View.VISIBLE
                binding.btn15sec.visibility = View.VISIBLE
                binding.btn10sec.visibility = View.VISIBLE
                binding.btn5sec.visibility = View.VISIBLE

                isPlay = false
                SaveSettingUtil.setSelectYn(this@HandTrackingActivity, 0)
            }
        }
    }


    // 태핑 테스트 시작
    fun tappingStart() {
        if (!isPlay) {
            tappingList.clear()
            count = 0
            binding.tvCount.text = "$count"

            is3sec = true

            binding.btnStart.text = "중지"
            binding.btnStart.visibility = View.INVISIBLE
            binding.btn15sec.visibility = View.INVISIBLE
            binding.btn10sec.visibility = View.INVISIBLE
            binding.btn5sec.visibility = View.INVISIBLE

            binding.tvDelayCount.visibility = View.VISIBLE
            sCountDown.start()
        }
        else {
            isPaused = false
            isCancelled = true
            isPlay = false
        }
    }




    // 처음 y값들 벌어진 거리의 절대값
    // 3초 준비시간인지 확인
    fun check3sec(): Boolean {
        return is3sec
    }

    fun getIntervalY(y: Int) {
        startAbsY = y
    }

    // list 저장 - all landmark
    fun getFingerDataDetail(data: Finger3DDataDetail) {
        fingerDataList.add(data)
    }



    // 가이드 문구 보이기
    fun showGuidLine(show: Boolean) {
        runOnUiThread {
            if (show) {
                binding.tvGuidText.visibility = View.VISIBLE
            }
            else {
                binding.tvGuidText.visibility = View.INVISIBLE
            }
        }
    }


    fun changeBtnText() {
        if (SaveSettingUtil.getSelectYn(this) == 0) {
            binding.btnStart.text = "선택"
        }
        else if (SaveSettingUtil.getSelectYn(this) == 1) {
            binding.btnStart.text = "시작"
        }
    }


}