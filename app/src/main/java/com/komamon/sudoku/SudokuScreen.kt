/**
 * SudokuScreen.kt
 *
 * 役割：
 *   アプリの全画面Composableと画面遷移ロジックを担うファイル。
 *   難易度選択・問題一覧・ゲームの3画面を定義し、ナビゲーション状態（Screen sealed class）
 *   によって表示する画面を切り替えるルートComposable（SudokuApp）を提供する。
 *   ゲーム画面では盤面の入力状態・メモ・エラー表示・クリア判定など
 *   ゲームプレイに関わるUIロジックも含む。
 *
 * 含まれるコンポーネント：
 *   sealed class
 *   - Screen                   : 画面状態を表す sealed class。
 *       Screen.DifficultySelect : 難易度選択画面（データなし）
 *       Screen.ProblemList      : 問題一覧画面（difficulty: String を保持）
 *       Screen.Game             : ゲーム画面（problem / index / total を保持）
 *
 *   enum class
 *   - CheckResult（private）    : チェック結果を表す列挙型。
 *                                 HAS_EMPTY / HAS_ERROR / CLEAR の3値。
 *
 *   private 関数
 *   - problemsFor()             : 難易度文字列 → SudokuProblem リストのマッピング関数。
 *
 *   Composable 関数
 *   - SudokuApp()               : ナビゲーション状態を管理するルートComposable。
 *   - DifficultySelectScreen()  : 難易度選択画面。
 *   - ProblemListScreen()       : 問題一覧画面（グリッド表示・クリア状態表示）。
 *   - GameScreen()              : ゲーム画面（盤面・入力パッド・チェック機能）。
 *
 * 依存関係：
 *   → SudokuViewModel.kt       : getClearedIds / markCleared / resetClearedForDifficulty /
 *                                 saveBoardState / loadBoardState を使用。
 *   → SudokuBoard.kt           : GameScreen 内で SudokuBoard() を呼び出す。
 *   → NumberPad.kt             : GameScreen 内で NumberPad() を呼び出す。
 *   → SudokuProblems.kt        : SudokuProblem 型・SudokuProblems オブジェクトを使用。
 *   ← MainActivity.kt          : SudokuApp() を呼び出す。
 *
 * C#開発者向けメモ：
 *   - sealed class は C# の abstract class に似るが、サブクラスを同一パッケージ内に限定でき、
 *     when 式（switch相当）で全ケース網羅をコンパイラが保証する。
 *     C# 9以降の switch expression + discriminated union に近い概念。
 *   - このアプリは Navigation Component を使わず、sealed class の currentScreen 変数を
 *     mutableStateOf で管理することで画面遷移を実現している（シンプルな状態ベースナビゲーション）。
 *     C# では ContentControl.Content の切り替えや Frame.Navigate() に相当する。
 *   - @Composable 関数は宣言的UIの関数で、状態が変わると自動的に再描画（リコンポーズ）される。
 *     C# の XAML + INotifyPropertyChanged の仕組みをコードで統合したものに近い。
 *   - remember { } はリコンポーズをまたいで値を保持するキャッシュ。
 *     C# の backing field や lazy 初期化に近い概念。
 *   - mutableStateListOf は Compose が要素の追加・変更・削除を検知できる MutableList。
 *     通常の MutableList では変更を Compose が検知できないため注意が必要。
 *   - Scaffold は Material Design の画面骨格コンポーネント。
 *     C# の MasterDetailPage や Shell（MAUI）に相当する。
 *   - SnackbarHostState + scope.launch は非同期でスナックバーを表示するパターン。
 *     C# の await Dispatcher.InvokeAsync() で Toast 表示するイメージに近い。
 *
 * 更新履歴：
 *   2026/03/23 MainActivity.kt から全画面Composableと画面遷移ロジックを分離して新規作成。
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.zIndex
import kotlin.random.Random
import kotlinx.coroutines.delay

// ---- ナビゲーション定義 ----

/**
 * アプリの画面を表す sealed クラス。
 *
 * このアプリはナビゲーションライブラリを使わず、sealed class で状態管理している。
 * currentScreen の値を変えるだけで画面遷移できるシンプルな設計。
 */
sealed class Screen {
    object DifficultySelect : Screen()
    data class ProblemList(val difficulty: String) : Screen()
    data class Game(val problem: SudokuProblem, val index: Int, val total: Int) : Screen()
}

