package com.blifeinc.pinch_game

import android.opengl.GLES20
import android.os.Looper
import android.util.Log
import com.blifeinc.pinch_game.http.Finger3DDataDetail
import com.blifeinc.pinch_game.http.FingerDataDetail
import com.blifeinc.pinch_game.http.MaxData
import com.blifeinc.pinch_game.util.SaveSettingUtil
import com.blifeinc.pinch_game.util.TimeConvert
import com.google.mediapipe.formats.proto.LandmarkProto
import com.google.mediapipe.solutioncore.ResultGlRenderer
import com.google.mediapipe.solutions.hands.Hands
import com.google.mediapipe.solutions.hands.HandsResult
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.text.SimpleDateFormat
import java.util.logging.Handler
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

class HandsResultGlRenderer : ResultGlRenderer<HandsResult> {

    private var program = 0
    private var positionHandle = 0
    private var projectionMatrixHandle = 0
    private var colorHandle = 0

    private val trackActivity = HandTrackingActivity.getInstance()
    private var now_state = "out"

    var startY = 0
    var countOkY = 0
    var inOkY = 0

    var basicZ = 0
    var count = 0


    var onePrint = false
    var maxList = mutableListOf<MaxData>()

    var zValueList = mutableListOf<Int>()




    private fun loadShader(type: Int, shaderCode: String): Int {
        val shader = GLES20.glCreateShader(type)
        GLES20.glShaderSource(shader, shaderCode)
        GLES20.glCompileShader(shader)

        return shader
    }


