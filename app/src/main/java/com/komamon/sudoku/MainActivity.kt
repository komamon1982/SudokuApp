// Kotlinのパッケージ宣言。C#の namespace に相当する。
package com.komamon.sudoku

// Android / Jetpack Compose のインポート。
// C#の using ディレクティブに相当する。
import android.content.Context                                    // Androidのコンテキスト（アプリ環境へのアクセス窓口）
import android.os.Bundle                                          // Activityの状態保存用データコンテナ
import androidx.activity.ComponentActivity                        // Composeを使うActivityの基底クラス
import androidx.activity.compose.setContent                       // ComposeのUIをActivityに設定する拡張関数
import androidx.activity.enableEdgeToEdge                         // ステータスバー・ナビゲーションバー下までUI拡張する設定
import androidx.compose.foundation.background                     // 背景色を設定するModifier
import androidx.compose.foundation.border                         // 枠線を設定するModifier
import androidx.compose.foundation.clickable                      // クリックイベントを設定するModifier
import androidx.compose.foundation.layout.Arrangement             // 子要素の配置方法を指定する
import androidx.compose.foundation.layout.Box                     // 重ね合わせレイアウト（C#のGridのZIndex的な概念）
import androidx.compose.foundation.layout.Column                  // 縦方向に積み重ねるレイアウト（C#のStackPanel Vertical相当）
import androidx.compose.foundation.layout.PaddingValues           // ボタン内の余白を指定するデータクラス
import androidx.compose.foundation.layout.Row                     // 横方向に並べるレイアウト（C#のStackPanel Horizontal相当）
import androidx.compose.foundation.layout.Spacer                  // 余白を作るための空コンポーネント
import androidx.compose.foundation.layout.WindowInsets            // システムUI（ステータスバー等）の領域情報
import androidx.compose.foundation.layout.aspectRatio             // アスペクト比を維持するModifier
import androidx.compose.foundation.layout.fillMaxHeight           // 親の高さいっぱいに広げるModifier
import androidx.compose.foundation.layout.fillMaxSize             // 親の幅・高さいっぱいに広げるModifier
import androidx.compose.foundation.layout.fillMaxWidth            // 親の幅いっぱいに広げるModifier
import androidx.compose.foundation.layout.height                  // 高さを固定値で指定するModifier
import androidx.compose.foundation.layout.padding                 // 内側の余白を指定するModifier
import androidx.compose.foundation.layout.systemBars              // ステータスバー＋ナビゲーションバーの領域
import androidx.compose.foundation.layout.width                   // 幅を固定値で指定するModifier
import androidx.compose.foundation.layout.windowInsetsPadding     // システムUI領域分のパディングを追加するModifier
import androidx.compose.foundation.lazy.grid.GridCells            // グリッドの列数設定
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid     // 遅延描画の縦グリッド（RecyclerView相当）
import androidx.compose.foundation.lazy.grid.itemsIndexed         // グリッドのアイテム生成（インデックス付き）
import androidx.compose.foundation.shape.RoundedCornerShape       // 角丸の形状定義
import androidx.compose.material3.AlertDialog                     // ダイアログコンポーネント
import androidx.compose.material3.Button                          // マテリアルデザインのボタン
import androidx.compose.material3.ButtonDefaults                  // ボタンのデフォルト設定（色など）
import androidx.compose.material3.MaterialTheme                   // マテリアルデザインのテーマ
import androidx.compose.material3.Scaffold                        // 画面の基本レイアウト骨格（AppBar・FAB・Snackbarの配置など）
import androidx.compose.material3.SnackbarHost                    // Snackbar表示領域
import androidx.compose.material3.SnackbarHostState               // Snackbarの状態管理オブジェクト
import androidx.compose.material3.Surface                         // 背景面を提供するコンポーネント
import androidx.compose.material3.Text                            // テキスト表示コンポーネント
import androidx.compose.material3.TextButton                      // テキストのみのボタン（枠なし）
import androidx.compose.runtime.Composable                        // Composable関数のアノテーション
import androidx.compose.runtime.getValue                          // by委譲でgetterを有効にするためのインポート
import androidx.compose.runtime.mutableStateListOf                // 監視可能なMutableListを生成する
import androidx.compose.runtime.mutableStateOf                    // 監視可能な状態変数を生成する（C#のINotifyPropertyChanged相当）
import androidx.compose.runtime.remember                          // リコンポーズをまたいで値を記憶する
import androidx.compose.runtime.rememberCoroutineScope            // コルーチンのスコープを記憶する
import androidx.compose.runtime.setValue                          // by委譲でsetterを有効にするためのインポート
import kotlinx.coroutines.launch                                  // コルーチンを起動する（C#のTask.Run相当）
import androidx.compose.ui.Alignment                              // 要素の整列位置を指定する
import androidx.compose.ui.Modifier                               // UIのスタイルや動作を定義するBuilderパターンのクラス
import androidx.compose.ui.graphics.Color                         // 色を表すクラス（ARGB値で指定）
import androidx.compose.ui.graphics.RectangleShape                // 四角形の形状定義
import androidx.compose.ui.platform.LocalContext                  // Composable内でContextを取得するための仕組み
import androidx.compose.ui.text.font.FontWeight                   // フォントの太さを指定する
import androidx.compose.ui.unit.dp                                // 密度非依存ピクセル単位（C#のWPFのDIPに相当）
import androidx.compose.ui.unit.sp                                // フォントサイズのスケールドピクセル単位

// ---- ナビゲーション定義 ----

