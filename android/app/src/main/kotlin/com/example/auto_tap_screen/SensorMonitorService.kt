package com.example.auto_tap_screen

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import android.util.Log

class SensorMonitorService : Service(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var proximitySensor: Sensor? = null
    
    // 【独自】連打防止用のフラグ（手をかざし続けている間は反応しない）
    private var isHandNear = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        Log.d("SensorMonitor", "センサー監視サービス生成！")

        // 1. 【公式】通知を出して「死なないサービス」にする
        createNotificationChannel()
        val notification = NotificationCompat.Builder(this, "SENSOR_CHANNEL")
            .setContentTitle("ジェスチャー監視中")
            .setContentText("スマホの上部に手をかざすとタップします")
            .setSmallIcon(android.R.drawable.ic_input_add) // 適当なアイコン
            .build()

        // Android 14対策：本来はここで type="specialUse" 等が必要だが、
        // センサーは比較的緩いので一旦通常のForegroundServiceとして起動
        startForeground(2, notification)

        // 2. 【公式】センサーの準備
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)

        if (proximitySensor == null) {
            Log.e("SensorMonitor", "このスマホには近接センサーがない！！")
            stopSelf() // センサーがないなら解散
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // 監視スタート！
        proximitySensor?.also { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
        return START_STICKY
    }

    // ---------------------------------------------------------
    // 【公式】センサーの値が変わった時に呼ばれる
    // ---------------------------------------------------------
    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            val distance = it.values[0]
            val maxRange = it.sensor.maximumRange
            
            // マクロな視点：多くの機種で「近づく=0.0」「離れる=maxRange(例:5.0)」となる
            // ここでは「最大距離より小さければ近づいた」と判定する
            
            if (distance < maxRange) {
                // 手が近づいた！ (NEAR)
                if (!isHandNear) {
                    isHandNear = true
                    Log.d("SensorMonitor", "手を検知！魔法を発動します。")
                    
                    // 【マクロ連携】指（AutoTapService）に命令！
                    AutoTapService.tapNow()
                }
            } else {
                // 手が離れた！ (FAR)
                isHandNear = false
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // 精度が変わっても気にしない
    }

    override fun onDestroy() {
        super.onDestroy()
        // サービス終了時はセンサーを解放（これ忘れるとバッテリー死ぬぞ！）
        sensorManager.unregisterListener(this)
        Log.d("SensorMonitor", "センサー監視終了")
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "SENSOR_CHANNEL", "ジェスチャー監視", NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}