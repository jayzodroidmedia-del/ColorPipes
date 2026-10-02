package com.color.pipe.puzzle

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import java.util.Random
import kotlin.math.abs

data class FlowPoint(val row: Int, val col: Int)

data class FlowColorPair(
    val id: Int,
    val color: Color,
    val darkGlow: Color,
    val name: String,
    val p1: FlowPoint,
    val p2: FlowPoint,
    val solution: List<FlowPoint>
)

data class FlowLevel(
    val level: Int,
    val gridSize: Int,
    val pairs: List<FlowColorPair>
)

object FlowPuzzleLevels {
    private val COLOR_PALETTE = listOf(
        Pair(Color(0xFF0055FF), Color(0xFF002288)), // 1. Blue
        Pair(Color(0xFFFF0000), Color(0xFF880000)), // 2. Red
        Pair(Color(0xFFFFE500), Color(0xFF887700)), // 3. Yellow
        Pair(Color(0xFFFF8800), Color(0xFF884400)), // 4. Orange
        Pair(Color(0xFF00AA22), Color(0xFF004411)), // 5. Green
        Pair(Color(0xFF00D2D3), Color(0xFF005555)), // 6. Cyan
        Pair(Color(0xFFA55EEA), Color(0xFF4B1B7A)), // 7. Purple
        Pair(Color(0xFFFF5252), Color(0xFF7A1B1B)), // 8. Pink
        Pair(Color(0xFF26DE81), Color(0xFF105A32)), // 9. Mint
        Pair(Color(0xFFFD9644), Color(0xFF6B3A10)), // 10. Amber
        Pair(Color(0xFF45AAF2), Color(0xFF1B4E75)), // 11. Sky Blue
        Pair(Color(0xFFE056FD), Color(0xFF5D1E6E))  // 12. Lavender
    )

    private val COLOR_NAMES = listOf(
        "Blue", "Red", "Yellow", "Orange", "Green",
        "Cyan", "Purple", "Pink", "Mint", "Amber", "SkyBlue", "Lavender"
    )

    fun getLevel(level: Int): FlowLevel {
        val safeLevel = level.coerceIn(1, 11178)

        // Level 1: Exact layout matching user reference screenshot
        if (safeLevel == 1) {
            return FlowLevel(
                level = 1,
                gridSize = 5,
                pairs = listOf(
                    FlowColorPair(
                        id = 1,
                        color = Color(0xFF0055FF),
                        darkGlow = Color(0xFF002288),
                        name = "Blue",
                        p1 = FlowPoint(0, 0),
                        p2 = FlowPoint(4, 1),
                        solution = listOf(
                            FlowPoint(0, 0), FlowPoint(1, 0), FlowPoint(2, 0),
                            FlowPoint(3, 0), FlowPoint(4, 0), FlowPoint(4, 1)
                        )
                    ),
                    FlowColorPair(
                        id = 2,
                        color = Color(0xFFFF0000),
                        darkGlow = Color(0xFF880000),
                        name = "Red",
                        p1 = FlowPoint(0, 3),
                        p2 = FlowPoint(3, 1),
                        solution = listOf(
                            FlowPoint(0, 3), FlowPoint(0, 2), FlowPoint(0, 1),
                            FlowPoint(1, 1), FlowPoint(2, 1), FlowPoint(3, 1)
                        )
                    ),
                    FlowColorPair(
                        id = 3,
                        color = Color(0xFFFFE500),
                        darkGlow = Color(0xFF887700),
                        name = "Yellow",
                        p1 = FlowPoint(1, 3),
                        p2 = FlowPoint(2, 2),
                        solution = listOf(
                            FlowPoint(1, 3), FlowPoint(1, 2), FlowPoint(2, 2)
                        )
                    ),
                    FlowColorPair(
                        id = 4,
                        color = Color(0xFFFF8800),
                        darkGlow = Color(0xFF884400),
                        name = "Orange",
                        p1 = FlowPoint(0, 4),
                        p2 = FlowPoint(3, 2),
                        solution = listOf(
                            FlowPoint(0, 4), FlowPoint(1, 4), FlowPoint(2, 4),
                            FlowPoint(2, 3), FlowPoint(3, 3), FlowPoint(3, 2)
                        )
                    ),
                    FlowColorPair(
                        id = 5,
                        color = Color(0xFF00AA22),
                        darkGlow = Color(0xFF004411),
                        name = "Green",
                        p1 = FlowPoint(3, 4),
                        p2 = FlowPoint(4, 2),
                        solution = listOf(
                            FlowPoint(3, 4), FlowPoint(4, 4), FlowPoint(4, 3), FlowPoint(4, 2)
                        )
                    )
                )
            )
        }

        // Exact User Configured Grid Sizes (1 to 11178):
        // Level 1 se 500: 5 × 5 Grid
        // Level 501 se 1000: 6 × 6 Grid 
        // Level 1001 se 2000: 7 × 7 Grid 
        // Level 2001 se 3500: 8 × 8 Grid 
        // Level 3501 se 5000: 9 × 9 Grid
        // Level 5001 se 11178: 10 × 10 Grid
        val gridSize = when {
            safeLevel <= 500 -> 5
            safeLevel <= 1000 -> 6
            safeLevel <= 2000 -> 7
            safeLevel <= 3500 -> 8
            safeLevel <= 5000 -> 9
            else -> 10
        }

        val numColors = when (gridSize) {
            5 -> 5
            6 -> 5 + (safeLevel % 2)
            7 -> 6 + (safeLevel % 2)
            8 -> 7 + (safeLevel % 2)
            9 -> 8 + (safeLevel % 2)
            else -> 9 + (safeLevel % 2)
        }.coerceIn(5, minOf(COLOR_PALETTE.size, gridSize))

        // Deterministic generator with seed
        val pairs = generateSolvablePuzzle(safeLevel, gridSize, numColors)
        return FlowLevel(level = safeLevel, gridSize = gridSize, pairs = pairs)
    }

