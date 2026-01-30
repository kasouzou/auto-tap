package com.kasouzou.autotapscreen

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Toast

class OverlayService : Service() {

    // 【公式】WindowManager: 画面に部品を重ねるための司令塔
    private lateinit var windowManager: WindowManager
    // 【独自】overlayView: 画面全体を覆う「透明な下敷き」
    private var overlayView: View? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()

        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        // 【公式】WindowManager.LayoutParams: 重ねる画面の「ルール」を決める設定値
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT, // 横幅：全画面
            WindowManager.LayoutParams.MATCH_PARENT, // 縦幅：全画面
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY, // 重ね合わせの優先順位（最上位）
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, // キーボード入力を受け付けない
            PixelFormat.TRANSLUCENT // 背景を透過させる
        )

        // 【独自】下敷き（View）を作成。ここではただの空っぽのViewだよ。
        overlayView = View(this).apply {
            setBackgroundColor(Color.argb(80, 255, 0, 0)) // 開発中は「赤色（半透明）」にすると見やすいぞ！
            
            // タップイベントを拾う「耳」を付ける
            setOnTouchListener { v, event ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    // 【公式】event.rawX, rawY: 画面全体の座標を取得（公式のプロパティ）
                    val x = event.rawX
                    val y = event.rawY

                    saveCoordinates(x, y) // 座標を保存して
                    stopSelf()            // 役目を終えてサービス終了
                }
                true
            }
        }

        // 下敷きを画面に貼り付ける！
        windowManager.addView(overlayView, params)
        Toast.makeText(this, "予約したい場所をタップしてね！", Toast.LENGTH_SHORT).show()
    }

    // 【独自】座標を保存する関数
    private fun saveCoordinates(x: Float, y: Float) {
        // 【公式】SharedPreferences: スマホ内に小さなデータを保存する仕組み
        val sharedPref = getSharedPreferences("TapSettings", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putFloat("targetX", x)
            putFloat("targetY", y)
            apply()
        }
        // 開発者向けにログを出しておく（これもおばあちゃんの知恵）
        println("座標予約完了！ X: $x, Y: $y")
    }

    override fun onDestroy() {
        super.onDestroy()
        // サービスが終わるときは、下敷きを片付ける。これを忘れると画面がずっとタップできなくなるぞ！
        overlayView?.let { windowManager.removeView(it) }
    }
}