package com.example.auto_tap_screen

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.util.Log // これが必要だった！
import android.os.Vibrator
import android.content.Context
import android.widget.Toast

// 【重要】ここが「家」の門構え。これがないとエラーになる！
class AutoTapService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        // 画面の変化を検知
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED || 
            event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            
            val rootNode = rootInActiveWindow ?: return

            // YouTube系アプリだけに限定
            val targetApps = listOf("com.google.android.youtube", "com.google.android.apps.youtube.music")
            val currentPackage = rootNode.packageName?.toString() ?: ""
            
            if (!targetApps.contains(currentPackage)) {
                return 
            }

            // スキップ対象の文字リスト
            val skipTerms = listOf("広告をスキップ", "スキップ", "Skip Ad", "動画をスキップ")

            for (term in skipTerms) {
                val nodes = rootNode.findAccessibilityNodeInfosByText(term)
                if (nodes != null) {
                    for (node in nodes) {
                        if (node.isVisibleToUser && isClickableRecursive(node)) {
                            // 魔法発動時のフィードバック
                            val v = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                            v.vibrate(100)
                            
                            // メインスレッドでトーストを表示
                            Log.d("AutoTap", "スナイプ成功：[$term] を自動タップしました！")
                            return 
                        }
                    }
                }
            }
        }
    }

    // 補助関数もクラスの中に入れる！
    private fun isClickableRecursive(node: AccessibilityNodeInfo?): Boolean {
        if (node == null) return false
        if (node.isClickable) {
            return node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
        }
        return isClickableRecursive(node.parent)
    }

    override fun onInterrupt() {
        // サービス中断時の処理
    }
}