    private fun generateSolvablePuzzle(level: Int, gridSize: Int, numColors: Int): List<FlowColorPair> {
        val rand = Random(level.toLong() * 999983L + 31337L)
        val dirs = listOf(Pair(-1, 0), Pair(1, 0), Pair(0, -1), Pair(0, 1))

        for (attempt in 0 until 50) {
            val grid = Array(gridSize) { IntArray(gridSize) { -1 } }
            val paths = Array(numColors) { mutableListOf<FlowPoint>() }

            // 1. Seed initial points
            val available = mutableListOf<FlowPoint>()
            for (r in 0 until gridSize) {
                for (c in 0 until gridSize) {
                    available.add(FlowPoint(r, c))
                }
            }
            available.shuffle(rand)

            val seedPoints = available.take(numColors)
            for (i in 0 until numColors) {
                val pt = seedPoints[i]
                grid[pt.row][pt.col] = i
                paths[i].add(pt)
            }

            // 2. Grow paths into self-avoiding snakes
            var changed = true
            var iterations = 0
            while (changed && iterations < 400) {
                changed = false
                iterations++
                val order = (0 until numColors).shuffled(rand)
                for (colorIdx in order) {
                    val path = paths[colorIdx]
                    if (path.isEmpty()) continue

                    val fromEnd = rand.nextBoolean()
                    val cur = if (fromEnd) path.last() else path.first()

                    val shuffledDirs = dirs.shuffled(rand)
                    for (d in shuffledDirs) {
                        val nr = cur.row + d.first
                        val nc = cur.col + d.second

                        if (nr in 0 until gridSize && nc in 0 until gridSize && grid[nr][nc] == -1) {
                            var adjacentCount = 0
                            for (cd in dirs) {
                                val ar = nr + cd.first
                                val ac = nc + cd.second
                                if (ar in 0 until gridSize && ac in 0 until gridSize && grid[ar][ac] == colorIdx) {
                                    adjacentCount++
                                }
                            }

                            if (adjacentCount <= 1) {
                                grid[nr][nc] = colorIdx
                                val newPt = FlowPoint(nr, nc)
                                if (fromEnd) {
                                    path.add(newPt)
                                } else {
                                    path.add(0, newPt)
                                }
                                changed = true
                                break
                            }
                        }
                    }
                }
            }

            // Check if all paths have length >= 2
            val allValid = paths.all { it.size >= 2 }
            val filledCells = paths.sumOf { it.size }

            if (allValid && filledCells >= gridSize * gridSize * 0.85) {
                // Attach remaining unassigned cells if any
                for (r in 0 until gridSize) {
                    for (c in 0 until gridSize) {
                        if (grid[r][c] == -1) {
                            val pt = FlowPoint(r, c)
                            for (d in dirs) {
                                val nr = r + d.first
                                val nc = c + d.second
                                if (nr in 0 until gridSize && nc in 0 until gridSize && grid[nr][nc] != -1) {
                                    val colorIdx = grid[nr][nc]
                                    val path = paths[colorIdx]
                                    if (path.last() == FlowPoint(nr, nc)) {
                                        grid[r][c] = colorIdx
                                        path.add(pt)
                                        break
                                    } else if (path.first() == FlowPoint(nr, nc)) {
                                        grid[r][c] = colorIdx
                                        path.add(0, pt)
                                        break
                                    }
                                }
                            }
                        }
                    }
                }

                // Build color pairs
                return paths.mapIndexed { idx, path ->
                    val colorPair = COLOR_PALETTE[idx % COLOR_PALETTE.size]
                    val colorName = COLOR_NAMES[idx % COLOR_NAMES.size]
                    FlowColorPair(
                        id = idx + 1,
                        color = colorPair.first,
                        darkGlow = colorPair.second,
                        name = colorName,
                        p1 = path.first(),
                        p2 = path.last(),
                        solution = path
                    )
                }
            }
        }

        // Guaranteed fallback: Snake bands across grid
        return generateFallbackSnakePuzzle(level, gridSize, numColors)
    }

