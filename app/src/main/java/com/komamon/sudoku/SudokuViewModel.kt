/**
 * SudokuViewModel.kt
 *
 * 役割：
 *   ゲームの状態永続化ロジックを担うファイル。
 *   Android の SharedPreferences を使ってクリア済み問題IDと盤面の入力状態を
 *   アプリ終了後も保持する。Composable に依存しない純粋なデータ操作関数群で構成される。
 *   ※ Android Architecture Components の ViewModel クラスは使用していないが、
 *     役割として「ビジネスロジック・永続化層」に相当するため、この命名を採用している。
 *
 * 含まれるコンポーネント：
 *   定数
 *   - PREFS_NAME           : SharedPreferences のファイル名（"sudoku_prefs"）
 *   - KEY_CLEARED          : クリア済みIDセットのキー
 *   - KEY_BOARD_PREFIX     : 盤面状態キーのプレフィックス（"board_" + 問題ID）
 *
 *   関数
 *   - getPrefs()           : SharedPreferences インスタンスを取得するヘルパー
 *   - getClearedIds()      : クリア済み問題IDのセットを読み込む
 *   - markCleared()        : 指定問題IDをクリア済みとして保存する
 *   - resetClearedForDifficulty() : 指定難易度のクリア状態を全リセットする
 *   - saveBoardState()     : 現在の盤面を81文字の文字列にシリアライズして保存する
 *   - loadBoardState()     : 保存済み盤面を9×9リストにデシリアライズして返す
 *
 * 依存関係：
 *   → SudokuProblems.kt    : SudokuProblem 型を resetClearedForDifficulty() の引数で使用。
 *   ← SudokuScreen.kt      : SudokuApp・GameScreen から呼び出される。
 *
 * C#開発者向けメモ：
 *   - SharedPreferences は C# の Properties.Settings（WPF/WinForms）や
 *     IsolatedStorageSettings（Silverlight）に相当するキーバリューストア。
 *     アプリをアンインストールするまでデータが保持される。
 *   - edit().putXxx().apply() のパターンは C# の using (var writer = ...) { writer.Write(); }
 *     に相当するトランザクション的な書き込みパターン。
 *     apply() は非同期保存（fire-and-forget）、commit() は同期保存。
 *   - Kotlin のトップレベル関数はクラスに属さず、ファイルスコープで直接定義できる。
 *     C# では static class の static メソッドとして定義する相当の機能。
 *   - also { } はスコープ関数で「オブジェクトを受け取って操作し、そのオブジェクト自身を返す」。
 *     C# には直接対応する構文はないが、流れるようなメソッドチェーンに近い。
 *   - ?: はエルビス演算子（C# の ?? と同じ）。左辺が null なら右辺を返す。
 *
 * 更新履歴：
 *   2026/03/23 MainActivity.kt から SharedPreferences 永続化ロジックを分離して新規作成。
 */
package com.komamon.sudoku

import android.content.Context

// ---- SharedPreferences ヘルパー ----
// SharedPreferences = Androidのキーバリューストア。
// C#の Properties.Settings や IsolatedStorage、AppSettings に相当する。
// アプリを閉じても値が保持される永続化ストレージ。

private const val PREFS_NAME = "sudoku_prefs"         // SharedPreferencesのファイル名
private const val KEY_CLEARED = "cleared_ids"          // クリア済み問題IDのセットを保存するキー
private const val KEY_BOARD_PREFIX = "board_"          // 問題ごとの盤面状態を保存するキーのプレフィックス

/**
 * SharedPreferencesのインスタンスを取得するヘルパー関数。
 */
fun getPrefs(context: Context) =
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

/**
 * クリア済み問題IDのセットを取得する。
 */
fun getClearedIds(context: Context): Set<String> =
    getPrefs(context).getStringSet(KEY_CLEARED, emptySet()) ?: emptySet()

/**
 * 指定した問題IDをクリア済みとして保存する。
 */
fun markCleared(context: Context, id: String) {
    val updated = getClearedIds(context).toMutableSet().also { it.add(id) }
    getPrefs(context).edit().putStringSet(KEY_CLEARED, updated).apply()
}

/**
 * 指定した難易度の問題のクリア状態をリセットする。
 */
fun resetClearedForDifficulty(context: Context, problems: List<SudokuProblem>) {
    val updated = getClearedIds(context).toMutableSet().also { set ->
        problems.forEach { set.remove(it.id) }
    }
    getPrefs(context).edit().putStringSet(KEY_CLEARED, updated).apply()
}

/**
 * 盤面の状態を SharedPreferences に保存する。
 */
fun saveBoardState(context: Context, id: String, board: List<List<Int?>>) {
    val str = board.flatten().joinToString("") { (it ?: 0).toString() }
    getPrefs(context).edit().putString(KEY_BOARD_PREFIX + id, str).apply()
}

/**
 * SharedPreferences から盤面の状態を読み込む。
 * 保存データがない場合は元のパズル（出題盤面）を返す。
 */
fun loadBoardState(context: Context, id: String, puzzle: List<List<Int?>>): List<List<Int?>> {
    val str = getPrefs(context).getString(KEY_BOARD_PREFIX + id, null)
        ?: return puzzle
    if (str.length != 81) return puzzle
    return List(9) { r ->
        List(9) { c ->
            val digit = str[r * 9 + c].digitToIntOrNull() ?: 0
            if (digit == 0) null else digit
        }
    }
}
