package com.test.myapplication

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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
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

    // ヒントセル（最初から入っている数字）を記録
    val hintCells = remember {
        Array(9) { row -> BooleanArray(9) { col -> initialBoard[row][col] != null } }
    }

    // 変更可能なボード状態
    val board = remember {
        mutableStateListOf(*Array(9) { row ->
            mutableStateListOf(*Array(9) { col -> initialBoard[row][col] })
        })
    }

    // 選択中のセル（row, col）、未選択はnull
    var selectedCell by remember { mutableStateOf<Pair<Int, Int>?>(null) }

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
            onCellClick = { row, col ->
                // ヒントセルも選択はできる（入力だけ無効）
                selectedCell = if (selectedCell == Pair(row, col)) null else Pair(row, col)
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        NumberPad(
            onNumberClick = { number ->
                selectedCell?.let { (row, col) ->
                    if (!hintCells[row][col]) {
                        board[row][col] = number
                    }
                }
            },
            onEraseClick = {
                selectedCell?.let { (row, col) ->
                    if (!hintCells[row][col]) {
                        board[row][col] = null
                    }
                }
            }
        )
    }
}

@Composable
fun SudokuBoard(
    board: List<List<Int?>>,
    hintCells: Array<BooleanArray>,
    selectedCell: Pair<Int, Int>?,
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
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSelected) Color(0xFFBBDEFB) else Color.Transparent

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
            .border(0.5.dp, Color.Gray)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = value?.toString() ?: "",
            fontSize = 18.sp,
            fontWeight = if (isHint) FontWeight.Bold else FontWeight.Normal,
            color = if (isHint) Color.Black else Color(0xFF1565C0)
        )
    }
}

@Composable
fun NumberPad(onNumberClick: (Int) -> Unit, onEraseClick: () -> Unit) {
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
}

@Preview(showBackground = true)
@Composable
fun SudokuPreview() {
    MaterialTheme {
        SudokuScreen()
    }
}