/**
 * アプリの画面を表す sealed クラス。
 *
 * Kotlin の sealed class はC#の abstract class に似ているが、
 * 同一ファイル（またはパッケージ）内でのみサブクラスを定義できる点が異なる。
 * これにより when 式（switch相当）で全ケースを網羅できることをコンパイラが保証する。
 * C# 9以降の discriminated union（switch expression）に近い概念。
 *
 * このアプリはナビゲーションライブラリを使わず、sealed class で状態管理している。
 * currentScreen の値を変えるだけで画面遷移できるシンプルな設計。
 */
sealed class Screen {
    // object = シングルトン。難易度選択画面はデータを持たないので object で定義。
    // C#の static readonly なシングルトンクラスに相当。
    object DifficultySelect : Screen()

    // data class = データを持つ画面。C#の record class に相当。
    // 問題一覧画面は表示する難易度を保持する。
    data class ProblemList(val difficulty: String) : Screen()

    // ゲーム画面は問題データ・現在インデックス・総問題数を保持する。
    data class Game(val problem: SudokuProblem, val index: Int, val total: Int) : Screen()
}

// ---- SharedPreferences ヘルパー ----
// SharedPreferences = Androidのキーバリューストア。
// C#の Properties.Settings や IsolatedStorage、AppSettings に相当する。
// アプリを閉じても値が保持される永続化ストレージ。

// const val = コンパイル時定数。C#の const string に相当。
// private = このファイル内からのみアクセス可能（トップレベルの private）。
private const val PREFS_NAME = "sudoku_prefs"         // SharedPreferencesのファイル名
private const val KEY_CLEARED = "cleared_ids"          // クリア済み問題IDのセットを保存するキー
private const val KEY_BOARD_PREFIX = "board_"          // 問題ごとの盤面状態を保存するキーのプレフィックス

/**
 * SharedPreferencesのインスタンスを取得するヘルパー関数。
 *
 * C#の拡張メソッドと異なり、Kotlinの関数はファイルのトップレベルに直接定義できる。
 * = で始まる単式関数（Single-expression function）はC#の => ラムダメソッドに相当する。
 * Context.MODE_PRIVATE = このアプリのみが読み書きできるプライベートモード。
 */
private fun getPrefs(context: Context) =
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

/**
 * クリア済み問題IDのセットを取得する。
 *
 * getStringSet() の第2引数はキーが存在しない場合のデフォルト値。
 * ?: はElvis演算子（C# の ?? に相当）。左辺がnullなら右辺を返す。
 * Set<String> はC#の ISet<string>（HashSet<string>）に相当する。
 */
private fun getClearedIds(context: Context): Set<String> =
    getPrefs(context).getStringSet(KEY_CLEARED, emptySet()) ?: emptySet()
    // emptySet() = 空の不変セット。emptySet<String>() の型省略形。

/**
 * 指定した問題IDをクリア済みとして保存する。
 *
 * also はスコープ関数の一つ。C#では相当する構文がなく、
 * 「オブジェクトを受け取って操作し、そのオブジェクト自体を返す」という挙動をする。
 * ここでは toMutableSet() で作ったコピーに対して add() を呼び出し、
 * そのセット自体を updated 変数に代入している。
 *
 * edit() → 編集セッションの開始（C#の using ブロックに似た流れ）
 * putStringSet() → 値を設定
 * apply() → 非同期で保存（commit() は同期保存）
 */
private fun markCleared(context: Context, id: String) {
    // toMutableSet() で読み取り専用の Set を変更可能な MutableSet にコピーする
    val updated = getClearedIds(context).toMutableSet().also { it.add(id) }
    // edit() で変更セッションを開始し、putStringSet() で保存、apply() で非同期コミット
    getPrefs(context).edit().putStringSet(KEY_CLEARED, updated).apply()
}

/**
 * 指定した難易度の問題のクリア状態をリセットする。
 *
 * forEach はC#の foreach に相当するコレクション操作。
 * { set -> problems.forEach { set.remove(it.id) } } は
 * 複数行のラムダ式で、set が also ブロックのレシーバー（it の名前付き版）。
 */
private fun resetClearedForDifficulty(context: Context, problems: List<SudokuProblem>) {
    val updated = getClearedIds(context).toMutableSet().also { set ->
        problems.forEach { set.remove(it.id) } // 各問題のIDをクリア済みセットから削除
    }
    getPrefs(context).edit().putStringSet(KEY_CLEARED, updated).apply()
}

/**
 * 盤面の状態を SharedPreferences に保存する。
 *
 * board.flatten() でネストしたリストを1次元に平坦化する（C#の SelectMany() に相当）。
 * joinToString("") でリストを文字列に結合する（C#の string.Join("", ...) に相当）。
 * (it ?: 0) はnullなら0に置き換えるElvis演算子の使用。
 * 81文字の文字列として保存する（例: "530678912671..."）。
 */
private fun saveBoardState(context: Context, id: String, board: List<List<Int?>>) {
    // flatten() で [[1,2],[3,4]] → [1,2,3,4] のように平坦化する
    // joinToString("") { ... } は各要素に変換処理を適用しながら結合する
    val str = board.flatten().joinToString("") { (it ?: 0).toString() }
    // KEY_BOARD_PREFIX + id = "board_intro_01" のようなキーを動的生成
    getPrefs(context).edit().putString(KEY_BOARD_PREFIX + id, str).apply()
}

