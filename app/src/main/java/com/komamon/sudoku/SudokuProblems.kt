// Kotlinのパッケージ宣言。C#の namespace に相当する。
// このファイル内のクラスは自動的に com.komamon.sudoku パッケージに属する。
package com.komamon.sudoku

/**
 * 数独の1問分のデータを表すデータクラス。
 *
 * Kotlinの data class はC#の record に相当する。
 * equals(), hashCode(), toString(), copy() が自動生成される。
 *
 * @param id       問題を一意に識別する文字列 (例: "intro_01")
 * @param difficulty 難易度ラベル (例: "入門", "初級", "中級", "上級")
 * @param puzzle   出題盤面。Int? はnull許容型で、null = 空マスを表す。
 *                 C#の int? (Nullable<int>) と同じ概念。
 * @param answer   正解盤面。全マス埋まっているので Int (非null) のリスト。
 */
data class SudokuProblem(
    val id: String,                    // 問題ID。val = C#の readonly に相当（再代入不可）
    val difficulty: String,            // 難易度文字列
    val puzzle: List<List<Int?>>,      // 9×9 の出題盤面。null = 空マス
    val answer: List<List<Int>>        // 9×9 の正解盤面
)

/**
 * 全問題データを管理するシングルトンオブジェクト。
 *
 * Kotlinの object 宣言はシングルトンパターンを言語レベルでサポートする。
 * C#の static class に近いが、インターフェースの実装やオブジェクト参照が可能な点が異なる。
 * インスタンス化は不要で、SudokuProblems.introProblems のように直接アクセスする。
 */
object SudokuProblems {

    // ---- 基底解（ハードコード済みの完成盤面 A〜J）----
    // 10種類の完成盤面を定義する。これらを変形して100問を生成する。

    /**
     * 基底解A。9×9の完成した数独盤面。
     * listOf() はC#の new List<int>{ ... } に相当する不変リストを生成する。
     * Kotlinの List は読み取り専用（C#の IReadOnlyList<T> に近い）。
     * 変更可能なリストは MutableList を使う。
     */
    private val solutionA = listOf(
        listOf(5, 3, 4, 6, 7, 8, 9, 1, 2), // 行0
        listOf(6, 7, 2, 1, 9, 5, 3, 4, 8), // 行1
        listOf(1, 9, 8, 3, 4, 2, 5, 6, 7), // 行2
        listOf(8, 5, 9, 7, 6, 1, 4, 2, 3), // 行3
        listOf(4, 2, 6, 8, 5, 3, 7, 9, 1), // 行4
        listOf(7, 1, 3, 9, 2, 4, 8, 5, 6), // 行5
        listOf(9, 6, 1, 5, 3, 7, 2, 8, 4), // 行6
        listOf(2, 8, 7, 4, 1, 9, 6, 3, 5), // 行7
        listOf(3, 4, 5, 2, 8, 6, 1, 7, 9)  // 行8
    )

    // 基底解B
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

    // 基底解C
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

    // 基底解D
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

    // 基底解E
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

    // 基底解F
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

    // 基底解G
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

    // 基底解H
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

    // 基底解I
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

    // 基底解J
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

    /**
     * 全10種類の基底解をリストにまとめたもの。
     * インデックス番号で参照するために使う。
     * private なのでこのオブジェクト内からのみアクセス可能。C#の private と同じ。
     */
    private val solutions = listOf(
        solutionA, solutionB, solutionC, solutionD, solutionE,
        solutionF, solutionG, solutionH, solutionI, solutionJ
    )

    // ---- マスク定義（どのマスを出題するかのパターン） ----
    // '1' = 数字を表示するマス、'0' = 空白にするマス

