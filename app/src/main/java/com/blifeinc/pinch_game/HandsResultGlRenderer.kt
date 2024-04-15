package com.blifeinc.pinch_game

import android.opengl.GLES20
import android.util.Log
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
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

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

    var onePrint = false
    var maxList = mutableListOf<MaxData>()
    //private val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.zzz")


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
            val lm = result.multiHandLandmarks()[i].landmarkList[0]
            val checkZ = (lm.z * 10000000).toInt()

            if (checkZ > 5) {
                //Log.d(TAG, "LandMark[0] | z : ${lm.z}, $checkZ")
                drawHollowCircle(lm.x, lm.y, if (isLeftHand) LEFT_HAND_HOLLOW_CIRCLE_COLOR else RIGHT_HAND_HOLLOW_CIRCLE_COLOR)
            }

            // 계산에 필요한 landmark value
            val point1X = (result.multiHandLandmarks()[i].landmarkList[4].x * 100).toInt()
            val point1Y = (result.multiHandLandmarks()[i].landmarkList[4].y * 100).toInt()
            val point2X = (result.multiHandLandmarks()[i].landmarkList[8].x * 100).toInt()
            val point2Y = (result.multiHandLandmarks()[i].landmarkList[8].y * 100).toInt()

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



            // 거리값 계산
            val interval_y = abs( point1Y - point2Y )


            // 초기 y 좌표들의 거리값 저장 부분 - 3sec 준비 시간 시점
            if (trackActivity?.check3sec() == true) {
                onePrint = false

                startY = interval_y
                countOkY = ((startY.toFloat() / 10) * 9).toInt()
                inOkY = startY / 10

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


                    // 값 쳐내기
                    for (item in maxList) {
                        if (item.max_height < percentValue(sumMax/maxList.size.toDouble(), 5.0)) {
                            val deleteData = MaxData(
                                max_height = item.max_height,
                                difference = item.difference
                            )
                            list.add(deleteData)
                        }
                    }
                    maxList.removeAll(list)
                    Log.d("Delete Value", "$list")


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

                    // sumDiffer / maxList.size

                    Log.d("Data Value", "$startY :: $countOkY :: $inOkY")
                    trackActivity.getIntervalY(newMax)

                    onePrint = true
                }



                val timestamp = System.currentTimeMillis()
                val timeChange = TimeConvert().convertTime(timestamp)

                val saveData = FingerDataDetail(
                    time = timeChange,
                    thumb_x = point1X.toDouble(),
                    thumb_y = point1Y.toDouble(),
                    thumb_z = point1Z.toDouble(),
                    index_x = point2X.toDouble(),
                    index_y = point2Y.toDouble(),
                    index_z = point2Z.toDouble()
                )
                //Log.d(TAG, "손가락 위치값 확인 | $saveData")

                trackActivity.getTappingDetail(saveData)
            }



            // z의 값..을 같이 검사??
            // 유효거리 판단 -> 90% 이상??
            if (interval_y <= inOkY) {
                now_state = "in"
            }
            if (interval_y >= countOkY && now_state == "in") {
                now_state = "out"
                trackActivity?.setCount()
            }


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