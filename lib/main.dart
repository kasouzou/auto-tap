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
        // 横画面時にAppBarがデカすぎると邪魔なので少しスリムに
        toolbarHeight: MediaQuery.of(context).orientation == Orientation.landscape ? 40 : null,
      ),
      // 【マクロな視点】横画面でのオーバーフローを防ぐための必須スクロール設定
      body: SingleChildScrollView(
        child: Padding(
          padding: const EdgeInsets.symmetric(horizontal: 30, vertical: 20),
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: <Widget>[
              // 状態アイコンとテキスト
              Icon(
                _isMonitoring ? Icons.check_circle : Icons.pause_circle_filled,
                size: 80,
                color: _isMonitoring ? Colors.green : Colors.grey,
              ),
              const SizedBox(height: 10),
              Text(
                _isMonitoring ? "稼働中" : "停止中",
                style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 16),
              ),
              const SizedBox(height: 30),
              
              // 横画面時にボタンが横に並ぶと使いやすいが、今回はシンプルに縦並びを維持しつつ間隔を調整
              ElevatedButton.icon(
                onPressed: _openAccessibilitySettings,
                icon: const Icon(Icons.settings_accessibility),
                label: const Text('設定でユーザー補助を許可'),
                style: ElevatedButton.styleFrom(
                  minimumSize: const Size(double.infinity, 50),
                ),
              ),
              
              const SizedBox(height: 15),

              ElevatedButton.icon(
                onPressed: _toggleMonitoring,
                icon: Icon(_isMonitoring ? Icons.stop : Icons.play_arrow),
                label: Text(_isMonitoring ? '自動スキップを停止' : '自動スキップを開始'),
                style: ElevatedButton.styleFrom(
                  backgroundColor: _isMonitoring ? Colors.red : Colors.green,
                  foregroundColor: Colors.white,
                  minimumSize: const Size(double.infinity, 50),
                  textStyle: const TextStyle(fontSize: 16, fontWeight: FontWeight.bold),
                ),
              ),

              const SizedBox(height: 30),
              // 動的に中身が変わるガイドセクション
              _buildGuideSection(),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildGuideSection() {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 20),
      decoration: BoxDecoration(
        // 稼働中は緑っぽく、停止中はグレーにする
        color: _isMonitoring ? Colors.green.shade50 : Colors.grey.shade50,
        borderRadius: BorderRadius.circular(12),
        border: Border.all(
          color: _isMonitoring ? Colors.green.shade200 : Colors.grey.shade300,
          width: 1.5,
        ),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Icon(
                _isMonitoring ? Icons.verified_user : Icons.list_alt,
                size: 20,
                color: _isMonitoring ? Colors.green : Colors.black87,
              ),
              const SizedBox(width: 8),
              Text(
                _isMonitoring ? 'システム稼働状況' : 'セットアップ手順',
                style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 15, letterSpacing: 1.2),
              ),
            ],
          ),
          const SizedBox(height: 16),
          
          // 監視中の場合はステータス表示、停止中は手順を表示
          if (_isMonitoring) ...[
            _buildStatusRow(Icons.check, "バックグラウンド監視：アクティブ"),
            _buildStatusRow(Icons.check, "ユーザー補助権限：有効"),
            _buildStatusRow(Icons.sync, "ターゲット：YouTube広告を待機中..."),
          ] else ...[
            _buildStepRow('01', '権限の有効化', '設定から本アプリのユーザー補助をONにしてください。'),
            _buildDivider(),
            _buildStepRow('02', 'サービスの開始', '開始ボタンを押し、通知欄にアイコンが出れば有効です。'),
            _buildDivider(),
            _buildStepRow('03', '自動実行', 'YouTubeを開くと広告スキップが自動実行されます。'),
          ],
        ],
      ),
    );
  }

  // 稼働中用の無機質なステータス行
  Widget _buildStatusRow(IconData icon, String label) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 4),
      child: Row(
        children: [
          Icon(icon, size: 16, color: Colors.green),
          const SizedBox(width: 10),
          Text(label, style: const TextStyle(fontSize: 13, color: Colors.black87)),
        ],
      ),
    );
  }

  // 手順行（高さを抑えるために微調整）
  Widget _buildStepRow(String stepNumber, String title, String description) {
    return Row(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(stepNumber, style: TextStyle(fontSize: 16, fontWeight: FontWeight.w900, color: Colors.orange.withOpacity(0.5))),
        const SizedBox(width: 12),
        Expanded(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(title, style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 13)),
              Text(description, style: const TextStyle(fontSize: 12, color: Colors.black54)),
            ],
          ),
        ),
      ],
    );
  }

  Widget _buildDivider() {
    return Padding(padding: const EdgeInsets.symmetric(vertical: 10), child: Divider(height: 1, color: Colors.grey.shade300));
  }
}