/**
 * チェック結果を表す列挙型。
 */
private enum class CheckResult {
    HAS_EMPTY,  // 未入力のマスがある
    HAS_ERROR,  // 間違いがある
    CLEAR       // 全マス正解
}

/**
 * 難易度文字列に対応する問題リストを返すマッピング関数。
 */
private fun problemsFor(difficulty: String): List<SudokuProblem> = when (difficulty) {
    "入門" -> SudokuProblems.introProblems
    "初級" -> SudokuProblems.beginnerProblems
    "中級" -> SudokuProblems.intermediateProblems
    else  -> SudokuProblems.advancedProblems
}

/**
 * アプリ全体のルートComposable関数。
 * 現在の画面状態（screen）を管理し、適切な画面を表示する。
 */
@Composable
fun SudokuApp() {
    val context = LocalContext.current
    var screen by remember { mutableStateOf<Screen>(Screen.DifficultySelect) }
    var clearedIds by remember { mutableStateOf(getClearedIds(context)) }

    fun refreshCleared() { clearedIds = getClearedIds(context) }

    when (val s = screen) {
        is Screen.DifficultySelect -> DifficultySelectScreen(
            onDifficultySelected = { difficulty -> screen = Screen.ProblemList(difficulty) }
        )
        is Screen.ProblemList -> {
            val problems = problemsFor(s.difficulty)
            ProblemListScreen(
                difficulty = s.difficulty,
                problems = problems,
                clearedIds = clearedIds,
                onProblemSelected = { problem, index ->
                    screen = Screen.Game(problem, index, problems.size)
                },
                onResetCleared = {
                    resetClearedForDifficulty(context, problems)
                    refreshCleared()
                },
                onBack = { screen = Screen.DifficultySelect }
            )
        }
        is Screen.Game -> GameScreen(
            problem = s.problem,
            index = s.index,
            total = s.total,
            onBack = {
                refreshCleared()
                screen = Screen.ProblemList(s.problem.difficulty)
            },
            onCleared = { id ->
                markCleared(context, id)
                refreshCleared()
                screen = Screen.ProblemList(s.problem.difficulty)
            }
        )
    }
}

// ---- 難易度選択画面 ----

private data class NumberParticle(
    val x: Float,
    val phase: Float,
    val speed: Float,
    val sizeSp: Float,
    val digit: String,
    val alpha: Float
)

/**
 * 難易度選択画面のComposable関数。和風テイスト＋3種のアニメーション付き。
 *
 * @param onDifficultySelected 難易度が選ばれた時に呼ばれるコールバック。
 */
@Composable
fun DifficultySelectScreen(onDifficultySelected: (String) -> Unit) {
    // 1. タイトルのフェードイン＋スライドイン (800ms)
    val titleAlpha = remember { Animatable(0f) }
    val titleOffsetY = remember { Animatable(-60f) }
    LaunchedEffect(Unit) {
        launch { titleAlpha.animateTo(1f, animationSpec = tween(800)) }
        launch { titleOffsetY.animateTo(0f, animationSpec = tween(800)) }
    }

    // 2. ボタンの順番フェードイン (入門→初級→中級→上級、100msずつ遅延)
    val buttonAlphas = remember { List(4) { Animatable(0f) } }
    LaunchedEffect(Unit) {
        (0..3).forEach { i ->
            launch {
                delay(400L + i * 100L)
                buttonAlphas[i].animateTo(1f, animationSpec = tween(500))
            }
        }
    }

    // 3. 落下する数字の背景パーティクル
    val bgParticles = remember {
        List(30) {
            NumberParticle(
                x = Random.nextFloat(),
                phase = Random.nextFloat(),
                speed = 0.3f + Random.nextFloat() * 0.4f,
                sizeSp = 24f + Random.nextFloat() * 16f,
                digit = ('1'..'9').random().toString(),
                alpha = 0.08f + Random.nextFloat() * 0.04f
            )
        }
    }
    val bgTransition = rememberInfiniteTransition(label = "bg_fall")
    val fallTime by bgTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(12_000, easing = LinearEasing)),
        label = "fall_time"
    )

    val difficultyItems = listOf("入門" to "○", "初級" to "◎", "中級" to "◆", "上級" to "★")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFFF5F0E8), Color(0xFFE8DCC8))))
            .windowInsetsPadding(WindowInsets.systemBars)
    ) {
        // 落下する数字の背景
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawIntoCanvas { canvas ->
                bgParticles.forEach { p ->
                    val paint = android.graphics.Paint().apply {
                        color = android.graphics.Color.rgb(26, 26, 46)
                        alpha = (255 * p.alpha).toInt()
                        textSize = p.sizeSp.sp.toPx()
                        isAntiAlias = true
                    }
                    val yFraction = ((p.phase + fallTime * p.speed) % 1f)
                    canvas.nativeCanvas.drawText(
                        p.digit,
                        p.x * size.width,
                        yFraction * (size.height + 80f) - 40f,
                        paint
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // アニメーション付きタイトル
            Text(
                text = "数独",
                fontSize = 60.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A2E),
                modifier = Modifier
                    .alpha(titleAlpha.value)
                    .offset(y = titleOffsetY.value.dp)
                    .padding(bottom = 56.dp)
            )

            // 難易度ボタン（順番フェードイン）
            difficultyItems.forEachIndexed { index, (label, icon) ->
                Button(
                    onClick = { onDifficultySelected(label) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .height(56.dp)
                        .alpha(buttonAlphas[index].value),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2C2C3E),
                        contentColor = Color.White
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(icon, fontSize = 18.sp, modifier = Modifier.padding(end = 12.dp))
                        Text(label, fontSize = 20.sp)
                    }
                }
            }
        }
    }
}

