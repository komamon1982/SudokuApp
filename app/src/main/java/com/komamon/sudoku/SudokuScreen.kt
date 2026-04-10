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
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.zIndex
import kotlin.random.Random

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

/**
 * 難易度選択画面のComposable関数。
 *
 * @param onDifficultySelected 難易度が選ばれた時に呼ばれるコールバック。
 */
@Composable
fun DifficultySelectScreen(onDifficultySelected: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.systemBars)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "数独",
            fontSize = 40.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 56.dp)
        )
        listOf("入門", "初級", "中級", "上級").forEach { difficulty ->
            Button(
                onClick = { onDifficultySelected(difficulty) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1976D2),
                    contentColor = Color.White
                )
            ) {
                Text(difficulty, fontSize = 20.sp)
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.systemBars)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onBack) {
                Text("← 戻る", fontSize = 16.sp)
            }
            Text(
                text = difficulty,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
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

        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(problems) { index, problem ->
                val isCleared = problem.id in clearedIds
                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .background(
                            color = if (isCleared) Color(0xFF388E3C) else Color(0xFF1976D2),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { onProblemSelected(problem, index) },
                    contentAlignment = Alignment.Center
                ) {
                    if (isCleared) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("✓", fontSize = 18.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            Text("${index + 1}", fontSize = 11.sp, color = Color.White)
                        }
                    } else {
                        Text(
                            text = "${index + 1}",
                            fontSize = 18.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
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

    // 紙吹雪アニメーション用の状態（showClearDialog が true のときのみ実行される）
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
                    selectedCell = if (selectedCell == Pair(r, c)) null else Pair(r, c)
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            NumberPad(
                onNumberClick = { number ->
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
                    selectedCell?.let { (r, c) ->
                        if (!hintCells[r][c] && board[r][c] == null) {
                            val key = Pair(r, c)
                            val cur = memos.getOrDefault(key, emptySet())
                            memos = memos + (key to if (number in cur) cur - number else if (cur.size < 4) cur + number else cur)
                        }
                    }
                },
                onEraseClick = {
                    selectedCell?.let { (r, c) ->
                        if (!hintCells[r][c]) {
                            board[r][c] = null
                            errorCells = errorCells - Pair(r, c)
                            saveBoard()
                        }
                    }
                },
                onMemoEraseClick = {
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
                    onClick = {
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
