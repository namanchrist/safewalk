package com.example.safewalk.service

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlin.math.sqrt

class SensorService(private val context: Context) {
    
    private val sensorManager: SensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    
    // Shake detection parameters
    private val SHAKE_THRESHOLD_GRAVITY = 2.7f
    private val SHAKE_SLOP_TIME_MS = 500
    private val SHAKE_COUNT_RESET_TIME_MS = 3000
    
    /**
     * Get a flow of shake events that emits a value whenever the device is shaken
     */
    fun getShakeDetection(): Flow<Unit> = callbackFlow {
        var shakeTimestamp: Long = 0
        var shakeCount: Int = 0
        
        val sensorEventListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
                    val x = event.values[0]
                    val y = event.values[1]
                    val z = event.values[2]
                    
                    val gForce = sqrt(x * x + y * y + z * z) / SensorManager.GRAVITY_EARTH
                    
                    if (gForce > SHAKE_THRESHOLD_GRAVITY) {
                        val now = System.currentTimeMillis()
                        
                        // Ignore shake events that are too close together (500ms)
                        if (shakeTimestamp + SHAKE_SLOP_TIME_MS > now) {
                            return
                        }
                        
                        // Reset shake count if too much time has passed
                        if (shakeTimestamp + SHAKE_COUNT_RESET_TIME_MS < now) {
                            shakeCount = 0
                        }
                        
                        shakeTimestamp = now
                        shakeCount++
                        
                        // If we've detected 3 shakes in the proper time span, send the alert
                        if (shakeCount >= 3) {
                            trySend(Unit)
                            shakeCount = 0
                        }
                    }
                }
            }
            
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                // Not needed for this implementation
            }
        }
        
        if (accelerometer != null) {
            sensorManager.registerListener(
                sensorEventListener,
                accelerometer,
                SensorManager.SENSOR_DELAY_GAME
            )
        }
        
        awaitClose {
            sensorManager.unregisterListener(sensorEventListener)
        }
    }
} 