// ---- 問題一覧画面 ----

/**
 * 問題一覧画面のComposable関数。
 * 難易度に対応する問題をグリッド表示し、クリア状態をビジュアルで示す。
 *
 * @param difficulty        現在の難易度文字列
 * @param problems          表示する問題のリスト
 * @param clearedIds        クリア済み問題IDのセット
 * @param onProblemSelected 問題が選ばれた時のコールバック（問題・インデックスを引数に取る）
 * @param onResetCleared    クリアリセット時のコールバック
 * @param onBack            戻るボタンのコールバック
 */
@Composable
fun ProblemListScreen(
    difficulty: String,
    problems: List<SudokuProblem>,
    clearedIds: Set<String>,
    onProblemSelected: (SudokuProblem, Int) -> Unit,
    onResetCleared: () -> Unit,
    onBack: () -> Unit
) {
    var showResetDialog by remember { mutableStateOf(false) }

    // ヘッダーのフェードイン＋スライドイン (600ms)
    val headerAlpha = remember { Animatable(0f) }
    val headerOffsetY = remember { Animatable(-40f) }
    LaunchedEffect(Unit) {
        launch { headerAlpha.animateTo(1f, animationSpec = tween(600)) }
        launch { headerOffsetY.animateTo(0f, animationSpec = tween(600)) }
    }

    // 落下する数字の背景パーティクル
    val bgParticles = remember {
        List(30) {
            NumberParticle(
                x = Random.nextFloat(),
                phase = Random.nextFloat(),
                speed = 0.3f + Random.nextFloat() * 0.4f,
                sizeSp = 24f + Random.nextFloat() * 16f,
                digit = ('1'..'9').random().toString(),
                alpha = 0.05f + Random.nextFloat() * 0.02f
            )
        }
    }
    val bgTransition = rememberInfiniteTransition(label = "bg_fall_list")
    val fallTime by bgTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(12_000, easing = LinearEasing)),
        label = "fall_time_list"
    )

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            text = { Text("この難易度のクリア状態をリセットしますか？") },
            confirmButton = {
                TextButton(onClick = {
                    onResetCleared()
                    showResetDialog = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) { Text("キャンセル") }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFFF5F0E8), Color(0xFFE8DCC8))))
            .windowInsetsPadding(WindowInsets.systemBars)
    ) {
        // 落下する数字の背景
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawIntoCanvas { canvas ->
                bgParticles.forEach { p ->
                    val paint = android.graphics.Paint().apply {
                        color = android.graphics.Color.rgb(26, 26, 46)
                        alpha = (255 * p.alpha).toInt()
                        textSize = p.sizeSp.sp.toPx()
                        isAntiAlias = true
                    }
                    val yFraction = ((p.phase + fallTime * p.speed) % 1f)
                    canvas.nativeCanvas.drawText(
                        p.digit,
                        p.x * size.width,
                        yFraction * (size.height + 80f) - 40f,
                        paint
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // アニメーション付きヘッダー
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(headerAlpha.value)
                    .offset(y = headerOffsetY.value.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onBack) {
                        Text("← 戻る", fontSize = 16.sp, color = Color(0xFF1A1A2E))
                    }
                    Text(
                        text = difficulty,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A2E),
                        modifier = Modifier.padding(start = 8.dp)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "${clearedIds.count { id -> problems.any { it.id == id } }}/${problems.size} クリア",
                        fontSize = 14.sp,
                        color = Color(0xFF388E3C)
                    )
                }
                TextButton(
                    onClick = { showResetDialog = true },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("クリア状態をリセット", fontSize = 13.sp, color = Color(0xFFD32F2F))
                }
                Spacer(modifier = Modifier.height(4.dp))
            }

            // グリッド（各マスが順番にフェードイン）
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(problems) { index, problem ->
                    val isCleared = problem.id in clearedIds
                    val cellAlpha = remember { Animatable(0f) }
                    val cellScale = remember { Animatable(0.8f) }
                    LaunchedEffect(Unit) {
                        delay(index * 30L)
                        launch { cellAlpha.animateTo(1f, animationSpec = tween(300)) }
                        launch { cellScale.animateTo(1f, animationSpec = tween(300)) }
                    }
                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .alpha(cellAlpha.value)
                            .scale(cellScale.value)
                            .background(
                                color = if (isCleared) Color(0xFF2E7D32) else Color(0xFF2C2C3E),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { onProblemSelected(problem, index) },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isCleared) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("✓", fontSize = 20.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                Text("${index + 1}", fontSize = 13.sp, color = Color.White)
                            }
                        } else {
                            Text(
                                text = "${index + 1}",
                                fontSize = 20.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

// ---- 紙吹雪 ----

/**
 * 紙吹雪の1パーティクルを表すデータクラス。
 * 全値は画面サイズ非依存の正規化座標・比率で保持し、Canvas描画時にpx変換する。
 */
private data class ConfettiParticle(
    val x: Float,              // 横位置 0..1（画面幅に対する割合）
    val phase: Float,          // 縦位置の初期オフセット 0..1（ループ用）
    val speed: Float,          // 落下速度倍率
    val width: Float,          // 短冊の幅（px）
    val height: Float,         // 短冊の高さ（px）
    val color: Color,
    val rotationOffset: Float, // 初期回転角（度）
    val rotationSpeed: Float,  // 1サイクルあたりの回転量（度）
    val isRect: Boolean        // true=短冊形、false=円形
)

// ---- ゲーム画面 ----

/**
 * ゲーム画面のComposable関数。
 * 数独の盤面・数字入力パッド・チェック機能を提供する。
 *
 * @param problem   表示する数独問題
 * @param index     問題一覧内での0始まりインデックス
 * @param total     この難易度の総問題数
 * @param onBack    戻るボタンのコールバック（盤面保存してから問題一覧へ）
 * @param onCleared クリア時のコールバック（問題IDを引数に取る）
 */
@Composable
fun GameScreen(
    problem: SudokuProblem,
    index: Int,
    total: Int,
    onBack: () -> Unit,
    onCleared: (String) -> Unit
) {
    val context = LocalContext.current

    val hintCells = remember {
        Array(9) { r -> BooleanArray(9) { c -> problem.puzzle[r][c] != null } }
    }

    val board = remember {
        val saved = loadBoardState(context, problem.id, problem.puzzle)
        mutableStateListOf(*Array(9) { r ->
            mutableStateListOf(*Array(9) { c -> saved[r][c] })
        })
    }

    var memos by remember { mutableStateOf(mapOf<Pair<Int, Int>, Set<Int>>()) }
    var selectedCell by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var errorCells by remember { mutableStateOf(emptySet<Pair<Int, Int>>()) }
    var showClearDialog by remember { mutableStateOf(false) }
    var showEraseAllDialog by remember { mutableStateOf(false) }
    // ヒントボタンの連続押しカウンター（5回連続で強制ヒント）
    var hintPressCount by remember { mutableStateOf(0) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    fun saveBoard() {
        saveBoardState(context, problem.id, List(9) { r -> List(9) { c -> board[r][c] } })
    }

    fun check(): CheckResult {
        val errors = mutableSetOf<Pair<Int, Int>>()
        var hasEmpty = false
        for (r in 0..8) {
            for (c in 0..8) {
                if (!hintCells[r][c]) {
                    val v = board[r][c]
                    when {
                        v == null -> hasEmpty = true
                        v != problem.answer[r][c] -> errors.add(Pair(r, c))
                    }
                }
            }
        }
        return when {
            hasEmpty          -> CheckResult.HAS_EMPTY
            errors.isNotEmpty() -> {
                errorCells = errors
                CheckResult.HAS_ERROR
            }
            else -> {
                errorCells = emptySet()
                CheckResult.CLEAR
            }
        }
    }

    // ---- ヒントロジック ----

    /**
     * ネイキッドシングル検出：あるマスに入れられる数字が1つだけの場合を検出する。
     * 同じ行・列・3×3ブロックに既に入っている数字を除外し、残り1つなら確定。
     */
    fun findNakedSingles(): List<Triple<Int, Int, Int>> {
        val results = mutableListOf<Triple<Int, Int, Int>>()
        for (r in 0..8) {
            for (c in 0..8) {
                if (board[r][c] != null) continue // 既に入力済みはスキップ
                val used = mutableSetOf<Int>()
                // 同じ行の数字を収集
                for (cc in 0..8) board[r][cc]?.let { used.add(it) }
                // 同じ列の数字を収集
                for (rr in 0..8) board[rr][c]?.let { used.add(it) }
                // 同じ3×3ブロックの数字を収集
                val br = (r / 3) * 3
                val bc = (c / 3) * 3
                for (rr in br..br+2) for (cc in bc..bc+2) board[rr][cc]?.let { used.add(it) }
                // 使われていない数字を候補とする
                val candidates = (1..9).filter { it !in used }
                if (candidates.size == 1) {
                    results.add(Triple(r, c, candidates[0]))
                }
            }
        }
        return results
    }

    /**
     * ヒドゥンシングル検出：ある行・列・ブロックで特定の数字が入れられるマスが1つだけの場合を検出。
     */
    fun findHiddenSingles(): List<Triple<Int, Int, Int>> {
        val results = mutableListOf<Triple<Int, Int, Int>>()

        // 行方向のヒドゥンシングルチェック
        for (r in 0..8) {
            for (num in 1..9) {
                val positions = (0..8).filter { c ->
                    board[r][c] == null && run {
                        val used = mutableSetOf<Int>()
                        for (cc in 0..8) board[r][cc]?.let { used.add(it) }
                        for (rr in 0..8) board[rr][c]?.let { used.add(it) }
                        val br = (r / 3) * 3; val bc = (c / 3) * 3
                        for (rr in br..br+2) for (cc in bc..bc+2) board[rr][cc]?.let { used.add(it) }
                        num !in used
                    }
                }
                if (positions.size == 1) results.add(Triple(r, positions[0], num))
            }
        }

        // 列方向のヒドゥンシングルチェック
        for (c in 0..8) {
            for (num in 1..9) {
                val positions = (0..8).filter { r ->
                    board[r][c] == null && run {
                        val used = mutableSetOf<Int>()
                        for (cc in 0..8) board[r][cc]?.let { used.add(it) }
                        for (rr in 0..8) board[rr][c]?.let { used.add(it) }
                        val br = (r / 3) * 3; val bc = (c / 3) * 3
                        for (rr in br..br+2) for (cc in bc..bc+2) board[rr][cc]?.let { used.add(it) }
                        num !in used
                    }
                }
                if (positions.size == 1) results.add(Triple(positions[0], c, num))
            }
        }

        // 3×3ブロック方向のヒドゥンシングルチェック
        for (blockR in 0..2) {
            for (blockC in 0..2) {
                val br = blockR * 3; val bc = blockC * 3
                for (num in 1..9) {
                    val positions = mutableListOf<Pair<Int, Int>>()
                    for (r in br..br+2) {
                        for (c in bc..bc+2) {
                            if (board[r][c] == null) {
                                val used = mutableSetOf<Int>()
                                for (cc in 0..8) board[r][cc]?.let { used.add(it) }
                                for (rr in 0..8) board[rr][c]?.let { used.add(it) }
                                for (rr in br..br+2) for (cc in bc..bc+2) board[rr][cc]?.let { used.add(it) }
                                if (num !in used) positions.add(Pair(r, c))
                            }
                        }
                    }
                    if (positions.size == 1) results.add(Triple(positions[0].first, positions[0].second, num))
                }
            }
        }

        // 重複除去して返す
        return results.distinctBy { Pair(it.first, it.second) }
    }

    /**
     * ヒントボタン押下時の処理。
     * - 確実に置けるマス（ネイキッド＋ヒドゥンシングル）がある場合：
     *   「まだ置ける箇所があるよ」と表示（5回連続押しなら強制入力）
     * - 確実に置けるマスがない場合：正解を1マスランダム入力
     */
    fun onHintClick() {
        // 確実に置けるマスを検出
        val solvable = (findNakedSingles() + findHiddenSingles())
            .distinctBy { Pair(it.first, it.second) }

        if (solvable.isNotEmpty() && hintPressCount < 2) {
            // まだ解けるマスがある → メッセージ表示してカウントアップ
            hintPressCount++
            scope.launch {
                snackbarHostState.showSnackbar("まだ置ける箇所があるよ")
            }
        } else {
            // 強制ヒント：ランダムに1マス正解を入力
            hintPressCount = 0
            // 候補があればそこから、なければ空きマス全体から正解を探す
            val candidates = if (solvable.isNotEmpty()) {
                solvable
            } else {
                // 空きマス全体から候補を作成
                (0..8).flatMap { r ->
                    (0..8).mapNotNull { c ->
                        if (board[r][c] == null) Triple(r, c, problem.answer[r][c] ?: 0) else null
                    }
                }.filter { it.third != 0 }
            }
            if (candidates.isEmpty()) return // 空きマスなし（すでに全部入力済み）

            val chosen = candidates.random()
            board[chosen.first][chosen.second] = chosen.third
            memos = memos - Pair(chosen.first, chosen.second)
            errorCells = errorCells - Pair(chosen.first, chosen.second)
            selectedCell = Pair(chosen.first, chosen.second)
            saveBoard()

            scope.launch {
                snackbarHostState.showSnackbar("ヒント：(${chosen.first + 1}行, ${chosen.second + 1}列)に${chosen.third}を入れました")
            }
        }
    }


    val confettiColors = remember {
        listOf(
            Color(0xFFE53935), Color(0xFF8E24AA), Color(0xFF1E88E5),
            Color(0xFF00897B), Color(0xFFFFB300), Color(0xFF43A047),
            Color(0xFFFF7043), Color(0xFF00ACC1), Color(0xFFEC407A),
            Color(0xFF7CB342)
        )
    }
    // 起動時に1度だけ生成し、リコンポーズで再生成しないよう remember で保持
    val particles = remember {
        List(280) { i ->
            ConfettiParticle(
                x              = Random.nextFloat(),
                phase          = Random.nextFloat(),
                speed          = 0.6f + Random.nextFloat() * 0.8f,
                width          = 16f  + Random.nextFloat() * 24f,
                height         = 8f   + Random.nextFloat() * 12f,
                color          = confettiColors[i % confettiColors.size],
                rotationOffset = Random.nextFloat() * 360f,
                rotationSpeed  = -200f + Random.nextFloat() * 400f,
                isRect         = Random.nextFloat() > 0.3f
            )
        }
    }
    // 0→1 を繰り返す時間軸（12秒サイクル）
    val infiniteTransition = rememberInfiniteTransition(label = "confetti")
    val time by infiniteTransition.animateFloat(
        initialValue  = 0f,
        targetValue   = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 12_000, easing = LinearEasing)
        ),
        label = "confetti_time"
    )

    if (showEraseAllDialog) {
        AlertDialog(
            onDismissRequest = { showEraseAllDialog = false },
            text = { Text("入力した数字を全て消去しますか？") },
            confirmButton = {
                TextButton(onClick = {
                    for (r in 0..8) for (c in 0..8) if (!hintCells[r][c]) board[r][c] = null
                    memos = emptyMap()
                    errorCells = emptySet()
                    saveBoard()
                    showEraseAllDialog = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showEraseAllDialog = false }) { Text("キャンセル") }
            }
        )
    }

    // Scaffold と紙吹雪オーバーレイを同一 Box に置き、zIndex で重ね順を制御する
    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.systemBars)
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = {
                        saveBoard()
                        onBack()
                    }) {
                        Text("← 戻る", fontSize = 16.sp)
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "${problem.difficulty}　${index + 1}/${total}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    TextButton(onClick = { showEraseAllDialog = true }) {
                        Text("全消去", fontSize = 16.sp, color = Color(0xFFD32F2F))
                    }
                }

                SudokuBoard(
                    board = board,
                    hintCells = hintCells,
                    selectedCell = selectedCell,
                    errorCells = errorCells,
                    memos = memos,
                    onCellClick = { r, c ->
                        hintPressCount = 0
                        selectedCell = if (selectedCell == Pair(r, c)) null else Pair(r, c)
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                NumberPad(
                    onNumberClick = { number ->
                        hintPressCount = 0
                        selectedCell?.let { (r, c) ->
                            if (!hintCells[r][c]) {
                                board[r][c] = number
                                memos = memos - Pair(r, c)
                                errorCells = errorCells - Pair(r, c)
                                saveBoard()
                            }
                        }
                    },
                    onMemoClick = { number ->
                        hintPressCount = 0
                        selectedCell?.let { (r, c) ->
                            if (!hintCells[r][c] && board[r][c] == null) {
                                val key = Pair(r, c)
                                val cur = memos.getOrDefault(key, emptySet())
                                memos = memos + (key to if (number in cur) cur - number else if (cur.size < 4) cur + number else cur)
                            }
                        }
                    },
                    onEraseClick = {
                        hintPressCount = 0
                        selectedCell?.let { (r, c) ->
                            if (!hintCells[r][c]) {
                                board[r][c] = null
                                errorCells = errorCells - Pair(r, c)
                                saveBoard()
                            }
                        }
                    },
                    onMemoEraseClick = {
                        hintPressCount = 0
                        selectedCell?.let { (r, c) ->
                            if (!hintCells[r][c]) {
                                memos = memos - Pair(r, c)
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                val emptyCount = (0..8).sumOf { r -> (0..8).count { c -> board[r][c] == null } }

                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Button(
                        onClick = { onHintClick() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFF57C00),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.align(Alignment.CenterStart)
                    ) {
                        Text("ヒント", fontSize = 16.sp)
                    }
                    Button(
                        onClick = {
                            hintPressCount = 0
                            when (check()) {
                                CheckResult.HAS_EMPTY -> scope.launch {
                                    snackbarHostState.showSnackbar("まだ空白があります")
                                }
                                CheckResult.HAS_ERROR -> scope.launch {
                                    snackbarHostState.showSnackbar("間違っている箇所があります")
                                }
                                CheckResult.CLEAR -> showClearDialog = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF388E3C),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("チェック", fontSize = 16.sp)
                    }
                    Text(
                        text = "残り${emptyCount}マス",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (emptyCount == 0) Color(0xFF388E3C) else Color.Black,
                        modifier = Modifier.align(Alignment.CenterEnd)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "青文字：入力する数字　　オレンジ文字：仮置きする数字",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Text(
                    text = "マスをタップしてから数字を入力してください",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }

        // 紙吹雪オーバーレイ（Scaffold より後に emit → zIndex で前面確定）
        if (showClearDialog) {
            // 半透明オーバーレイ＋紙吹雪 Canvas（zIndex = 10 で Scaffold より前面）
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x99000000))
                    .zIndex(10f)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    particles.forEach { p ->
                        val screenX = p.x * size.width
                        // (phase + time*speed) % 1f で各パーティクルが独立してループ落下
                        val yFraction = ((p.phase + time * p.speed) % 1f)
                        val screenY   = yFraction * size.height * 1.1f - size.height * 0.05f
                        val angle     = (p.rotationOffset + time * p.rotationSpeed) % 360f
                        withTransform({
                            translate(screenX, screenY)
                            rotate(degrees = angle)
                        }) {
                            if (p.isRect) {
                                drawRect(
                                    color   = p.color,
                                    topLeft = Offset(-p.width / 2f, -p.height / 2f),
                                    size    = Size(p.width, p.height)
                                )
                            } else {
                                drawCircle(
                                    color  = p.color,
                                    radius = p.height / 2f
                                )
                            }
                        }
                    }
                }
            }

            // ダイアログカード（紙吹雪より手前 zIndex = 11）
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(11f),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier  = Modifier
                        .padding(horizontal = 40.dp)
                        .fillMaxWidth(),
                    shape     = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    colors    = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier            = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text       = "クリア！",
                            fontSize   = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color      = Color(0xFF1976D2)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "おめでとうございます！", fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = { onCleared(problem.id) },
                            colors  = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF1976D2),
                                contentColor   = Color.White
                            ),
                            shape   = RoundedCornerShape(8.dp)
                        ) {
                            Text("OK", fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    } // outer Box
}