/**
 * SharedPreferences から盤面の状態を読み込む。
 * 保存データがない場合は元のパズル（出題盤面）を返す。
 *
 * getString() の第2引数 null はデフォルト値（キーが存在しない場合）。
 * ?: return puzzle は、nullなら早期リターンするElvis演算子の慣用パターン。
 * r * 9 + c で二次元インデックスを一次元インデックスに変換する。
 * digitToIntOrNull() は文字を整数に安全変換する拡張関数。変換失敗時に null を返す。
 *
 * @param context Android のコンテキスト
 * @param id      問題ID
 * @param puzzle  デフォルトとして返す出題盤面（保存データなし時）
 * @return 保存されていた盤面、または出題盤面
 */
private fun loadBoardState(context: Context, id: String, puzzle: List<List<Int?>>): List<List<Int?>> {
    // 保存された文字列を取得。null なら出題盤面をそのまま返す（早期リターン）
    val str = getPrefs(context).getString(KEY_BOARD_PREFIX + id, null)
        ?: return puzzle                          // Elvis演算子 + return で早期リターン
    if (str.length != 81) return puzzle           // 不正データの場合も出題盤面を返す
    // 81文字の文字列を 9×9 の二次元リストに変換する
    return List(9) { r ->                         // 行 r: 0〜8
        List(9) { c ->                            // 列 c: 0〜8
            val digit = str[r * 9 + c].digitToIntOrNull() ?: 0 // 文字を数字に変換。失敗時は0
            if (digit == 0) null else digit       // 0は空マス（null）、1〜9はそのまま
        }
    }
}

// ---- Activity ----

/**
 * アプリのエントリーポイントとなる Activity クラス。
 *
 * Android の Activity は C# の Window や Form に相当する画面の基本単位。
 * ComponentActivity は Jetpack Compose 対応の基底クラス。
 * : ComponentActivity() は C# の : ComponentActivity に相当する継承構文。
 * () がついているのは、Kotlin では基底クラスのコンストラクタ呼び出しが必要なため。
 */
class MainActivity : ComponentActivity() {