    private fun generateFallbackSnakePuzzle(level: Int, gridSize: Int, numColors: Int): List<FlowColorPair> {
        val result = mutableListOf<FlowColorPair>()
        var currentColor = 0
        var currentPath = mutableListOf<FlowPoint>()

        for (r in 0 until gridSize) {
            val cols = if (r % 2 == 0) (0 until gridSize) else (gridSize - 1 downTo 0)
            for (c in cols) {
                val assignedColor = (r * numColors / gridSize).coerceIn(0, numColors - 1)
                if (assignedColor != currentColor && currentPath.size >= 2) {
                    val colorPair = COLOR_PALETTE[currentColor % COLOR_PALETTE.size]
                    val colorName = COLOR_NAMES[currentColor % COLOR_NAMES.size]
                    result.add(
                        FlowColorPair(
                            id = currentColor + 1,
                            color = colorPair.first,
                            darkGlow = colorPair.second,
                            name = colorName,
                            p1 = currentPath.first(),
                            p2 = currentPath.last(),
                            solution = currentPath
                        )
                    )
                    currentColor = assignedColor
                    currentPath = mutableListOf()
                }
                currentPath.add(FlowPoint(r, c))
            }
        }

        if (currentPath.isNotEmpty()) {
            val colorPair = COLOR_PALETTE[currentColor % COLOR_PALETTE.size]
            val colorName = COLOR_NAMES[currentColor % COLOR_NAMES.size]
            result.add(
                FlowColorPair(
                    id = currentColor + 1,
                    color = colorPair.first,
                    darkGlow = colorPair.second,
                    name = colorName,
                    p1 = currentPath.first(),
                    p2 = currentPath.last(),
                    solution = currentPath
                )
            )
        }

        return result
    }
}

