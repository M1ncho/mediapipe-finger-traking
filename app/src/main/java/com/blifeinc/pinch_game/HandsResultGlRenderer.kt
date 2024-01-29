package com.blifeinc.pinch_game

import android.opengl.GLES20
import android.util.Log
import com.blifeinc.pinch_game.http.FingerDataDetail
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


            var point1X = (result.multiHandLandmarks()[i].landmarkList[4].x * 100).toInt()
            var point1Y = (result.multiHandLandmarks()[i].landmarkList[4].y * 100).toInt()
            var point2X = (result.multiHandLandmarks()[i].landmarkList[8].x * 100).toInt()
            var point2Y = (result.multiHandLandmarks()[i].landmarkList[8].y * 100).toInt()

            var point1Z = (result.multiHandLandmarks()[i].landmarkList[4].z * 100)
            var point2Z = (result.multiHandLandmarks()[i].landmarkList[8].z * 100)


            //val interval_x = abs( point1X - point2X )
            val interval_y = abs( point1Y - point2Y )


            // 초기 y 좌표들의 거리값 저장 부분
            if (trackActivity?.check3sec() == true) {
                startY = interval_y
                countOkY = ((startY.toFloat() / 10) * 9).toInt()
                inOkY = startY / 10

                trackActivity.getIntervalY(interval_y)
            }


            if (trackActivity?.isPlay == true) {
                var timestamp = System.currentTimeMillis()
                val timeChange = TimeConvert().convertTime(timestamp)
                val test = TimeConvert().convertDateTime(timeChange)


                var saveData = FingerDataDetail(
                    time = timeChange,
                    thumb_x = point1X.toDouble(),
                    thumb_y = point1Y.toDouble(),
                    thumb_z = point1Z.toDouble(),
                    index_x = point2X.toDouble(),
                    index_y = point2Y.toDouble(),
                    index_z = point2Z.toDouble()
                )

                //Log.d(TAG, "timestamp 확인 | $timestamp")
                //Log.d(TAG, "timestamp convert 확인 | $timeChange   $test")

                Log.d(TAG, "손가락 위치값 확인 | $saveData")

                trackActivity.getTappingDetail(saveData)
            }



            // z의 값..을 같이 검사??
            // 유효거리 판단 -> 90% 이상??
            // in->5  out->30
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