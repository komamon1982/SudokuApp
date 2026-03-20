package com.komamon.sudoku

import org.junit.Assert.*
import org.junit.Test

class SudokuProblemsTest {

    private val validSet = (1..9).toSet()

    private fun checkAnswer(problem: SudokuProblem) {
        val id = problem.id
        val answer = problem.answer
        val puzzle = problem.puzzle

        // 各行に1〜9が重複なく揃っているか
        for (r in 0..8) {
            assertEquals("$id: row $r", validSet, answer[r].toSet())
        }

        // 各列に1〜9が重複なく揃っているか
        for (c in 0..8) {
            val col = (0..8).map { r -> answer[r][c] }.toSet()
            assertEquals("$id: col $c", validSet, col)
        }

        // 各3x3ブロックに1〜9が重複なく揃っているか
        for (br in 0..2) {
            for (bc in 0..2) {
                val block = mutableSetOf<Int>()
                for (r in 0..2) for (c in 0..2) block.add(answer[br * 3 + r][bc * 3 + c])
                assertEquals("$id: block ($br,$bc)", validSet, block)
            }
        }

        // puzzleのヒント数字がanswerと一致しているか
        for (r in 0..8) {
            for (c in 0..8) {
                val hint = puzzle[r][c]
                if (hint != null) {
                    assertEquals("$id: hint at ($r,$c)", answer[r][c], hint)
                }
            }
        }
    }

    @Test
    fun introProblems_areValid() {
        SudokuProblems.introProblems.forEach { checkAnswer(it) }
    }

    @Test
    fun beginnerProblems_areValid() {
        SudokuProblems.beginnerProblems.forEach { checkAnswer(it) }
    }

    @Test
    fun intermediateProblems_areValid() {
        SudokuProblems.intermediateProblems.forEach { checkAnswer(it) }
    }

    @Test
    fun advancedProblems_areValid() {
        SudokuProblems.advancedProblems.forEach { checkAnswer(it) }
    }
}
