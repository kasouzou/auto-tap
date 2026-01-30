package com.example.auto_tap_screen

import android.app.*
import android.content.*
import android.os.Build
import android.os.IBinder
import android.widget.Toast
import androidx.core.app.NotificationCompat
import android.util.Log

class FloatingButtonService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        Log.d("FloatingService", "自動監視エンジン起動！")
        
        // 1. フォアグラウンド通知を開始（これでアプリが裏でも死ななくなる）
        startForegroundServiceWithNotification()

        // 2. ユーザーへのフィードバック
        // これが出れば、ボタンがなくても「あ、動いたな」とわかる
        Toast.makeText(this, "広告スナイパー、監視を開始しました", Toast.LENGTH_SHORT).show()
    }

    private fun startForegroundServiceWithNotification() {
        val channelId = "floating_btn_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, 
                "広告スキップ監視", 
                NotificationManager.IMPORTANCE_LOW // MINだと通知が出ない場合があるためLOWを推奨
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("広告スナイパー稼働中")
            .setContentText("YouTubeの広告を自動でスキャンしています")
            .setSmallIcon(android.R.drawable.ic_menu_view)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        // ID 2 でフォアグラウンドサービスとして登録
        startForeground(2, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("FloatingService", "自動監視エンジン停止")
    }
}