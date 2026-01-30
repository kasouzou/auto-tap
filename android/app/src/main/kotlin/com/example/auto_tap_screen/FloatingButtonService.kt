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
        Log.d("FloatingService", "自動スキップ起動！")
        
        // 1. フォアグラウンド通知を開始（これでアプリが裏でも死ななくなる）
        startForegroundServiceWithNotification()

        // 2. ユーザーへのフィードバック
        // これが出れば、ボタンがなくても「あ、動いたな」とわかる
        Toast.makeText(this, "自動スキップを開始しました。", Toast.LENGTH_SHORT).show()
    }

    private fun startForegroundServiceWithNotification() {
        val channelId = "floating_btn_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, 
                "広告スキップボタン監視", 
                NotificationManager.IMPORTANCE_LOW // MINだと通知が出ない場合があるためLOWを推奨
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("広告スキップボタン自動タップアプリ稼働中")
            .setContentText("広告スキップボタンを自動でスキャンしています")
            .setSmallIcon(android.R.drawable.ic_menu_view)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        // ID 2 でフォアグラウンドサービスとして登録
        startForeground(2, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("FloatingService", "自動スキップ停止！")
    }
}