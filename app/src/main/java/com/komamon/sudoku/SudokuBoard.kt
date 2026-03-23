/**
 * SudokuBoard.kt
 *
 * 役割：
 *   数独盤面の描画に特化したComposableを提供するファイル。
 *   9×9グリッドの全体レイアウトと1マスごとの描画を担い、
 *   選択状態・エラー・ハイライト・メモ表示といったセルの視覚表現を管理する。
 *   入力処理は持たず、タップイベントをコールバックで上位に委譲する
 *   純粋な「表示コンポーネント層」として設計されている。
 *
 * 含まれるコンポーネント：
 *   Composable 関数
 *   - SudokuBoard()            : 9×9盤面全体を描画するComposable。
 *                                3×3ブロックの区切り線（太線）と各セルを配置する。
 *                                選択セルを基準に「同グループ」「同数字」のハイライトを計算する。
 *   - SudokuCell()             : 盤面の1マスを描画するComposable。
 *                                背景色・数字の色・フォント・メモ（2×2レイアウト）を
 *                                isSelected / isHint / isError / isSameGroup / isSameNumber
 *                                の各フラグに応じて切り替える。
 *
 * 依存関係：
 *   → なし（Android/Compose標準ライブラリのみ使用）
 *   ← SudokuScreen.kt          : GameScreen() 内で SudokuBoard() を呼び出す。
 *
 * C#開発者向けメモ：
 *   - Composable 関数は C# の UserControl（WPF/WinForms）に相当する再利用可能なUIパーツ。
 *     ただし XAML ではなくコード（Kotlin）だけで UI を定義する。
 *   - Modifier はUIのスタイルや動作をメソッドチェーンで定義するBuilderパターン。
 *     C# の Style + Behavior を流れるように書けるイメージ。
 *   - Box は重ね合わせレイアウト。C# の Grid（ZIndex管理）や Canvas に相当する。
 *     contentAlignment で子要素のデフォルト配置を指定できる。
 *   - modifier = Modifier（デフォルト引数）は C# のオプション引数と同じ概念。
 *     親から weight など追加 Modifier を受け取るための拡張ポイントになっている。
 *   - when { } は C# の switch expression に相当するが、型チェック・範囲・条件など
 *     あらゆる条件に対応でき、式として値を返せる（ここでは背景色の決定に使用）。
 *   - fillMaxSize() / weight(1f) の組み合わせは C# の
 *     HorizontalAlignment=Stretch + proportional sizing（Grid の * サイズ）に相当する。
 *   - aspectRatio(1f) は幅と高さを 1:1 に保つ Modifier。
 *     C# の Viewbox や UniformGrid の正方形維持と同じ効果。
 *
 * 更新履歴：
 *   2026/03/23 MainActivity.kt から盤面・セルComposableを分離して新規作成。
 */
package com.komamon.sudoku

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ---- 盤面コンポーネント ----

/**
 * 数独盤面全体を描画するComposable関数。
 * 9×9のセルグリッドと3×3ブロックの区切り線を描画する。
 *
 * @param board        現在の盤面データ（null = 空マス）
 * @param hintCells    ヒントマス（固定値）のフラグ配列
 * @param selectedCell 現在選択中のセル座標（null = 未選択）
 * @param errorCells   チェックでエラーとなったセルの座標セット
 * @param memos        各セルのメモ数字セット
 * @param onCellClick  セルタップ時のコールバック（行・列を引数に取る）
 */
@Composable
fun SudokuBoard(
    board: List<List<Int?>>,
    hintCells: Array<BooleanArray>,
    selectedCell: Pair<Int, Int>?,
    errorCells: Set<Pair<Int, Int>>,
    memos: Map<Pair<Int, Int>, Set<Int>>,
    onCellClick: (Int, Int) -> Unit
) {
    val selectedValue = selectedCell?.let { (r, c) -> board[r][c] }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .border(3.dp, Color.Black, RectangleShape)
    ) {
        for (row in 0..8) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                for (col in 0..8) {
                    val isSameGroup = selectedCell != null && selectedCell != Pair(row, col) &&
                        (selectedCell.first == row ||
                         selectedCell.second == col ||
                         (row / 3 == selectedCell.first / 3 && col / 3 == selectedCell.second / 3))

                    val isSameNumber = selectedCell != null && selectedCell != Pair(row, col) &&
                        selectedValue != null && board[row][col] == selectedValue

                    SudokuCell(
                        value = board[row][col],
                        isHint = hintCells[row][col],
                        isSelected = selectedCell == Pair(row, col),
                        isError = Pair(row, col) in errorCells,
                        isSameGroup = isSameGroup,
                        isSameNumber = isSameNumber,
                        memos = memos[Pair(row, col)] ?: emptySet(),
                        onClick = { onCellClick(row, col) },
                        modifier = Modifier.weight(1f)
                    )
                    if (col == 2 || col == 5) {
                        Spacer(
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(2.dp)
                                .background(Color.Black)
                        )
                    }
                }
            }
            if (row == 2 || row == 5) {
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .background(Color.Black)
                )
            }
        }
    }
}

/**
 * 数独の1マスを描画するComposable関数。
 * 背景色・テキスト色・メモ表示を状態に応じて切り替える。
 *
 * @param value        表示する数字（null = 空マス）
 * @param isHint       ヒントマス（変更不可の固定値）かどうか
 * @param isSelected   現在選択中かどうか
 * @param isError      エラーマスかどうか（チェック後に設定される）
 * @param isSameGroup  選択セルと同じ行・列・ブロックかどうか（薄いハイライト）
 * @param isSameNumber 選択セルと同じ数字かどうか（中くらいのハイライト）
 * @param memos        仮置きした数字のセット（最大4個）
 * @param onClick      タップ時のコールバック
 * @param modifier     外部から追加のModifierを受け取る（weight等を親から指定するため）
 */
@Composable
fun SudokuCell(
    value: Int?,
    isHint: Boolean,
    isSelected: Boolean,
    isError: Boolean,
    isSameGroup: Boolean,
    isSameNumber: Boolean,
    memos: Set<Int>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor = when {
        isSelected   -> Color(0xFF90CAF9)
        isSameNumber -> Color(0xFFBBDEFB)
        isSameGroup  -> Color(0xFFF5F5F5)
        else         -> Color.White
    }
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(bgColor)
            .border(0.5.dp, Color.Gray)
            .clickable { onClick() }
    ) {
        if (value != null) {
            Text(
                text = value.toString(),
                fontSize = 18.sp,
                fontWeight = if (isHint || isSelected) FontWeight.Bold else FontWeight.Normal,
                color = when {
                    isError                -> Color(0xFFD32F2F)
                    isSelected && !isHint  -> Color.White
                    isHint                 -> Color.Black
                    else                   -> Color(0xFF1565C0)
                },
                modifier = Modifier.align(Alignment.Center)
            )
        } else if (memos.isNotEmpty()) {
            val sorted = memos.sorted().toList()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(2.dp),
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                for (memoRow in 0..1) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        for (memoCol in 0..1) {
                            val idx = memoRow * 2 + memoCol
                            Text(
                                text = if (idx < sorted.size) sorted[idx].toString() else "",
                                fontSize = 12.sp,
                                color = Color(0xFFE65100),
                                lineHeight = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
