package com.example.auto_tap_screen

import android.app.*
import android.content.*
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.WindowManager
import android.widget.Button
import android.widget.Toast // 【これが必要！】Toastを使えるようにする
import androidx.core.app.NotificationCompat
import android.util.Log

class FloatingButtonService : Service() {
    private var windowManager: WindowManager? = null
    private var floatingButton: Button? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        Log.d("FloatingService", "ボタンサービス起動！")
        
        startForegroundServiceWithNotification()

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        
        floatingButton = Button(this).apply {
            text = "TEST" // 全自動のテストボタンにする
            setBackgroundColor(Color.BLUE) // わかりやすく青色にしてみる？
            setTextColor(Color.WHITE)
            
            setOnClickListener {
                Log.d("FloatingService", "テスト実行：画面内の『スキップ』を探します")
                
                // 【修正ポイント】存在しない tapNow() ではなく、サービスに通知を送るか、
                // ログを出して今の状態（全自動が動いているか）を確認する
                Toast.makeText(applicationContext, "全自動監視中！YouTubeを開いてみてね", Toast.LENGTH_SHORT).show()
            }
        }

        val params = WindowManager.LayoutParams(
            180, // 幅
            180, // 高さ
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY, // 重ね合わせ権限
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.BOTTOM or Gravity.END
            x = 50
            y = 250
        }

        windowManager?.addView(floatingButton, params)
    }

    private fun startForegroundServiceWithNotification() {
        val channelId = "floating_btn_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Tap Button", NotificationManager.IMPORTANCE_MIN)
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("自動タップ準備完了")
            .setContentText("TAP!ボタンを押すと予約位置を叩きます")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .build()
        startForeground(2, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        floatingButton?.let { windowManager?.removeView(it) }
    }
}