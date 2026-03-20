package com.komamon.sudoku

data class SudokuProblem(
    val id: String,
    val difficulty: String,
    val puzzle: List<List<Int?>>,
    val answer: List<List<Int>>
)

object SudokuProblems {

    private val solutionA = listOf(
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

    private val solutionB = listOf(
        listOf(9, 7, 6, 5, 3, 1, 2, 8, 4),
        listOf(5, 4, 3, 2, 7, 8, 6, 9, 1),
        listOf(8, 2, 1, 4, 6, 9, 3, 5, 7),
        listOf(4, 9, 5, 6, 1, 2, 8, 7, 3),
        listOf(6, 8, 7, 3, 5, 4, 1, 2, 9),
        listOf(1, 3, 2, 9, 8, 7, 4, 6, 5),
        listOf(3, 5, 9, 8, 4, 6, 7, 1, 2),
        listOf(2, 1, 8, 7, 9, 3, 5, 4, 6),
        listOf(7, 6, 4, 1, 2, 5, 9, 3, 8)
    )

    private val solutionC = listOf(
        listOf(9, 6, 1, 5, 7, 4, 3, 8, 2),
        listOf(7, 5, 3, 6, 2, 8, 9, 1, 4),
        listOf(2, 8, 4, 1, 3, 9, 5, 7, 6),
        listOf(4, 3, 9, 7, 8, 6, 1, 2, 5),
        listOf(8, 1, 6, 4, 5, 2, 7, 3, 9),
        listOf(5, 2, 7, 9, 1, 3, 4, 6, 8),
        listOf(6, 7, 8, 3, 4, 5, 2, 9, 1),
        listOf(1, 9, 5, 2, 6, 7, 8, 4, 3),
        listOf(3, 4, 2, 8, 9, 1, 6, 5, 7)
    )

    private val solutionD = listOf(
        listOf(8, 7, 3, 5, 4, 9, 2, 1, 6),
        listOf(2, 6, 4, 3, 8, 1, 5, 7, 9),
        listOf(9, 1, 5, 6, 2, 7, 3, 4, 8),
        listOf(6, 4, 2, 9, 3, 8, 1, 5, 7),
        listOf(7, 9, 1, 2, 5, 4, 6, 8, 3),
        listOf(5, 3, 8, 7, 1, 6, 4, 9, 2),
        listOf(1, 5, 6, 8, 9, 3, 7, 2, 4),
        listOf(4, 8, 7, 1, 6, 2, 9, 3, 5),
        listOf(3, 2, 9, 4, 7, 5, 8, 6, 1)
    )

    private val solutionE = listOf(
        listOf(8, 2, 6, 7, 9, 5, 4, 3, 1),
        listOf(5, 3, 7, 1, 6, 4, 9, 8, 2),
        listOf(9, 1, 4, 2, 3, 8, 6, 5, 7),
        listOf(2, 5, 9, 6, 8, 7, 3, 1, 4),
        listOf(6, 8, 1, 3, 4, 9, 7, 2, 5),
        listOf(4, 7, 3, 5, 1, 2, 8, 6, 9),
        listOf(1, 9, 2, 8, 7, 6, 5, 4, 3),
        listOf(7, 6, 5, 4, 2, 3, 1, 9, 8),
        listOf(3, 4, 8, 9, 5, 1, 2, 7, 6)
    )

    private val solutionF = listOf(
        listOf(6, 5, 8, 7, 2, 9, 1, 4, 3),
        listOf(9, 7, 3, 6, 4, 1, 2, 8, 5),
        listOf(4, 2, 1, 8, 3, 5, 7, 9, 6),
        listOf(2, 4, 9, 1, 6, 3, 8, 5, 7),
        listOf(8, 1, 5, 2, 9, 7, 3, 6, 4),
        listOf(3, 6, 7, 4, 5, 8, 9, 2, 1),
        listOf(1, 9, 2, 5, 7, 4, 6, 3, 8),
        listOf(7, 3, 4, 9, 8, 6, 5, 1, 2),
        listOf(5, 8, 6, 3, 1, 2, 4, 7, 9)
    )

    private val solutionG = listOf(
        listOf(1, 5, 6, 3, 2, 7, 8, 9, 4),
        listOf(7, 9, 8, 6, 5, 4, 3, 1, 2),
        listOf(4, 3, 2, 1, 9, 8, 6, 5, 7),
        listOf(2, 6, 4, 7, 1, 9, 5, 8, 3),
        listOf(3, 7, 1, 8, 6, 5, 4, 2, 9),
        listOf(5, 8, 9, 2, 4, 3, 1, 7, 6),
        listOf(8, 1, 3, 4, 7, 2, 9, 6, 5),
        listOf(9, 4, 7, 5, 8, 6, 2, 3, 1),
        listOf(6, 2, 5, 9, 3, 1, 7, 4, 8)
    )

    private val solutionH = listOf(
        listOf(7, 3, 2, 9, 5, 8, 6, 4, 1),
        listOf(6, 5, 1, 4, 2, 3, 8, 9, 7),
        listOf(9, 4, 8, 1, 6, 7, 3, 2, 5),
        listOf(8, 6, 7, 5, 3, 9, 2, 1, 4),
        listOf(5, 2, 4, 6, 7, 1, 9, 3, 8),
        listOf(1, 9, 3, 8, 4, 2, 5, 7, 6),
        listOf(4, 8, 9, 2, 1, 6, 7, 5, 3),
        listOf(3, 1, 6, 7, 9, 5, 4, 8, 2),
        listOf(2, 7, 5, 3, 8, 4, 1, 6, 9)
    )

    private val solutionI = listOf(
        listOf(6, 5, 4, 7, 1, 2, 9, 3, 8),
        listOf(8, 7, 1, 5, 9, 3, 4, 6, 2),
        listOf(3, 2, 9, 4, 8, 6, 1, 7, 5),
        listOf(2, 1, 3, 6, 5, 4, 7, 8, 9),
        listOf(4, 9, 5, 3, 7, 8, 2, 1, 6),
        listOf(7, 6, 8, 9, 2, 1, 3, 5, 4),
        listOf(5, 3, 2, 8, 4, 7, 6, 9, 1),
        listOf(1, 8, 7, 2, 6, 9, 5, 4, 3),
        listOf(9, 4, 6, 1, 3, 5, 8, 2, 7)
    )

    private val solutionJ = listOf(
        listOf(7, 3, 5, 9, 8, 6, 1, 2, 4),
        listOf(4, 8, 1, 3, 2, 7, 5, 9, 6),
        listOf(6, 9, 2, 1, 5, 4, 7, 3, 8),
        listOf(2, 5, 9, 6, 4, 3, 8, 1, 7),
        listOf(1, 4, 8, 7, 9, 5, 2, 6, 3),
        listOf(3, 6, 7, 2, 1, 8, 4, 5, 9),
        listOf(9, 2, 4, 8, 6, 1, 3, 7, 5),
        listOf(8, 1, 3, 5, 7, 9, 6, 4, 2),
        listOf(5, 7, 6, 4, 3, 2, 9, 8, 1)
    )

    private val solutions = listOf(
        solutionA, solutionB, solutionC, solutionD, solutionE,
        solutionF, solutionG, solutionH, solutionI, solutionJ
    )

    private val introMasks = listOf(
        mask(
            "111011101",
            "111110111",
            "111111011",
            "111111101",
            "111101111",
            "111111110",
            "111111110",
            "111111101",
            "111111111"
        ),
        mask(
            "111111101",
            "111111110",
            "111101111",
            "111111011",
            "111110111",
            "111111110",
            "111111101",
            "111111111",
            "111111111"
        ),
        mask(
            "111111110",
            "111111101",
            "111111111",
            "111101111",
            "111111111",
            "111111110",
            "111111101",
            "111111111",
            "111111011"
        )
    )

    private val beginnerMasks = listOf(
        mask(
            "110010000",
            "100111000",
            "011000010",
            "100010001",
            "100101001",
            "100010001",
            "010000110",
            "000111001",
            "000010011"
        ),
        mask(
            "010010010",
            "010010010",
            "010101010",
            "000111010",
            "001000101",
            "000010110",
            "101000100",
            "011010000",
            "010111000"
        ),
        mask(
            "111100000",
            "000011001",
            "100110000",
            "100100010",
            "100100010",
            "011100010",
            "000010101",
            "111000010",
            "100000011"
        ),
        mask(
            "111110000",
            "001001000",
            "000011001",
            "001010000",
            "100101001",
            "001010000",
            "100011000",
            "000100111",
            "000011111"
        )
    )

    private val intermediateMasks = listOf(
        mask(
            "100010000",
            "001100010",
            "010001100",
            "100100001",
            "010001010",
            "101010001",
            "010010100",
            "001101001",
            "000010011"
        ),
        mask(
            "010101010",
            "100010001",
            "010100010",
            "101010001",
            "010011010",
            "001010101",
            "101001010",
            "010101001",
            "001010100"
        ),
        mask(
            "001010001",
            "010101010",
            "100010101",
            "011001001",
            "101010110",
            "001100011",
            "110010001",
            "001101100",
            "100011010"
        ),
        mask(
            "101010101",
            "011001011",
            "110110001",
            "101101101",
            "011010110",
            "101101101",
            "100011011",
            "110100110",
            "011011010"
        ),
        mask(
            "000101001",
            "101010100",
            "010101010",
            "001010101",
            "110001011",
            "101010100",
            "010101010",
            "001010101",
            "100101000"
        )
    )

    private val advancedMasks = listOf(
        mask(
            "001010001",
            "010101010",
            "100010101",
            "011001001",
            "101010110",
            "001100011",
            "110010001",
            "001101100",
            "100011010"
        ),
        mask(
            "101010101",
            "011001011",
            "110110001",
            "101101101",
            "011010110",
            "101101101",
            "100011011",
            "110100110",
            "011011010"
        ),
        mask(
            "110010000",
            "100111000",
            "011000010",
            "100010001",
            "100101001",
            "100010001",
            "010000110",
            "000111001",
            "000010011"
        )
    )

    private fun mask(vararg rows: String): List<List<Boolean>> =
        rows.map { row -> row.map { ch -> ch == '1' } }

    private fun applyMask(answer: List<List<Int>>, mask: List<List<Boolean>>): List<List<Int?>> =
        answer.mapIndexed { r, row ->
            row.mapIndexed { c, value -> if (mask[r][c]) value else null }
        }

    private fun transpose(board: List<List<Int>>): List<List<Int>> =
        List(9) { r -> List(9) { c -> board[c][r] } }

    private fun transposeNullable(board: List<List<Int?>>): List<List<Int?>> =
        List(9) { r -> List(9) { c -> board[c][r] } }

    private fun reverseRows(board: List<List<Int>>): List<List<Int>> = board.reversed()
    private fun reverseRowsNullable(board: List<List<Int?>>): List<List<Int?>> = board.reversed()

    private fun reverseCols(board: List<List<Int>>): List<List<Int>> = board.map { it.reversed() }
    private fun reverseColsNullable(board: List<List<Int?>>): List<List<Int?>> = board.map { it.reversed() }

    private fun transformAnswer(board: List<List<Int>>, seed: Int): List<List<Int>> {
        var current = board
        if (seed and 1 != 0) current = transpose(current)
        if (seed and 2 != 0) current = reverseRows(current)
        if (seed and 4 != 0) current = reverseCols(current)
        return current
    }

    private fun transformPuzzle(board: List<List<Int?>>, seed: Int): List<List<Int?>> {
        var current = board
        if (seed and 1 != 0) current = transposeNullable(current)
        if (seed and 2 != 0) current = reverseRowsNullable(current)
        if (seed and 4 != 0) current = reverseColsNullable(current)
        return current
    }

    private fun makeProblem(
        id: String,
        difficulty: String,
        solutionIndex: Int,
        maskIndex: Int,
        transformSeed: Int
    ): SudokuProblem {
        val answer = transformAnswer(solutions[solutionIndex], transformSeed)
        val puzzle = transformPuzzle(
            applyMask(solutions[solutionIndex], when (difficulty) {
                "入門" -> introMasks[maskIndex % introMasks.size]
                "初級" -> beginnerMasks[maskIndex % beginnerMasks.size]
                "中級" -> intermediateMasks[maskIndex % intermediateMasks.size]
                else -> advancedMasks[maskIndex % advancedMasks.size]
            }),
            transformSeed
        )
        return SudokuProblem(
            id = id,
            difficulty = difficulty,
            puzzle = puzzle,
            answer = answer
        )
    }

    val introProblems: List<SudokuProblem> = List(10) { index ->
        makeProblem(
            id = "intro_%02d".format(index + 1),
            difficulty = "入門",
            solutionIndex = index % solutions.size,
            maskIndex = index,
            transformSeed = index % 8
        )
    }

    val beginnerProblems: List<SudokuProblem> = List(20) { index ->
        makeProblem(
            id = "beginner_%02d".format(index + 1),
            difficulty = "初級",
            solutionIndex = index % solutions.size,
            maskIndex = index,
            transformSeed = (index * 3) % 8
        )
    }

    val intermediateProblems: List<SudokuProblem> = List(50) { index ->
        makeProblem(
            id = "intermediate_%02d".format(index + 1),
            difficulty = "中級",
            solutionIndex = index % solutions.size,
            maskIndex = index,
            transformSeed = (index * 5) % 8
        )
    }

    val advancedProblems: List<SudokuProblem> = List(20) { index ->
        makeProblem(
            id = "advanced_%02d".format(index + 1),
            difficulty = "上級",
            solutionIndex = index % solutions.size,
            maskIndex = index,
            transformSeed = (index * 7) % 8
        )
    }

    val allProblems: List<SudokuProblem> =
        introProblems + beginnerProblems + intermediateProblems + advancedProblems
}