    /**
     * Activity 生成時に呼ばれるライフサイクルメソッド。
     * C#の Windows Forms の Load イベントや WPF の OnInitialized() に相当する。
     *
     * override キーワードは C# と同じ意味。
     * savedInstanceState は画面回転などで破棄・再生成された場合に
     * 前の状態を復元するためのデータ（C#にはない Android 独自の仕組み）。
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)         // 基底クラスの onCreate を必ず呼ぶ
        enableEdgeToEdge()                         // ステータスバー・ナビバー下まで描画領域を拡張する
        // setContent はJetpack Composeのエントリーポイント。
        // C#のXAMLで <Window> を定義するのと同じように、ここでUIツリーを構築する。
        setContent {
            MaterialTheme {                        // マテリアルデザインのテーマを適用する
                // Surface は背景色（テーマのデフォルト色）を提供するコンポーネント
                Surface(modifier = Modifier.fillMaxSize()) {
                    SudokuApp()                    // アプリのルートComposable関数を呼び出す
                }
            }
        }
    }
}

/**
 * アプリ全体のルートComposable関数。
 * 現在の画面状態（screen）を管理し、適切な画面を表示する。
 *
 * @Composable アノテーションはJetpack Compose の UI 関数であることを示す。
 * Composable関数は UIの「宣言」であり、状態が変わると自動的に再描画（リコンポーズ）される。
 * C#のXAMLとは異なり、コードとUIが統合されたアプローチ（Reactに近い考え方）。
 */
@Composable
fun SudokuApp() {
    // LocalContext.current はComposable内でAndroidのContextを取得する仕組み。
    // Dependency Injectionなしでコンテキストにアクセスできる。
    val context = LocalContext.current

    // remember { } はリコンポーズ（再描画）をまたいで値を記憶する。
    // mutableStateOf はObservableな状態変数を作る（C#のINotifyPropertyChangedに相当）。
    // 値が変わると自動的にUIが再描画される。
    // by キーワードはプロパティ委譲。getValue/setValue演算子を委譲先に委ねる。
    // by を使うことで screen.value ではなく screen と直接アクセスできる。
    var screen by remember { mutableStateOf<Screen>(Screen.DifficultySelect) }
    // クリア済み問題IDのセットを状態として保持する
    var clearedIds by remember { mutableStateOf(getClearedIds(context)) }

    // SharedPreferencesから最新のクリア済みIDを読み込んで状態を更新するローカル関数
    // fun キーワードで定義するローカル関数はC#のローカル関数と同じ概念
    fun refreshCleared() { clearedIds = getClearedIds(context) }

    // when はC#の switch 式に相当するが、より強力。
    // is で型チェックと同時にスマートキャスト（自動型変換）が行われる。
    // C#では (s as ProblemList)? のようなキャストが必要だが、
    // Kotlinでは is でチェックすればそのブロック内で自動的に型が確定する。
    when (val s = screen) {                        // s に screen の現在値をスマートキャスト付きで束縛
        // 難易度選択画面
        is Screen.DifficultySelect -> DifficultySelectScreen(
            // ラムダ式でコールバックを渡す。C#のAction<string>デリゲートに相当。
            onDifficultySelected = { difficulty -> screen = Screen.ProblemList(difficulty) }
        )
        // 問題一覧画面。s は Screen.ProblemList 型にスマートキャストされている。
        is Screen.ProblemList -> {
            val problems = problemsFor(s.difficulty) // 難易度に応じた問題リストを取得
            ProblemListScreen(
                difficulty = s.difficulty,
                problems = problems,
                clearedIds = clearedIds,
                onProblemSelected = { problem, index ->
                    screen = Screen.Game(problem, index, problems.size) // ゲーム画面に遷移
                },
                onResetCleared = {
                    resetClearedForDifficulty(context, problems) // クリア状態をリセット
                    refreshCleared()                               // 表示を更新
                },
                onBack = { screen = Screen.DifficultySelect }     // 難易度選択に戻る
            )
        }
        // ゲーム画面
        is Screen.Game -> GameScreen(
            problem = s.problem,
            index = s.index,
            total = s.total,
            onBack = {
                refreshCleared()                                   // バック時にクリア状態を更新
                screen = Screen.ProblemList(s.problem.difficulty)  // 問題一覧に戻る
            },
            onCleared = { id ->
                markCleared(context, id)                           // クリアを保存
                refreshCleared()
                screen = Screen.ProblemList(s.problem.difficulty)  // 問題一覧に戻る
            }
        )
    }
}

/**
 * 難易度文字列に対応する問題リストを返すマッピング関数。
 *
 * when 式を使ったシンプルなルーティング処理。
 * C#の switch 式と同様に値を返す式として使える。
 * private fun はこのファイル内でのみ使える関数。
 */
private fun problemsFor(difficulty: String): List<SudokuProblem> = when (difficulty) {
    "入門" -> SudokuProblems.introProblems          // SudokuProblems はシングルトンオブジェクト
    "初級" -> SudokuProblems.beginnerProblems
    "中級" -> SudokuProblems.intermediateProblems
    else  -> SudokuProblems.advancedProblems        // else はデフォルトケース（C#の default:）
}

// ---- 難易度選択画面 ----

/**
 * 難易度選択画面のComposable関数。
 *
 * @param onDifficultySelected 難易度が選ばれた時に呼ばれるコールバック。
 *                             (String) -> Unit はC#の Action<string> に相当する関数型。
 *                             Unit はC#の void に相当する型。
 */
@Composable
fun DifficultySelectScreen(onDifficultySelected: (String) -> Unit) {
    // Column は垂直方向に子要素を積み重ねるレイアウト。
    // C#のWPFの StackPanel (Orientation=Vertical) に相当する。
    Column(
        modifier = Modifier                            // Modifier はUIのスタイル・動作を定義するBuilderパターン
            .fillMaxSize()                             // 親View全体に広げる（C#のHorizontalAlignment=Stretch）
            .windowInsetsPadding(WindowInsets.systemBars) // ステータスバー・ナビバー分のパディングを追加
            .padding(32.dp),                           // 内側の余白を32dp設定（C#のPadding="32"相当）
        horizontalAlignment = Alignment.CenterHorizontally, // 水平方向中央揃え
        verticalArrangement = Arrangement.Center            // 垂直方向中央配置
    ) {
        // アプリタイトルテキスト
        Text(
            text = "数独",
            fontSize = 40.sp,                          // フォントサイズ（sp = スケールドピクセル、ユーザーのフォントサイズ設定に追従）
            fontWeight = FontWeight.Bold,              // 太字
            modifier = Modifier.padding(bottom = 56.dp) // 下方向に56dpのパディング
        )
        // 難易度リストを listOf() で定義し forEach でボタンを生成する
        // C#の foreach (var difficulty in new[]{"入門",...}) に相当
        listOf("入門", "初級", "中級", "上級").forEach { difficulty ->
            Button(
                onClick = { onDifficultySelected(difficulty) }, // ボタンタップ時にコールバック呼び出し
                modifier = Modifier
                    .fillMaxWidth()                    // 横幅いっぱいに広げる
                    .padding(vertical = 8.dp)          // 上下8dpのパディング
                    .height(56.dp),                    // 高さ固定
                shape = RoundedCornerShape(12.dp),     // 12dpの角丸
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1976D2), // 背景色（ARGBの16進数指定。0xFF=不透明）
                    contentColor = Color.White          // テキスト色
                )
            ) {
                // ボタン内のコンテンツをラムダで定義（C#のContentプロパティに相当）
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
    onProblemSelected: (SudokuProblem, Int) -> Unit, // (SudokuProblem, int) -> void 型の関数
    onResetCleared: () -> Unit,                      // 引数なし・戻り値なし の関数型
    onBack: () -> Unit
) {
    // リセット確認ダイアログの表示状態を管理する。
    // remember { mutableStateOf(false) } で初期値false の状態変数を作る。
    var showResetDialog by remember { mutableStateOf(false) }

    // showResetDialog が true のときだけダイアログを表示する（条件付き描画）
    if (showResetDialog) {
        // AlertDialog はAndroidのマテリアルデザインのアラートダイアログ
        AlertDialog(
            onDismissRequest = { showResetDialog = false }, // ダイアログ外タップで閉じる
            text = { Text("この難易度のクリア状態をリセットしますか？") },
            confirmButton = {
                TextButton(onClick = {
                    onResetCleared()             // クリアリセット処理を呼ぶ
                    showResetDialog = false      // ダイアログを閉じる
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
            .windowInsetsPadding(WindowInsets.systemBars) // システムUI領域を避けてパディング
            .padding(16.dp)
    ) {
        // ヘッダー行：戻るボタン、難易度名、クリア数
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically // 垂直方向の中央揃え
        ) {
            TextButton(onClick = onBack) {         // 戻るボタン（枠なしテキストボタン）
                Text("← 戻る", fontSize = 16.sp)
            }
            Text(
                text = difficulty,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp)  // 左に8dpの余白
            )
            // Spacer + weight(1f) は Flexbox の flex-grow:1 と同じ効果。
            // 残りのスペースを全部占有することで右側の要素を右端に押しやる。
            Spacer(modifier = Modifier.weight(1f))
            // クリア数 / 総問題数 を表示する
            Text(
                // clearedIds の中からこの難易度の問題IDだけをカウントする
                // C#の clearedIds.Count(id => problems.Any(p => p.id == id)) に相当
                text = "${clearedIds.count { id -> problems.any { it.id == id } }}/${problems.size} クリア",
                fontSize = 14.sp,
                color = Color(0xFF388E3C)          // 緑色
            )
        }
        // クリア状態リセットボタン（右端に配置）
        TextButton(
            onClick = { showResetDialog = true },  // ダイアログ表示フラグを立てる
            modifier = Modifier.align(Alignment.End) // Column内で右端に配置
        ) {
            Text("クリア状態をリセット", fontSize = 13.sp, color = Color(0xFFD32F2F)) // 赤色
        }
        Spacer(modifier = Modifier.height(4.dp))   // 4dpの縦余白

        // LazyVerticalGrid は仮想化された縦グリッドレイアウト。
        // RecyclerView の GridLayoutManager に相当する。
        // 「Lazy」= 画面に見えている部分だけ描画するため、大量アイテムに効率的。
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),           // 4列固定グリッド
            horizontalArrangement = Arrangement.spacedBy(8.dp), // 列間8dpの隙間
            verticalArrangement = Arrangement.spacedBy(8.dp)    // 行間8dpの隙間
        ) {
            // itemsIndexed はインデックス付きでアイテムを生成するLazyGrid用拡張関数
            // C#の for (int i=0; i<problems.Count; i++) に相当
            itemsIndexed(problems) { index, problem ->
                // in 演算子でセット内に問題IDが含まれているか確認する（C#の Contains() に相当）
                val isCleared = problem.id in clearedIds
                // Box は重ね合わせレイアウト。複数の子要素を同じ位置に配置できる。
                Box(
                    modifier = Modifier
                        .aspectRatio(1f)           // 幅と高さを1:1（正方形）に維持
                        .background(
                            // if-else はKotlinでは式（値を返す）なので、直接引数に使える
                            color = if (isCleared) Color(0xFF388E3C) else Color(0xFF1976D2),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { onProblemSelected(problem, index) }, // タップ時のハンドラ
                    contentAlignment = Alignment.Center // 子要素を中央揃え
                ) {
                    if (isCleared) {
                        // クリア済み：チェックマークと番号を縦に並べる
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("✓", fontSize = 18.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            Text("${index + 1}", fontSize = 11.sp, color = Color.White)
                        }
                    } else {
                        // 未クリア：問題番号のみ表示
                        Text(
                            text = "${index + 1}",          // 文字列テンプレート（C#の$"{index+1}"に相当）
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

// ---- ゲーム画面 ----

/**
 * チェック結果を表す列挙型。
 * C#の enum と同じ構文で定義する。
 * private なのでこのファイル内でのみ使用可能。
 */
private enum class CheckResult {
    HAS_EMPTY,  // 未入力のマスがある
    HAS_ERROR,  // 間違いがある
    CLEAR       // 全マス正解
}

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
    val context = LocalContext.current // Composable内でContextを取得

    // ヒントマス（最初から数字が埋まっているマス）のフラグ配列。
    // remember { } でリコンポーズをまたいで値を保持する。
    // Array<BooleanArray> はKotlinのプリミティブ配列（Javaのboolean[][]に相当）。
    // C#の bool[,] または bool[][] に相当する。
    val hintCells = remember {
        // Array(9) { r -> ... } は9要素の配列を初期化ラムダで生成する
        Array(9) { r -> BooleanArray(9) { c -> problem.puzzle[r][c] != null } }
        // puzzle[r][c] != null なら最初から埋まっているマス（ヒント）
    }

    // 現在の盤面状態を管理する Observable なリスト。
    // mutableStateListOf はComposeが変更を検知できる MutableList を生成する。
    // 通常のMutableListでは要素の変更をComposeが検知できないため、これを使う。
    val board = remember {
        val saved = loadBoardState(context, problem.id, problem.puzzle) // 保存された状態を読み込む
        // *Array(...) の * はスプレッド演算子（C#の配列展開 ... に相当）。
        // mutableStateListOf(*array) で配列をばらして可変長引数に渡す。
        mutableStateListOf(*Array(9) { r ->
            mutableStateListOf(*Array(9) { c -> saved[r][c] }) // 各行も監視可能なリストにする
        })
    }

    // メモ（仮置き）: セルの座標 -> 数字のセット（最大4個）
    // Map<Pair<Int,Int>, Set<Int>> はC#の Dictionary<(int,int), HashSet<int>> に相当
    var memos by remember { mutableStateOf(mapOf<Pair<Int, Int>, Set<Int>>()) }
    // 現在選択中のセルの座標（null = 未選択）
    var selectedCell by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    // チェック後のエラーセル座標セット（チェックボタン押下時のみ設定される）
    var errorCells by remember { mutableStateOf(emptySet<Pair<Int, Int>>()) }
    // クリアダイアログの表示フラグ
    var showClearDialog by remember { mutableStateOf(false) }
    // 全消去確認ダイアログの表示フラグ
    var showEraseAllDialog by remember { mutableStateOf(false) }
    // Snackbar（Toast的な通知バー）の状態管理オブジェクト
    val snackbarHostState = remember { SnackbarHostState() }
    // コルーチンスコープ。Snackbarの表示はコルーチンで行う必要がある。
    // C#の SynchronizationContext や TaskScheduler に相当する概念。
    val scope = rememberCoroutineScope()

    // 現在の盤面状態を保存するローカル関数
    fun saveBoard() {
        // board（mutableStateListOf）を通常のListに変換して保存
        saveBoardState(context, problem.id, List(9) { r -> List(9) { c -> board[r][c] } })
    }

    /**
     * 盤面の正誤チェックを行い、CheckResult を返すローカル関数。
     * ヒントマス以外のセルをチェックし、空きやエラーを検出する。
     */
    fun check(): CheckResult {
        val errors = mutableSetOf<Pair<Int, Int>>() // エラーセルの座標を収集するセット
        var hasEmpty = false                         // 空マスが存在するかフラグ
        for (r in 0..8) {                           // 0..8 は0〜8の範囲（C#の for (int r=0; r<=8; r++)）
            for (c in 0..8) {
                if (!hintCells[r][c]) {             // ヒントマス（固定）以外をチェック
                    val v = board[r][c]
                    when {
                        v == null -> hasEmpty = true                     // 空マスを発見
                        v != problem.answer[r][c] -> errors.add(Pair(r, c)) // 正解と不一致
                    }
                }
            }
        }
        // when 式で結果を判定して返す
        return when {
            hasEmpty          -> CheckResult.HAS_EMPTY
            errors.isNotEmpty() -> {
                errorCells = errors                 // エラーセルを状態に反映（UI更新トリガー）
                CheckResult.HAS_ERROR
            }
            else -> {
                errorCells = emptySet()             // エラーをクリア
                CheckResult.CLEAR
            }
        }
    }

    // クリアダイアログ（OKを押すと問題一覧に戻る）
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = {},                  // 空ラムダ = ダイアログ外タップで閉じない
            title = { Text("クリア！") },
            text = { Text("おめでとうございます！") },
            confirmButton = {
                TextButton(onClick = { onCleared(problem.id) }) { Text("OK") }
            }
        )
    }

    // 全消去確認ダイアログ
    if (showEraseAllDialog) {
        AlertDialog(
            onDismissRequest = { showEraseAllDialog = false },
            text = { Text("入力した数字を全て消去しますか？") },
            confirmButton = {
                TextButton(onClick = {
                    // ヒントマス以外を全てnull（空白）にする
                    for (r in 0..8) for (c in 0..8) if (!hintCells[r][c]) board[r][c] = null
                    memos = emptyMap()              // メモも全消去
                    errorCells = emptySet()         // エラー表示もクリア
                    saveBoard()                     // 保存（全消去状態を永続化）
                    showEraseAllDialog = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showEraseAllDialog = false }) { Text("キャンセル") }
            }
        )
    }

    // Scaffold はマテリアルデザインの画面骨格コンポーネント。
    // AppBar・FAB・SnackbarHost などの定位置を定義できる。
    // innerPadding は Scaffold が計算したコンテンツ領域のパディング値。
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }, // Snackbar表示エリアを登録
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.systemBars)   // システムUI領域を避ける
    ) { innerPadding ->                                      // innerPadding はScaffoldが提供するパディング値
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)             // ScaffoldのinnerPaddingを適用（重要：これがないとUIが被る）
            .padding(horizontal = 16.dp),      // 左右に16dpの余白
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ヘッダー行：戻るボタン・問題番号・全消去ボタン
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = {
                saveBoard()  // 盤面を保存してから戻る（ナビゲーション前に副作用を実行）
                onBack()
            }) {
                Text("← 戻る", fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.weight(1f)) // 中央の要素を真ん中に固定するスペーサー
            Text(
                text = "${problem.difficulty}　${index + 1}/${total}", // 例: "中級　5/50"
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.weight(1f)) // 右端のボタンを右端に固定するスペーサー
            TextButton(onClick = { showEraseAllDialog = true }) {
                Text("全消去", fontSize = 16.sp, color = Color(0xFFD32F2F)) // 赤文字
            }
        }

        // 数独盤面コンポーネント
        SudokuBoard(
            board = board,
            hintCells = hintCells,
            selectedCell = selectedCell,
            errorCells = errorCells,
            memos = memos,
            onCellClick = { r, c ->
                // 既に選択中のセルを再タップしたら選択解除、それ以外は選択
                selectedCell = if (selectedCell == Pair(r, c)) null else Pair(r, c)
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 数字入力パッドコンポーネント
        NumberPad(
            onNumberClick = { number ->
                // selectedCell?.let { (r, c) -> ... } はC#の if(selectedCell != null) { var (r,c) = selectedCell; ... } に相当
                // ?. はnull安全呼び出し演算子（C#の ?. と同じ）
                // let はnullでない場合だけラムダを実行するスコープ関数
                // (r, c) はPair<Int,Int>の分割宣言（C#の var (r,c) = pair; に相当）
                selectedCell?.let { (r, c) ->
                    if (!hintCells[r][c]) {        // ヒントマスは変更不可
                        board[r][c] = number        // 数字を入力
                        memos = memos - Pair(r, c)  // このセルのメモを消去
                        errorCells = errorCells - Pair(r, c) // エラー表示を消去
                        saveBoard()                 // 入力ごとに自動保存
                    }
                }
            },
            onMemoClick = { number ->
                selectedCell?.let { (r, c) ->
                    // メモはセルが空（board[r][c] == null）のときだけ有効
                    if (!hintCells[r][c] && board[r][c] == null) {
                        val key = Pair(r, c)
                        val cur = memos.getOrDefault(key, emptySet()) // 現在のメモセット
                        // メモのトグル処理：
                        //   - 既に入っていれば削除（cur - number）
                        //   - まだなく4個未満なら追加（cur + number）
                        //   - 4個以上なら変更しない（cur）
                        memos = memos + (key to if (number in cur) cur - number else if (cur.size < 4) cur + number else cur)
                        // to は Pair を作る中置演算子（C#の new KeyValuePair<K,V>(key, value) に相当）
                    }
                }
            },
            onEraseClick = {
                selectedCell?.let { (r, c) ->
                    if (!hintCells[r][c]) {
                        board[r][c] = null          // セルを空白に戻す
                        errorCells = errorCells - Pair(r, c)
                        saveBoard()
                    }
                }
            },
            onMemoEraseClick = {
                selectedCell?.let { (r, c) ->
                    if (!hintCells[r][c]) {
                        memos = memos - Pair(r, c)  // このセルのメモを削除
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 残りの空マス数を計算する
        // sumOf はC#の Sum() に相当する集計関数
        // count { ... } はC#の Count(predicate) に相当
        val emptyCount = (0..8).sumOf { r -> (0..8).count { c -> board[r][c] == null } }

        // チェックボタンと残りマス数を同じBoxに配置（Box = 重ね合わせレイアウト）
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center   // デフォルトは中央
        ) {
            Button(
                onClick = {
                    // check() の戻り値で分岐（when 式を文として使う）
                    when (check()) {
                        CheckResult.HAS_EMPTY -> scope.launch {
                            // コルーチンでSnackbarを表示（非同期処理）
                            // C#の await Task.Run(() => ShowSnackbar()) に相当
                            snackbarHostState.showSnackbar("まだ空白があります")
                        }
                        CheckResult.HAS_ERROR -> scope.launch {
                            snackbarHostState.showSnackbar("間違っている箇所があります")
                        }
                        CheckResult.CLEAR -> showClearDialog = true // クリアダイアログを表示
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF388E3C), // 緑色
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("チェック", fontSize = 16.sp)
            }
            // 残りマス数テキスト（右端に配置）
            Text(
                text = "残り${emptyCount}マス",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                // 全マス埋まったら緑色、それ以外は黒
                color = if (emptyCount == 0) Color(0xFF388E3C) else Color.Black,
                modifier = Modifier.align(Alignment.CenterEnd) // Box内で右端中央に配置
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 操作説明テキスト（灰色で小さく表示）
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
    } // Scaffold の閉じ括弧
}

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
    // 現在選択中のセルの数字を取得（ハイライト表示に使用）
    // let は selectedCell が非null のときのみ実行する（null安全スコープ関数）
    // (r, c) はPairを分割宣言する構文（C#の分割代入 var (r,c) = ... に相当）
    val selectedValue = selectedCell?.let { (r, c) -> board[r][c] }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)               // 正方形を維持（幅=高さ）
            .border(3.dp, Color.Black, RectangleShape) // 外枠を太線で描画
    ) {
        for (row in 0..8) {                // 0〜8行をループ
            Row(
                modifier = Modifier
                    .weight(1f)            // 全9行が均等な高さを占有する（flex: 1 に相当）
                    .fillMaxWidth()
            ) {
                for (col in 0..8) {        // 0〜8列をループ
                    // 同じグループ（行・列・3×3ブロック）かどうかを判定
                    val isSameGroup = selectedCell != null && selectedCell != Pair(row, col) &&
                        (selectedCell.first == row ||                          // 同じ行
                         selectedCell.second == col ||                         // 同じ列
                         (row / 3 == selectedCell.first / 3 && col / 3 == selectedCell.second / 3)) // 同じ3×3ブロック

                    // 選択セルと同じ数字かどうかを判定（ハイライト用）
                    val isSameNumber = selectedCell != null && selectedCell != Pair(row, col) &&
                        selectedValue != null && board[row][col] == selectedValue

                    // 各セルを描画
                    SudokuCell(
                        value = board[row][col],       // セルの数字（null = 空）
                        isHint = hintCells[row][col],  // ヒントマスかどうか
                        isSelected = selectedCell == Pair(row, col), // 選択中かどうか
                        isError = Pair(row, col) in errorCells,      // エラーセルかどうか
                        isSameGroup = isSameGroup,
                        isSameNumber = isSameNumber,
                        memos = memos[Pair(row, col)] ?: emptySet(), // このセルのメモ（なければ空セット）
                        onClick = { onCellClick(row, col) },
                        modifier = Modifier.weight(1f) // 全9列が均等な幅を占有する
                    )
                    // 3列ごとに縦の区切り線を追加（col=2の後、col=5の後）
                    if (col == 2 || col == 5) {
                        Spacer(
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(2.dp)           // 2pxの縦線
                                .background(Color.Black)
                        )
                    }
                }
            }
            // 3行ごとに横の区切り線を追加（row=2の後、row=5の後）
            if (row == 2 || row == 5) {
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)                  // 2pxの横線
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
 * @param value       表示する数字（null = 空マス）
 * @param isHint      ヒントマス（変更不可の固定値）かどうか
 * @param isSelected  現在選択中かどうか
 * @param isError     エラーマスかどうか（チェック後に設定される）
 * @param isSameGroup 選択セルと同じ行・列・ブロックかどうか（薄いハイライト）
 * @param isSameNumber 選択セルと同じ数字かどうか（中くらいのハイライト）
 * @param memos       仮置きした数字のセット（最大4個）
 * @param onClick     タップ時のコールバック
 * @param modifier    外部から追加のModifierを受け取る（weight等を親から指定するため）
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
    modifier: Modifier = Modifier      // デフォルト引数。C#のオプション引数 Modifier modifier = Modifier() に相当。
) {
    // when 式で背景色を決定する（優先順位：選択中 > 同じ数字 > 同じグループ > 通常）
    val bgColor = when {
        isSelected   -> Color(0xFF90CAF9)  // 選択中：薄い青
        isSameNumber -> Color(0xFFBBDEFB)  // 同じ数字：中くらいの青
        isSameGroup  -> Color(0xFFF5F5F5)  // 同じ行・列・ブロック：薄いグレー
        else         -> Color.White        // 通常：白
    }
    Box(
        modifier = modifier               // 親から渡されたmodifier（weight等）を最初に適用
            .fillMaxSize()
            .background(bgColor)
            .border(0.5.dp, Color.Gray)   // セルの境界線（薄いグレー）
            .clickable { onClick() }       // タップイベントハンドラ
    ) {
        if (value != null) {
            // 数字が入っているセルの表示
            Text(
                text = value.toString(),
                fontSize = 18.sp,
                // 選択中またはヒントマスは太字
                fontWeight = if (isHint || isSelected) FontWeight.Bold else FontWeight.Normal,
                color = when {
                    isError                -> Color(0xFFD32F2F)  // エラー：赤
                    isSelected && !isHint  -> Color.White        // 選択中かつユーザー入力：白
                    isHint                 -> Color.Black        // ヒントマス：黒
                    else                   -> Color(0xFF1565C0)  // ユーザー入力：青
                },
                modifier = Modifier.align(Alignment.Center) // Box内で中央揃え
            )
        } else if (memos.isNotEmpty()) {
            // メモ（仮置き）表示：セルが空でメモがある場合
            // メモを昇順ソートしてリストに変換
            val sorted = memos.sorted().toList()
            // 2×2のグリッドレイアウトでメモを表示（最大4個）
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(2.dp),
                verticalArrangement = Arrangement.SpaceEvenly // 縦方向に均等配置
            ) {
                for (memoRow in 0..1) {    // 2行のメモ行をループ
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly // 横方向に均等配置
                    ) {
                        for (memoCol in 0..1) { // 2列のメモ列をループ
                            val idx = memoRow * 2 + memoCol // 0〜3のインデックス
                            Text(
                                // インデックスがメモ数未満なら数字を表示、それ以外は空文字
                                text = if (idx < sorted.size) sorted[idx].toString() else "",
                                fontSize = 12.sp,
                                color = Color(0xFFE65100),  // オレンジ色（仮置きを示す）
                                lineHeight = 13.sp          // 行高さ（小さいフォントで詰める）
                            )
                        }
                    }
                }
            }
        }
        // value == null かつ memos.isEmpty() の場合は何も表示しない（空マス）
    }
}

// ---- 数字入力パッド ----

/**
 * 数字入力パッドのComposable関数。
 * 上段（青）：確定入力ボタン 1〜9 + 消去
 * 下段（橙）：メモ入力ボタン 1〜9 + メモ消去
 *
 * @param onNumberClick   数字ボタンタップ時（確定入力）のコールバック
 * @param onMemoClick     メモボタンタップ時のコールバック
 * @param onEraseClick    消去ボタンタップ時のコールバック（確定数字を削除）
 * @param onMemoEraseClick メモ消去ボタンタップ時のコールバック
 */
@Composable
fun NumberPad(
    onNumberClick: (Int) -> Unit,    // Int -> void 型のコールバック（C#の Action<int>）
    onMemoClick: (Int) -> Unit,
    onEraseClick: () -> Unit,        // void -> void 型のコールバック（C#の Action）
    onMemoEraseClick: () -> Unit
) {
    // Column で2行のボタン列を縦に並べる（上段＝確定、下段＝メモ）
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) { // ボタン行間に4dpの隙間
        // ---- 上段：確定入力ボタン（青） ----
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp) // ボタン間に4dpの隙間
        ) {
            // 1〜9の数字ボタンをループで生成
            for (n in 1..9) {                      // 1..9 はC#の Enumerable.Range(1,9) に相当する範囲
                Button(
                    onClick = { onNumberClick(n) },
                    modifier = Modifier.weight(1f), // 10個のボタンが等幅になるよう weight で分配
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(0.dp), // ボタン内のデフォルトパディングを0にする
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1976D2), // マテリアルブルー
                        contentColor = Color.White
                    )
                ) { Text(n.toString(), fontSize = 16.sp) }
            }
            // 消去ボタン（確定数字を消す）
            Button(
                onClick = onEraseClick,            // 引数なしコールバックを直接渡す（ラムダ不要）
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(0.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1976D2),
                    contentColor = Color.White
                )
            ) { Text("消", fontSize = 16.sp) }
        }

        // ---- 下段：メモ入力ボタン（オレンジ） ----
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // 1〜9のメモボタン
            for (n in 1..9) {
                Button(
                    onClick = { onMemoClick(n) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(0.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE65100), // ディープオレンジ（メモ色）
                        contentColor = Color.White
                    )
                ) { Text(n.toString(), fontSize = 16.sp) }
            }
            // メモ消去ボタン
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
