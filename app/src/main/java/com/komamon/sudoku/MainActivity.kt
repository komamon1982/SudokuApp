package com.komamon.sudoku

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ---- Navigation ----
sealed class Screen {
    object DifficultySelect : Screen()
    data class ProblemList(val difficulty: String) : Screen()
    data class Game(val problem: SudokuProblem, val index: Int, val total: Int) : Screen()
}

// ---- SharedPreferences helpers ----
private const val PREFS_NAME = "sudoku_prefs"
private const val KEY_CLEARED = "cleared_ids"
private const val KEY_BOARD_PREFIX = "board_"

private fun getPrefs(context: Context) =
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

private fun getClearedIds(context: Context): Set<String> =
    getPrefs(context).getStringSet(KEY_CLEARED, emptySet()) ?: emptySet()

private fun markCleared(context: Context, id: String) {
    val updated = getClearedIds(context).toMutableSet().also { it.add(id) }
    getPrefs(context).edit().putStringSet(KEY_CLEARED, updated).apply()
}

private fun resetClearedForDifficulty(context: Context, problems: List<SudokuProblem>) {
    val updated = getClearedIds(context).toMutableSet().also { set ->
        problems.forEach { set.remove(it.id) }
    }
    getPrefs(context).edit().putStringSet(KEY_CLEARED, updated).apply()
}

private fun saveBoardState(context: Context, id: String, board: List<List<Int?>>) {
    val str = board.flatten().joinToString("") { (it ?: 0).toString() }
    getPrefs(context).edit().putString(KEY_BOARD_PREFIX + id, str).apply()
}

private fun loadBoardState(context: Context, id: String, puzzle: List<List<Int?>>): List<List<Int?>> {
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

// ---- Activity ----
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

private fun problemsFor(difficulty: String): List<SudokuProblem> = when (difficulty) {
    "入門" -> SudokuProblems.introProblems
    "初級" -> SudokuProblems.beginnerProblems
    "中級" -> SudokuProblems.intermediateProblems
    else -> SudokuProblems.advancedProblems
}

// ---- Difficulty Select Screen ----
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

// ---- Problem List Screen ----
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

// ---- Game Screen ----
private enum class CheckResult { HAS_EMPTY, HAS_ERROR, CLEAR }

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
            hasEmpty -> CheckResult.HAS_EMPTY
            errors.isNotEmpty() -> { errorCells = errors; CheckResult.HAS_ERROR }
            else -> { errorCells = emptySet(); CheckResult.CLEAR }
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("クリア！") },
            text = { Text("おめでとうございます！") },
            confirmButton = {
                TextButton(onClick = { onCleared(problem.id) }) { Text("OK") }
            }
        )
    }

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
        // Header
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
    } // Scaffold
}

// ---- Board & Cell ----
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
        isSelected    -> Color(0xFF90CAF9)  // 選択中：薄い青
        isSameNumber  -> Color(0xFFBBDEFB)  // 同じ数字：中くらいの青
        isSameGroup   -> Color(0xFFF5F5F5)  // 同じ行・列・ブロック：グレー
        else          -> Color.White
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
                    isError -> Color(0xFFD32F2F)
                    isSelected && !isHint -> Color.White
                    isHint -> Color.Black
                    else -> Color(0xFF1565C0)
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

// ---- Number Pad ----
@Composable
fun NumberPad(
    onNumberClick: (Int) -> Unit,
    onMemoClick: (Int) -> Unit,
    onEraseClick: () -> Unit,
    onMemoEraseClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
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
