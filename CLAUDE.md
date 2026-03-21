# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

```bash
./gradlew assembleDebug          # Debug build
./gradlew assembleRelease        # Release build
./gradlew test                   # Unit tests
./gradlew connectedAndroidTest   # Instrumented tests (requires emulator/device)
./gradlew test --tests com.komamon.sudoku.SudokuProblemsTest  # Single test class
./gradlew lint                   # Lint check
./gradlew clean                  # Clean build outputs
./gradlew installDebug           # Install debug APK to connected device
```

## Architecture

Single-Activity Jetpack Compose app with state-based navigation using a sealed class:

```kotlin
sealed class Screen {
    object DifficultySelect : Screen()
    data class ProblemList(val difficulty: String) : Screen()
    data class Game(val problem: SudokuProblem, val index: Int, val total: Int) : Screen()
}
```

All UI lives in `MainActivity.kt` — the composable root `SudokuApp()` owns the `currentScreen` state and renders one of three screens based on it. There is no Navigation Component; navigation is just `currentScreen = Screen.X`.

### Key Files

| File | Role |
|------|------|
| `app/src/main/java/com/komamon/sudoku/MainActivity.kt` | All UI: composables for all three screens + game logic |
| `app/src/main/java/com/komamon/sudoku/SudokuProblems.kt` | Problem data, generation, and SharedPreferences persistence |
| `app/src/test/java/com/komamon/sudoku/SudokuProblemsTest.kt` | Validates all 100 problems have correct solutions |

### Problem Generation

100 problems are generated at startup from 10 hard-coded base solutions (labeled A–J) combined with board transformations (transpose, row/column reversal — 8 combinations) and difficulty-based puzzle masks. Problems are immutable `SudokuProblem(id, difficulty, puzzle, answer)` objects where `puzzle` cells are `Int?` (null = empty).

### State Persistence

SharedPreferences (`sudoku_prefs`) stores:
- `cleared_ids` — Set of completed problem IDs
- `board_<id>` — In-progress board as a flattened 81-character string (0 = empty, 1–9 = value)

Board state is saved on every cell entry and on back navigation.

### Game State

Key state in `GameScreen`:
- `board: Array<Array<Int?>>` — 9×9 mutable grid
- `hintCells: BooleanArray` — 81-element array marking immutable puzzle cells
- `memos: Map<Pair<Int,Int>, Set<Int>>` — Candidate numbers per cell (max 4, displayed in 2×2 layout, orange)
- `errorCells: Set<Pair<Int,Int>>` — Populated only after "Check" button tap; cleared on any edit

### Build Config

- **Namespace:** `com.komamon.sudoku`
- **Compile/Target SDK:** 36, **Min SDK:** 24 (Android 7.0)
- **Language:** Kotlin 2.2.10, Java 11 compatibility
- **UI:** Jetpack Compose with Material3 (BOM 2024.09.00)
- Dependency versions managed via `gradle/libs.versions.toml`

## 開発ルール
- コミットメッセージは日本語で書く
- コミット前にビルドエラーがないか確認する
- パッケージ名はcom.komamon.sudokuで固定

## 作業ルール
- 作業完了後は必ず/usageでトークン使用量を確認する

## 今後の予定
- Google Play申請準備中
- バーチャルオフィスの住所取得後に申請予定
