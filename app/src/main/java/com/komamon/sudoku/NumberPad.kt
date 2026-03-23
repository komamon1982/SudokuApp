/**
 * NumberPad.kt
 *
 * 役割：
 *   数独の数字入力パッドUIを提供するファイル。
 *   確定入力（青ボタン）とメモ入力（オレンジボタン）の2段構成で、
 *   それぞれ1〜9の数字ボタンと消去ボタンを配置する。
 *   入力処理の実態は持たず、タップイベントをコールバックで上位に委譲する
 *   純粋な「入力UIコンポーネント層」として設計されている。
 *
 * 含まれるコンポーネント：
 *   Composable 関数
 *   - NumberPad()              : 数字入力パッド全体を描画するComposable。
 *       上段（青 #1976D2）      : 確定入力ボタン 1〜9 + 消去（「消」）
 *       下段（橙 #E65100）      : メモ入力ボタン 1〜9 + メモ消去（「消」）
 *       各ボタンは weight(1f) で均等幅に分配される。
 *
 * 依存関係：
 *   → なし（Android/Compose標準ライブラリのみ使用）
 *   ← SudokuScreen.kt          : GameScreen() 内で NumberPad() を呼び出す。
 *
 * C#開発者向けメモ：
 *   - コールバック引数 onNumberClick: (Int) -> Unit は C# の Action<int> デリゲートに相当。
 *     onEraseClick: () -> Unit は C# の Action（引数なし・戻り値なし）に相当。
 *     呼び出し側でラムダ式 { number -> ... } として実装を渡す（C# の () => ... と同じ構文）。
 *   - for (n in 1..9) は C# の for (int n = 1; n <= 9; n++) に相当する範囲ループ。
 *     1..9 は IntRange オブジェクトで、1 から 9 の閉区間を表す。
 *   - Modifier.weight(1f) は C# の WPF Grid の「*（スター）サイズ」と同じ概念。
 *     Row 内の全ボタンに weight(1f) を付けることで均等幅になる。
 *   - contentPadding = PaddingValues(0.dp) はボタン内のデフォルトパディングを0にする設定。
 *     指定しないと Material3 のデフォルト余白でボタン内テキストが窮屈になる。
 *   - ButtonDefaults.buttonColors() はボタンの色をカスタマイズするファクトリ関数。
 *     containerColor が背景色（C# の Background）、contentColor が前景色（C# の Foreground）。
 *   - RoundedCornerShape(8.dp) は角丸の形状定義。C# の CornerRadius="8" に相当する。
 *
 * 更新履歴：
 *   2026/03/23 MainActivity.kt から数字入力パッドComposableを分離して新規作成。
 */
package com.komamon.sudoku

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ---- 数字入力パッド ----

/**
 * 数字入力パッドのComposable関数。
 * 上段（青）：確定入力ボタン 1〜9 + 消去
 * 下段（橙）：メモ入力ボタン 1〜9 + メモ消去
 *
 * @param onNumberClick   数字ボタンタップ時（確定入力）のコールバック
 * @param onMemoClick     メモボタンタップ時のコールバック
 * @param onEraseClick    消去ボタンタップ時のコールバック（確定数字を削除）
 * @param onMemoEraseClick メモ消去ボタンタップ時のコールバック
 */
@Composable
fun NumberPad(
    onNumberClick: (Int) -> Unit,
    onMemoClick: (Int) -> Unit,
    onEraseClick: () -> Unit,
    onMemoEraseClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        // ---- 上段：確定入力ボタン（青） ----
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            for (n in 1..9) {
                Button(
                    onClick = { onNumberClick(n) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(0.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1976D2),
                        contentColor = Color.White
                    )
                ) { Text(n.toString(), fontSize = 16.sp) }
            }
            Button(
                onClick = onEraseClick,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(0.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1976D2),
                    contentColor = Color.White
                )
            ) { Text("消", fontSize = 16.sp) }
        }

        // ---- 下段：メモ入力ボタン（オレンジ） ----
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            for (n in 1..9) {
                Button(
                    onClick = { onMemoClick(n) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(0.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE65100),
                        contentColor = Color.White
                    )
                ) { Text(n.toString(), fontSize = 16.sp) }
            }
            Button(
                onClick = onMemoEraseClick,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(0.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE65100),
                    contentColor = Color.White
                )
            ) { Text("消", fontSize = 16.sp) }
        }
    }
}
