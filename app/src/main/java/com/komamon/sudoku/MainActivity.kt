package com.komamon.sudoku

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    SudokuScreen()
                }
            }
        }
    }
}

@Composable
fun SudokuScreen() {
    val initialBoard = listOf(
        listOf(5, 3, null, null, 7, null, null, null, null),
        listOf(6, null, null, 1, 9, 5, null, null, null),
        listOf(null, 9, 8, null, null, null, null, 6, null),
        listOf(8, null, null, null, 6, null, null, null, 3),
        listOf(4, null, null, 8, null, 3, null, null, 1),
        listOf(7, null, null, null, 2, null, null, null, 6),
        listOf(null, 6, null, null, null, null, 2, 8, null),
        listOf(null, null, null, 4, 1, 9, null, null, 5),
        listOf(null, null, null, null, 8, null, null, 7, 9)
    )

    val solution = listOf(
        listOf(5, 3, 4, 6, 7, 8, 9, 1, 2),
        listOf(6, 7, 2, 1, 9, 5, 3, 4, 8),
        listOf(1, 9, 8, 3, 4, 2, 5, 6, 7),
        listOf(8, 5, 9, 7, 6, 1, 4, 2, 3),
        listOf(4, 2, 6, 8, 5, 3, 7, 9, 1),
        listOf(7, 1, 3, 9, 2, 4, 8, 5, 6),
        listOf(9, 6, 1, 5, 3, 7, 2, 8, 4),
        listOf(2, 8, 7, 4, 1, 9, 6, 3, 5),
        listOf(3, 4, 5, 2, 8, 6, 1, 7, 9)
    )

    val hintCells = remember {
        Array(9) { row -> BooleanArray(9) { col -> initialBoard[row][col] != null } }
    }

    val board = remember {
        mutableStateListOf(*Array(9) { row ->
            mutableStateListOf(*Array(9) { col -> initialBoard[row][col] })
        })
    }

    // memos: Map<(row,col), Set<number>> — メモ数字の管理
    var memos by remember { mutableStateOf(mapOf<Pair<Int, Int>, Set<Int>>()) }
    var selectedCell by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var errorCells by remember { mutableStateOf(emptySet<Pair<Int, Int>>()) }
    var showClearDialog by remember { mutableStateOf(false) }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("クリア！") },
            text = { Text("おめでとうございます！") },
            confirmButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("OK")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.systemBars)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Sudoku",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        SudokuBoard(
            board = board,
            hintCells = hintCells,
            selectedCell = selectedCell,
            errorCells = errorCells,
            memos = memos,
            onCellClick = { row, col ->
                selectedCell = if (selectedCell == Pair(row, col)) null else Pair(row, col)
            }
        )

        Spacer(modifier = Modifier.height(20.dp))

        NumberPad(
            onNumberClick = { number ->
                selectedCell?.let { (row, col) ->
                    if (!hintCells[row][col]) {
                        board[row][col] = number
                        memos = memos - Pair(row, col)           // メモをすべて消す
                        errorCells = errorCells - Pair(row, col) // エラー表示を消す
                    }
                }
            },
            onMemoClick = { number ->
                selectedCell?.let { (row, col) ->
                    if (!hintCells[row][col] && board[row][col] == null) {
                        val key = Pair(row, col)
                        val current = memos.getOrDefault(key, emptySet())
                        memos = memos + (key to if (number in current) current - number else current + number)
                    }
                }
            },
            onEraseClick = {
                selectedCell?.let { (row, col) ->
                    if (!hintCells[row][col]) {
                        board[row][col] = null
                        errorCells = errorCells - Pair(row, col)
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                val newErrors = mutableSetOf<Pair<Int, Int>>()
                var allFilled = true
                for (row in 0 until 9) {
                    for (col in 0 until 9) {
                        if (!hintCells[row][col]) {
                            val value = board[row][col]
                            when {
                                value == null -> allFilled = false
                                value != solution[row][col] -> newErrors.add(Pair(row, col))
                            }
                        }
                    }
                }
                errorCells = newErrors
                if (newErrors.isEmpty() && allFilled) {
                    showClearDialog = true
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
    }
}

@Composable
fun SudokuBoard(
    board: List<List<Int?>>,
    hintCells: Array<BooleanArray>,
    selectedCell: Pair<Int, Int>?,
    errorCells: Set<Pair<Int, Int>>,
    memos: Map<Pair<Int, Int>, Set<Int>>,
    onCellClick: (Int, Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .border(3.dp, Color.Black, RectangleShape)
    ) {
        for (row in 0 until 9) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                for (col in 0 until 9) {
                    SudokuCell(
                        value = board[row][col],
                        isHint = hintCells[row][col],
                        isSelected = selectedCell == Pair(row, col),
                        isError = Pair(row, col) in errorCells,
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
    memos: Set<Int>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSelected) Color(0xFFBBDEFB) else Color.Transparent

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
            .border(0.5.dp, Color.Gray)
            .clickable { onClick() }
    ) {
        if (value != null) {
            // 本番の数字を中央に表示
            Text(
                text = value.toString(),
                fontSize = 18.sp,
                fontWeight = if (isHint) FontWeight.Bold else FontWeight.Normal,
                color = when {
                    isError -> Color(0xFFD32F2F)
                    isHint -> Color.Black
                    else -> Color(0xFF1565C0)
                },
                modifier = Modifier.align(Alignment.Center)
            )
        } else if (memos.isNotEmpty()) {
            // メモ数字を3×3ミニグリッドで表示（オレンジ色）
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(1.dp),
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                for (memoRow in 0..2) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        for (memoCol in 0..2) {
                            val num = memoRow * 3 + memoCol + 1
                            Text(
                                text = if (num in memos) num.toString() else " ",
                                fontSize = 7.sp,
                                color = Color(0xFFE65100),
                                lineHeight = 8.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NumberPad(
    onNumberClick: (Int) -> Unit,
    onMemoClick: (Int) -> Unit,
    onEraseClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        // 上段：通常入力（青色）
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
                ) {
                    Text(text = n.toString(), fontSize = 16.sp)
                }
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
            ) {
                Text(text = "消", fontSize = 16.sp)
            }
        }

        // 下段：メモ入力（オレンジ色）
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
                ) {
                    Text(text = n.toString(), fontSize = 16.sp)
                }
            }
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SudokuPreview() {
    MaterialTheme {
        SudokuScreen()
    }
}
