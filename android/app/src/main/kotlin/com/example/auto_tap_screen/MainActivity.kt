package com.example.auto_tap_screen

import android.content.Intent
import android.os.Build // 【公式】Androidのバージョンを確認するため
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel

class MainActivity: FlutterActivity() {
    
    private val CHANNEL = "com.example.auto_tap_screen/tap"

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)

        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL).setMethodCallHandler {
            call, result ->

            // 【独自ロジック】if文を when文 に変えて、複数の命令をさばく！
            when (call.method) {
                // Flutter側で invokeMethod("startOverlay") した時
                "startOverlay" -> {
                    startService(Intent(this, OverlayService::class.java))
                    result.success(null)
                }

                // Flutter側で invokeMethod("startMonitoring") した時
                "startMonitoring" -> {
                    // 【変更点】センサーサービスを起動する
                    val intent = Intent(this, SensorMonitorService::class.java)
                    
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        startForegroundService(intent)
                    } else {
                        startService(intent)
                    }
                    result.success(null)
                }

                // 知らない合言葉が来た時
                else -> {
                    result.notImplemented()
                }
            }
        }
    }

    // --- 独自関数：監視サービスの起動（マクロな視点での分岐） ---
    // MainActivity.kt の startAudioMonitorService 内
    // --- 独自関数：センサー監視サービスの起動 ---
    private fun startSensorMonitorService() {
        // ここがコンパイルエラーの元だった場所だよ！
        val intent = Intent(this, SensorMonitorService::class.java)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }
}