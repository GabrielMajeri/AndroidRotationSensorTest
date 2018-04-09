package com.example.rotationsensortest

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.Process
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.sqrt

class MainActivity : AppCompatActivity(), SensorEventListener {
    private val handlerThread = HandlerThread("Gyro Thread", Process.THREAD_PRIORITY_MORE_FAVORABLE)

    private var x = 0.0
    private var y = 0.0
    private var z = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        val gyro = sensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR)

        run {
            handlerThread.start()
            val handler = Handler(handlerThread.looper)

            sensorManager.registerListener(this, gyro, SensorManager.SENSOR_DELAY_FASTEST, handler)
        }

        run {
            val delay = 40L
            val handler = Handler(mainLooper)
            handler.postDelayed(object: Runnable {
                override fun run() {
                    val xDeg = x / Math.PI * 180.0
                    val yDeg = y / Math.PI * 180.0
                    val zDeg = z / Math.PI * 180.0

                    xRotView.text = "X: %.2f".format(xDeg)
                    yRotView.text = "Y: %.2f".format(yDeg)
                    zRotView.text = "Z: %.2f".format(zDeg)

                    handler.postDelayed(this, delay)
                }
            }, delay)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type != Sensor.TYPE_GAME_ROTATION_VECTOR)
            return

        val (q1, q2, q3) = event.values.take(3).map { it.toDouble() }
        val q0 = sqrt(1 - q1*q1 - q2*q2 - q3*q3)

        x = atan2(2 * (q2*q3 + q0*q1), q3*q3 - q2*q2 - q1*q1 + q0*q0)
        y = -asin(2*q1*q3 - 2*q0*q2)
        z = atan2(2*q1*q2 + 2*q0*q3, q1*q1 + q0*q0 - q3*q3 - q2*q2)
    }
}
