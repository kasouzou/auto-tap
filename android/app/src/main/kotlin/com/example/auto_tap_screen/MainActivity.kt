package com.example.auto_tap_screen

import android.content.Intent
import android.os.Build
import android.provider.Settings
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel

class MainActivity: FlutterActivity() {
    
    // Flutterと通信するための合言葉の通り道
    private val CHANNEL = "com.example.auto_tap_screen/tap"

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)

        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL).setMethodCallHandler { call, result ->
            // 【マクロな視点】ここでFlutterからの命令をすべて交通整理する
            when (call.method) {
                // 座標を予約するためのオーバーレイを表示
                "startOverlay" -> {
                    startService(Intent(this, OverlayService::class.java))
                    result.success(null)
                }

                // 監視スタート（赤いボタンを表示）
                "startMonitoring" -> {
                    val intent = Intent(this, FloatingButtonService::class.java)
                    // Android 8.0(Oreo)以上はフォアグラウンド制限があるため分岐
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        startForegroundService(intent)
                    } else {
                        startService(intent)
                    }
                    result.success(null)
                }

                // 監視ストップ（赤いボタンを消す）
                "stopMonitoring" -> {
                    stopService(Intent(this, FloatingButtonService::class.java))
                    result.success(null)
                }

                // 重ね合わせ権限のチェック
                "checkOverlayPermission" -> {
                    result.success(Settings.canDrawOverlays(this))
                }

                // MainActivity.kt の MethodChannel の handle 内に追加
                "openAccessibilitySettings" -> {
                    val intent = Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    result.success(null)
                }

                else -> {
                    result.notImplemented()
                }
            }
        }
    }
    // さっきはここに "startMonitoring" -> {...} がはみ出していたよ！
}