@Composable
fun FlowGameBoard(
    level: Int,
    restartTrigger: Int,
    hintTrigger: Int,
    onMoveMade: () -> Unit,
    onWin: () -> Unit,
    onLifeLost: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val currentLevelData = remember(level) {
        FlowPuzzleLevels.getLevel(level)
    }

    val gridSize = currentLevelData.gridSize
    val pairs = currentLevelData.pairs

    // State of paths drawn for each color id
    val paths = remember(level, restartTrigger) {
        mutableStateMapOf<Int, List<FlowPoint>>()
    }

    var currentDrawingPairId by remember(level, restartTrigger) {
        mutableStateOf<Int?>(null)
    }

    var isWon by remember(level, restartTrigger) {
        mutableStateOf(false)
    }

    // Dynamic Spacing and Corner radius based on Grid Size for optimal fit
    val spacingDp = when {
        gridSize <= 6 -> 6.dp
        gridSize <= 8 -> 4.dp
        else -> 3.dp
    }
    val cornerDp = when {
        gridSize <= 6 -> 8.dp
        gridSize <= 8 -> 6.dp
        else -> 4.dp
    }
    val spacingPx = with(LocalDensity.current) { spacingDp.toPx() }

    fun isPairFullyConnected(pairId: Int, path: List<FlowPoint>): Boolean {
        val pair = pairs.find { it.id == pairId } ?: return false
        return path.isNotEmpty() &&
            ((path.first() == pair.p1 && path.last() == pair.p2) ||
             (path.first() == pair.p2 && path.last() == pair.p1))
    }

    // Win check function: checks if all pairs are connected
    fun checkWinCondition() {
        if (isWon) return
        var allConnected = true
        for (pair in pairs) {
            val p = paths[pair.id] ?: emptyList()
            val isConn = p.isNotEmpty() &&
                ((p.first() == pair.p1 && p.last() == pair.p2) || (p.first() == pair.p2 && p.last() == pair.p1))
            if (!isConn) {
                allConnected = false
                break
            }
        }

        if (allConnected && !isWon) {
            isWon = true
            SoundManager.playLevelWinSound()
            onWin()
        }
    }

    // Handle Hint Trigger
    LaunchedEffect(hintTrigger) {
        if (hintTrigger > 0 && !isWon) {
            val unconnected = pairs.firstOrNull { pair ->
                val p = paths[pair.id] ?: emptyList()
                val isConn = p.isNotEmpty() &&
                    ((p.first() == pair.p1 && p.last() == pair.p2) || (p.first() == pair.p2 && p.last() == pair.p1))
                !isConn
            }
            if (unconnected != null) {
                // Clear any other paths that intersect with the solution path
                for (solCell in unconnected.solution) {
                    for ((id, path) in paths.entries.toList()) {
                        if (id != unconnected.id && path.contains(solCell)) {
                            val cutIdx = path.indexOf(solCell)
                            paths[id] = path.take(cutIdx)
                        }
                    }
                }
                paths[unconnected.id] = unconnected.solution
                SoundManager.playPipeConnectSound()
                onMoveMade()
                checkWinCondition()
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(level, restartTrigger, isWon) {
                if (isWon) return@pointerInput

                detectDragGestures(
                    onDragStart = { offset ->
                        val totalW = size.width
                        val totalH = size.height
                        val cellW = (totalW - (gridSize - 1) * spacingPx) / gridSize
                        val cellH = (totalH - (gridSize - 1) * spacingPx) / gridSize
                        val stepX = cellW + spacingPx
                        val stepY = cellH + spacingPx

                        val col = (offset.x / stepX).toInt().coerceIn(0, gridSize - 1)
                        val row = (offset.y / stepY).toInt().coerceIn(0, gridSize - 1)
                        val touchedPoint = FlowPoint(row, col)

                        val touchedPair = pairs.find { it.p1 == touchedPoint || it.p2 == touchedPoint }
                        if (touchedPair != null) {
                            currentDrawingPairId = touchedPair.id
                            paths[touchedPair.id] = listOf(touchedPoint)
                        } else {
                            // Check if touched existing path of a dot
                            for ((id, path) in paths) {
                                val idx = path.indexOf(touchedPoint)
                                if (idx != -1) {
                                    currentDrawingPairId = id
                                    paths[id] = path.take(idx + 1)
                                    break
                                }
                            }
                        }
                    },
                    onDrag = { change, _ ->
                        val activeId = currentDrawingPairId ?: return@detectDragGestures
                        val pair = pairs.find { it.id == activeId } ?: return@detectDragGestures
                        val currentPath = paths[activeId] ?: return@detectDragGestures

                        val totalW = size.width
                        val totalH = size.height
                        val cellW = (totalW - (gridSize - 1) * spacingPx) / gridSize
                        val cellH = (totalH - (gridSize - 1) * spacingPx) / gridSize
                        val stepX = cellW + spacingPx
                        val stepY = cellH + spacingPx

                        val targetCol = (change.position.x / stepX).toInt().coerceIn(0, gridSize - 1)
                        val targetRow = (change.position.y / stepY).toInt().coerceIn(0, gridSize - 1)
                        val curCell = FlowPoint(targetRow, targetCol)

                        if (currentPath.isNotEmpty() && currentPath.last() != curCell) {
                            val lastCell = currentPath.last()
                            val dr = curCell.row - lastCell.row
                            val dc = curCell.col - lastCell.col

                            // Strictly Orthogonal Movement: Only Left, Right, Up, Down
                            val stepsToTake = mutableListOf<FlowPoint>()
                            if (abs(dr) + abs(dc) == 1) {
                                stepsToTake.add(curCell)
                            } else if (abs(dr) > 0 || abs(dc) > 0) {
                                if (abs(dr) >= abs(dc) && dr != 0) {
                                    val stepR = if (dr > 0) 1 else -1
                                    stepsToTake.add(FlowPoint(lastCell.row + stepR, lastCell.col))
                                } else if (dc != 0) {
                                    val stepC = if (dc > 0) 1 else -1
                                    stepsToTake.add(FlowPoint(lastCell.row, lastCell.col + stepC))
                                }
                            }

                            for (stepCell in stepsToTake) {
                                val activePathNow = paths[activeId] ?: currentPath
                                val lastStep = activePathNow.last()
                                val isAdjacent = (abs(stepCell.row - lastStep.row) + abs(stepCell.col - lastStep.col)) == 1

                                if (isAdjacent) {
                                    val isOtherEndpoint = pairs.any {
                                        it.id != activeId && (it.p1 == stepCell || it.p2 == stepCell)
                                    }

                                    if (!isOtherEndpoint) {
                                        // Disconnect other color if crossing
                                        for ((otherId, otherPath) in paths.entries.toList()) {
                                            if (otherId != activeId && otherPath.contains(stepCell)) {
                                                val wasConnected = isPairFullyConnected(otherId, otherPath)
                                                val cutIdx = otherPath.indexOf(stepCell)
                                                paths[otherId] = otherPath.take(cutIdx)
                                                SoundManager.playPipeBreakSound()
                                                if (wasConnected) {
                                                    onLifeLost()
                                                }
                                            }
                                        }

                                        // Check backtracking
                                        val backIdx = activePathNow.indexOf(stepCell)
                                        if (backIdx != -1) {
                                            paths[activeId] = activePathNow.take(backIdx + 1)
                                        } else {
                                            val isAlreadyConnected = (activePathNow.first() == pair.p1 && activePathNow.last() == pair.p2) ||
                                                    (activePathNow.first() == pair.p2 && activePathNow.last() == pair.p1)

                                            if (!isAlreadyConnected) {
                                                val newPath = activePathNow + stepCell
                                                paths[activeId] = newPath
                                                if (isPairFullyConnected(activeId, newPath)) {
                                                    SoundManager.playPipeConnectSound()
                                                }
                                                checkWinCondition()
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    },
                    onDragEnd = {
                        currentDrawingPairId = null
                        onMoveMade()
                        checkWinCondition()
                    },
                    onDragCancel = {
                        currentDrawingPairId = null
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        // 1. EXACT ORIGINAL GRID BOXES
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(spacingDp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            for (row in 0 until gridSize) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(spacingDp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (col in 0 until gridSize) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .background(
                                    color = Color.White.copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(cornerDp)
                                )
                                .border(
                                    width = 1.dp,
                                    color = Color.White.copy(alpha = 0.25f),
                                    shape = RoundedCornerShape(cornerDp)
                                )
                        )
                    }
                }
            }
        }

        // 2. CANVAS FOR PIPES AND DOTS ON TOP OF THE EXACT GRID
        Canvas(modifier = Modifier.fillMaxSize()) {
            val totalW = size.width
            val totalH = size.height
            val cellW = (totalW - (gridSize - 1) * spacingPx) / gridSize
            val cellH = (totalH - (gridSize - 1) * spacingPx) / gridSize
            val stepX = cellW + spacingPx
            val stepY = cellH + spacingPx

            val pipeStroke = cellW * 0.44f
            val dotRadius = cellW * 0.36f

            // Helper to get center offset of any grid box (row, col)
            fun getCenter(r: Int, c: Int): Offset {
                val cx = c * stepX + cellW / 2f
                val cy = r * stepY + cellH / 2f
                return Offset(cx, cy)
            }

            // Draw Pipes (Connecting paths)
            for (pair in pairs) {
                val path = paths[pair.id] ?: continue
                if (path.size > 1) {
                    val composePath = Path()
                    val firstCenter = getCenter(path[0].row, path[0].col)
                    composePath.moveTo(firstCenter.x, firstCenter.y)

                    for (i in 1 until path.size) {
                        val nextCenter = getCenter(path[i].row, path[i].col)
                        composePath.lineTo(nextCenter.x, nextCenter.y)
                    }

                    // Pipe Stroke
                    drawPath(
                        path = composePath,
                        color = pair.color,
                        style = Stroke(
                            width = pipeStroke,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                }
            }

            // Draw Endpoint Dots (Solid clean circular dots centered in their exact boxes)
            for (pair in pairs) {
                val p1Center = getCenter(pair.p1.row, pair.p1.col)
                val p2Center = getCenter(pair.p2.row, pair.p2.col)

                // Dot 1
                drawCircle(
                    color = pair.color,
                    radius = dotRadius,
                    center = p1Center
                )

                // Dot 2
                drawCircle(
                    color = pair.color,
                    radius = dotRadius,
                    center = p2Center
                )
            }
        }
    }
}