    override fun setupRendering() {
        program = GLES20.glCreateProgram()

        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, VERTEX_SHADER)
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, FRAGMENT_SHADER)

        GLES20.glAttachShader(program, vertexShader)
        GLES20.glAttachShader(program, fragmentShader)
        GLES20.glLinkProgram(program)

        positionHandle = GLES20.glGetAttribLocation(program, "vPosition")
        projectionMatrixHandle = GLES20.glGetUniformLocation(program, "uProjectionMatrix")
        colorHandle = GLES20.glGetUniformLocation(program, "uColor")
    }



    override fun renderResult(result: HandsResult?, projectionMatrix: FloatArray?) {
        if (result == null)  {
            return
        }

        GLES20.glUseProgram(program)
        GLES20.glUniformMatrix4fv(projectionMatrixHandle, 1, false, projectionMatrix, 0)
        GLES20.glLineWidth(CONNECTION_THICKNESS)

        val numHands = result.multiHandLandmarks().size
        for (i in 0 until numHands) {
            val isLeftHand = result.multiHandedness()[i].label == "Left"
            drawConnections(result.multiHandLandmarks()[i].landmarkList, if (isLeftHand) LEFT_HAND_CONNECTION_COLOR else RIGHT_HAND_CONNECTION_COLOR)


            // z 값 판별 -> 값이 작을수록 카메라에 가깝다? => int 변환 시 가까울수록 값이 커짐
            // 손목 기준
            val lm = result.multiHandLandmarks()[i].landmarkList[0]
            val checkZ = (lm.z * 10000000).toInt()

            if (checkZ > 5) {
                drawHollowCircle(lm.x, lm.y, if (isLeftHand) LEFT_HAND_HOLLOW_CIRCLE_COLOR else RIGHT_HAND_HOLLOW_CIRCLE_COLOR)
            }

            // 계산에 필요한 landmark value
            val point1X = (result.multiHandLandmarks()[i].landmarkList[4].x * 100)
            val point1Y = (result.multiHandLandmarks()[i].landmarkList[4].y * 100)
            val point2X = (result.multiHandLandmarks()[i].landmarkList[8].x * 100)
            val point2Y = (result.multiHandLandmarks()[i].landmarkList[8].y * 100)

            val point1Z = (result.multiHandLandmarks()[i].landmarkList[4].z * 100)
            val point2Z = (result.multiHandLandmarks()[i].landmarkList[8].z * 100)

            // 이외의 landmark value
            val wristX = (result.multiHandLandmarks()[i].landmarkList[0].x * 100)
            val wristY = (result.multiHandLandmarks()[i].landmarkList[0].y * 100)
            val wristZ = (result.multiHandLandmarks()[i].landmarkList[0].z * 100)

            val thumbCX = (result.multiHandLandmarks()[i].landmarkList[1].x * 100)
            val thumbCY = (result.multiHandLandmarks()[i].landmarkList[1].y * 100)
            val thumbCZ = (result.multiHandLandmarks()[i].landmarkList[1].z * 100)
            val thumbMX = (result.multiHandLandmarks()[i].landmarkList[2].x * 100)
            val thumbMY = (result.multiHandLandmarks()[i].landmarkList[2].y * 100)
            val thumbMZ = (result.multiHandLandmarks()[i].landmarkList[2].z * 100)
            val thumbIX = (result.multiHandLandmarks()[i].landmarkList[3].x * 100)
            val thumbIY = (result.multiHandLandmarks()[i].landmarkList[3].y * 100)
            val thumbIZ = (result.multiHandLandmarks()[i].landmarkList[3].z * 100)

            val indexMX = (result.multiHandLandmarks()[i].landmarkList[5].x * 100)
            val indexMY = (result.multiHandLandmarks()[i].landmarkList[5].y * 100)
            val indexMZ = (result.multiHandLandmarks()[i].landmarkList[5].z * 100)
            val indexPX = (result.multiHandLandmarks()[i].landmarkList[6].x * 100)
            val indexPY = (result.multiHandLandmarks()[i].landmarkList[6].y * 100)
            val indexPZ = (result.multiHandLandmarks()[i].landmarkList[6].z * 100)
            val indexDX = (result.multiHandLandmarks()[i].landmarkList[7].x * 100)
            val indexDY = (result.multiHandLandmarks()[i].landmarkList[7].y * 100)
            val indexDZ = (result.multiHandLandmarks()[i].landmarkList[7].z * 100)

            val middleMX = (result.multiHandLandmarks()[i].landmarkList[9].x * 100)
            val middleMY = (result.multiHandLandmarks()[i].landmarkList[9].y * 100)
            val middleMZ = (result.multiHandLandmarks()[i].landmarkList[9].z * 100)
            val middlePX = (result.multiHandLandmarks()[i].landmarkList[10].x * 100)
            val middlePY = (result.multiHandLandmarks()[i].landmarkList[10].y * 100)
            val middlePZ = (result.multiHandLandmarks()[i].landmarkList[10].z * 100)
            val middleDX = (result.multiHandLandmarks()[i].landmarkList[11].x * 100)
            val middleDY = (result.multiHandLandmarks()[i].landmarkList[11].y * 100)
            val middleDZ = (result.multiHandLandmarks()[i].landmarkList[11].z * 100)
            val middleTX = (result.multiHandLandmarks()[i].landmarkList[12].x * 100)
            val middleTY = (result.multiHandLandmarks()[i].landmarkList[12].y * 100)
            val middleTZ = (result.multiHandLandmarks()[i].landmarkList[12].z * 100)

            val ringMX = (result.multiHandLandmarks()[i].landmarkList[13].x * 100)
            val ringMY = (result.multiHandLandmarks()[i].landmarkList[13].y * 100)
            val ringMZ = (result.multiHandLandmarks()[i].landmarkList[13].z * 100)
            val ringPX = (result.multiHandLandmarks()[i].landmarkList[14].x * 100)
            val ringPY = (result.multiHandLandmarks()[i].landmarkList[14].y * 100)
            val ringPZ = (result.multiHandLandmarks()[i].landmarkList[14].z * 100)
            val ringDX = (result.multiHandLandmarks()[i].landmarkList[15].x * 100)
            val ringDY = (result.multiHandLandmarks()[i].landmarkList[15].y * 100)
            val ringDZ = (result.multiHandLandmarks()[i].landmarkList[15].z * 100)
            val ringTX = (result.multiHandLandmarks()[i].landmarkList[16].x * 100)
            val ringTY = (result.multiHandLandmarks()[i].landmarkList[16].y * 100)
            val ringTZ = (result.multiHandLandmarks()[i].landmarkList[16].z * 100)

            val pinkyMX = (result.multiHandLandmarks()[i].landmarkList[17].x * 100)
            val pinkyMY = (result.multiHandLandmarks()[i].landmarkList[17].y * 100)
            val pinkyMZ = (result.multiHandLandmarks()[i].landmarkList[17].z * 100)
            val pinkyPX = (result.multiHandLandmarks()[i].landmarkList[18].x * 100)
            val pinkyPY = (result.multiHandLandmarks()[i].landmarkList[18].y * 100)
            val pinkyPZ = (result.multiHandLandmarks()[i].landmarkList[18].z * 100)
            val pinkyDX = (result.multiHandLandmarks()[i].landmarkList[19].x * 100)
            val pinkyDY = (result.multiHandLandmarks()[i].landmarkList[19].y * 100)
            val pinkyDZ = (result.multiHandLandmarks()[i].landmarkList[19].z * 100)
            val pinkyTX = (result.multiHandLandmarks()[i].landmarkList[20].x * 100)
            val pinkyTY = (result.multiHandLandmarks()[i].landmarkList[20].y * 100)
            val pinkyTZ = (result.multiHandLandmarks()[i].landmarkList[20].z * 100)


            // 손가락 길이
            //val index_height = abs(point2Y - indexMY)
            //val thumb_height = abs(point1Y - thumbMY)


            // 거리값 계산
            val interval_y = abs(point1Y.toInt() - point2Y.toInt())
            //Log.d("tapping 거리 기준값 Value 확인", "${point1Y - point2Y}")




            // 유클리드 거리 - test
            //val aTox = (point2X - point1X).pow(2)
            //val bToy = (point2Y - point1Y).pow(2)
            //val cToz = (point2Z - point1Z).pow(2)
            //val euclidean3D = sqrt(aTox + bToy + cToz)



            // 맨하탄 거리 - test
            //val manhattan_y = abs(point1Y - point2Y)
            //val manhattan_x = abs(point1X - point2X)
            //val manhattan_z = abs(point1Z - point2Z)

            //val manhattan3D = manhattan_x + manhattan_y + manhattan_z


            //
            val indexTothumbX = abs(indexMX - point1X)
            val indexTothumbY = abs(indexMY - point2Y)
            val indexTothumbZ = abs(indexMZ - point2Z)

            val checkInterval = (indexMY - point2Y).toInt()

            val manhattanTest = indexTothumbX + indexTothumbY + indexTothumbZ





            // 거리 기준 가이드 - 기준치 10~20까지 값
            if (trackActivity?.isPlay != true && trackActivity?.check3sec() != true) {
                if (point1Y - point2Y < 18 ) {
                    trackActivity?.showGuidLine(true)
                }
                else {
                    trackActivity?.showGuidLine(false)
                }
            }



            // 초기 y 좌표들의 거리값 저장 부분 - 3sec 준비 시간 시점
            if (trackActivity?.check3sec() == true) {
                onePrint = false

                //startY = interval_y
                //countOkY = ((startY.toFloat() / 10) * 9).toInt()
                //inOkY = startY / 10


                // test value
                startY = indexTothumbY.toInt()
                countOkY = ((startY.toFloat() / 10) * 9).toInt()
                inOkY = startY / 10

                zValueList.add(indexTothumbZ.toInt())


                // log 출력용 - 초기 거리값, 계산한 카운트 초기값
                val maxData = MaxData(
                    max_height = startY,
                    difference = countOkY
                )

                maxList.add(maxData)

                trackActivity.getIntervalY(interval_y)
            }



            // 본 태핑 시작 시점
            if (trackActivity?.isPlay == true) {
                if (!onePrint) {
                    var sumMax = 0
                    var sumDiffer = 0
                    val list = mutableListOf<MaxData>()


                    // 평균값 계산
                    for (data in maxList) {
                        sumMax += data.max_height
                        sumDiffer += data.difference
                    }


                    // z값 test
                    var avgZ = 0
                    for (item in zValueList) {
                        avgZ += item
                    }

                    basicZ = avgZ / zValueList.size



                    // 값 쳐내기
                    for (item in maxList) {
                        if (item.max_height <= percentValue(sumMax/maxList.size.toDouble(), 5.0)) {
                            val deleteData = MaxData(
                                max_height = item.max_height,
                                difference = item.difference
                            )
                            list.add(deleteData)
                        }
                    }
                    maxList.removeAll(list)


                    // 기준값 다시 계산 - max 값 가져오기??
                    var newMax = 0

                    for (data in maxList) {
                        if (newMax < data.max_height) {
                            newMax = data.max_height
                        }
                    }

                    startY = newMax
                    countOkY = ((startY.toFloat() / 10) * 9).toInt()
                    inOkY = startY / 10

                    trackActivity.getIntervalY(newMax)

                    onePrint = true
                    maxList.clear()


                    count = 0
                    Log.d("기준값 확인 - 초기화", "$startY  :::  $countOkY  :::  $inOkY  :::  $basicZ")
                }


                val timestamp = System.currentTimeMillis()
                val timeChange = TimeConvert().convertTime(timestamp)

                val fingerData = Finger3DDataDetail(
                    time = timeChange,
                    wrist_x = wristX.toDouble(),
                    wrist_y = wristY.toDouble(),
                    wrist_z = wristZ.toDouble(),
                    thumb_cmc_x = thumbCX.toDouble(),
                    thumb_cmc_y = thumbCY.toDouble(),
                    thumb_cmc_z = thumbCZ.toDouble(),
                    thumb_mcp_x = thumbMX.toDouble(),
                    thumb_mcp_y = thumbMY.toDouble(),
                    thumb_mcp_z = thumbMZ.toDouble(),
                    thumb_ip_x = thumbIX.toDouble(),
                    thumb_ip_y = thumbIY.toDouble(),
                    thumb_ip_z = thumbIZ.toDouble(),
                    thumb_tip_x = point1X.toDouble(),
                    thumb_tip_y = point1Y.toDouble(),
                    thumb_tip_z = point1Z.toDouble(),
                    index_finger_mcp_x = indexMX.toDouble(),
                    index_finger_mcp_y = indexMY.toDouble(),
                    index_finger_mcp_z = indexMZ.toDouble(),
                    index_finger_pip_x = indexPX.toDouble(),
                    index_finger_pip_y = indexPY.toDouble(),
                    index_finger_pip_z = indexPZ.toDouble(),
                    index_finger_dip_x = indexDX.toDouble(),
                    index_finger_dip_y = indexDY.toDouble(),
                    index_finger_dip_z = indexDZ.toDouble(),
                    index_finger_tip_x = point2X.toDouble(),
                    index_finger_tip_y = point2Y.toDouble(),
                    index_finger_tip_z = point2Z.toDouble(),
                    middle_finger_mcp_x = middleMX.toDouble(),
                    middle_finger_mcp_y = middleMY.toDouble(),
                    middle_finger_mcp_z = middleMZ.toDouble(),
                    middle_finger_pip_x = middlePX.toDouble(),
                    middle_finger_pip_y = middlePY.toDouble(),
                    middle_finger_pip_z = middlePZ.toDouble(),
                    middle_finger_dip_x = middleDX.toDouble(),
                    middle_finger_dip_y = middleDY.toDouble(),
                    middle_finger_dip_z = middleDZ.toDouble(),
                    middle_finger_tip_x = middleTX.toDouble(),
                    middle_finger_tip_y = middleTY.toDouble(),
                    middle_finger_tip_z = middleTZ.toDouble(),
                    ring_finger_mcp_x = ringMX.toDouble(),
                    ring_finger_mcp_y = ringMY.toDouble(),
                    ring_finger_mcp_z = ringMZ.toDouble(),
                    ring_finger_pip_x = ringPX.toDouble(),
                    ring_finger_pip_y = ringPY.toDouble(),
                    ring_finger_pip_z = ringPZ.toDouble(),
                    ring_finger_dip_x = ringDX.toDouble(),
                    ring_finger_dip_y = ringDY.toDouble(),
                    ring_finger_dip_z = ringDZ.toDouble(),
                    ring_finger_tip_x = ringTX.toDouble(),
                    ring_finger_tip_y = ringTY.toDouble(),
                    ring_finger_tip_z = ringTZ.toDouble(),
                    pinky_mcp_x = pinkyMX.toDouble(),
                    pinky_mcp_y = pinkyMY.toDouble(),
                    pinky_mcp_z = pinkyMZ.toDouble(),
                    pinky_pip_x = pinkyPX.toDouble(),
                    pinky_pip_y = pinkyPY.toDouble(),
                    pinky_pip_z = pinkyPZ.toDouble(),
                    pinky_dip_x = pinkyDX.toDouble(),
                    pinky_dip_y = pinkyDY.toDouble(),
                    pinky_dip_z = pinkyDZ.toDouble(),
                    pinky_tip_x = pinkyTX.toDouble(),
                    pinky_tip_y = pinkyTY.toDouble(),
                    pinky_tip_z = pinkyTZ.toDouble()
                )

                //Log.d(TAG, "손가락 위치값 확인 | $saveData")

                trackActivity.getFingerDataDetail(fingerData)
            }




            Log.d("현재 거리 차이", "$manhattanTest  :::  $indexTothumbX  :::  $checkInterval  :::  $indexTothumbZ")



            // 조건 설정
            // z값 변화 체크후 변화가 있을 시 기울기 값과 같이 계산
            // z의 값..을 같이 검사
            // 유효거리 판단 -> 90% 이상
            //

            if (basicZ < indexTothumbZ.toInt()) {
                val sumInterval = checkInterval + indexTothumbZ.toInt()

                if (sumInterval <= inOkY) {
                    now_state = "in"
                }
                if (sumInterval >= countOkY && now_state == "in") {
                    now_state = "out"
                    //count++

                    trackActivity?.setCount()

                    //Log.d("현재 count", "$count")
                }
            }
            else {
                if (checkInterval <= inOkY) {
                    now_state = "in"
                }
                if (checkInterval >= countOkY && now_state == "in") {
                    now_state = "out"
                    //count++

                    trackActivity?.setCount()

                    //Log.d("현재 count", "$count")
                }
            }



//            if (interval_y <= inOkY) {
//                now_state = "in"
//            }
//            if (interval_y >= countOkY && now_state == "in") {
//                now_state = "out"
//                trackActivity?.setCount()
//            }




            // 표시 그리기 함수
            for (landmark in result.multiHandLandmarks()[i].landmarkList) {
                drawCircle(landmark.x, landmark.y, if (isLeftHand) LEFT_HAND_LANDMARK_COLOR else RIGHT_HAND_LANDMARK_COLOR)
            }
        }
    }



    //퍼센트값 구하기
    fun percentValue(sum: Double, percent: Double): Double {
        val result = sum / 100 * percent
        return  result
    }




    // 종료
    fun release() {
        GLES20.glDeleteProgram(program)
    }


    // 연결선 그리는 부분???
    fun drawConnections(handLandmarkList: List<LandmarkProto.NormalizedLandmark>, colorArray: FloatArray) {
        GLES20.glUniform4fv(colorHandle, 1, colorArray, 0)

        for (c in Hands.HAND_CONNECTIONS) {
            val start = handLandmarkList[c.start()]
            val end = handLandmarkList[c.end()]
            val vertex = floatArrayOf(start.x, start.y, end.x, end.y)
            val vertexBuffer = ByteBuffer.allocateDirect(vertex.size * 4).order(ByteOrder.nativeOrder()).asFloatBuffer().put(vertex)

            vertexBuffer.position(0)
            GLES20.glEnableVertexAttribArray(positionHandle)
            GLES20.glVertexAttribPointer(positionHandle, 2, GLES20.GL_FLOAT, false, 0, vertexBuffer)
            GLES20.glDrawArrays(GLES20.GL_LINES, 0, 2)
        }
    }



    // landmark 를 표시할 원을 그리는 함수??
    fun drawCircle(x: Float, y: Float, colorArray: FloatArray) {
        GLES20.glUniform4fv(colorHandle, 1, colorArray, 0)

        val vertexCount = NUM_SEGMENTS + 2
        val vertices = FloatArray(vertexCount * 3)
        vertices[0] = x
        vertices[1] = y
        vertices[2] = 0f

        for (i in 1 until vertexCount) {
            val angle = 2.0f * i * Math.PI.toFloat() / NUM_SEGMENTS
            val currentIndex = 3 * i

            vertices[currentIndex] = x + (LANDMARK_RADIUS * cos(angle.toDouble())).toFloat()
            vertices[currentIndex + 1] = y + (LANDMARK_RADIUS * sin(angle.toDouble())).toFloat()
            vertices[currentIndex + 2] = 0f
        }

        val vertexBuffer = ByteBuffer.allocateDirect(vertices.size * 4).order(ByteOrder.nativeOrder()).asFloatBuffer().put(vertices)
        vertexBuffer.position(0)
        GLES20.glEnableVertexAttribArray(positionHandle)
        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, vertexCount)
    }



    // 감싸는 원
    fun drawHollowCircle(x: Float, y: Float, colorArray: FloatArray) {
        GLES20.glUniform4fv(colorHandle, 1, colorArray, 0)

        val vertexCount = NUM_SEGMENTS + 1
        val vertices = FloatArray(vertexCount * 3)

        for (i in 0 until vertexCount) {
            val angle = 2.0f * i * Math.PI.toFloat() / NUM_SEGMENTS
            val currentIndex = 3 * i

            vertices[currentIndex] = x + (HOLLOW_CIRCLE_RADIUS * cos(angle.toDouble())).toFloat()
            vertices[currentIndex + 1] = y + (HOLLOW_CIRCLE_RADIUS * sin(angle.toDouble())).toFloat()
            vertices[currentIndex + 2] = 0f
        }

        val vertexBuffer = ByteBuffer.allocateDirect(vertices.size * 4).order(ByteOrder.nativeOrder()).asFloatBuffer().put(vertices)
        vertexBuffer.position(0)
        GLES20.glEnableVertexAttribArray(positionHandle)
        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer)
        GLES20.glDrawArrays(GLES20.GL_LINE_STRIP, 0, vertexCount)
    }



    // rgb hex 값??
    companion object {
        private const val TAG = "HandsResultGlRenderer"
        private val LEFT_HAND_CONNECTION_COLOR = floatArrayOf(0.2f, 1f, 0.2f, 1f)
        private val RIGHT_HAND_CONNECTION_COLOR = floatArrayOf(1f, 0.2f, 0.2f, 1f)
        private const val CONNECTION_THICKNESS = 8.0f
        private val LEFT_HAND_HOLLOW_CIRCLE_COLOR = floatArrayOf(0.2f, 1f, 0.2f, 1f)
        private val RIGHT_HAND_HOLLOW_CIRCLE_COLOR = floatArrayOf(1f, 0.2f, 0.2f, 1f)
        private const val HOLLOW_CIRCLE_RADIUS = 0.02f
        private val LEFT_HAND_LANDMARK_COLOR = floatArrayOf(1f, 0.2f, 0.2f, 1f)
        private val RIGHT_HAND_LANDMARK_COLOR = floatArrayOf(0.2f, 1f, 0.2f, 1f)
        private const val LANDMARK_RADIUS = 0.008f
        private const val NUM_SEGMENTS = 120
        private const val VERTEX_SHADER = ("uniform mat4 uProjectionMatrix;\n"
                + "attribute vec4 vPosition;\n"
                + "void main() {\n"
                + "  gl_Position = uProjectionMatrix * vPosition;\n"
                + "}")
        private const val FRAGMENT_SHADER = ("precision mediump float;\n"
                + "uniform vec4 uColor;\n"
                + "void main() {\n"
                + "  gl_FragColor = uColor;\n"
                + "}")
    }


}