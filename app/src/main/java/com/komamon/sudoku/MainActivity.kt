/**
 * MainActivity.kt
 *
 * 役割：
 *   アプリのエントリーポイント。Android OSからアプリが起動される際に最初に呼ばれる
 *   Activity クラスのみを定義する。UIの構築は SudokuApp()（SudokuScreen.kt）に委譲し、
 *   このファイル自体はライフサイクル管理とComposeの起動のみを担う。
 *
 * 含まれるコンポーネント：
 *   - MainActivity         : アプリ唯一の Activity クラス。
 *                            onCreate() でエッジツーエッジ表示の有効化と
 *                            Compose UIのセットアップを行う。
 *
 * 依存関係：
 *   → SudokuScreen.kt      : ルートComposable「SudokuApp()」を呼び出す。
 *   ← なし（他のファイルからこのファイルへの依存はない）
 *
 * C#開発者向けメモ：
 *   - Activity は C# の Windows Forms における Form、WPF における Window に相当する。
 *     ただし Android では画面の生成・破棄がOSによって管理されるため、
 *     コンストラクタではなく onCreate() でUIを初期化するのが基本パターン。
 *   - ComponentActivity は Jetpack Compose を使うための基底クラス。
 *     C# では継承元クラスのコンストラクタを : base() で呼ぶが、
 *     Kotlin では : ComponentActivity() のように継承宣言の時点でコンストラクタを呼ぶ。
 *   - setContent { } は C# の InitializeComponent() / Content = new View() に相当し、
 *     Compose の UI ツリーをこの Activity にアタッチする。
 *   - enableEdgeToEdge() はステータスバー・ナビゲーションバーの下までUIを描画する設定。
 *     C# にはない Android 固有の概念（WindowInsets による余白制御とセットで使う）。
 *
 * 更新履歴：
 *   2026/03/23 MainActivity.kt から Activity クラスのみ抽出して新規作成。
 *              UI・ナビゲーション・状態管理ロジックは各専用ファイルへ分離。
 */
package com.komamon.sudoku

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier

/**
 * アプリのエントリーポイントとなる Activity クラス。
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    SudokuApp()
                }
            }
        }
    }
}
