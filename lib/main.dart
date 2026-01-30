import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:permission_handler/permission_handler.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'YouTube Ad Sniper',
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(seedColor: Colors.orange),
        useMaterial3: true,
      ),
      home: const MyHomePage(title: '自動スキップアプリ'),
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
  static const platform = MethodChannel('com.example.auto_tap_screen/tap');
  
  // 【独自変数】今、監視エンジンが動いているかどうかを管理
  bool _isMonitoring = false;

  Future<void> _openAccessibilitySettings() async {
    try {
      await platform.invokeMethod('openAccessibilitySettings');
    } on PlatformException catch (e) {
      debugPrint("設定画面が開けません: '${e.message}'.");
    }
  }

  // 監視の「開始」と「停止」を切り替えるメインロジック
  Future<void> _toggleMonitoring() async {
    if (_isMonitoring) {
      // 停止処理
      await _invokeNativeMethod('stopMonitoring');
      setState(() {
        _isMonitoring = false;
      });
      _showSnackBar('自動スキップを停止しました。');
    } else {
      // 開始処理
      if (await Permission.notification.request().isGranted) {
        if (await Permission.systemAlertWindow.request().isGranted) {
          await _invokeNativeMethod('startMonitoring');
          setState(() {
            _isMonitoring = true;
          });
          _showSnackBar('自動スキップを開始しました！YouTubeを開いてください。');
        }
      }
    }
  }

  Future<void> _invokeNativeMethod(String methodName) async {
    try {
      await platform.invokeMethod(methodName);
    } on PlatformException catch (e) {
      debugPrint("Nativeエラー ($methodName): '${e.message}'.");
    }
  }

  void _showSnackBar(String message) {
    if (!mounted) return;
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(content: Text(message), duration: const Duration(seconds: 2)),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        backgroundColor: Theme.of(context).colorScheme.inversePrimary,
        title: Text(widget.title),
      ),
      body: Center(
        child: Padding(
          padding: const EdgeInsets.symmetric(horizontal: 30),
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: <Widget>[
              // 状態に合わせてアイコンの色や形を変える（鋭い観察眼！）
              Icon(
                _isMonitoring ? Icons.visibility : Icons.visibility_off,
                size: 100,
                color: _isMonitoring ? Colors.green : Colors.grey,
              ),
              const SizedBox(height: 10),
              Text(
                _isMonitoring ? "現在：自動スキップ稼働中" : "現在：自動スキップ停止中",
                style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 18),
              ),
              const SizedBox(height: 40),
              
              // 1. ユーザー補助設定（これは常に必要）
              ElevatedButton.icon(
                onPressed: _openAccessibilitySettings,
                icon: const Icon(Icons.settings_accessibility),
                label: const Text('設定でユーザー補助を許可'),
                style: ElevatedButton.styleFrom(
                  minimumSize: const Size(double.infinity, 60),
                ),
              ),
              
              const SizedBox(height: 20),

              // 2. 開始 / 停止ボタン（動的に変化！）
              ElevatedButton.icon(
                onPressed: _toggleMonitoring,
                icon: Icon(_isMonitoring ? Icons.stop : Icons.play_arrow),
                label: Text(_isMonitoring ? '自動スキップを停止' : '自動スキップを開始'),
                style: ElevatedButton.styleFrom(
                  backgroundColor: _isMonitoring ? Colors.red : Colors.green,
                  foregroundColor: Colors.white,
                  minimumSize: const Size(double.infinity, 60),
                  textStyle: const TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
                ),
              ),

              const SizedBox(height: 40),
              // 説明書
              _buildGuideSection(),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildGuideSection() {
    return Container(
      padding: const EdgeInsets.all(20),
      decoration: BoxDecoration(
        color: Colors.orange.withOpacity(0.1),
        borderRadius: BorderRadius.circular(15),
        border: Border.all(color: Colors.orange.withOpacity(0.3)),
      ),
      child: const Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text('【使い方】', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 16)),
          SizedBox(height: 10),
          Text('・まず、上のボタンからユーザー補助設定を開きこのアプリのユーザー補助をONにしてください。'),
          Text('・開始すると通知欄にアイコンが出ます。'),
          Text('・止めたい時は赤いボタンを押してください。'),
          Text('・もう一度押せば、いつでも再開できます。'),
        ],
      ),
    );
  }
}