    /**
     * 入門難易度のマスクリスト。
     * 入門は空白が少なく（難易度が低い）、ほぼ全マスが埋まっている。
     * mask() ヘルパー関数で文字列をBoolean二次元リストに変換している。
     */
    private val introMasks = listOf(
        mask(
            "111011101", // 行0: 列3,6 のみ空白
            "111110111", // 行1: 列4 のみ空白
            "111111011", // 行2: 列7 のみ空白
            "111111101", // 行3: 列7 のみ空白
            "111101111", // 行4: 列4 のみ空白
            "111111110", // 行5: 列8 のみ空白
            "111111110", // 行6: 列8 のみ空白
            "111111101", // 行7: 列7 のみ空白
            "111111111"  // 行8: 全マス表示
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

    /**
     * 初級難易度のマスクリスト。
     * 入門よりも空白が多い（0が多い）。
     */
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

    /**
     * 中級難易度のマスクリスト。
     * 空白が多くなり、解くのに論理的推論が必要になる。
     */
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

    /**
     * 上級難易度のマスクリスト。
     * 中級と同じパターンを一部流用しているが、transformSeed の違いで
     * 盤面が異なる問題になる。
     */
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

    // ---- ヘルパー関数群 ----

    /**
     * 文字列の配列を Boolean の二次元リストに変換するヘルパー関数。
     *
     * vararg は C#の params キーワードに相当する可変長引数。
     * rows.map { row -> ... } はC#の rows.Select(row => ...) に相当する LINQ 操作。
     * row.map { ch -> ch == '1' } は各文字が '1' かどうかを Boolean に変換する。
     *
     * @param rows 9文字の文字列を9行分渡す
     * @return 各文字が '1' なら true、'0' なら false の二次元リスト
     */
    private fun mask(vararg rows: String): List<List<Boolean>> =
        // rows は Array<String> として扱われる。map は IEnumerable<T>.Select() 相当。
        rows.map { row -> row.map { ch -> ch == '1' } }
        // 末尾の波括弧はラムダ式。Kotlinでは最後の引数がラムダの場合、括弧の外に書ける。

    /**
     * 完成盤面にマスクを適用して出題盤面（パズル）を生成する。
     *
     * mapIndexed はC#の Select((value, index) => ...) に相当する。
     * mask[r][c] が true なら数字を表示、false なら null（空白）にする。
     *
     * @param answer 完成盤面（9×9の整数リスト）
     * @param mask   表示パターン（true = 表示、false = 空白）
     * @return 出題盤面（null = 空白、Int = 表示する数字）
     */
    private fun applyMask(answer: List<List<Int>>, mask: List<List<Boolean>>): List<List<Int?>> =
        answer.mapIndexed { r, row ->          // r: 行インデックス、row: その行のリスト
            row.mapIndexed { c, value ->        // c: 列インデックス、value: マスの数字
                if (mask[r][c]) value else null // マスクが true なら数字を、false なら null を返す
            }
        }

    /**
     * 盤面を転置する（行と列を入れ替える）。
     * 9×9グリッドの [r][c] と [c][r] を入れ替える操作。
     *
     * List(9) { r -> ... } はC#の new List<T> で9要素を初期化するコンストラクタに相当。
     * ラムダ引数 r は0〜8のインデックスが渡される。
     *
     * @param board 変換前の盤面（Int型）
     * @return 転置後の盤面
     */
    private fun transpose(board: List<List<Int>>): List<List<Int>> =
        List(9) { r -> List(9) { c -> board[c][r] } } // [r][c] と [c][r] を入れ替える

    /**
     * null許容型（出題盤面）の転置処理。
     * transpose() と同じロジックだが、Int? 型に対応している。
     * KotlinはJavaの型消去の影響で、ジェネリクスのオーバーロードが制限されるため
     * 別関数として定義している。
     */
    private fun transposeNullable(board: List<List<Int?>>): List<List<Int?>> =
        List(9) { r -> List(9) { c -> board[c][r] } }

    /**
     * 盤面の行を逆順にする（上下反転）。
     * reversed() はC#の Reverse() に相当するが、新しいリストを返す点に注意
     * （元のリストは変更されない = イミュータブル操作）。
     */
    private fun reverseRows(board: List<List<Int>>): List<List<Int>> = board.reversed()

    // null許容型版の上下反転
    private fun reverseRowsNullable(board: List<List<Int?>>): List<List<Int?>> = board.reversed()

    /**
     * 盤面の各行内の列を逆順にする（左右反転）。
     * board.map { it.reversed() } の it は C#のラムダの引数省略形に相当。
     * Kotlinでは引数が1つの場合 it という名前で省略できる。
     */
    private fun reverseCols(board: List<List<Int>>): List<List<Int>> = board.map { it.reversed() }

    // null許容型版の左右反転
    private fun reverseColsNullable(board: List<List<Int?>>): List<List<Int?>> = board.map { it.reversed() }

    /**
     * seed のビットフラグに基づいて完成盤面に変形を適用する。
     *
     * seed はビット演算で3種類の変形を組み合わせる（2^3 = 8通り）。
     * C#の & 演算子に相当する and キーワードを使用している（Kotlinでは and はビット演算）。
     *   ビット0 (seed and 1): 転置（行列入れ替え）
     *   ビット1 (seed and 2): 上下反転
     *   ビット2 (seed and 4): 左右反転
     *
     * @param board 元の完成盤面
     * @param seed  0〜7の変形パターン番号
     * @return 変形後の完成盤面
     */
    private fun transformAnswer(board: List<List<Int>>, seed: Int): List<List<Int>> {
        var current = board                              // var = 再代入可能な変数（C#の var と同じ）
        if (seed and 1 != 0) current = transpose(current)    // ビット0が立っていれば転置
        if (seed and 2 != 0) current = reverseRows(current)  // ビット1が立っていれば上下反転
        if (seed and 4 != 0) current = reverseCols(current)  // ビット2が立っていれば左右反転
        return current
    }

    /**
     * seed のビットフラグに基づいて出題盤面（null含む）に変形を適用する。
     * transformAnswer() と同じロジックだが、Int? 型に対応している。
     *
     * @param board 元の出題盤面（null = 空白）
     * @param seed  0〜7の変形パターン番号
     * @return 変形後の出題盤面
     */
    private fun transformPuzzle(board: List<List<Int?>>, seed: Int): List<List<Int?>> {
        var current = board
        if (seed and 1 != 0) current = transposeNullable(current)    // 転置
        if (seed and 2 != 0) current = reverseRowsNullable(current)  // 上下反転
        if (seed and 4 != 0) current = reverseColsNullable(current)  // 左右反転
        return current
    }

    /**
     * 1問分の SudokuProblem を生成する。
     *
     * 処理の流れ:
     *   1. 基底解にマスクを適用して出題盤面を作る
     *   2. 出題盤面と正解盤面の両方に同じ変形を適用する
     *   3. SudokuProblem オブジェクトとして返す
     *
     * when 式はC#の switch 式に相当する。Kotlinの when はより強力で
     * 任意の式を条件にできる。
     *
     * @param id            問題ID文字列
     * @param difficulty    難易度文字列
     * @param solutionIndex 使用する基底解のインデックス（0〜9）
     * @param maskIndex     使用するマスクのインデックス
     * @param transformSeed 変形パターン番号（0〜7）
     * @return 生成された SudokuProblem
     */
    private fun makeProblem(
        id: String,
        difficulty: String,
        solutionIndex: Int,
        maskIndex: Int,
        transformSeed: Int
    ): SudokuProblem {
        // 正解盤面 = 基底解に変形を適用したもの
        val answer = transformAnswer(solutions[solutionIndex], transformSeed)

        // 出題盤面 = 基底解にマスクを適用してから、同じ変形を適用したもの
        val puzzle = transformPuzzle(
            applyMask(
                solutions[solutionIndex],           // 基底解
                when (difficulty) {                 // 難易度に応じたマスクを選択
                    "入門" -> introMasks[maskIndex % introMasks.size]               // % で範囲内に収める
                    "初級" -> beginnerMasks[maskIndex % beginnerMasks.size]
                    "中級" -> intermediateMasks[maskIndex % intermediateMasks.size]
                    else  -> advancedMasks[maskIndex % advancedMasks.size]          // else は default 相当
                }
            ),
            transformSeed                           // 同じ変形パターンを適用
        )

        // SudokuProblem のインスタンスを生成して返す
        // Kotlinにはコンストラクタの new キーワードがない（C#も最近は省略可能）
        return SudokuProblem(
            id = id,                // 名前付き引数。C#の名前付き引数と同じ構文
            difficulty = difficulty,
            puzzle = puzzle,
            answer = answer
        )
    }

    // ---- 各難易度の問題リスト ----

    /**
     * 入門問題リスト（10問）。
     *
     * List(10) { index -> ... } はC#の Enumerable.Range(0,10).Select(index => ...).ToList() に相当。
     * index は 0〜9 の値が渡される。
     * val はイミュータブルな参照（C#の readonly フィールドに近い）。
     * transformSeed = index % 8 で 0〜7 の変形パターンを順番に使う。
     */
    val introProblems: List<SudokuProblem> = List(10) { index ->
        makeProblem(
            id = "intro_%02d".format(index + 1),    // %02d は2桁ゼロ埋め (C#の {0:D2} に相当)
            difficulty = "入門",
            solutionIndex = index % solutions.size, // 10問で10種類の基底解を順番に使う
            maskIndex = index,
            transformSeed = index % 8               // 8通りの変形を順番に適用
        )
    }

    /**
     * 初級問題リスト（20問）。
     * transformSeed = (index * 3) % 8 で変形パターンをより分散させる。
     */
    val beginnerProblems: List<SudokuProblem> = List(20) { index ->
        makeProblem(
            id = "beginner_%02d".format(index + 1),
            difficulty = "初級",
            solutionIndex = index % solutions.size, // 20問を10種の基底解で巡回
            maskIndex = index,
            transformSeed = (index * 3) % 8        // 3倍の間隔でパターンを選ぶことで多様性を出す
        )
    }

    /**
     * 中級問題リスト（50問）。
     * 最も問題数が多い。transformSeed = (index * 5) % 8 で変形を分散。
     */
    val intermediateProblems: List<SudokuProblem> = List(50) { index ->
        makeProblem(
            id = "intermediate_%02d".format(index + 1),
            difficulty = "中級",
            solutionIndex = index % solutions.size, // 50問を10種の基底解で5周巡回
            maskIndex = index,
            transformSeed = (index * 5) % 8
        )
    }

    /**
     * 上級問題リスト（20問）。
     * transformSeed = (index * 7) % 8 で最も多様な変形パターンを使用。
     */
    val advancedProblems: List<SudokuProblem> = List(20) { index ->
        makeProblem(
            id = "advanced_%02d".format(index + 1),
            difficulty = "上級",
            solutionIndex = index % solutions.size,
            maskIndex = index,
            transformSeed = (index * 7) % 8
        )
    }

    /**
     * 全難易度の問題をまとめた一覧リスト（計100問）。
     * + 演算子でリストを結合している（C#の Concat() に相当）。
     * テスト用途などで全問題にアクセスする際に使用する。
     */
    val allProblems: List<SudokuProblem> =
        introProblems + beginnerProblems + intermediateProblems + advancedProblems
}
