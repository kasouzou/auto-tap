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
        
        // 【独自】実行ボタン！
        floatingButton = Button(this).apply {
            text = "TAP!"
            setBackgroundColor(Color.RED)
            setTextColor(Color.WHITE)
            alpha = 0.9f
            
            setOnClickListener {
                Log.d("FloatingService", "ボタンが押された！指に命令を送るぞ")
                // 【マクロ連携】指（AutoTapService）にタップを依頼
                AutoTapService.tapNow()
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