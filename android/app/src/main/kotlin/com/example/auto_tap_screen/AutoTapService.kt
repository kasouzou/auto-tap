package com.example.auto_tap_screen

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Context
import android.graphics.Path
import android.view.accessibility.AccessibilityEvent
import android.util.Log // ログ出力用に追加

class AutoTapService : AccessibilityService() {

    // ---------------------------------------------------------
    // 【独自】魔法の窓口 (これがないと他から呼べない！)
    // ---------------------------------------------------------
    companion object {
        private var instance: AutoTapService? = null

        // 音響サービスが「AutoTapService.tapNow()」と呼ぶための関数
        fun tapNow() {
            if (instance == null) {
                Log.e("AutoTap", "サービスが未起動！ユーザー補助設定をONにして！")
            }
            instance?.triggerTapFromSavedCoordinates()
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this // サービス起動時に自分を登録
        Log.d("AutoTap", "魔法の指、準備完了！")
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null // 終了時に登録解除
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
    override fun onInterrupt() {}

    fun triggerTapFromSavedCoordinates() {
        val sharedPref = getSharedPreferences("TapSettings", Context.MODE_PRIVATE)
        val targetX = sharedPref.getFloat("targetX", -1f)
        val targetY = sharedPref.getFloat("targetY", -1f)

        if (targetX == -1f || targetY == -1f) return
        performTap(targetX, targetY)
    }

    private fun performTap(x: Float, y: Float) {
        val path = Path()
        path.moveTo(x, y)
        val gestureBuilder = GestureDescription.Builder()
        val stroke = GestureDescription.StrokeDescription(path, 0, 100)
        gestureBuilder.addStroke(stroke)

        dispatchGesture(gestureBuilder.build(), object : GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription?) {
                super.onCompleted(gestureDescription)
                Log.d("AutoTap", "タップ成功: ($x, $y)")
            }
        }, null)
    }
}