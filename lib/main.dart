import 'package:flutter/material.dart';
import 'package:flutter/services.dart'; // 【公式】MethodChannel用
import 'package:permission_handler/permission_handler.dart'; // 【外部】権限管理用

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'SoundTap Auto-Clicker',
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(seedColor: Colors.deepPurple),
        useMaterial3: true,
      ),
      home: const MyHomePage(title: 'おばあちゃんの自動タップ'),
      debugShowCheckedModeBanner: false,
    );
  }
}

class MyHomePage extends StatefulWidget {
  const MyHomePage({super.key, required this.title});
  final String title;

  @override
  State<MyHomePage> createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {
  // ---------------------------------------------------------
  // 【独自変数】platform
  // ---------------------------------------------------------
  static const platform = MethodChannel('com.example.auto_tap_screen/tap');

  // --- 1. 予約系の処理（既存のもの） ---

  Future<void> _checkPermissionAndStart() async {
    var status = await Permission.systemAlertWindow.status;
    if (status.isGranted) {
      _startOverlayService();
    } else {
      await Permission.systemAlertWindow.request();
    }
  }

  Future<void> _startOverlayService() async {
    try {
      // MainActivity.kt の when文 の中の "startOverlay" に届く
      await platform.invokeMethod('startOverlay');
    } on PlatformException catch (e) {
      debugPrint("Nativeエラー: '${e.message}'.");
    }
  }

  // --- 2. 監視系の処理（今回追加！） ---

  // 【独自関数】マイクと通知の権限を確認してから監視をスタートする
  // _handleStartMonitoring をシンプルに！
  Future<void> _handleStartMonitoring() async {
    // センサーには特別な実行時権限（ポップアップ）は不要！
    // 通知権限だけ確認すればOK（Android 13+）
    
    if (await Permission.notification.request().isGranted) {
        _invokeNativeMethod('startMonitoring');
    } else {
        // 通知許可がないと、バックグラウンドで死ぬ可能性が高い
        if (mounted) {
            ScaffoldMessenger.of(context).showSnackBar(
              const SnackBar(content: Text('通知を許可しないと、裏で監視できません！')),
            );
        }
    }
  }

  // 【独自関数】Native側へ命令を送る共通処理（コードをスッキリさせるため）
  Future<void> _invokeNativeMethod(String methodName) async {
    try {
      await platform.invokeMethod(methodName);
    } on PlatformException catch (e) {
      debugPrint("Nativeエラー ($methodName): '${e.message}'.");
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        backgroundColor: Theme.of(context).colorScheme.inversePrimary,
        title: Text(widget.title),
      ),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: <Widget>[
            const Icon(Icons.spatial_audio_off, size: 80, color: Colors.deepPurple),
            const SizedBox(height: 30),
            
            // 予約ボタン
            ElevatedButton.icon(
              onPressed: _checkPermissionAndStart,
              icon: const Icon(Icons.add_location_alt),
              label: const Text('1. タップ位置を予約'),
              style: ElevatedButton.styleFrom(
                padding: const EdgeInsets.symmetric(horizontal: 30, vertical: 15),
              ),
            ),
            
            const SizedBox(height: 20),

            // 【追加】監視ボタン
            ElevatedButton.icon(
              onPressed: _handleStartMonitoring,
              icon: const Icon(Icons.play_arrow),
              label: const Text('2. 監視スタート！'),
              style: ElevatedButton.styleFrom(
                backgroundColor: Colors.orange,
                foregroundColor: Colors.white,
                padding: const EdgeInsets.symmetric(horizontal: 30, vertical: 15),
              ),
            ),

            const SizedBox(height: 40),
            const Text(
              '使い方：\n①位置を予約する\n②監視をスタートする\n③スマホの近くで手を叩く！',
              textAlign: TextAlign.center,
              style: TextStyle(color: Colors.grey),
            ),
          ],
        ),
      ),
    );
  }
}