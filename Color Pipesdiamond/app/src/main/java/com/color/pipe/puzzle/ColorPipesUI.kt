package com.color.pipe.puzzle

import android.accounts.AccountManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import android.content.Intent
import android.content.Context
import android.net.Uri
import androidx.activity.compose.BackHandler
import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.zIndex
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.material3.ripple
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.draw.blur
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.lazy.LazyColumn
import android.os.Environment
import android.os.StatFs
import com.secmtp.sdk.nativead.api.NativeAd
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.secmtp.sdk.nativead.api.ATNativeAdView
import com.secmtp.sdk.nativead.api.ATNativePrepareInfo
import android.widget.FrameLayout
import android.graphics.BitmapFactory
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.color.pipe.puzzle.R
import com.color.pipe.puzzle.model.AppSettingsData
import com.color.pipe.puzzle.model.RewardSetting
import com.onesignal.OneSignal
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import android.util.Log
import com.google.gson.Gson

@Composable
fun Modifier.zoomClickable(
    enabled: Boolean = true,
    zoomScale: Float = 1.08f,
    onClick: () -> Unit
): Modifier {
    val scope = rememberCoroutineScope()
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    var isAnimatingClick by remember { mutableStateOf(false) }
    val isVisualPressed = isPressed || isAnimatingClick

    val scale by animateFloatAsState(
        targetValue = if (isVisualPressed) zoomScale else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "zoomClickScale"
    )

    return this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .clickable(
            interactionSource = interactionSource,
            indication = null,
            enabled = enabled
        ) {
            SoundManager.playClickSound()
            scope.launch {
                isAnimatingClick = true
                kotlinx.coroutines.delay(140)
                isAnimatingClick = false
                kotlinx.coroutines.delay(60)
                onClick()
            }
        }
}

private const val TOTAL_LEVELS = 11178

// --- MODELS ---
enum class AppScreen { Splash, Login, Home, LevelSelect, Quiz, VpnBlocked, Update, RewardCenter, TesterRegister, TesterPending, TesterRejected, TesterDashboard }
enum class PopupType { None, Wrong, Correct, TimeOut, LevelComplete, RewardClaim, PaymentMethodSelect, EmailSubmit, PaymentSuccess, SoundSettings, GameOver, ExitConfirmation, Paused, Tutorial, HintExplanation, SkinShop, TesterDailyReview, TesterRewardClaim, RewardHistory }

data class QuizQuestion(
    val text: String,
    val options: List<String>,
    val correctAnswer: String
)

// --- COLOR PIPES PUZZLE CHEERFUL & JOYFUL CANDY COLORS ---
val MintPurpleBg = Color(0xFF1E293B)
val MintPurpleLight = Color(0xFF334155)
val VibrantGreen = Color(0xFF06D6A0) // Cheerful Candy Mint
val VibrantGreenShadow = Color(0xFF049669)
val VibrantBlue = Color(0xFF00B4D8) // Cheerful Sky Cyan
val VibrantBlueShadow = Color(0xFF0077B6)
val MintWhite = Color(0xFFFFFFFF)
val MintBlackboard = Color(0xFF1E293B)
val SunnyYellow = Color(0xFFFFD166) // Sunshine Candy Yellow
val SunsetOrange = Color(0xFFFF5E7E) // Cheerful Bubblegum Coral
val SkySoft = Color(0xFFF0F8FF)
val TextGray = Color(0xFF94A3B8)
val TextDark = Color(0xFF1E293B)
val CandyPurple = Color(0xFF9D4EDD) // Joyful Grape Violet
val CandyPink = Color(0xFFFF70A6) // Joyful Bubblegum Pink

// --- BEIGE & BROWN PUZZLE DESIGN SYSTEM ---
val BeigeBg = Color(0xFFF5EFEB)
val BeigeCardBg = Color(0xFFFCFBF9)
val BrownText = Color(0xFF4A3525)
val SoftPurple = Color(0xFF8B5CF6)

// --- SLEEK MINIMALIST GLASSMORPHIC COLORS ---
val ObsidianBlack = Color(0xFF030712)
val SpaceSlate = Color(0xFF0F172A)
val NeonCyan = Color(0xFF22D3EE)
val NeonViolet = Color(0xFFC084FC)
val PremiumViolet = Color(0xFF8B5CF6)
val PremiumBlue = Color(0xFF3B82F6)
val GlassBase = Color(0xFF1E293B).copy(alpha = 0.5f)
val GlassBorder = Color.White.copy(alpha = 0.12f)
val GlassShine = Color.White.copy(alpha = 0.08f)


// --- FONTS ---
val LuckiestGuyFontFamily = FontFamily(
    androidx.compose.ui.text.font.Font(R.font.fredoka, FontWeight.Normal),
    androidx.compose.ui.text.font.Font(R.font.fredoka, FontWeight.Medium),
    androidx.compose.ui.text.font.Font(R.font.fredoka, FontWeight.SemiBold),
    androidx.compose.ui.text.font.Font(R.font.fredoka, FontWeight.Bold),
    androidx.compose.ui.text.font.Font(R.font.fredoka, FontWeight.ExtraBold),
    androidx.compose.ui.text.font.Font(R.font.fredoka, FontWeight.Black)
)

val SkyBlue = Color(0xFFEBF4F6)
val SkyBlueDark = Color(0xFFF0F4F8)
val GrassGreen = Color(0xFF34C759)
val DirtBrown = Color(0xFF262F3C)
val DirtBrownDark = Color(0xFF1E293B)

// --- HELPERS ---
fun getColorFromName(name: String): Color {
    return when (name.uppercase()) {
        "RED" -> Color(0xFFFF3B30)
        "BLUE" -> Color(0xFF007AFF)
        "GREEN" -> Color(0xFF34C759)
        "YELLOW" -> Color(0xFFFFCC00)
        "PURPLE" -> Color(0xFFAF52DE)
        "ORANGE" -> Color(0xFFFF9500)
        "PINK" -> Color(0xFFFF2D55)
        "WHITE" -> Color(0xFFFFFFFF)
        "GRAY" -> Color(0xFF8E8E93)
        "BLACK" -> Color(0xFF1C1C1E)
        else -> Color.White
    }
}

fun getQuestionsForLevel(level: Int): List<QuizQuestion> {
    val colors = listOf("RED", "BLUE", "GREEN", "YELLOW", "PURPLE", "ORANGE", "PINK", "WHITE", "GRAY", "BLACK")
    
    return (1..5).map { index ->
        val seed = level * 100 + index
        val random = java.util.Random(seed.toLong())
        
        val textName = colors[random.nextInt(colors.size)]
        var colorName = colors[random.nextInt(colors.size)]
        while (colorName == textName) {
            colorName = colors[random.nextInt(colors.size)]
        }
        
        val optionCount = when {
            level <= 5 -> 4
            else -> 6
        }
        
        val options = mutableSetOf<String>()
        options.add(colorName)
        
        while (options.size < optionCount) {
            val randomOption = colors[random.nextInt(colors.size)]
            options.add(randomOption)
        }
        
        QuizQuestion(
            text = textName,
            options = options.toList().shuffled(random),
            correctAnswer = colorName
        )
    }
}


fun calculateProgressChecksum(
    currentLevel: Int,
    unlockedLevels: Set<Int>,
    completedLevels: Set<Int>,
    stars: Int,
    coins: Int,
    currentStreak: Int
): String {
    val unlockedStr = unlockedLevels.sorted().joinToString(",")
    val completedStr = completedLevels.sorted().joinToString(",")
    val rawData = "$currentLevel|$unlockedStr|$completedStr|$stars|$coins|$currentStreak|ArrowPuzzleProSecretSalt_2026_#$!"
    return try {
        val digest = java.security.MessageDigest.getInstance("MD5")
        val bytes = digest.digest(rawData.toByteArray(Charsets.UTF_8))
        bytes.joinToString("") { "%02x".format(it) }
    } catch (e: Exception) {
        ""
    }
}

// --- MAIN APP CONTAINER ---

@Composable
fun ColorPipesApp() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("user_progress", Context.MODE_PRIVATE) }
    val scope = rememberCoroutineScope()

    var isInternetConnected by remember { mutableStateOf(true) }
    var isSyncingLevel by remember { mutableStateOf(false) }
    var syncPendingLevelNum by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(context) {
        while (true) {
            val hasAccess = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                NetworkUtils.isInternetAvailable(context)
            }
            if (isInternetConnected != hasAccess) {
                isInternetConnected = hasAccess
            }
            kotlinx.coroutines.delay(3000)
        }
    }
    
    var currentScreen by remember { 
        mutableStateOf(if (VpnDetector.isVpnActive(context)) AppScreen.VpnBlocked else AppScreen.Splash) 
    }
    


    var activePopup by remember { mutableStateOf(PopupType.None) }
    
    var showImportantTaskPopup by remember { mutableStateOf(false) }
    var showTaskFailedPopup by remember { mutableStateOf(false) }
    var pendingInstallTask by remember { mutableStateOf<com.color.pipe.puzzle.model.InstallTask?>(null) }
    var isLoadingAd by remember { mutableStateOf(false) }

    var testerStatusData by remember { mutableStateOf<com.color.pipe.puzzle.model.TesterStatusResponse?>(null) }
    var testerActiveReviewDay by remember { mutableIntStateOf(1) }
    var isCheckingTesterStatus by remember { mutableStateOf(false) }

    fun refreshTesterStatus(onDone: () -> Unit = {}) {
        isCheckingTesterStatus = true
        SettingsManager.getTesterStatus(context) { success, status, _ ->
            isCheckingTesterStatus = false
            if (success && status != null) {
                testerStatusData = status
            }
            onDone()
        }
    }

    fun openTesterProgram() {
        val isProgramActive = SettingsManager.settings?.testerProgram?.isActive ?: 1
        refreshTesterStatus {
            val s = testerStatusData
            val isRegistered = s != null && s.registered && s.status != "not_registered"
            if (!isRegistered && isProgramActive == 0) {
                Toast.makeText(context, "Tester Program is currently closed by admin", Toast.LENGTH_LONG).show()
                return@refreshTesterStatus
            }
            if (s == null || !s.registered || s.status == "not_registered") {
                currentScreen = AppScreen.TesterRegister
            } else if (s.status == "pending") {
                currentScreen = AppScreen.TesterPending
            } else if (s.status == "rejected") {
                currentScreen = AppScreen.TesterRejected
            } else {
                currentScreen = AppScreen.TesterDashboard
            }
        }
    }
    
    // Game State
    var score by remember { mutableIntStateOf(0) }
    var lives by remember { mutableIntStateOf(3) }
    var stars by remember { mutableIntStateOf(prefs.getInt("stars", 0)) }
    var currentLevel by remember { 
        mutableIntStateOf(prefs.getInt("current_level", 1)) 
    }
    var highestSyncedLevel by remember { mutableIntStateOf(prefs.getInt("highest_synced_level", 0)) }
    var coins by remember { 
        mutableIntStateOf(
            if (currentLevel > highestSyncedLevel) {
                maxOf(prefs.getInt("coins", 0), currentLevel * 10)
            } else {
                prefs.getInt("coins", 0)
            }
        ) 
    }
    var lastLevelCoinsEarned by remember { mutableIntStateOf(0) }
    var currentLevelStars by remember { mutableIntStateOf(3) }
    var currentLevelMoves by remember { mutableIntStateOf(0) }
    var currentLevelTime by remember { mutableIntStateOf(0) }
    
    var unlockedLevels by remember { 
        mutableStateOf(
            prefs.getStringSet("unlocked_levels", setOf("1"))?.map { it.toInt() }?.toSet() ?: setOf(1)
        ) 
    }
    var completedLevels by remember { 
        mutableStateOf(
            prefs.getStringSet("completed_levels", emptySet())?.map { it.toInt() }?.toSet() ?: emptySet<Int>()
        ) 
    }

    // Dedicated Tester Gameplay Mode State
    var isTesterPlayMode by remember { mutableStateOf(false) }
    var testerPlayingDayNumber by remember { mutableIntStateOf(1) }
    var testerDayStartLevel by remember { mutableIntStateOf(1) }
    var testerDayTargetLevels by remember { mutableIntStateOf(10) }
    var testerDayCompletedLevels by remember { mutableIntStateOf(0) }
    var testerPuzzleLevel by remember { mutableIntStateOf(1) }

    val activePuzzleLevel = if (isTesterPlayMode) testerPuzzleLevel else currentLevel

    // Streak Logic
    var currentStreak by remember { mutableIntStateOf(prefs.getInt("current_streak", 1)) }
    
    var selectedBottleSkin by remember { 
        mutableStateOf(
            try {
                BottleSkin.valueOf(prefs.getString("selected_bottle_skin", BottleSkin.Standard.name) ?: BottleSkin.Standard.name)
            } catch (e: Exception) {
                BottleSkin.Standard
            }
        ) 
    }
    var unlockedBottleSkins by remember { 
        mutableStateOf(
            prefs.getStringSet("unlocked_bottle_skins", setOf(BottleSkin.Standard.name)) ?: setOf(BottleSkin.Standard.name)
        ) 
    }
    
    var selectedBackgroundSkin by remember { 
        mutableStateOf(
            try {
                BackgroundSkin.valueOf(prefs.getString("selected_background_skin", BackgroundSkin.OceanBubble.name) ?: BackgroundSkin.OceanBubble.name)
            } catch (e: Exception) {
                BackgroundSkin.OceanBubble
            }
        ) 
    }
    var unlockedBackgroundSkins by remember { 
        mutableStateOf(
            prefs.getStringSet("unlocked_background_skins", setOf(BackgroundSkin.OceanBubble.name)) ?: setOf(BackgroundSkin.OceanBubble.name)
        ) 
    }
    
    LaunchedEffect(selectedBottleSkin, unlockedBottleSkins, selectedBackgroundSkin, unlockedBackgroundSkins) {
        prefs.edit()
            .putString("selected_bottle_skin", selectedBottleSkin.name)
            .putStringSet("unlocked_bottle_skins", unlockedBottleSkins)
            .putString("selected_background_skin", selectedBackgroundSkin.name)
            .putStringSet("unlocked_background_skins", unlockedBackgroundSkins)
            .apply()
    }
    
    var isTampered by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val storedHash = prefs.getString("progress_hash", null)
        val currentLvlVal = prefs.getInt("current_level", 1)
        val unlockedLvlSet = prefs.getStringSet("unlocked_levels", setOf("1"))?.map { it.toInt() }?.toSet() ?: setOf(1)
        val completedLvlSet = prefs.getStringSet("completed_levels", emptySet())?.map { it.toInt() }?.toSet() ?: emptySet()
        val starsVal = prefs.getInt("stars", 0)
        val coinsVal = prefs.getInt("coins", 0)
        val streakVal = prefs.getInt("current_streak", 1)

        val calculatedHash = calculateProgressChecksum(
            currentLevel = currentLvlVal,
            unlockedLevels = unlockedLvlSet,
            completedLevels = completedLvlSet,
            stars = starsVal,
            coins = coinsVal,
            currentStreak = streakVal
        )

        if (storedHash != null) {
            if (storedHash != calculatedHash) {
                isTampered = true
                Log.e("Security", "Progress tampering detected! Stored: $storedHash, Calculated: $calculatedHash")
            }
        } else {
            // First time running this version, save initial checksum of current local state (no false-positive lockout)
            prefs.edit().putString("progress_hash", calculatedHash).apply()
        }
    }

    LaunchedEffect(Unit) {
        UserManager.syncPendingLevels(context)
        val lastLogin = prefs.getLong("last_login_time", 0L)
        val currentTime = System.currentTimeMillis()
        val calendar = java.util.Calendar.getInstance()
        
        if (lastLogin != 0L) {
            calendar.timeInMillis = lastLogin
            val lastDay = calendar.get(java.util.Calendar.DAY_OF_YEAR)
            val lastYear = calendar.get(java.util.Calendar.YEAR)
            
            calendar.timeInMillis = currentTime
            val currentDay = calendar.get(java.util.Calendar.DAY_OF_YEAR)
            val currentYear = calendar.get(java.util.Calendar.YEAR)
            
            if (currentYear > lastYear || (currentYear == lastYear && currentDay > lastDay)) {
                if (currentYear == lastYear && currentDay == lastDay + 1) {
                    currentStreak = (currentStreak % 7) + 1
                } else {
                    currentStreak = 1
                }
            }
        }
        
        prefs.edit().putLong("last_login_time", currentTime).putInt("current_streak", currentStreak).apply()
    }

    var selectedPaymentMethod by remember { mutableStateOf("") }
    var selectedRewardAmount by remember { mutableIntStateOf(50) }
    var selectedRewardReqLevel by remember { mutableIntStateOf(5) }
    var claimedRewardLevels by remember {
        mutableStateOf(
            prefs.getStringSet("claimed_reward_levels", emptySet())?.map { it.toInt() }?.toSet() ?: emptySet<Int>()
        )
    }

    LaunchedEffect(currentLevel, unlockedLevels, completedLevels, stars, coins, currentStreak, highestSyncedLevel) {
        val newHash = calculateProgressChecksum(
            currentLevel = currentLevel,
            unlockedLevels = unlockedLevels,
            completedLevels = completedLevels,
            stars = stars,
            coins = coins,
            currentStreak = currentStreak
        )
        prefs.edit().apply {
            putInt("current_level", currentLevel)
            putStringSet("unlocked_levels", unlockedLevels.map { it.toString() }.toSet())
            putStringSet("completed_levels", completedLevels.map { it.toString() }.toSet())
            putInt("stars", stars)
            putInt("coins", coins)
            putInt("current_streak", currentStreak)
            putInt("highest_synced_level", highestSyncedLevel)
            putString("progress_hash", newHash)
            apply()
        }
    }

    LaunchedEffect(currentLevel) {
        if (currentLevel > highestSyncedLevel) {
            val minCoins = currentLevel * 10
            if (coins < minCoins) {
                coins = minCoins
            }
            highestSyncedLevel = currentLevel
        }
    }

    var currentQuestionIndex by remember { mutableIntStateOf(0) }
    val questionsData = remember(activePuzzleLevel) { getQuestionsForLevel(activePuzzleLevel) }
    var isTimerPaused by remember { mutableStateOf(false) }
    var restartTrigger by remember { mutableIntStateOf(0) }
    var resumeTrigger by remember { mutableIntStateOf(0) }
    var levelStartTrigger by remember { mutableIntStateOf(0) }
    var isTutorialFromPause by remember { mutableStateOf(false) }

    var gameMode by remember { mutableStateOf(prefs.getString("game_mode", "NORMAL") ?: "NORMAL") }

    LaunchedEffect(currentScreen, isTimerPaused) {
        if (currentScreen == AppScreen.Home || currentScreen == AppScreen.LevelSelect || (currentScreen == AppScreen.Quiz && !isTimerPaused)) {
            if (!SoundManager.hasEnteredHomeScreen) {
                SoundManager.hasEnteredHomeScreen = true
            }
            SoundManager.startMusic()
        } else {
            SoundManager.stopMusic()
        }
    }

    val adminSettings = SettingsManager.settings
    val isServerAvailable = SettingsManager.isServerAvailable

    LaunchedEffect(Unit) {
        SettingsManager.init(context)
    }

    fun startLevelSync(nextLvl: Int) {
        isSyncingLevel = true
        syncPendingLevelNum = nextLvl
        UserManager.syncLevel(context, nextLvl) { success ->
            if (success) {
                isSyncingLevel = false
                syncPendingLevelNum = null
                // Unlock level locally and proceed
                completedLevels = completedLevels + currentLevel
                unlockedLevels = unlockedLevels + nextLvl
                currentLevel = nextLvl
                currentQuestionIndex = 0
                lives = 3
                isTimerPaused = false
                restartTrigger++
                levelStartTrigger++
                currentScreen = AppScreen.Quiz
            } else {
                // If sync failed, but internet is still physically connected, auto-retry after 2 seconds
                if (isSyncingLevel && isInternetConnected) {
                    scope.launch {
                        delay(2000)
                        if (isSyncingLevel && isInternetConnected) {
                            startLevelSync(nextLvl)
                        }
                    }
                }
            }
        }
    }

    var currentShuffledOptions by remember { mutableStateOf<List<String>>(emptyList()) }

    LaunchedEffect(currentQuestionIndex, questionsData, restartTrigger) {
        if (currentQuestionIndex < questionsData.size) {
            currentShuffledOptions = questionsData[currentQuestionIndex].options.shuffled()
        }
    }

    LaunchedEffect(adminSettings, isServerAvailable) {
        adminSettings?.adSettings?.onesignalAppId?.let { appId ->
            if (appId.isNotEmpty()) {
                OneSignal.initWithContext(context, appId)
                try {
                    OneSignal.Notifications.requestPermission(true)
                } catch (e: Exception) {
                    android.util.Log.e("OneSignal", "Error requesting notification permission: ", e)
                }
            }
        }
        if (isServerAvailable) {
            if (adminSettings?.dailyReset == 1) {
                currentLevel = 1
                unlockedLevels = setOf(1)
                completedLevels = emptySet()
                stars = 0
                coins = 0
                
                // Align local sync storage to level 1 to prevent anti-cheat from ignoring updates
                context.getSharedPreferences("sync_prefs", Context.MODE_PRIVATE)
                    .edit()
                    .putInt("max_synced_level", 1)
                    .putStringSet("pending_levels", emptySet())
                    .apply()
            } else {
                adminSettings?.userLevel?.let { serverLevel ->
                    if (serverLevel > 0 && serverLevel != currentLevel) {
                        currentLevel = serverLevel
                        unlockedLevels = (1..serverLevel).toSet()
                        completedLevels = (1 until serverLevel).toSet()
                        
                        // Keep max_synced_level aligned so future syncs are not blocked by anti-cheat
                        context.getSharedPreferences("sync_prefs", Context.MODE_PRIVATE)
                            .edit()
                            .putInt("max_synced_level", serverLevel)
                            .apply()
                    }
                }
            }
        }
    }

    LaunchedEffect(currentScreen) {
        if (currentScreen != AppScreen.VpnBlocked) {
            while (true) {
                val isVpn = VpnDetector.isVpnActive(context)
                val isAdBlock = false // AdBlockDetector.isAdBlockerActive(context)
                if (isVpn || isAdBlock) {
                    currentScreen = AppScreen.VpnBlocked
                    break
                }
                delay(3000)
            }
        }
    }

    LaunchedEffect(Unit) {
        SoundManager.init(context)
    }

    LaunchedEffect(currentScreen) {
        if (currentScreen == AppScreen.Home) {
            SoundManager.startMusic()
        }
    }

    LaunchedEffect(currentScreen, adminSettings) {
        if (currentScreen == AppScreen.Home) {
            val settings = adminSettings?.adSettings
            if (settings != null && settings.updateStatus == 1) {
                val serverVersion = settings.updateVersion
                val packageInfo = try {
                    context.packageManager.getPackageInfo(context.packageName, 0)
                } catch (e: Exception) {
                    null
                }
                val currentVersion = packageInfo?.versionName
                if (serverVersion != null && currentVersion != null && serverVersion != currentVersion) {
                    currentScreen = AppScreen.Update
                }
            }
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) {
                SoundManager.stopMusic()
            } else if (event == Lifecycle.Event.ON_RESUME) {
                if (currentScreen != AppScreen.Splash && currentScreen != AppScreen.Login && currentScreen != AppScreen.VpnBlocked) {
                    SoundManager.startMusic()
                }
                
                val hasActiveTask = prefs.getBoolean("install_task_active", false)
                if (hasActiveTask && (currentScreen == AppScreen.Quiz || currentScreen == AppScreen.LevelSelect)) {
                    prefs.edit().putBoolean("install_task_active", false).apply()
                    
                    val storageBefore = prefs.getLong("install_task_storage_before", 0L)
                    val requiredMb = prefs.getInt("install_task_required_mb", 0)
                    val taskLevel = prefs.getInt("install_task_level", 0)
                    
                    val storageAfter = getFreeInternalStorageMB()
                    val usedMbDifference = storageBefore - storageAfter
                    
                    Log.d("InstallTask", "Check: before=$storageBefore, after=$storageAfter, diff=$usedMbDifference, req=$requiredMb")
                    
                    if (usedMbDifference >= requiredMb) {
                        val nextLvl = taskLevel + 1
                        if (nextLvl <= TOTAL_LEVELS) {
                            UserManager.syncLevel(context, nextLvl)
                            completedLevels = completedLevels + taskLevel
                            unlockedLevels = unlockedLevels + nextLvl
                            currentLevel = nextLvl
                            currentQuestionIndex = 0
                            lives = 3
                            isTimerPaused = false
                            restartTrigger++
                            levelStartTrigger++
                            activePopup = PopupType.None
                            showImportantTaskPopup = false
                            showTaskFailedPopup = false
                            currentScreen = AppScreen.LevelSelect
                            Toast.makeText(context, "Task Successful! Next level unlocked.", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(context, "Task Successful! All $TOTAL_LEVELS levels completed!", Toast.LENGTH_LONG).show()
                            currentScreen = AppScreen.Home
                        }
                    } else {
                        pendingInstallTask = SettingsManager.getInstallTaskForLevel(taskLevel)
                        showTaskFailedPopup = true
                    }
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    fun showAdAndContinue(screenName: String, onFail: () -> Unit = {}, onComplete: () -> Unit) {
        val adType = SettingsManager.getAdTypeForScreen(screenName)
        val activity = context as Activity
        
        if (adType == "app_open" || adType == "splash") {
            AdManager.showSplashAd(activity) {
                onComplete()
            }
            return
        }

        if (adType == "interstitial" || adType == "rewarded") {
            if (!NetworkUtils.isInternetAvailable(activity)) {
                if (adType == "interstitial") {
                    onComplete()
                } else {
                    Toast.makeText(activity, "Network connection unavailable. Please check your internet connection.", Toast.LENGTH_LONG).show()
                    onFail()
                }
                return
            }
            isLoadingAd = true
            AdManager.showAdWithLoading(
                activity = activity,
                adType = adType,
                onAdShowStart = {
                    isLoadingAd = false
                },
                onAdComplete = {
                    isLoadingAd = false
                    onComplete()
                },
                onAdFailed = { errorMsg ->
                    isLoadingAd = false
                    android.util.Log.w("ColorPipesUI", "Ad failed to show ($adType): $errorMsg")
                    if (adType == "interstitial") {
                        onComplete()
                    } else {
                        Toast.makeText(activity, errorMsg, Toast.LENGTH_LONG).show()
                        onFail()
                    }
                }
            )
        } else {
            onComplete()
        }
    }

    if (isTampered) {
        TamperedProgressScreen()
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            // Vibrant Cheerful Background Image globally for the entire app!
            Image(
                painter = painterResource(id = R.drawable.bg),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
            )
            
            // Soft subtle gradient tint for perfect contrast
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.15f)
                            )
                        )
                    )
            )
        Crossfade(targetState = currentScreen, label = "screenTransition") { screen ->
            when (screen) {
                AppScreen.VpnBlocked -> {
                    GenericVpnBlockedScreen(onRetry = {
                        scope.launch {
                            val isVpn = VpnDetector.isVpnActive(context)
                            val isAdBlock = false // AdBlockDetector.isAdBlockerActive(context)
                            if (!isVpn && !isAdBlock) {
                                (context as? Activity)?.recreate()
                            }
                        }
                    })
                }
                AppScreen.Splash -> SplashScreen(selectedBackgroundSkin = selectedBackgroundSkin) {
                    showAdAndContinue("splash_open") {
                        currentScreen = AppScreen.Home
                    }
                }
                AppScreen.Login -> LoginScreen { currentScreen = AppScreen.Home }
                AppScreen.Update -> {
                    val settings = adminSettings?.adSettings
                    UpdateScreen(
                        currentVersion = try {
                            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.0.0"
                        } catch (e: Exception) {
                            "1.0.0"
                        },
                        newVersion = settings?.updateVersion ?: "1.0.0",
                        updateUrl = settings?.updateUrl ?: "https://play.google.com/store/apps/details?id=${context.packageName}",
                        onBack = {
                            (context as? Activity)?.finish()
                        }
                    )
                }
                AppScreen.Home -> {
                    BackHandler { activePopup = PopupType.ExitConfirmation }
                    HomeScreen(
                        stars = stars, coins = coins, currentLevel = currentLevel,
                        currentStreak = currentStreak,
                        selectedBackgroundSkin = selectedBackgroundSkin,
                        onPlayClick = { 
                            showAdAndContinue("home_tap_to_play") {
                                isTesterPlayMode = false
                                currentScreen = AppScreen.LevelSelect 
                            }
                        },
                        onSettingsClick = { activePopup = PopupType.SoundSettings },
                        onRewardsClick = { currentScreen = AppScreen.RewardCenter },
                        onOpenTester = { openTesterProgram() },
                        onSkinsShopClick = { activePopup = PopupType.SkinShop }
                    )
                }
                AppScreen.TesterRegister -> {
                    BackHandler { currentScreen = AppScreen.Home }
                    TesterRegistrationScreen(
                        onBack = { currentScreen = AppScreen.Home },
                        onSuccess = { isInstant ->
                            showAdAndContinue("tester_form_submit") {
                                if (isInstant) {
                                    refreshTesterStatus { currentScreen = AppScreen.TesterDashboard }
                                } else {
                                    currentScreen = AppScreen.TesterPending
                                }
                            }
                        }
                    )
                }
                AppScreen.TesterPending -> {
                    BackHandler { currentScreen = AppScreen.Home }
                    TesterPendingScreen(
                        onBack = { currentScreen = AppScreen.Home },
                        onRefresh = {
                            showAdAndContinue("tester_refresh") {
                                refreshTesterStatus {
                                    val st = testerStatusData?.status
                                    if (st == "active") {
                                        currentScreen = AppScreen.TesterDashboard
                                    } else if (st == "rejected") {
                                        currentScreen = AppScreen.TesterRejected
                                    }
                                }
                            }
                        }
                    )
                }
                AppScreen.TesterRejected -> {
                    BackHandler { currentScreen = AppScreen.Home }
                    TesterRejectedScreen(
                        onBack = { currentScreen = AppScreen.Home },
                        onReapply = {
                            currentScreen = AppScreen.TesterRegister
                        },
                        onRefresh = {
                            showAdAndContinue("tester_refresh") {
                                refreshTesterStatus {
                                    val st = testerStatusData?.status
                                    if (st == "active") {
                                        currentScreen = AppScreen.TesterDashboard
                                    } else if (st == "pending") {
                                        currentScreen = AppScreen.TesterPending
                                    } else if (st == "not_registered" || testerStatusData?.registered == false) {
                                        currentScreen = AppScreen.TesterRegister
                                    }
                                }
                            }
                        }
                    )
                }
                AppScreen.TesterDashboard -> {
                    BackHandler { currentScreen = AppScreen.Home }
                    TesterDashboardScreen(
                        status = testerStatusData,
                        onBack = { currentScreen = AppScreen.Home },
                        onPlayDay = { dayNum, startLvl, currentLvlToPlay, targetLvls, completedLvls ->
                            isTesterPlayMode = true
                            testerPlayingDayNumber = dayNum
                            testerDayStartLevel = startLvl
                            testerDayTargetLevels = targetLvls
                            testerDayCompletedLevels = completedLvls
                            testerPuzzleLevel = currentLvlToPlay
                            currentQuestionIndex = 0
                            lives = 3
                            restartTrigger++
                            levelStartTrigger++
                            currentScreen = AppScreen.Quiz
                            isTutorialFromPause = false
                            if (testerPuzzleLevel == 1) {
                                isTimerPaused = true
                                activePopup = PopupType.Tutorial
                            } else {
                                isTimerPaused = false
                                activePopup = PopupType.None
                            }
                        },
                        onSubmitReviewClick = { dayNum ->
                            testerActiveReviewDay = dayNum
                            activePopup = PopupType.TesterDailyReview
                        },
                        onClaimRewardClick = {
                            activePopup = PopupType.TesterRewardClaim
                        },
                        onOpenHistoryClick = {
                            activePopup = PopupType.RewardHistory
                        },
                        onRestartCycle = {
                            showAdAndContinue("tester_rejoin_cycle") {
                                testerStatusData = testerStatusData?.copy(
                                    isFinished = false,
                                    currentCycle = (testerStatusData?.currentCycle ?: 1) + 1,
                                    currentDay = 1,
                                    isTodayCompleted = false,
                                    lastCompletedDay = 0,
                                    hasClaimed = false,
                                    claimInfo = null
                                )
                                SettingsManager.resetTesterCycle(context) { success, _ ->
                                    android.os.Handler(android.os.Looper.getMainLooper()).post {
                                        refreshTesterStatus()
                                        if (success) {
                                            Toast.makeText(context, "New Round Started! Welcome to Day 1!", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            }
                        },
                        onEndTesterProgram = {
                            showAdAndContinue("tester_end_program") {
                                testerStatusData = testerStatusData?.copy(isFinished = true)
                                SettingsManager.endTesterProgram(context) { success, _ ->
                                    android.os.Handler(android.os.Looper.getMainLooper()).post {
                                        refreshTesterStatus()
                                        if (success) {
                                            Toast.makeText(context, "Tester Program Exited.", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            }
                        },
                        onRefresh = {
                            showAdAndContinue("tester_refresh") {
                                refreshTesterStatus()
                            }
                        }
                    )
                }
                AppScreen.LevelSelect -> {
                    BackHandler { currentScreen = AppScreen.Home }
                    LevelSelectScreen(
                        userLevel = currentLevel,
                        stars = stars,
                        coins = coins,
                        unlockedLevels = unlockedLevels, completedLevels = completedLevels,
                        selectedBackgroundSkin = selectedBackgroundSkin,
                        onLevelClick = { level ->
                            isTesterPlayMode = false
                            currentLevel = level
                            currentQuestionIndex = 0
                            lives = 3
                            restartTrigger++
                            levelStartTrigger++
                            currentScreen = AppScreen.Quiz
                            isTutorialFromPause = false
                            if (level == 1) {
                                isTimerPaused = true
                                activePopup = PopupType.Tutorial
                            } else {
                                isTimerPaused = false
                                activePopup = PopupType.None
                            }
                        },
                        onBack = { currentScreen = AppScreen.Home },
                        onRewardClaim = { currentScreen = AppScreen.RewardCenter },
                        claimedRewardLevels = claimedRewardLevels
                    )
                }
                AppScreen.RewardCenter -> {
                    BackHandler { currentScreen = AppScreen.Home }
                    RewardCenterScreen(
                        userCurrentLevel = maxOf(
                            currentLevel,
                            completedLevels.size,
                            completedLevels.maxOrNull() ?: 0,
                            (currentLevel - 1).coerceAtLeast(1)
                        ),
                        claimedRewardLevels = claimedRewardLevels,
                        rewardSettings = SettingsManager.settings?.rewardSettings ?: adminSettings?.rewardSettings ?: SettingsManager.getRewardSettings(),
                        onBack = { currentScreen = AppScreen.Home },
                        onClaimMethodSelected = { method, amount, reqLevel ->
                            val apiMethod = when(method) {
                                "Amazon Voucher", "Amazon Pay" -> "amazon"
                                "Google Play Code", "Google Play" -> "google_play"
                                "Free Fire Diamonds", "FF Diamond" -> "ff_diamond"
                                "UPI / Paytm", "UPI" -> "upi"
                                else -> method.lowercase()
                            }
                            selectedPaymentMethod = apiMethod
                            selectedRewardAmount = amount
                            selectedRewardReqLevel = reqLevel
                            activePopup = PopupType.EmailSubmit
                        }
                    )
                }
                AppScreen.Quiz -> {
                    BackHandler { 
                        currentScreen = if (isTesterPlayMode) AppScreen.TesterDashboard else AppScreen.LevelSelect
                        if (isTesterPlayMode) isTesterPlayMode = false
                    }
                    ArrowPuzzleScreen(
                        level = activePuzzleLevel,
                        coins = coins,
                        onCoinsChange = { coins = it },
                        restartTrigger = restartTrigger,
                        resumeTrigger = resumeTrigger,
                        selectedBackgroundSkin = selectedBackgroundSkin,
                        onBack = { 
                            currentScreen = if (isTesterPlayMode) AppScreen.TesterDashboard else AppScreen.LevelSelect
                            if (isTesterPlayMode) isTesterPlayMode = false
                        },
                        onPauseClick = {
                            isTimerPaused = true
                            activePopup = PopupType.Paused
                        },
                        onWin = { starsEarned, moves, timeInSeconds ->
                            isTimerPaused = true
                            stars += starsEarned
                            currentLevelStars = starsEarned
                            currentLevelMoves = moves
                            currentLevelTime = timeInSeconds

                            if (isTesterPlayMode) {
                                testerDayCompletedLevels++
                                SettingsManager.syncTesterLevel(context, testerPlayingDayNumber, testerPuzzleLevel) { success, newCompleted, _, _, _ ->
                                    if (success && newCompleted > 0) {
                                        testerDayCompletedLevels = newCompleted
                                        refreshTesterStatus()
                                    }
                                }
                            } else {
                                val isFirstTime = !completedLevels.contains(currentLevel)
                                val coinsAwarded = if (isFirstTime) 10 else 0
                                coins += coinsAwarded
                                lastLevelCoinsEarned = coinsAwarded
                                if (isFirstTime) {
                                    completedLevels = completedLevels + currentLevel
                                }
                            }
                            activePopup = PopupType.LevelComplete
                        },
                        onGameOver = {
                            isTimerPaused = true
                            activePopup = PopupType.GameOver
                        },
                        skin = selectedBottleSkin,
                        isTimerPaused = isTimerPaused,
                        onShowAd = { screenName, onFail, onComplete ->
                            showAdAndContinue(screenName, onFail, onComplete)
                        }
                    )
                }
            }
        }

        // Popup Overlays
        AnimatedVisibility(
            visible = activePopup != PopupType.None,
            enter = fadeIn() + scaleIn(initialScale = 0.8f),
            exit = fadeOut() + scaleOut(targetScale = 0.8f)
        ) {
            BackHandler(enabled = true) { /* Block */ }
            val popup = activePopup
            Box(modifier = Modifier.fillMaxSize().clickable(enabled = false) {}) {
                when (popup) {
                    PopupType.Correct -> CorrectAnswerPopup(
                        onNext = {
                            activePopup = PopupType.None
                            if (currentQuestionIndex < questionsData.size - 1) { currentQuestionIndex++; isTimerPaused = false }
                            else { activePopup = PopupType.LevelComplete }
                        }
                    )
                    PopupType.Wrong -> WrongAnswerPopup(
                        onRetry = { activePopup = PopupType.None; isTimerPaused = false; restartTrigger++ },
                        onBack = { activePopup = PopupType.None; currentScreen = AppScreen.LevelSelect }
                    )
                    PopupType.TimeOut -> TimeOutPopup(
                        onRetry = { activePopup = PopupType.None; isTimerPaused = false; restartTrigger++ },
                        onBack = { activePopup = PopupType.None; currentScreen = AppScreen.LevelSelect }
                    )
                    PopupType.GameOver -> {
                        GameOverPopup(
                            score = currentQuestionIndex * 100,
                            onWatchAd = {
                                activePopup = PopupType.None
                                isLoadingAd = true
                                showAdAndContinue("free_lives_rewarded",
                                    onFail = {
                                        isLoadingAd = false
                                        activePopup = PopupType.GameOver
                                    }
                                ) {
                                    lives = 3
                                    isTimerPaused = false
                                    resumeTrigger++
                                    scope.launch {
                                        delay(800)
                                        isLoadingAd = false
                                    }
                                }
                            },
                            onRestart = { 
                                lives = 3; currentQuestionIndex = 0; activePopup = PopupType.None; isTimerPaused = false; restartTrigger++; levelStartTrigger++
                            },
                            onBack = { 
                                activePopup = PopupType.None
                                currentScreen = if (isTesterPlayMode) AppScreen.TesterDashboard else AppScreen.LevelSelect
                                if (isTesterPlayMode) isTesterPlayMode = false
                            }
                        )
                    }
                    PopupType.Paused -> PausedPopup(
                        onResume = { activePopup = PopupType.None; isTimerPaused = false },
                        onRestart = {
                            activePopup = PopupType.None
                            lives = 3
                            currentQuestionIndex = 0
                            isTimerPaused = false
                            restartTrigger++
                            levelStartTrigger++
                        },
                        onTutorial = {
                            isTutorialFromPause = true
                            activePopup = PopupType.Tutorial
                        },
                        onQuit = { 
                            activePopup = PopupType.None
                            currentScreen = if (isTesterPlayMode) AppScreen.TesterDashboard else AppScreen.LevelSelect
                            if (isTesterPlayMode) isTesterPlayMode = false
                        }
                    )
                    PopupType.Tutorial -> TutorialPopup(
                        selectedBackgroundSkin = selectedBackgroundSkin,
                        onClose = {
                            if (isTutorialFromPause) {
                                isTutorialFromPause = false
                                activePopup = PopupType.Paused
                            } else {
                                activePopup = PopupType.None
                                isTimerPaused = false
                            }
                        }
                    )
                    PopupType.HintExplanation -> {
                        if (currentQuestionIndex < questionsData.size) {
                            val currentQuestion = questionsData[currentQuestionIndex]
                            val correctOptionIndex = currentShuffledOptions.indexOf(currentQuestion.correctAnswer).let { if (it == -1) 1 else it + 1 }
                            HintExplanationPopup(
                                optionIndex = correctOptionIndex,
                                colorName = currentQuestion.correctAnswer,
                                onClose = {
                                    activePopup = PopupType.None
                                    isTimerPaused = false
                                }
                            )
                        } else {
                            activePopup = PopupType.None
                            isTimerPaused = false
                        }
                    }
                    PopupType.LevelComplete -> {
                        val milestoneRewardForThisLevel = adminSettings?.rewardSettings
                            ?.firstOrNull { it.status == 1 && it.requiredLevel <= activePuzzleLevel && it.requiredLevel !in claimedRewardLevels }

                        val shouldShowNextLevelAd = if (isTesterPlayMode) {
                            val adType = SettingsManager.getAdTypeForScreen("tester_next_level")
                            adType == "interstitial" || adType == "rewarded"
                        } else {
                            val adType = SettingsManager.getAdTypeForScreen("next_level")
                            (activePuzzleLevel > 2) && (adType == "interstitial" || adType == "rewarded")
                        }

                        LevelCompletePopup(
                            level = activePuzzleLevel,
                            showAdBadge = shouldShowNextLevelAd,
                            starsEarned = currentLevelStars,
                            moves = currentLevelMoves,
                            timeInSeconds = currentLevelTime,
                            coinsEarned = lastLevelCoinsEarned,
                            activeMilestoneReward = milestoneRewardForThisLevel,
                            onClaimReward = {
                                activePopup = PopupType.None
                                isTimerPaused = false
                                currentScreen = AppScreen.RewardCenter
                            },
                            onHome = { 
                                activePopup = PopupType.None
                                isTimerPaused = false
                                currentScreen = if (isTesterPlayMode) AppScreen.TesterDashboard else AppScreen.LevelSelect
                                if (isTesterPlayMode) isTesterPlayMode = false
                            },
                            onRestart = {
                                activePopup = PopupType.None
                                lives = 3
                                currentQuestionIndex = 0
                                isTimerPaused = false
                                restartTrigger++
                                levelStartTrigger++
                            },
                        onNext = {
                            if (isTesterPlayMode) {
                                activePopup = PopupType.None
                                showAdAndContinue("tester_next_level") {
                                    val nextTesterLvl = testerDayStartLevel + testerDayCompletedLevels
                                    val dayMaxLvl = testerDayStartLevel + testerDayTargetLevels - 1
                                    if (testerDayCompletedLevels >= testerDayTargetLevels || testerPuzzleLevel >= dayMaxLvl) {
                                        Toast.makeText(context, "Day $testerPlayingDayNumber Mission Complete! Submit your daily bug review.", Toast.LENGTH_LONG).show()
                                        currentScreen = AppScreen.TesterDashboard
                                        isTesterPlayMode = false
                                        refreshTesterStatus()
                                    } else {
                                        testerPuzzleLevel = nextTesterLvl
                                        currentQuestionIndex = 0
                                        lives = 3
                                        isTimerPaused = false
                                        restartTrigger++
                                        levelStartTrigger++
                                        currentScreen = AppScreen.Quiz
                                    }
                                }
                            } else {
                                val nextLvl = currentLevel + 1
                                val bypassAd = false
                                
                                if (bypassAd) {
                                    activePopup = PopupType.None
                                    if (nextLvl <= TOTAL_LEVELS) {
                                        if (isServerAvailable) {
                                            startLevelSync(nextLvl)
                                        } else {
                                            UserManager.syncLevel(context, nextLvl)
                                            completedLevels = completedLevels + currentLevel
                                            unlockedLevels = unlockedLevels + nextLvl
                                            currentLevel = nextLvl
                                            currentQuestionIndex = 0
                                            lives = 3
                                            isTimerPaused = false
                                            restartTrigger++
                                            levelStartTrigger++
                                            currentScreen = AppScreen.Quiz
                                        }
                                    } else {
                                        Toast.makeText(context, "Congratulations! You have completed all $TOTAL_LEVELS levels!", Toast.LENGTH_LONG).show()
                                        currentScreen = AppScreen.Home
                                    }
                                } else {
                                    val installTask = SettingsManager.getInstallTaskForLevel(currentLevel)
                                    if (installTask != null) {
                                        pendingInstallTask = installTask
                                        activePopup = PopupType.None
                                        showImportantTaskPopup = true
                                    } else {
                                        activePopup = PopupType.None
                                        isLoadingAd = true
                                        showAdAndContinue("level_completed_interstitial",
                                            onFail = {
                                                isLoadingAd = false
                                                activePopup = PopupType.LevelComplete
                                            }
                                        ) {
                                             if (nextLvl <= TOTAL_LEVELS) {
                                                 if (isServerAvailable) {
                                                     startLevelSync(nextLvl)
                                                 } else {
                                                     UserManager.syncLevel(context, nextLvl)
                                                     completedLevels = completedLevels + currentLevel
                                                     unlockedLevels = unlockedLevels + nextLvl
                                                     currentLevel = nextLvl
                                                     currentQuestionIndex = 0
                                                     lives = 3
                                                     isTimerPaused = false
                                                     restartTrigger++
                                                     levelStartTrigger++
                                                     currentScreen = AppScreen.Quiz
                                                 }
                                             } else {
                                                 Toast.makeText(context, "Congratulations! You have completed all $TOTAL_LEVELS levels!", Toast.LENGTH_LONG).show()
                                                 currentScreen = AppScreen.Home
                                             }
                                             scope.launch {
                                                 delay(800)
                                                 isLoadingAd = false
                                             }
                                        }
                                    }
                                }
                            }
                        }
                    )
                }
                PopupType.SkinShop -> { /* Removed bottle skins shop */ }
                    PopupType.RewardClaim -> {
                        val activeRewardForClaim = adminSettings?.rewardSettings
                            ?.filter { it.status == 1 && it.requiredLevel !in claimedRewardLevels }
                            ?.minByOrNull { it.requiredLevel }
                        val rewardAmount = activeRewardForClaim?.rewardAmount ?: 1000
                        RewardClaimPopup(
                            amount = rewardAmount,
                            onClaim = { activePopup = PopupType.PaymentMethodSelect }
                        )
                    }
                    PopupType.PaymentMethodSelect -> PaymentMethodSelectPopup(
                        onMethodSelected = { method ->
                            val apiMethod = when(method) {
                                "Amazon Voucher" -> "amazon"
                                "Google Play" -> "google_play"
                                "FF Diamond" -> "ff_diamond"
                                "UPI" -> "upi"
                                else -> method.lowercase()
                            }
                            selectedPaymentMethod = apiMethod
                            activePopup = PopupType.EmailSubmit
                        },
                        onClose = { activePopup = PopupType.None }
                    )
                    PopupType.EmailSubmit -> EmailSubmitPopup(
                        method = selectedPaymentMethod,
                        rewardAmount = selectedRewardAmount,
                        onSubmit = { detail ->
                            val activity = context as Activity
                            val rewardAmount = selectedRewardAmount
                            val claimedLevel = selectedRewardReqLevel
                            UserManager.claimReward(context, selectedPaymentMethod, detail, rewardAmount) { status ->
                                activity.runOnUiThread {
                                    when (status) {
                                        "success" -> {
                                            if (claimedLevel > 0) {
                                                claimedRewardLevels = claimedRewardLevels + claimedLevel
                                                prefs.edit().putStringSet("claimed_reward_levels", claimedRewardLevels.map { it.toString() }.toSet()).apply()
                                            }
                                            activePopup = PopupType.PaymentSuccess
                                        }
                                        "already_requested" -> {
                                            Toast.makeText(context, "You already have a pending request", Toast.LENGTH_LONG).show()
                                        }
                                        else -> {
                                            Toast.makeText(context, "Error submitting request. Try again.", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                }
                            }
                        },
                        onBack = { activePopup = PopupType.None }
                    )
                    PopupType.PaymentSuccess -> PaymentSuccessPopup(onDone = { 
                        activePopup = PopupType.None
                        currentScreen = AppScreen.Home 
                    })
                    PopupType.SoundSettings -> SoundSettingsPopup(
                        showResetButton = (currentLevel >= 5000 || completedLevels.size >= 5000),
                        onResetLevels = {
                            currentLevel = 1
                            unlockedLevels = setOf(1)
                            completedLevels = emptySet()
                            stars = 0
                            coins = 10
                            highestSyncedLevel = 1
                            activePopup = PopupType.None
                            Toast.makeText(context, "Level progress reset successfully!", Toast.LENGTH_SHORT).show()
                        },
                        onClose = { activePopup = PopupType.None }
                    )
                    PopupType.TesterDailyReview -> TesterDailyReviewPopup(
                        dayNumber = testerActiveReviewDay,
                        onSubmit = { rating, reviewText ->
                            showAdAndContinue("tester_review_submit") {
                                SettingsManager.submitTesterReview(context, testerActiveReviewDay, rating, reviewText) { success, err ->
                                    if (success) {
                                        activePopup = PopupType.None
                                        refreshTesterStatus()
                                        Toast.makeText(context, "Review submitted! Day $testerActiveReviewDay completed. Next day unlocks tomorrow!", Toast.LENGTH_LONG).show()
                                    } else {
                                        Toast.makeText(context, err ?: "Submission failed", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        },
                        onClose = { activePopup = PopupType.None }
                    )
                    PopupType.TesterRewardClaim -> TesterRewardClaimPopup(
                        rewardAmount = testerStatusData?.rewardAmount ?: 150,
                        payoutMethods = testerStatusData?.payoutMethods ?: emptyList(),
                        onUnwrap = { onDone ->
                            showAdAndContinue("tester_claim_reward") {
                                onDone()
                            }
                        },
                        onSubmit = { method, account ->
                            SettingsManager.submitTesterClaim(context, method, account) { success, err ->
                                if (success) {
                                    activePopup = PopupType.None
                                    refreshTesterStatus()
                                    Toast.makeText(context, "🎉 Tester Reward Claim Submitted!", Toast.LENGTH_LONG).show()
                                } else {
                                    Toast.makeText(context, err ?: "Failed to submit claim", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        onClose = { activePopup = PopupType.None }
                    )
                    PopupType.RewardHistory -> RewardHistoryPopup(
                        onClose = { activePopup = PopupType.None }
                    )
                    PopupType.ExitConfirmation -> ExitPopup(onConfirm = { (context as? Activity)?.finish() }, onDismiss = { activePopup = PopupType.None })
                    else -> {}
                }
            }
        }

        if (showImportantTaskPopup && pendingInstallTask != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(enabled = false) {}
            ) {
                ImportantTaskPopup(
                    requiredMb = pendingInstallTask!!.requiredMb,
                    onInstallClick = {
                        val storageBefore = getFreeInternalStorageMB()
                        prefs.edit()
                            .putBoolean("install_task_active", true)
                            .putLong("install_task_storage_before", storageBefore)
                            .putInt("install_task_required_mb", pendingInstallTask!!.requiredMb)
                            .putInt("install_task_level", currentLevel)
                            .apply()
                        
                        val adType = SettingsManager.getAdTypeForScreen("UnlockLevel")
                        val activity = context as Activity
                        if (adType == "rewarded") {
                            AdManager.showRewardedAd(activity, {}, {})
                        } else if (adType == "interstitial") {
                            AdManager.showInterstitialAd(activity) {}
                        }
                        showImportantTaskPopup = false
                    },
                    onCancel = {
                        showImportantTaskPopup = false
                        activePopup = PopupType.None
                        currentScreen = AppScreen.LevelSelect
                    }
                )
            }
        }

        if (showTaskFailedPopup && pendingInstallTask != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(enabled = false) {}
            ) {
                TaskFailedPopup(
                    requiredMb = pendingInstallTask!!.requiredMb,
                    onOkay = {
                        showTaskFailedPopup = false
                        activePopup = PopupType.LevelComplete
                    }
                )
            }
        }

        if (isLoadingAd) {
            BackHandler(enabled = true) { /* Block system back press while loading challenge/ad */ }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.85f))
                    .clickable(enabled = false) {},
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        color = SunnyYellow,
                        strokeWidth = 4.dp
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "LOADING CHALLENGE...",
                        color = MintWhite,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = LuckiestGuyFontFamily,
                        style = TextStyle(shadow = Shadow(Color.Black.copy(alpha = 0.5f), offset = Offset(0f, 3f)))
                    )
                }
            }
        }

        if (isSyncingLevel) {
            BackHandler(enabled = true) { /* Block system back press while loading level */ }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.85f))
                    .clickable(enabled = false) {},
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        color = SunnyYellow,
                        strokeWidth = 4.dp
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "LEVEL LOADING...",
                        color = MintWhite,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = LuckiestGuyFontFamily,
                        style = TextStyle(shadow = Shadow(Color.Black.copy(alpha = 0.5f), offset = Offset(0f, 3f)))
                    )
                }
            }
        }

        // No Internet Overlay
        AnimatedVisibility(
            visible = !isInternetConnected,
            enter = fadeIn(animationSpec = tween(durationMillis = 300)),
            exit = fadeOut(animationSpec = tween(durationMillis = 300))
        ) {
            BackHandler(enabled = true) { /* Block system back button while offline */ }
            NoInternetScreen(
                onRetry = {
                    scope.launch {
                        val hasAccess = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                            NetworkUtils.isInternetAvailable(context)
                        }
                        isInternetConnected = hasAccess
                        if (hasAccess) {
                            Toast.makeText(context, "Internet Connected!", Toast.LENGTH_SHORT).show()
                            if (isSyncingLevel && syncPendingLevelNum != null) {
                                startLevelSync(syncPendingLevelNum!!)
                            }
                        } else {
                            Toast.makeText(context, "Please connect to the internet first.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            )
        }
    }
    }
}

// --- HYPER-VIBRANT 3D REUSABLE COMPONENTS ---

enum class BackgroundSkin(val displayName: String, val price: Int) {
    OceanBubble("Aqua Reef", 0),
    Classic("Deep Space", 300),
    Aurora("Northern Lights", 500),
    CosmicDust("Nebula Dust", 800),
    Jungle("Jungle Canopy", 1000),
    Desert("Dune Oasis", 1200),
    Volcano("Lava Core", 1500),
    Sky("Cloud Valley", 1800),
    Glacier("Frozen Tundra", 2000),
    Cyberpunk("Neon City", 2500)
}

fun getThemeAccentColor(skin: BackgroundSkin): Color {
    return when (skin) {
        BackgroundSkin.OceanBubble -> Color(0xFF00FBFB)
        BackgroundSkin.Aurora -> Color(0xFFFF007F)
        BackgroundSkin.Classic -> Color(0xFF9C27B0)
        BackgroundSkin.CosmicDust -> Color(0xFFE040FB)
        BackgroundSkin.Jungle -> Color(0xFF00FF88)
        BackgroundSkin.Desert -> Color(0xFFFF9100)
        BackgroundSkin.Volcano -> Color(0xFFFF1744)
        BackgroundSkin.Sky -> Color(0xFF29B6F6)
        BackgroundSkin.Glacier -> Color(0xFF80DEEA)
        BackgroundSkin.Cyberpunk -> Color(0xFFFF007F)
    }
}

@Composable
fun ClassicBackgroundOverlay() {
    val infiniteTransition = rememberInfiniteTransition(label = "classic")
    val shootingStarProgress by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(7000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "star"
    )
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        
        // Stars
        val random = java.util.Random(10)
        repeat(40) {
            val sx = random.nextFloat() * w
            val sy = random.nextFloat() * (h * 0.6f)
            val sizeVal = (1f + random.nextFloat() * 1.5f) * density
            val alpha = 0.3f + random.nextFloat() * 0.7f
            drawCircle(Color.White.copy(alpha = alpha), radius = sizeVal, center = Offset(sx, sy))
        }
        
        // Shooting Star
        if (shootingStarProgress < 0.25f) {
            val t = shootingStarProgress / 0.25f
            val startX = w * 0.8f
            val startY = h * 0.1f
            val endX = w * 0.2f
            val endY = h * 0.3f
            val curX = startX + (endX - startX) * t
            val curY = startY + (endY - startY) * t
            drawLine(
                color = Color.White.copy(alpha = (1f - t) * 0.8f),
                start = Offset(curX, curY),
                end = Offset(curX + 30f * density, curY - 10f * density),
                strokeWidth = 1.5f * density
            )
        }
        
        // Far Mountains (Deep Purple silhouette)
        val mountainPath = Path().apply {
            moveTo(0f, h * 0.75f)
            lineTo(0f, h * 0.58f)
            lineTo(w * 0.25f, h * 0.48f)
            lineTo(w * 0.45f, h * 0.55f)
            lineTo(w * 0.75f, h * 0.45f)
            lineTo(w, h * 0.58f)
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        drawPath(mountainPath, Color(0xFF17092F))
        
        // Mid Hills with pine tree silhouettes
        val midHillPath = Path().apply {
            moveTo(0f, h * 0.82f)
            lineTo(0f, h * 0.68f)
            quadraticTo(w * 0.3f, h * 0.62f, w * 0.6f, h * 0.7f)
            quadraticTo(w * 0.8f, h * 0.74f, w, h * 0.68f)
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        drawPath(midHillPath, Color(0xFF100522))
        
        // Draw some simple pine trees (triangles)
        val treeColor = Color(0xFF0A0216)
        val treePositions = listOf(w * 0.1f, w * 0.18f, w * 0.28f, w * 0.75f, w * 0.85f, w * 0.92f)
        treePositions.forEachIndexed { idx, tx ->
            val ty = h * 0.68f + (if (idx % 2 == 0) 10f else -10f) * density
            val treeH = (25f + (idx % 3) * 10f) * density
            val treeW = 12f * density
            val treePath = Path().apply {
                moveTo(tx, ty - treeH)
                lineTo(tx - treeW/2, ty)
                lineTo(tx + treeW/2, ty)
                close()
            }
            drawPath(treePath, treeColor)
        }
        
        // Close Camp Ground
        val campGround = Path().apply {
            moveTo(0f, h * 0.85f)
            quadraticTo(w * 0.5f, h * 0.8f, w, h * 0.85f)
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        drawPath(campGround, Color(0xFF090214))
        
        // Campfire tent & fire details
        val tentX = w * 0.45f
        val tentY = h * 0.84f
        val tentW = 24f * density
        val tentH = 20f * density
        
        // Tent path
        val tentPath = Path().apply {
            moveTo(tentX, tentY - tentH)
            lineTo(tentX - tentW/2, tentY)
            lineTo(tentX + tentW/2, tentY)
            close()
        }
        drawPath(tentPath, Color(0xFF2C105A))
        
        // Campfire glow
        val fireX = tentX + 32f * density
        val fireY = tentY + 2f * density
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFFBBF24).copy(alpha = 0.5f), Color.Transparent),
                center = Offset(fireX, fireY),
                radius = 16f * density
            ),
            radius = 16f * density,
            center = Offset(fireX, fireY)
        )
        // Draw fire logs/ember triangle
        val firePath = Path().apply {
            moveTo(fireX, fireY - 8f * density)
            lineTo(fireX - 6f * density, fireY)
            lineTo(fireX + 6f * density, fireY)
            close()
        }
        drawPath(firePath, Color(0xFFEF4444))
    }
}

@Composable
fun OceanBubbleBackgroundOverlay() {
    val infiniteTransition = rememberInfiniteTransition(label = "moonlitSea")
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(10000, easing = LinearEasing)), label = "wave"
    )
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        
        // Big Moon
        val mx = w * 0.5f
        val my = h * 0.28f
        val mr = 50f * density
        
        // Moon Glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color.White.copy(alpha = 0.2f), Color.Transparent),
                center = Offset(mx, my),
                radius = mr * 2.2f
            ),
            radius = mr * 2.2f,
            center = Offset(mx, my)
        )
        // Moon Body
        drawCircle(Color(0xFFE0F7FA), radius = mr, center = Offset(mx, my))
        
        // Sea/Water Horizon line (bottom half)
        val seaY = h * 0.65f
        
        // Sea reflection glow
        val reflectionBrush = Brush.verticalGradient(
            colors = listOf(Color(0xFF00E5FF).copy(alpha = 0.4f), Color.Transparent),
            startY = seaY,
            endY = h
        )
        
        // Draw animated waves layers
        val waveColor1 = Color(0xFF0A3D54).copy(alpha = 0.8f)
        val waveColor2 = Color(0xFF042130)
        
        val wavePath1 = Path().apply {
            moveTo(0f, seaY)
            var x = 0f
            while (x < w + 50f) {
                val sinVal = kotlin.math.sin(Math.toRadians((x + waveOffset).toDouble())).toFloat()
                lineTo(x, seaY + sinVal * 6f * density)
                x += 10f
            }
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        drawPath(wavePath1, waveColor1)
        
        // Moon reflection lines on water
        drawRect(
            brush = reflectionBrush,
            topLeft = Offset(mx - 30f * density, seaY),
            size = Size(60f * density, h - seaY)
        )
        
        val wavePath2 = Path().apply {
            moveTo(0f, seaY + 12f * density)
            var x = 0f
            while (x < w + 50f) {
                val sinVal = kotlin.math.sin(Math.toRadians((x - waveOffset + 180).toDouble())).toFloat()
                lineTo(x, seaY + 12f * density + sinVal * 8f * density)
                x += 10f
            }
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        drawPath(wavePath2, waveColor2)
        
        // Soft bubbles floating up
        val random = java.util.Random(42)
        repeat(15) { i ->
            val bx = random.nextFloat() * w
            val by = seaY + random.nextFloat() * (h - seaY)
            val br = (2f + random.nextFloat() * 4f) * density
            drawCircle(Color(0xFF00E5FF).copy(alpha = 0.25f), radius = br, center = Offset(bx, by))
        }
    }
}

@Composable
fun AuroraBackgroundOverlay() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        
        // Rising Sun/Moon in the center valley
        val sx = w * 0.5f
        val sy = h * 0.58f
        val sr = 60f * density
        
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFFFEEAA).copy(alpha = 0.25f), Color.Transparent),
                center = Offset(sx, sy),
                radius = sr * 2.5f
            ),
            radius = sr * 2.5f,
            center = Offset(sx, sy)
        )
        drawCircle(Color(0xFFFFF9C4), radius = sr, center = Offset(sx, sy))
        
        // Far hills
        val hillPath1 = Path().apply {
            moveTo(0f, h * 0.72f)
            quadraticTo(w * 0.25f, h * 0.55f, w * 0.5f, h * 0.62f)
            quadraticTo(w * 0.75f, h * 0.52f, w, h * 0.65f)
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        drawPath(hillPath1, Color(0xFF6A1B9A)) // Indigo Purple hill
        
        // Mid hills
        val hillPath2 = Path().apply {
            moveTo(0f, h * 0.78f)
            quadraticTo(w * 0.35f, h * 0.68f, w * 0.7f, h * 0.72f)
            quadraticTo(w * 0.85f, h * 0.75f, w, h * 0.68f)
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        drawPath(hillPath2, Color(0xFF4A148C)) // Dark purple
        
        // Close hills
        val hillPath3 = Path().apply {
            moveTo(0f, h * 0.84f)
            quadraticTo(w * 0.5f, h * 0.75f, w, h * 0.82f)
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        drawPath(hillPath3, Color(0xFF310260)) // Very dark violet
        
        // Pine tree shapes
        val treeColor = Color(0xFF1A003D)
        val trees = listOf(
            Pair(w * 0.08f, h * 0.78f), Pair(w * 0.14f, h * 0.77f), Pair(w * 0.22f, h * 0.79f),
            Pair(w * 0.78f, h * 0.78f), Pair(w * 0.88f, h * 0.81f), Pair(w * 0.94f, h * 0.83f)
        )
        trees.forEachIndexed { i, tree ->
            val tx = tree.first
            val ty = tree.second
            val th = (20f + (i % 3) * 8f) * density
            val tw = 10f * density
            val path = Path().apply {
                moveTo(tx, ty - th)
                lineTo(tx - tw/2, ty)
                lineTo(tx + tw/2, ty)
                close()
            }
            drawPath(path, treeColor)
        }
    }
}

@Composable
fun CosmicBackgroundOverlay() {
    val infiniteTransition = rememberInfiniteTransition(label = "clouds")
    val cloudOffset by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 100f,
        animationSpec = infiniteRepeatable(tween(25000, easing = LinearEasing)), label = "cloud"
    )
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        
        // Stars/particles
        val random = java.util.Random(55)
        repeat(25) {
            val sx = random.nextFloat() * w
            val sy = random.nextFloat() * (h * 0.7f)
            drawCircle(Color.White.copy(alpha = 0.4f), radius = 1.5f * density, center = Offset(sx, sy))
        }
        
        // Far hill silhouette
        val farHill = Path().apply {
            moveTo(0f, h * 0.75f)
            quadraticTo(w * 0.3f, h * 0.65f, w * 0.6f, h * 0.72f)
            quadraticTo(w * 0.8f, h * 0.75f, w, h * 0.68f)
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        drawPath(farHill, Color(0xFFF1C40F).copy(alpha = 0.15f)) // soft golden outline
        
        // Fluffy clouds at bottom (composite circles)
        val cloudColor = Color(0xFFF8BBD0).copy(alpha = 0.5f) // Pastel pinkish clouds
        val cloudColor2 = Color(0xFFE1BEE7).copy(alpha = 0.6f) // Pastel violet
        
        val baseCloudY = h * 0.82f
        var cx = -cloudOffset % 150f
        while (cx < w + 150f) {
            drawCircle(cloudColor, radius = 50f * density, center = Offset(cx, baseCloudY))
            drawCircle(cloudColor, radius = 70f * density, center = Offset(cx + 40f * density, baseCloudY + 10f * density))
            cx += 150f * density
        }
        
        var cx2 = (cloudOffset + 75f) % 180f
        while (cx2 < w + 180f) {
            drawCircle(cloudColor2, radius = 40f * density, center = Offset(cx2, baseCloudY + 25f * density))
            drawCircle(cloudColor2, radius = 55f * density, center = Offset(cx2 - 30f * density, baseCloudY + 30f * density))
            cx2 += 180f * density
        }
        
        // Close pine trees
        val treeColor = Color(0xFF9C27B0).copy(alpha = 0.2f)
        listOf(w * 0.12f, w * 0.85f).forEach { tx ->
            val ty = h * 0.8f
            val path = Path().apply {
                moveTo(tx, ty - 35f * density)
                lineTo(tx - 12f * density, ty)
                lineTo(tx + 12f * density, ty)
                close()
            }
            drawPath(path, treeColor)
        }
    }
}

@Composable
fun JungleBackgroundOverlay() {
    val infiniteTransition = rememberInfiniteTransition(label = "fireflies")
    val fireflyProgress by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(4000, easing = LinearEasing), RepeatMode.Reverse), label = "ff"
    )
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        
        // Giant Full Moon
        val mx = w * 0.5f
        val my = h * 0.25f
        val mr = 70f * density
        
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFE8F5E9).copy(alpha = 0.3f), Color.Transparent),
                center = Offset(mx, my),
                radius = mr * 2.8f
            ),
            radius = mr * 2.8f,
            center = Offset(mx, my)
        )
        drawCircle(Color(0xFFE8F5E9), radius = mr, center = Offset(mx, my))
        
        // Jungle Ground
        val ground = Path().apply {
            moveTo(0f, h * 0.88f)
            quadraticTo(w * 0.5f, h * 0.84f, w, h * 0.88f)
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        drawPath(ground, Color(0xFF011C0E))
        
        // Jungle left tree canopy silhouette
        val leftCanopy = Path().apply {
            moveTo(0f, 0f)
            quadraticTo(w * 0.28f, h * 0.1f, w * 0.28f, h * 0.25f)
            quadraticTo(w * 0.2f, h * 0.45f, 0f, h * 0.55f)
            close()
        }
        drawPath(leftCanopy, Color(0xFF021B0F))
        
        // Jungle right tree canopy silhouette
        val rightCanopy = Path().apply {
            moveTo(w, 0f)
            quadraticTo(w * 0.72f, h * 0.08f, w * 0.72f, h * 0.2f)
            quadraticTo(w * 0.8f, h * 0.4f, w, h * 0.5f)
            close()
        }
        drawPath(rightCanopy, Color(0xFF021B0F))
        
        // Glowing Neon Fireflies
        val random = java.util.Random(101)
        repeat(16) { i ->
            val startX = random.nextFloat() * w
            val startY = h * 0.4f + random.nextFloat() * (h * 0.45f)
            val jitterY = (kotlin.math.sin((i * 2 + fireflyProgress * 2 * Math.PI).toDouble()).toFloat()) * 8f * density
            val sizeVal = (1.5f + random.nextFloat() * 2f) * density
            drawCircle(
                color = Color(0xFF00FF88).copy(alpha = 0.6f),
                radius = sizeVal,
                center = Offset(startX, startY + jitterY)
            )
            // soft glow ring
            drawCircle(
                color = Color(0xFF00FF88).copy(alpha = 0.12f),
                radius = sizeVal * 3f,
                center = Offset(startX, startY + jitterY)
            )
        }
    }
}

@Composable
fun DesertBackgroundOverlay() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        
        // Giant Setting Sun
        val sx = w * 0.5f
        val sy = h * 0.6f
        val sr = 80f * density
        
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFFF8A65).copy(alpha = 0.3f), Color.Transparent),
                center = Offset(sx, sy),
                radius = sr * 2.2f
            ),
            radius = sr * 2.2f,
            center = Offset(sx, sy)
        )
        drawCircle(Color(0xFFFFB74D), radius = sr, center = Offset(sx, sy))
        
        // Far dune
        val dune1 = Path().apply {
            moveTo(0f, h * 0.75f)
            quadraticTo(w * 0.4f, h * 0.65f, w, h * 0.73f)
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        drawPath(dune1, Color(0xFF5E2A07)) // Deep orange-brown terracotta dune
        
        // Mid dune
        val dune2 = Path().apply {
            moveTo(0f, h * 0.8f)
            quadraticTo(w * 0.7f, h * 0.72f, w, h * 0.78f)
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        drawPath(dune2, Color(0xFF421C02)) // Darker terracotta
        
        // Close dune
        val dune3 = Path().apply {
            moveTo(0f, h * 0.86f)
            quadraticTo(w * 0.3f, h * 0.8f, w, h * 0.84f)
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        drawPath(dune3, Color(0xFF2E1200)) // Dune silhouette
        
        // Cactus silhouettes on close dunes
        val treeColor = Color(0xFF200C00)
        
        // Draw Left Cactus
        val cx1 = w * 0.15f
        val cy1 = h * 0.83f
        val cHeight1 = 40f * density
        val cWidth1 = 5f * density
        
        // Main stem
        drawRoundRect(treeColor, topLeft = Offset(cx1 - cWidth1/2, cy1 - cHeight1), size = Size(cWidth1, cHeight1), cornerRadius = CornerRadius(2f*density, 2f*density))
        // Left arm
        drawRoundRect(treeColor, topLeft = Offset(cx1 - 12f * density, cy1 - cHeight1 * 0.7f), size = Size(12f*density, 4f*density), cornerRadius = CornerRadius(1f*density, 1f*density))
        drawRoundRect(treeColor, topLeft = Offset(cx1 - 12f * density, cy1 - cHeight1 * 0.9f), size = Size(4f*density, cHeight1 * 0.25f), cornerRadius = CornerRadius(1f*density, 1f*density))
        // Right arm
        drawRoundRect(treeColor, topLeft = Offset(cx1, cy1 - cHeight1 * 0.5f), size = Size(10f*density, 4f*density), cornerRadius = CornerRadius(1f*density, 1f*density))
        drawRoundRect(treeColor, topLeft = Offset(cx1 + 8f * density, cy1 - cHeight1 * 0.7f), size = Size(4f*density, cHeight1 * 0.25f), cornerRadius = CornerRadius(1f*density, 1f*density))
    }
}

@Composable
fun VolcanoBackgroundOverlay() {
    val infiniteTransition = rememberInfiniteTransition(label = "embers")
    val animProgress by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(5000, easing = LinearEasing)), label = "em"
    )
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        
        // Far mountain peaks
        val peakPath = Path().apply {
            moveTo(0f, h * 0.7f)
            lineTo(w * 0.2f, h * 0.52f)
            lineTo(w * 0.35f, h * 0.62f)
            lineTo(w * 0.6f, h * 0.44f)
            lineTo(w * 0.78f, h * 0.58f)
            lineTo(w, h * 0.48f)
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        drawPath(peakPath, Color(0xFF19062E)) // Dark purple mountain ridge
        
        // Glowing Neon pink lines on the mountain edges!
        val ridgeHighlight = Path().apply {
            moveTo(0f, h * 0.7f)
            lineTo(w * 0.2f, h * 0.52f)
            lineTo(w * 0.35f, h * 0.62f)
            lineTo(w * 0.6f, h * 0.44f)
            lineTo(w * 0.78f, h * 0.58f)
            lineTo(w, h * 0.48f)
        }
        drawPath(
            ridgeHighlight,
            color = Color(0xFFFF007F), // Neon Magenta/Pink glow
            style = Stroke(width = 3f * density, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
        // Blur neon shadow
        drawPath(
            ridgeHighlight,
            color = Color(0xFFFF007F).copy(alpha = 0.3f),
            style = Stroke(width = 8f * density, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
        
        // Foreground low hills
        val foreHill = Path().apply {
            moveTo(0f, h * 0.85f)
            quadraticTo(w * 0.4f, h * 0.78f, w * 0.7f, h * 0.82f)
            quadraticTo(w * 0.85f, h * 0.84f, w, h * 0.78f)
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        drawPath(foreHill, Color(0xFF0C0118))
        
        // Lava rising glowing particles (embers)
        val random = java.util.Random(90)
        repeat(15) { i ->
            val startX = random.nextFloat() * w
            val startY = random.nextFloat() * h
            val speedY = -(random.nextFloat() * 100f + 60f) * density
            val curY = (startY + speedY * animProgress + h) % h
            val alpha = (1f - (curY / h)).coerceIn(0.1f, 0.9f)
            
            drawCircle(
                color = Color(0xFFFF007F).copy(alpha = alpha * 0.5f),
                radius = (1.5f + random.nextFloat() * 2f) * density,
                center = Offset(startX, curY)
            )
        }
    }
}

@Composable
fun SkyBackgroundOverlay() {
    val infiniteTransition = rememberInfiniteTransition(label = "skyTransition")
    val animProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(40000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "clouds"
    )
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        
        // Pale glowing sun
        val sx = w * 0.7f
        val sy = h * 0.22f
        val sr = 45f * density
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFFFF9E6).copy(alpha = 0.35f), Color.Transparent),
                center = Offset(sx, sy),
                radius = sr * 2.2f
            ),
            radius = sr * 2.2f,
            center = Offset(sx, sy)
        )
        drawCircle(Color(0xFFFFFEE6), radius = sr, center = Offset(sx, sy))
        
        // Far mountains - soft pinkish lavender
        val farMtn = Path().apply {
            moveTo(0f, h * 0.75f)
            lineTo(w * 0.2f, h * 0.58f)
            lineTo(w * 0.45f, h * 0.68f)
            lineTo(w * 0.75f, h * 0.54f)
            lineTo(w, h * 0.72f)
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        drawPath(farMtn, Color(0xFFC3C9E9))
        
        // Mid hills - soft blue-lavender
        val midHills = Path().apply {
            moveTo(0f, h * 0.82f)
            lineTo(0f, h * 0.68f)
            quadraticTo(w * 0.3f, h * 0.64f, w * 0.6f, h * 0.74f)
            quadraticTo(w * 0.85f, h * 0.78f, w, h * 0.69f)
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        drawPath(midHills, Color(0xFF9FA8DA))
        
        // Draw pine trees on mid hills
        val treeColor = Color(0xFF7986CB)
        val treePositions = listOf(w * 0.12f, w * 0.25f, w * 0.68f, w * 0.82f)
        treePositions.forEachIndexed { i, tx ->
            val ty = h * 0.75f + (if (i % 2 == 0) 10f else -10f) * density
            val treeH = (22f + (i % 3) * 6f) * density
            val treeW = 10f * density
            val treePath = Path().apply {
                moveTo(tx, ty - treeH)
                lineTo(tx - treeW/2, ty)
                lineTo(tx + treeW/2, ty)
                close()
            }
            drawPath(treePath, treeColor)
        }
        
        // Drifting Clouds (animating horizontally)
        val cloudColor = Color.White.copy(alpha = 0.55f)
        val cloudColor2 = Color(0xFFFBE9E7).copy(alpha = 0.7f) // Peach white
        val baseCloudY = h * 0.84f
        
        // Drifting cloud layer 1
        val cx = (animProgress * w) % (w + 300f) - 150f
        drawCircle(cloudColor, radius = 45f * density, center = Offset(cx, baseCloudY))
        drawCircle(cloudColor, radius = 60f * density, center = Offset(cx + 35f * density, baseCloudY + 8f * density))
        drawCircle(cloudColor, radius = 45f * density, center = Offset(cx - 30f * density, baseCloudY + 12f * density))
        
        // Drifting cloud layer 2 (opposite or slower)
        val cx2 = (w - (animProgress * 0.8f * w)) % (w + 300f) - 150f
        drawCircle(cloudColor2, radius = 38f * density, center = Offset(cx2, baseCloudY + 20f * density))
        drawCircle(cloudColor2, radius = 50f * density, center = Offset(cx2 + 30f * density, baseCloudY + 25f * density))
    }
}

@Composable
fun GlacierBackgroundOverlay() {
    val infiniteTransition = rememberInfiniteTransition(label = "glacierTransition")
    val animProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "snow"
    )
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        
        // Pale glowing winter moon
        val mx = w * 0.3f
        val my = h * 0.25f
        val mr = 40f * density
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFE0F7FA).copy(alpha = 0.25f), Color.Transparent),
                center = Offset(mx, my),
                radius = mr * 2.5f
            ),
            radius = mr * 2.5f,
            center = Offset(mx, my)
        )
        drawCircle(Color(0xFFE0F7FA), radius = mr, center = Offset(mx, my))
        
        // Far glaciers (Dark cyan silhouettes)
        val glacierFar = Path().apply {
            moveTo(0f, h * 0.72f)
            lineTo(w * 0.25f, h * 0.48f)
            lineTo(w * 0.45f, h * 0.62f)
            lineTo(w * 0.75f, h * 0.42f)
            lineTo(w, h * 0.65f)
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        drawPath(glacierFar, Color(0xFF0D47A1).copy(alpha = 0.3f))
        
        // Sharp icy peaks (polygonal flat vector look)
        // Peak 1: left side
        val peak1 = Path().apply {
            moveTo(0f, h * 0.75f)
            lineTo(w * 0.3f, h * 0.52f)
            lineTo(w * 0.55f, h * 0.78f)
            close()
        }
        drawPath(peak1, Color(0xFF006064)) // Dark cyan/teal ice shadow
        
        // Highlight snow cap for Peak 1
        val peak1Cap = Path().apply {
            moveTo(w * 0.3f, h * 0.52f)
            lineTo(w * 0.25f, h * 0.57f)
            lineTo(w * 0.35f, h * 0.58f)
            close()
        }
        drawPath(peak1Cap, Color(0xFFE0F7FA)) // White/cyan snow cap
        
        // Peak 2: right side
        val peak2 = Path().apply {
            moveTo(w * 0.25f, h * 0.8f)
            lineTo(w * 0.7f, h * 0.46f)
            lineTo(w, h * 0.7f)
            close()
        }
        drawPath(peak2, Color(0xFF004D40)) // Icy shadow deep
        
        // Snow cap for Peak 2
        val peak2Cap = Path().apply {
            moveTo(w * 0.7f, h * 0.46f)
            lineTo(w * 0.62f, h * 0.53f)
            lineTo(w * 0.75f, h * 0.52f)
            close()
        }
        drawPath(peak2Cap, Color(0xFFFFFFFF))
        
        // Snowy ground slope
        val snowSlope = Path().apply {
            moveTo(0f, h * 0.82f)
            quadraticTo(w * 0.4f, h * 0.84f, w * 0.7f, h * 0.78f)
            quadraticTo(w * 0.9f, h * 0.76f, w, h * 0.8f)
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        drawPath(snowSlope, Color(0xFF0B2535))
        
        // Falling Snow animation
        val random = java.util.Random(11)
        repeat(20) { i ->
            val startX = random.nextFloat() * w
            val startY = random.nextFloat() * h
            val speedX = (random.nextFloat() * 20f - 10f) * density
            val speedY = (random.nextFloat() * 60f + 40f) * density
            
            val currentX = (startX + speedX * animProgress + w) % w
            val currentY = (startY + speedY * animProgress) % h
            
            drawCircle(
                color = Color.White.copy(alpha = 0.4f),
                radius = (1.5f + random.nextFloat() * 2f) * density,
                center = Offset(currentX, currentY)
            )
        }
    }
}

@Composable
fun CyberpunkBackgroundOverlay() {
    val infiniteTransition = rememberInfiniteTransition(label = "cyberTransition")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        
        // Horizon Y
        val horizonY = h * 0.68f
        
        // Retro Synthwave Sun (Giant sun with horizontal cut stripes)
        val sunX = w * 0.5f
        val sunY = horizonY - 10f * density
        val sunR = 75f * density
        
        drawCircle(
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFFFF007F), Color(0xFFFFCC00)),
                startY = sunY - sunR,
                endY = sunY + sunR
            ),
            radius = sunR,
            center = Offset(sunX, sunY)
        )
        
        // Horizontal black stripe cuts in the sun (wider towards the bottom)
        var stripeY = sunY - sunR + 25f * density
        var stripeH = 2f * density
        while (stripeY < sunY + sunR) {
            drawRect(
                color = Color(0xFF180324), // Background color
                topLeft = Offset(sunX - sunR, stripeY),
                size = Size(sunR * 2, stripeH)
            )
            stripeY += 14f * density
            stripeH += 1.2f * density // lines get thicker towards bottom
        }
        
        // Retro mountain silhouette in front of the sun
        val cyberMtns = Path().apply {
            moveTo(0f, horizonY)
            lineTo(w * 0.15f, horizonY - 45f * density)
            lineTo(w * 0.3f, horizonY - 20f * density)
            lineTo(w * 0.5f, horizonY - 60f * density)
            lineTo(w * 0.65f, horizonY - 30f * density)
            lineTo(w * 0.8f, horizonY - 50f * density)
            lineTo(w, horizonY)
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        drawPath(cyberMtns, Color(0xFF0B0016))
        
        // Neon strokes on the mountain ridges
        val ridgePath = Path().apply {
            moveTo(0f, horizonY)
            lineTo(w * 0.15f, horizonY - 45f * density)
            lineTo(w * 0.3f, horizonY - 20f * density)
            lineTo(w * 0.5f, horizonY - 60f * density)
            lineTo(w * 0.65f, horizonY - 30f * density)
            lineTo(w * 0.8f, horizonY - 50f * density)
            lineTo(w, horizonY)
        }
        drawPath(
            ridgePath,
            color = Color(0xFF00FBFB), // Neon Cyan outline
            style = Stroke(width = 2f * density, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
        drawPath(
            ridgePath,
            color = Color(0xFF00FBFB).copy(alpha = 0.3f * pulse), // Glowing cyan bloom
            style = Stroke(width = 6f * density, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
        
        // Perspective digital grid below the horizon line
        val cols = 8
        repeat(cols + 1) { i ->
            val startX = w * (i.toFloat() / cols)
            val endX = w / 2 + (startX - w / 2) * 1.6f
            drawLine(
                color = Color(0xFFFF007F).copy(alpha = 0.15f * pulse),
                start = Offset(startX, horizonY),
                end = Offset(endX, h),
                strokeWidth = 1.5f * density
            )
        }
        
        val rows = 8
        repeat(rows + 1) { i ->
            val t = i.toFloat() / rows
            val py = horizonY + (h - horizonY) * (t * t)
            drawLine(
                color = Color(0xFFFF007F).copy(alpha = 0.15f * pulse),
                start = Offset(0f, py),
                end = Offset(w, py),
                strokeWidth = 1.5f * density
            )
        }
    }
}

@Composable
fun NeonSwitch(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    val transition = updateTransition(targetState = checked, label = "neonSwitchState")
    val thumbOffset by transition.animateDp(
        transitionSpec = { tween(200) },
        label = "neonThumbOffset"
    ) { isChecked ->
        if (isChecked) 24.dp else 0.dp
    }
    
    val trackColor by transition.animateColor(label = "neonTrackColor") { isChecked ->
        if (isChecked) SoftPurple.copy(alpha = 0.3f) else BrownText.copy(alpha = 0.1f)
    }
    val thumbColor by transition.animateColor(label = "neonThumbColor") { isChecked ->
        if (isChecked) SoftPurple else BrownText.copy(alpha = 0.4f)
    }

    Box(
        modifier = Modifier
            .width(52.dp)
            .height(28.dp)
            .clip(CircleShape)
            .background(trackColor)
            .border(1.5.dp, if (checked) SoftPurple else BrownText.copy(alpha = 0.2f), CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    onCheckedChange(!checked)
                }
            )
            .padding(2.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .offset(x = thumbOffset)
                .background(thumbColor, CircleShape)
        )
    }
}

@Composable
fun GlassmorphicPopupContainer(
    onClose: (() -> Unit)? = null,
    title: String? = null,
    cardColor: Color = Color(0xFFFFF9E6),
    borderColor: Color = Color(0xFF2C1B47),
    titleColor: Color = Color(0xFFFFFFD166),
    titleShadowColor: Color = Color(0xFF2C1B47),
    closeButtonBaseColor: Color = Color(0xFFC84B02),
    closeButtonTopGradient: List<Color> = listOf(Color(0xFFFF9F1C), Color(0xFFF15A24)),
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.65f))
            .clickable(enabled = false) {}
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .shadow(8.dp, RoundedCornerShape(24.dp), clip = false)
                .background(cardColor, RoundedCornerShape(24.dp))
                .border(6.dp, borderColor, RoundedCornerShape(24.dp))
                .padding(24.dp)
        ) {
            if (title != null || onClose != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (title != null) {
                        Text(
                            text = title,
                            style = TextStyle(
                                color = titleColor,
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = LuckiestGuyFontFamily,
                                shadow = Shadow(
                                    color = titleShadowColor,
                                    offset = Offset(2f, 4f),
                                    blurRadius = 2f
                                )
                            )
                        )
                    }
                    if (onClose != null) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .bouncyClickable {
                                    onClose()
                                }
                        ) {
                            // 3D Shadow layer
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(32.dp)
                                    .align(Alignment.BottomCenter)
                                    .background(closeButtonBaseColor, CircleShape)
                            )
                            // Top Face
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(32.dp)
                                    .align(Alignment.TopCenter)
                                    .background(
                                        brush = Brush.verticalGradient(
                                            colors = closeButtonTopGradient
                                        ),
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(20.dp))
            }
            content()
        }
    }
}

@Composable
fun ArrowPuzzleProBackground(skin: BackgroundSkin = BackgroundSkin.Classic, content: @Composable BoxScope.() -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            content()
        }
    }
}

@Composable
fun AnimatedBackgroundElements() {
    val infiniteTransition = rememberInfiniteTransition(label = "clouds")
    
    // Cloud 1
    val cloud1Offset by infiniteTransition.animateFloat(
        initialValue = -100f, targetValue = 400f,
        animationSpec = infiniteRepeatable(tween(25000, easing = LinearEasing)), label = "cloud1"
    )

    // Cloud 2
    val cloud2Offset by infiniteTransition.animateFloat(
        initialValue = 500f, targetValue = -200f,
        animationSpec = infiniteRepeatable(tween(35000, easing = LinearEasing)), label = "cloud2"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Icon(
            Icons.Default.Cloud, null, tint = MintWhite.copy(alpha = 0.6f),
            modifier = Modifier.size(120.dp).offset(x = cloud1Offset.dp, y = 80.dp)
        )
        Icon(
            Icons.Default.Cloud, null, tint = MintWhite.copy(alpha = 0.4f),
            modifier = Modifier.size(160.dp).offset(x = cloud2Offset.dp, y = 200.dp)
        )
        Icon(
            Icons.Default.Cloud, null, tint = MintWhite.copy(alpha = 0.3f),
            modifier = Modifier.size(100.dp).offset(x = (cloud1Offset + 150).dp, y = 350.dp)
        )
    }
}

@Composable
fun AnimatedSurface() {
    val infiniteTransition = rememberInfiniteTransition(label = "surface")
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 100f,
        animationSpec = infiniteRepeatable(tween(4000, easing = LinearEasing)), label = "wave"
    )

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
        Canvas(modifier = Modifier.fillMaxWidth().height(140.dp)) {
            val width = size.width
            val height = size.height
            
            // Draw Dirt
            val dirtPath = Path().apply {
                moveTo(0f, height)
                lineTo(0f, 40f)
                val segmentWidth = 200f
                var x = -waveOffset % segmentWidth
                while (x < width + segmentWidth) {
                    relativeQuadraticTo(segmentWidth / 4, -30f, segmentWidth / 2, 0f)
                    relativeQuadraticTo(segmentWidth / 4, 30f, segmentWidth / 2, 0f)
                    x += segmentWidth
                }
                lineTo(width, height)
                close()
            }
            drawPath(dirtPath, DirtBrown)
            
            // Draw Dirt Patterns (Circles)
            val circleSpacing = 150f
            var cx = -waveOffset % circleSpacing
            while (cx < width + circleSpacing) {
                drawCircle(DirtBrownDark, radius = 15f, center = Offset(cx + 20f, height - 40f))
                drawCircle(DirtBrownDark, radius = 25f, center = Offset(cx + 80f, height - 60f))
                cx += circleSpacing
            }

            // Draw Grass (Slightly above dirt)
            val grassPath = Path().apply {
                moveTo(0f, 45f)
                val segmentWidth = 200f
                var x = -waveOffset % segmentWidth
                while (x < width + segmentWidth) {
                    relativeQuadraticTo(segmentWidth / 4, -30f, segmentWidth / 2, 0f)
                    relativeQuadraticTo(segmentWidth / 4, 30f, segmentWidth / 2, 0f)
                    x += segmentWidth
                }
                lineTo(width, 55f)
                lineTo(0f, 55f)
                close()
            }
            drawPath(grassPath, GrassGreen)
        }
    }
}

@Composable
fun FloatingBackgroundIcons() {
    // Deprecated in favor of AnimatedBackgroundElements
}

@Composable
fun Modifier.bouncyClickable(
    enabled: Boolean = true,
    onClick: () -> Unit
): Modifier {
    val scope = rememberCoroutineScope()
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    var isAnimatingClick by remember { mutableStateOf(false) }
    val isVisualPressed = isPressed || isAnimatingClick

    val scale by animateFloatAsState(
        targetValue = if (isVisualPressed) 0.88f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "bouncyClickScale"
    )
    return this
        .scale(scale)
        .clickable(
            interactionSource = interactionSource,
            indication = null,
            enabled = enabled,
            onClick = {
                SoundManager.playClickSound()
                scope.launch {
                    isAnimatingClick = true
                    delay(100)
                    isAnimatingClick = false
                    delay(120)
                    onClick()
                }
            }
        )
}

@Composable
fun ThreeDButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    baseColor: Color = VibrantBlue,
    textColor: Color = MintWhite,
    icon: ImageVector? = null,
    isLarge: Boolean = false,
    enabled: Boolean = true,
    isSmall: Boolean = false,
    fontSize: androidx.compose.ui.unit.TextUnit? = null,
    iconSize: Dp? = null,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    val scope = rememberCoroutineScope()
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    var isAnimatingClick by remember { mutableStateOf(false) }
    val isVisualPressed = isPressed || isAnimatingClick

    val buttonScale by animateFloatAsState(
        targetValue = if (isVisualPressed) 0.92f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "btnScale"
    )
    
    val thickness = if (isLarge) 8.dp else if (isSmall) 5.dp else 7.dp
    val buttonOffsetY by animateDpAsState(if (isVisualPressed) thickness else 0.dp, label = "btnOffsetY")

    val activeColor = if (enabled) baseColor else Color(0xFFCBD5E1)
    val shadowColor = remember(activeColor) {
        activeColor.copy(
            red = (activeColor.red * 0.7f).coerceIn(0f, 1f),
            green = (activeColor.green * 0.7f).coerceIn(0f, 1f),
            blue = (activeColor.blue * 0.7f).coerceIn(0f, 1f)
        )
    }

    val containerHeight = if (isLarge) 68.dp else if (isSmall) 45.dp else 56.dp
    val faceHeight = if (isLarge) 60.dp else if (isSmall) 40.dp else 49.dp
    val cornerRadius = if (isLarge) 28.dp else if (isSmall) 20.dp else 24.dp

    Box(
        modifier = modifier
            .height(containerHeight)
            .scale(buttonScale)
    ) {
        // Shadow/bottom layer
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(shadowColor, RoundedCornerShape(cornerRadius))
        )

        // Active/top face layer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(faceHeight)
                .offset(y = buttonOffsetY)
                .background(activeColor, RoundedCornerShape(cornerRadius))
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    enabled = enabled,
                    onClick = {
                        SoundManager.playClickSound()
                        scope.launch {
                            isAnimatingClick = true
                            delay(100)
                            isAnimatingClick = false
                            delay(120)
                            onClick()
                        }
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                if (icon != null) {
                    Icon(icon, null, tint = textColor, modifier = Modifier.size(iconSize ?: 24.dp))
                    if (text.isNotEmpty()) {
                        Spacer(Modifier.width(8.dp))
                    }
                }
                if (text.isNotEmpty()) {
                    Text(
                        text = text,
                        color = textColor,
                        fontSize = fontSize ?: (if (isLarge) 20.sp else if (isSmall) 14.sp else 16.sp),
                        fontWeight = FontWeight.Bold,
                        fontFamily = LuckiestGuyFontFamily,
                        textAlign = TextAlign.Center
                    )
                }
                if (trailingIcon != null) {
                    Spacer(Modifier.width(8.dp))
                    trailingIcon()
                }
            }
        }
    }
}

@Composable
fun PopupThreeDButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    baseColor: Color = VibrantGreen,
    textColor: Color = MintWhite,
    icon: ImageVector? = null,
    isLarge: Boolean = false,
    enabled: Boolean = true
) {
    ThreeDButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        baseColor = baseColor,
        textColor = textColor,
        icon = icon,
        isLarge = isLarge,
        enabled = enabled
    )
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    rotation: Float = 0f,
    onClose: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = modifier
            .graphicsLayer { rotationZ = rotation }
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(2.dp, Color(0xFFE2E8F0)),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                content()
            }
        }

        if (onClose != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 8.dp, y = (-8).dp)
                    .size(36.dp)
                    .background(Color(0xFFE1F5FE), CircleShape)
                    .border(1.5.dp, Color(0xFF00A2FF), CircleShape)
                    .clickable {
                        onClose()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color(0xFF00A2FF),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}


@Composable
fun DrawScope.drawElipse(color: Color, topLeft: Offset, size: Size) {
    drawOval(color = color, topLeft = topLeft, size = size)
}

@Composable
fun Mascot(modifier: Modifier = Modifier, pose: String = "excited") {
    val transition = rememberInfiniteTransition(label = "mascotMove")
    val floatAnim by transition.animateFloat(
        initialValue = -6f, targetValue = 6f,
        animationSpec = infiniteRepeatable(tween(2000, easing = EaseInOutSine), RepeatMode.Reverse), label = "float"
    )

    Canvas(modifier = modifier.size(160.dp).offset(y = floatAnim.dp)) {
        val w = size.width
        val h = size.height
        val cx = w / 2
        val cy = h / 2

        // 1. Drop Shadow
        drawOval(
            color = Color.Black.copy(alpha = 0.15f),
            topLeft = Offset(cx - 50.dp.toPx(), h - 20.dp.toPx()),
            size = Size(100.dp.toPx(), 12.dp.toPx())
        )

        // 2. Ears
        val earWidth = 35.dp.toPx()
        val earHeight = 35.dp.toPx()
        
        // Left Ear
        drawArc(
            color = Color(0xFF00A2FF),
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = true,
            topLeft = Offset(cx - 50.dp.toPx(), cy - 60.dp.toPx()),
            size = Size(earWidth, earHeight)
        )
        // Right Ear
        drawArc(
            color = Color(0xFF00A2FF),
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = true,
            topLeft = Offset(cx + 15.dp.toPx(), cy - 60.dp.toPx()),
            size = Size(earWidth, earHeight)
        )

        // 3. Body Blob (slightly flattened circle/oval)
        drawCircle(
            color = Color(0xFF00A2FF),
            radius = 55.dp.toPx(),
            center = Offset(cx, cy - 5.dp.toPx())
        )

        // 4. Eyes
        val eyeW = 22.dp.toPx()
        val eyeH = 28.dp.toPx()
        val eyeY = cy - 18.dp.toPx()
        val leftEyeX = cx - 20.dp.toPx()
        val rightEyeX = cx + 2.dp.toPx()

        if (pose == "dizzy") {
            // Draw dizzy spiral eyes
            val spiralPathLeft = Path().apply {
                val radius = 12.dp.toPx()
                for (angle in 0..720 step 5) {
                    val rad = Math.toRadians(angle.toDouble())
                    val r = radius * (angle / 720f)
                    val px = (cx - 16.dp.toPx()) + (r * Math.cos(rad)).toFloat()
                    val py = (cy - 12.dp.toPx()) + (r * Math.sin(rad)).toFloat()
                    if (angle == 0) moveTo(px, py) else lineTo(px, py)
                }
            }
            val spiralPathRight = Path().apply {
                val radius = 12.dp.toPx()
                for (angle in 0..720 step 5) {
                    val rad = Math.toRadians(angle.toDouble())
                    val r = radius * (angle / 720f)
                    val px = (cx + 16.dp.toPx()) + (r * Math.cos(rad)).toFloat()
                    val py = (cy - 12.dp.toPx()) + (r * Math.sin(rad)).toFloat()
                    if (angle == 0) moveTo(px, py) else lineTo(px, py)
                }
            }
            drawPath(spiralPathLeft, Color(0xFF1E293B), style = Stroke(width = 3.dp.toPx().coerceAtLeast(1f), cap = StrokeCap.Round))
            drawPath(spiralPathRight, Color(0xFF1E293B), style = Stroke(width = 3.dp.toPx().coerceAtLeast(1f), cap = StrokeCap.Round))
            
            // Draw dizzy halo/spiral above head
            val haloPath = Path().apply {
                val radius = 24.dp.toPx()
                val hcy = cy - 75.dp.toPx()
                for (angle in 0..540 step 5) {
                    val rad = Math.toRadians(angle.toDouble())
                    val rx = radius * (angle / 540f) * 1.5f
                    val ry = radius * (angle / 540f) * 0.4f
                    val px = cx + (rx * Math.cos(rad)).toFloat()
                    val py = hcy + (ry * Math.sin(rad)).toFloat()
                    if (angle == 0) moveTo(px, py) else lineTo(px, py)
                }
            }
            drawPath(haloPath, Color(0xFFB0BEC5), style = Stroke(width = 3.dp.toPx().coerceAtLeast(1f), cap = StrokeCap.Round))
        } else {
            // Oval white eyes
            drawRoundRect(
                color = Color.White,
                topLeft = Offset(leftEyeX, eyeY),
                size = Size(eyeW, eyeH),
                cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx())
            )
            drawRoundRect(
                color = Color.White,
                topLeft = Offset(rightEyeX, eyeY),
                size = Size(eyeW, eyeH),
                cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx())
            )

            // Black pupils (oval)
            val pupilW = 10.dp.toPx()
            val pupilH = 14.dp.toPx()
            val pupilY = eyeY + 7.dp.toPx()
            val leftPupilX = leftEyeX + 6.dp.toPx()
            val rightPupilX = rightEyeX + 6.dp.toPx()

            drawRoundRect(
                color = Color(0xFF1E293B),
                topLeft = Offset(leftPupilX, pupilY),
                size = Size(pupilW, pupilH),
                cornerRadius = CornerRadius(7.dp.toPx(), 7.dp.toPx())
            )
            drawRoundRect(
                color = Color(0xFF1E293B),
                topLeft = Offset(rightPupilX, pupilY),
                size = Size(pupilW, pupilH),
                cornerRadius = CornerRadius(7.dp.toPx(), 7.dp.toPx())
            )
        }

        // 5. Smile/Mouth
        val mouthPath = Path().apply {
            if (pose == "sad") {
                moveTo(cx - 12.dp.toPx(), cy + 18.dp.toPx())
                quadraticTo(cx, cy + 8.dp.toPx(), cx + 12.dp.toPx(), cy + 18.dp.toPx())
            } else {
                moveTo(cx - 12.dp.toPx(), cy + 10.dp.toPx())
                quadraticTo(cx, cy + 20.dp.toPx(), cx + 12.dp.toPx(), cy + 10.dp.toPx())
            }
        }
        drawPath(mouthPath, Color(0xFF1E293B), style = Stroke(width = 4.dp.toPx().coerceAtLeast(1f), cap = StrokeCap.Round))
    }
}

// --- SCREENS ---

@Composable
fun SplashScreen(selectedBackgroundSkin: BackgroundSkin, onTimeout: () -> Unit) {
    val splashLottieComposition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.nkgek_lhn_yt))
    val splashLottieProgress by animateLottieCompositionAsState(
        composition = splashLottieComposition,
        iterations = LottieConstants.IterateForever
    )

    var startAnimation by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.4f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessVeryLow
        ),
        label = "scale"
    )

    val progressAnim by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 2300, easing = FastOutSlowInEasing),
        label = "progress"
    )

    LaunchedEffect(Unit) {
        startAnimation = true
        delay(2500)
        while (SettingsManager.apiStatus == "INITIALIZING" || 
               SettingsManager.apiStatus == "LOADING_CACHE" || 
               SettingsManager.apiStatus == "FETCHING_API") {
            delay(100)
        }
        onTimeout()
    }

    val infiniteTransition = rememberInfiniteTransition(label = "splashAnim")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    val textAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "textAlpha"
    )

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo Image with Spring Floating & Breathing Animation
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    },
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.splash_logo),
                    contentDescription = "Color Pipes Puzzle Logo",
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(Modifier.height(20.dp))

            // Brand Title Graphic Logo (Appnm-text.png)
            Image(
                painter = painterResource(id = R.drawable.appnm_text),
                contentDescription = "Color Pipes Title",
                modifier = Modifier
                    .width(260.dp)
                    .height(85.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(Modifier.height(10.dp))

            Text(
                text = "CONNECT PIPES & CLEAR THE MAZE!",
                style = TextStyle(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontFamily = LuckiestGuyFontFamily,
                    letterSpacing = 1.sp,
                    shadow = Shadow(
                        color = Color(0xFF1E293B),
                        offset = Offset(2f, 4f),
                        blurRadius = 3f
                    )
                )
            )

            Spacer(Modifier.height(36.dp))

            // Juicy Candy Loading Progress Bar with Yellow Border and Blue -> Yellow Gradient
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(230.dp)
                        .height(20.dp)
                        .background(Color(0xFF1E293B).copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                        .border(2.5.dp, Color(0xFFFFD233), RoundedCornerShape(10.dp))
                        .padding(2.5.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progressAnim.coerceIn(0.06f, 1f))
                            .fillMaxHeight()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(Color(0xFF0187FD), Color(0xFF00C6FF), Color(0xFFFFD233))
                                ),
                                RoundedCornerShape(8.dp)
                            )
                    )
                }

                Text(
                    text = "Loading ${(progressAnim * 100).toInt()}%",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = LuckiestGuyFontFamily,
                    style = TextStyle(
                        shadow = Shadow(Color(0xFF1E293B), Offset(2f, 2f), 2f)
                    )
                )
            }
        }
    }
}


@Composable
fun UpdateScreen(
    currentVersion: String,
    newVersion: String,
    updateUrl: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    BackHandler {
        onBack()
    }

    val infiniteTransition = rememberInfiniteTransition(label = "updatePulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Background Wallpaper
        Image(
            painter = painterResource(id = R.drawable.bg),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Dark dim overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.65f))
        )

        // Centered Content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth(0.94f)
                    .shadow(8.dp, RoundedCornerShape(26.dp), clip = false)
                    .background(Color(0xFFFFF9E6), RoundedCornerShape(26.dp))
                    .border(
                        width = 3.5.dp,
                        brush = Brush.horizontalGradient(
                            listOf(Color(0xFF0284C7), Color(0xFFFACC15), Color(0xFF22C55E))
                        ),
                        shape = RoundedCornerShape(26.dp)
                    )
                    .padding(horizontal = 20.dp, vertical = 24.dp)
            ) {
                // Logo / App Icon Badge
                Box(
                    modifier = Modifier
                        .size(92.dp)
                        .scale(scale)
                        .background(Color.White, RoundedCornerShape(22.dp))
                        .border(
                            2.dp,
                            Brush.horizontalGradient(
                                listOf(Color(0xFF0284C7).copy(alpha = 0.5f), Color(0xFF22C55E).copy(alpha = 0.5f))
                            ),
                            RoundedCornerShape(22.dp)
                        )
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.logo_image),
                        contentDescription = "Logo",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(Modifier.height(16.dp))

                // Title
                Text(
                    text = "UPDATE REQUIRED!",
                    style = TextStyle(
                        color = Color(0xFF2563EB),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = LuckiestGuyFontFamily,
                        letterSpacing = 0.5.sp,
                        shadow = Shadow(
                            color = Color(0x33000000),
                            offset = Offset(0f, 2f),
                            blurRadius = 2f
                        )
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "A new version of Color Pipes Puzzle is available with new levels & improvements. Please update to continue playing!",
                    color = Color(0xFF64748B),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )

                Spacer(Modifier.height(20.dp))

                // Version Comparison Card
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White, RoundedCornerShape(18.dp))
                        .border(
                            2.dp,
                            Brush.horizontalGradient(
                                listOf(Color(0xFF0284C7).copy(alpha = 0.4f), Color(0xFF22C55E).copy(alpha = 0.4f))
                            ),
                            RoundedCornerShape(18.dp)
                        )
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "CURRENT",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = LuckiestGuyFontFamily
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = "v$currentVersion",
                            color = Color(0xFF1E293B),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = LuckiestGuyFontFamily
                        )
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Color(0xFF0284C7),
                        modifier = Modifier.size(24.dp)
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "NEW VERSION",
                            color = Color(0xFF059669),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = LuckiestGuyFontFamily
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = "v$newVersion",
                            color = Color(0xFF16A34A),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = LuckiestGuyFontFamily
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                // UPDATE NOW Button (Green 3D Gradient)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .zoomClickable {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(updateUrl))
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                android.util.Log.e("UpdateScreen", "Error launching update URL: ", e)
                            }
                            (context as? Activity)?.finishAffinity()
                        }
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color(0xFF4ADE80), Color(0xFF16A34A))
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .border(
                            width = 2.dp,
                            brush = Brush.horizontalGradient(
                                listOf(Color(0xFF0284C7), Color(0xFFFACC15), Color(0xFF22C55E))
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "UPDATE NOW",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = LuckiestGuyFontFamily,
                        letterSpacing = 1.sp
                    )
                }

                Spacer(Modifier.height(12.dp))

                // EXIT APP Button (Red 3D Gradient)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .zoomClickable {
                            onBack()
                        }
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color(0xFFF87171), Color(0xFFDC2626))
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .border(
                            width = 2.dp,
                            brush = Brush.horizontalGradient(
                                listOf(Color(0xFF0284C7), Color(0xFFFACC15), Color(0xFF22C55E))
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "EXIT APP",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = LuckiestGuyFontFamily,
                        letterSpacing = 0.8.sp
                    )
                }
            }
        }
    }
}

@Composable
fun LoginScreen(onLogin: () -> Unit) {
    ArrowPuzzleProBackground {
        Column(modifier = Modifier.fillMaxSize().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Mascot(modifier = Modifier.size(200.dp))
            Spacer(Modifier.height(32.dp))
            
            // Bubbly Title Style
            Box(contentAlignment = Alignment.Center) {
                Text(
                    "PIPE CRAZE", color = Color.Black.copy(alpha = 0.3f), fontSize = 52.sp, fontWeight = FontWeight.Black,
                    modifier = Modifier.offset(y = 4.dp),
                    fontFamily = LuckiestGuyFontFamily
                )
                Text(
                    "PIPE CRAZE", color = MintWhite, fontSize = 52.sp, fontWeight = FontWeight.Black,
                    style = TextStyle(shadow = Shadow(VibrantBlue, offset = Offset(0f, 8f), blurRadius = 0f)),
                    fontFamily = LuckiestGuyFontFamily
                )
            }
            
            Text("The Ultimate Pipe Craze Puzzle!", color = MintWhite.copy(alpha = 0.7f), fontSize = 18.sp, textAlign = TextAlign.Center)
            Spacer(Modifier.height(60.dp))
            ThreeDButton("START JOURNEY", onLogin, isLarge = true, baseColor = VibrantGreen, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(24.dp))
            ThreeDButton("CLOUD SYNC", onLogin, isLarge = false, baseColor = VibrantBlue, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
fun WoodenIconButton(
    drawableRes: Int,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.88f else 1.0f, label = "buttonPress")

    Image(
        painter = painterResource(id = drawableRes),
        contentDescription = contentDescription,
        modifier = modifier
            .size(80.dp)
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    onClick()
                }
            )
    )
}

@Composable
fun ThreeDPlayButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    baseColor: Color = SoftPurple,
    textColor: Color = Color.White
) {
    val scope = rememberCoroutineScope()
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    var isAnimatingClick by remember { mutableStateOf(false) }
    val isVisualPressed = isPressed || isAnimatingClick
    
    val buttonScale by animateFloatAsState(
        targetValue = if (isVisualPressed) 0.90f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "playBtnScale"
    )
    val buttonOffsetY by animateDpAsState(if (isVisualPressed) 6.dp else 0.dp, label = "playBtnOffsetY")
    
    val shimmerTransition = rememberInfiniteTransition(label = "shimmerTransition")
    val shimmerTranslate by shimmerTransition.animateFloat(
        initialValue = -400f,
        targetValue = 800f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )
    
    val shadowColor = remember(baseColor) {
        baseColor.copy(
            red = (baseColor.red * 0.7f).coerceIn(0f, 1f),
            green = (baseColor.green * 0.7f).coerceIn(0f, 1f),
            blue = (baseColor.blue * 0.7f).coerceIn(0f, 1f)
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth(0.95f)
            .height(68.dp)
            .scale(buttonScale)
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(shadowColor, RoundedCornerShape(28.dp))
        )
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .offset(y = buttonOffsetY)
                .clip(RoundedCornerShape(28.dp))
                .background(baseColor)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = {
                        scope.launch {
                            isAnimatingClick = true
                            delay(100)
                            isAnimatingClick = false
                            delay(120)
                            onClick()
                        }
                    }
                )
                .drawBehind {
                    val shimmerBrush = Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0f),
                            Color.White.copy(alpha = 0.35f),
                            Color.White.copy(alpha = 0f)
                        ),
                        start = Offset(shimmerTranslate, 0f),
                        end = Offset(shimmerTranslate + 200f, size.height)
                    )
                    drawRect(brush = shimmerBrush)
                },
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = text.uppercase(),
                    color = textColor,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = LuckiestGuyFontFamily,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

@Composable
fun GameActionButton(
    icon: ImageVector,
    text: String,
    baseColor: Color,
    enabled: Boolean,
    onClick: () -> Unit,
    textColor: Color = Color.White,
    priceTag: String? = null
) {
    val scope = rememberCoroutineScope()
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    var isAnimatingClick by remember { mutableStateOf(false) }
    val isVisualPressed = isPressed || isAnimatingClick
    
    val buttonScale by animateFloatAsState(
        targetValue = if (isVisualPressed && enabled) 0.92f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "btnScale"
    )
    val buttonOffsetY by animateDpAsState(if (isVisualPressed && enabled) 4.dp else 0.dp, label = "btnOffsetY")
    
    val shadowColor = remember(baseColor) {
        baseColor.copy(
            red = (baseColor.red * 0.7f).coerceIn(0f, 1f),
            green = (baseColor.green * 0.7f).coerceIn(0f, 1f),
            blue = (baseColor.blue * 0.7f).coerceIn(0f, 1f)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .scale(buttonScale)
            .alpha(if (enabled) 1f else 0.5f)
    ) {
        // Shadow layer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .offset(y = 4.dp)
                .background(shadowColor, RoundedCornerShape(16.dp))
        )
        
        // Button Face
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .offset(y = buttonOffsetY)
                .clip(RoundedCornerShape(16.dp))
                .background(baseColor)
                .clickable(
                    enabled = enabled,
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = {
                        scope.launch {
                            isAnimatingClick = true
                            delay(80)
                            isAnimatingClick = false
                            delay(80)
                            onClick()
                        }
                    }
                )
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = textColor,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(6.dp))
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = text.uppercase(),
                    color = textColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = LuckiestGuyFontFamily,
                    letterSpacing = 0.5.sp
                )
                if (priceTag != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        if (priceTag == "Ad") {
                            Box(
                                modifier = Modifier
                                    .border(1.dp, textColor, RoundedCornerShape(3.dp))
                                    .padding(horizontal = 4.dp, vertical = 0.5.dp)
                            ) {
                                Text(
                                    text = "AD",
                                    color = textColor,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.2.sp
                                )
                            }
                        } else {
                            Text("🪙", fontSize = 10.sp)
                            Spacer(Modifier.width(2.dp))
                            Text(
                                text = priceTag,
                                color = textColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

data class PreviewScenario(
    val wordText: String,
    val wordColor: Color,
    val options: List<Pair<String, Color>>,
    val correctIndex: Int
)

@Composable
fun OptionPreviewButton(
    text: String,
    bg: Color,
    isHighlighted: Boolean,
    modifier: Modifier = Modifier
) {
    val translationY by animateDpAsState(if (isHighlighted) 4.dp else 0.dp, label = "previewTranslation")
    val scale by animateFloatAsState(if (isHighlighted) 1.05f else 1.0f, label = "previewScale")
    val borderStroke = if (isHighlighted) BorderStroke(3.dp, Color(0xFF00A2FF)) else null

    val shadowColor = remember(bg) {
        bg.copy(
            red = (bg.red * 0.7f).coerceIn(0f, 1f),
            green = (bg.green * 0.7f).coerceIn(0f, 1f),
            blue = (bg.blue * 0.7f).coerceIn(0f, 1f)
        )
    }

    Box(
        modifier = modifier
            .height(48.dp)
            .scale(scale)
    ) {
        // Shadow/bottom layer of button
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(shadowColor, RoundedCornerShape(22.dp))
        )
        
        // Active/top layer of button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp) // leave 4dp for 3D thickness
                .offset(y = translationY)
                .background(bg, RoundedCornerShape(22.dp))
                .then(
                    if (borderStroke != null) Modifier.border(borderStroke, RoundedCornerShape(22.dp))
                    else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = Color.Black,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}



@Composable
fun GamePreviewCard(
    stars: Int,
    coins: Int,
    currentLevel: Int,
    onPlayClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.nkgek_lhn_yt))
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever
    )

    Box(
        modifier = modifier
            .fillMaxWidth(0.96f)
            .height(440.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        // Outer Card Container (Cream/Beige with thick dark border and shadow)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .align(Alignment.BottomCenter)
                .shadow(12.dp, RoundedCornerShape(32.dp), clip = false)
                .background(Color(0xFFFFF9E6), RoundedCornerShape(32.dp))
                .border(8.dp, Color(0xFF2C1B47), RoundedCornerShape(32.dp))
                .padding(bottom = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxSize().padding(top = 40.dp, start = 18.dp, end = 18.dp)
            ) {
                // Lottie Animation Container
                Box(
                    modifier = Modifier
                        .weight(1.3f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    LottieAnimation(
                        composition = composition,
                        progress = { progress },
                        modifier = Modifier.size(220.dp)
                    )
                }

                // Stats Dashboard: Two separate colorful tablets
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Level Tablet (Purple)
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .background(Color(0xFF8338EC), RoundedCornerShape(16.dp))
                            .border(2.5.dp, Color(0xFF2C1B47), RoundedCornerShape(16.dp)),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "🏆 ", fontSize = 16.sp)
                        Text(
                            text = "LVL $currentLevel",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = LuckiestGuyFontFamily
                        )
                    }

                    // Diamonds Tablet (Orange)
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .background(Color(0xFFFB8500), RoundedCornerShape(16.dp))
                            .border(2.5.dp, Color(0xFF2C1B47), RoundedCornerShape(16.dp)),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.diamond),
                            contentDescription = "Diamond",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "$coins",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = LuckiestGuyFontFamily
                        )
                    }
                }

                // Play Game Button (Premium 3D Cartoon Orange Gradient)
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.90f)
                        .height(60.dp)
                        .bouncyClickable {
                            onPlayClick()
                        }
                ) {
                    // 3D Bottom Dark shadow base layer
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .align(Alignment.BottomCenter)
                            .background(Color(0xFF9E2A2B), RoundedCornerShape(20.dp))
                    )

                    // Top layer with bright orange gradient and border glow
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .align(Alignment.TopCenter)
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(Color(0xFFFFB703), Color(0xFFFB8500))
                                ),
                                shape = RoundedCornerShape(20.dp)
                            )
                            .border(2.5.dp, Color(0xFFFFF9E6), RoundedCornerShape(20.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Play Game",
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = LuckiestGuyFontFamily,
                            letterSpacing = 1.5.sp
                        )
                    }
                }
            }
        }

        // Top Overlapping Folded Ribbon Banner (Canvas 3D vector styling)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .align(Alignment.TopCenter),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                
                val ribbonDark = Color(0xFF5A189A)
                val ribbonMain = Color(0xFF7B2CBF)
                
                // Left fold shadow
                val leftFold = Path().apply {
                    moveTo(w * 0.14f, h * 0.65f)
                    lineTo(w * 0.20f, h * 0.65f)
                    lineTo(w * 0.20f, h * 0.85f)
                    close()
                }
                drawPath(leftFold, ribbonDark)
                
                // Right fold shadow
                val rightFold = Path().apply {
                    moveTo(w * 0.86f, h * 0.65f)
                    lineTo(w * 0.80f, h * 0.65f)
                    lineTo(w * 0.80f, h * 0.85f)
                    close()
                }
                drawPath(rightFold, ribbonDark)

                // Left wing
                val leftWing = Path().apply {
                    moveTo(w * 0.05f, h * 0.25f)
                    lineTo(w * 0.20f, h * 0.25f)
                    lineTo(w * 0.20f, h * 0.75f)
                    lineTo(w * 0.05f, h * 0.75f)
                    lineTo(w * 0.09f, h * 0.5f)
                    close()
                }
                drawPath(leftWing, ribbonMain)
                
                // Right wing
                val rightWing = Path().apply {
                    moveTo(w * 0.95f, h * 0.25f)
                    lineTo(w * 0.80f, h * 0.25f)
                    lineTo(w * 0.80f, h * 0.75f)
                    lineTo(w * 0.95f, h * 0.75f)
                    lineTo(w * 0.91f, h * 0.5f)
                    close()
                }
                drawPath(rightWing, ribbonMain)

                // Main center banner
                val mainBanner = Path().apply {
                    moveTo(w * 0.15f, h * 0.15f)
                    lineTo(w * 0.85f, h * 0.15f)
                    lineTo(w * 0.85f, h * 0.65f)
                    lineTo(w * 0.15f, h * 0.65f)
                    close()
                }
                drawPath(mainBanner, ribbonMain)
            }
            
            Text(
                text = "Pipe Craze",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = LuckiestGuyFontFamily,
                modifier = Modifier.padding(bottom = 10.dp)
            )
        }
    }
}

private fun DrawScope.drawPreviewArrow(
    pathCells: List<GridCell>,
    direction: GameArrowDirection,
    color: Color,
    t: Float,
    cellW: Float,
    cellH: Float,
    density: Float
) {
    if (pathCells.isEmpty()) return
    
    val nodes = mutableListOf<Offset>()
    pathCells.forEach { cell ->
        nodes.add(Offset((cell.c + 0.5f) * cellW, (cell.r + 0.5f) * cellH))
    }
    val lastCell = pathCells.last()
    for (i in 1..20) {
        val nextC = lastCell.c + direction.dc * i
        val nextR = lastCell.r + direction.dr * i
        nodes.add(Offset((nextC + 0.5f) * cellW, (nextR + 0.5f) * cellH))
    }
    
    val sMin = t
    val sMax = t + (pathCells.size - 1)
    
    val points = mutableListOf<Offset>()
    
    fun getPointAt(s: Float): Offset {
        val idx = s.toInt().coerceIn(0, nodes.size - 2)
        val f = s - s.toInt()
        val p1 = nodes[idx]
        val p2 = nodes[idx + 1]
        return Offset(
            p1.x * (1f - f) + p2.x * f,
            p1.y * (1f - f) + p2.y * f
        )
    }
    
    points.add(getPointAt(sMin))
    val startInt = (sMin + 0.0001f).toInt() + 1
    val endInt = (sMax - 0.0001f).toInt()
    for (j in startInt..endInt) {
        if (j >= 0 && j < nodes.size) {
            points.add(nodes[j])
        }
    }
    points.add(getPointAt(sMax))
    
    val strokeWidth = 3.5f.dp.toPx()
    val cornerRadius = 12.dp.toPx()
    
    val shadowPath = Path().apply {
        moveTo(points.first().x + 2.dp.toPx(), points.first().y + 3.dp.toPx())
        for (i in 1 until points.size) {
            lineTo(points[i].x + 2.dp.toPx(), points[i].y + 3.dp.toPx())
        }
    }
    drawPath(
        path = shadowPath,
        color = Color.Black.copy(alpha = 0.15f),
        style = Stroke(
            width = strokeWidth,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round,
            pathEffect = PathEffect.cornerPathEffect(cornerRadius)
        )
    )

    val bodyPath = Path().apply {
        moveTo(points.first().x, points.first().y)
        for (i in 1 until points.size) {
            lineTo(points[i].x, points[i].y)
        }
    }
    drawPath(
        path = bodyPath,
        color = color,
        style = Stroke(
            width = strokeWidth,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round,
            pathEffect = PathEffect.cornerPathEffect(cornerRadius)
        )
    )

    val headPoint = points.last()
    val headSize = 9.dp.toPx()
    val headPath = Path()
    
    val lastIdx = sMax.toInt().coerceIn(0, nodes.size - 2)
    val p1 = nodes[lastIdx]
    val p2 = nodes[lastIdx + 1]
    val dx = p2.x - p1.x
    val dy = p2.y - p1.y
    val currentDir = when {
        kotlin.math.abs(dx) > kotlin.math.abs(dy) -> {
            if (dx > 0) GameArrowDirection.RIGHT else GameArrowDirection.LEFT
        }
        else -> {
            if (dy > 0) GameArrowDirection.DOWN else GameArrowDirection.UP
        }
    }
    
    when (currentDir) {
        GameArrowDirection.UP -> {
            headPath.moveTo(headPoint.x, headPoint.y - headSize)
            headPath.lineTo(headPoint.x - headSize * 0.8f, headPoint.y + headSize * 0.2f)
            headPath.lineTo(headPoint.x + headSize * 0.8f, headPoint.y + headSize * 0.2f)
            headPath.close()
        }
        GameArrowDirection.DOWN -> {
            headPath.moveTo(headPoint.x, headPoint.y + headSize)
            headPath.lineTo(headPoint.x - headSize * 0.8f, headPoint.y - headSize * 0.2f)
            headPath.lineTo(headPoint.x + headSize * 0.8f, headPoint.y - headSize * 0.2f)
            headPath.close()
        }
        GameArrowDirection.LEFT -> {
            headPath.moveTo(headPoint.x - headSize, headPoint.y)
            headPath.lineTo(headPoint.x + headSize * 0.2f, headPoint.y - headSize * 0.8f)
            headPath.lineTo(headPoint.x + headSize * 0.2f, headPoint.y + headSize * 0.8f)
            headPath.close()
        }
        GameArrowDirection.RIGHT -> {
            headPath.moveTo(headPoint.x + headSize, headPoint.y)
            headPath.lineTo(headPoint.x - headSize * 0.2f, headPoint.y - headSize * 0.8f)
            headPath.lineTo(headPoint.x - headSize * 0.2f, headPoint.y + headSize * 0.8f)
            headPath.close()
        }
    }

    val shadowHeadPath = Path()
    shadowHeadPath.addPath(headPath, Offset(2f * density, 3f * density))
    drawPath(shadowHeadPath, color = Color.Black.copy(alpha = 0.12f))
    drawPath(headPath, color = color)
}

suspend fun PointerInputScope.detectTransformGesturesCustom(
    onGesture: (centroid: Offset, pan: Offset, zoom: Float) -> Unit
) {
    awaitEachGesture {
        var zoom = 1f
        var pan = Offset.Zero
        var pastTouchSlop = false
        val touchSlop = viewConfiguration.touchSlop
        
        awaitFirstDown(requireUnconsumed = false)
        do {
            val event = awaitPointerEvent()
            val canceled = event.changes.any { it.isConsumed }
            if (!canceled) {
                val zoomChange = event.calculateZoom()
                val panChange = event.calculatePan()
                
                if (!pastTouchSlop) {
                    zoom *= zoomChange
                    pan += panChange
                    
                    val centroidSize = event.calculateCentroidSize(useCurrent = false)
                    val zoomMotion = kotlin.math.abs(1 - zoom) * centroidSize
                    val panMotion = pan.getDistance()
                    
                    if (zoomMotion > touchSlop || panMotion > touchSlop) {
                        pastTouchSlop = true
                    }
                }
                
                if (pastTouchSlop) {
                    val centroid = event.calculateCentroid(useCurrent = false)
                    if (zoomChange != 1f || panChange != Offset.Zero) {
                        onGesture(centroid, panChange, zoomChange)
                    }
                    event.changes.forEach { it.consume() }
                }
            }
        } while (event.changes.any { it.pressed })
    }
}

suspend fun PointerInputScope.detectTapAndTransformGestures(
    onTap: (Offset) -> Unit,
    onGesture: (centroid: Offset, pan: Offset, zoom: Float) -> Unit
) {
    awaitEachGesture {
        var zoom = 1f
        var pan = Offset.Zero
        var pastTouchSlop = false
        val touchSlop = viewConfiguration.touchSlop
        val down = awaitFirstDown(requireUnconsumed = false)
        var isTap = true
        val startPosition = down.position
        
        do {
            val event = awaitPointerEvent()
            val canceled = event.changes.any { it.isConsumed }
            if (!canceled) {
                if (event.changes.size > 1) {
                    isTap = false
                }
                
                val zoomChange = event.calculateZoom()
                val panChange = event.calculatePan()
                
                if (!pastTouchSlop) {
                    zoom *= zoomChange
                    pan += panChange
                    
                    val centroidSize = event.calculateCentroidSize(useCurrent = false)
                    val zoomMotion = kotlin.math.abs(1 - zoom) * centroidSize
                    val panMotion = pan.getDistance()
                    
                    if (zoomMotion > touchSlop || panMotion > touchSlop * 2.0f) {
                        pastTouchSlop = true
                        isTap = false
                    }
                }
                
                if (pastTouchSlop) {
                    val centroid = event.calculateCentroid(useCurrent = false)
                    if (zoomChange != 1f || panChange != Offset.Zero) {
                        onGesture(centroid, panChange, zoomChange)
                    }
                    event.changes.forEach { it.consume() }
                }
            } else {
                isTap = false
            }
        } while (event.changes.any { it.pressed })
        
        if (isTap) {
            onTap(startPosition)
        }
    }
}


private data class GridCell(val r: Int, val c: Int)

private fun DrawScope.drawPreviewArrow(
    startCell: GridCell,
    endCell: GridCell,
    direction: GameArrowDirection,
    color: Color,
    offset: Offset,
    cellW: Float,
    cellH: Float,
    density: Float = 2.5f
) {
    val t = when (direction) {
        GameArrowDirection.LEFT -> -offset.x / cellW
        GameArrowDirection.RIGHT -> offset.x / cellW
        GameArrowDirection.UP -> -offset.y / cellH
        GameArrowDirection.DOWN -> offset.y / cellH
    }
    drawPreviewArrow(
        pathCells = listOf(startCell, endCell),
        direction = direction,
        color = color,
        t = t,
        cellW = cellW,
        cellH = cellH,
        density = density
    )
}

@Composable
fun FloatingBubblesBackground(modifier: Modifier = Modifier) {
    // Bubbles removed from the background as requested
}

@Composable
fun HomeScreenActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .bouncyClickable {
                    onClick()
                }
                .shadow(4.dp, RoundedCornerShape(16.dp), clip = false)
                .background(Color(0xFFFFF9C4), RoundedCornerShape(16.dp))
                .border(
                    width = 2.5.dp,
                    brush = Brush.horizontalGradient(
                        listOf(Color(0xFF0284C7), Color(0xFFFACC15), Color(0xFF22C55E))
                    ),
                    shape = RoundedCornerShape(16.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color(0xFF1E152A),
                modifier = Modifier.size(26.dp)
            )
        }

        Text(
            text = label,
            color = Color(0xFF1E152A),
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            fontFamily = LuckiestGuyFontFamily,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun HomeScreen(
    stars: Int,
    coins: Int,
    currentLevel: Int,
    currentStreak: Int,
    selectedBackgroundSkin: BackgroundSkin,
    onPlayClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onRewardsClick: () -> Unit = {},
    onOpenTester: () -> Unit = {},
    onSkinsShopClick: () -> Unit
) {
    val context = LocalContext.current
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.nkgek_lhn_yt))
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever
    )

    val infiniteTransition = rememberInfiniteTransition(label = "playScaleTransition")
    val playScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "playScale"
    )

    LaunchedEffect(Unit) {
        SettingsManager.init(context)
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Subtle ambient glowing backdrop light circles
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(NeonCyan.copy(alpha = 0.08f), Color.Transparent),
                    radius = w * 0.5f,
                    center = Offset(w * 0.1f, h * 0.3f)
                ),
                radius = w * 0.5f,
                center = Offset(w * 0.1f, h * 0.3f)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(NeonViolet.copy(alpha = 0.08f), Color.Transparent),
                    radius = w * 0.5f,
                    center = Offset(w * 0.9f, h * 0.7f)
                ),
                radius = w * 0.5f,
                center = Offset(w * 0.9f, h * 0.7f)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Elegant Cartoon Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Circular Level/Stage Badge
                Box(
                    modifier = Modifier
                        .height(40.dp)
                        .background(Color.White, RoundedCornerShape(20.dp))
                        .border(
                            width = 2.5.dp,
                            brush = Brush.horizontalGradient(listOf(Color(0xFF0284C7), Color(0xFF22C55E))),
                            shape = RoundedCornerShape(20.dp)
                        )
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "LVL $currentLevel",
                        color = Color.Black,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = LuckiestGuyFontFamily
                    )
                }

                // Circular Diamond Badge
                Box(
                    modifier = Modifier
                        .height(40.dp)
                        .background(Color(0xFFFFF9C4), RoundedCornerShape(20.dp))
                        .border(
                            width = 2.5.dp,
                            brush = Brush.horizontalGradient(listOf(Color(0xFFFB8500), Color(0xFF22C55E))),
                            shape = RoundedCornerShape(20.dp)
                        )
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.diamond),
                            contentDescription = "Diamond",
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "$coins",
                            color = Color.Black,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = LuckiestGuyFontFamily
                        )
                    }
                }
            }

            Spacer(Modifier.height(4.dp))

            // Nested Column to keep mascot and title graphic close
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                // Central 3D Animated Mascot
                val mascotTransition = rememberInfiniteTransition(label = "mascotBounce")
                val mascotOffset by mascotTransition.animateFloat(
                    initialValue = -10f,
                    targetValue = 10f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1500, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "mascotY"
                )
                val mascotScale by mascotTransition.animateFloat(
                    initialValue = 0.96f,
                    targetValue = 1.04f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1500, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "mascotScale"
                )

                Box(
                    modifier = Modifier
                        .size(210.dp)
                        .offset(y = mascotOffset.dp)
                        .graphicsLayer {
                            scaleX = mascotScale
                            scaleY = mascotScale
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.home_screen_logo),
                        contentDescription = "Color Pipes Mascot",
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(Modifier.height(8.dp))

                // Brand Title Graphic Logo (Appnm-text.png) placed below the logo
                Image(
                    painter = painterResource(id = R.drawable.appnm_text),
                    contentDescription = "Color Pipes Title",
                    modifier = Modifier
                        .width(240.dp)
                        .height(75.dp),
                    contentScale = ContentScale.Fit
                )
            }

            // Modern 2-Layer TAP TO PLAY Button
            Box(
                modifier = Modifier
                    .width(260.dp)
                    .height(60.dp)
                    .graphicsLayer {
                        scaleX = playScale
                        scaleY = playScale
                    }
                    .zoomClickable {
                        onPlayClick()
                    }
            ) {
                // Bottom 3D Base Layer (Dark Purple with Asmani, Yellow, Green Border)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF4C1D95), RoundedCornerShape(18.dp))
                        .border(
                            width = 2.5.dp,
                            brush = Brush.horizontalGradient(listOf(Color(0xFF0284C7), Color(0xFFFBBF24), Color(0xFF22C55E))),
                            shape = RoundedCornerShape(18.dp)
                        )
                )
                // Top Layer (Bengani/Purple Gradient with Dark Bengani Border)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(3.dp)
                        .background(
                            Brush.verticalGradient(listOf(Color(0xFFA855F7), Color(0xFF7E22CE))),
                            RoundedCornerShape(15.dp)
                        )
                        .border(
                            width = 2.dp,
                            color = Color(0xFF3B0764),
                            shape = RoundedCornerShape(15.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play",
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "TAP TO PLAY",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = LuckiestGuyFontFamily,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }

            // Dynamic Home Banner Slider (Only renders when active banners are configured in Admin Panel)
            val isTesterProgramActive = SettingsManager.settings?.testerProgram?.isActive ?: 1
            val activeBanners = (SettingsManager.settings?.banners ?: emptyList()).filter { banner ->
                !(banner.actionType == "tester_program" && isTesterProgramActive == 0)
            }

            if (activeBanners.isNotEmpty()) {
                Spacer(Modifier.height(4.dp))
                HomeBannerSlider(
                    banners = activeBanners,
                    onBannerClick = { banner ->
                        when (banner.actionType) {
                            "tester_program" -> {
                                if (isTesterProgramActive == 1) {
                                    onOpenTester()
                                } else {
                                    Toast.makeText(context, "Tester Program is currently paused by admin", Toast.LENGTH_SHORT).show()
                                }
                            }
                            "reward_center" -> onRewardsClick()
                            "open_url" -> {
                                if (banner.actionValue.isNotEmpty()) {
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(banner.actionValue))
                                        context.startActivity(intent)
                                    } catch (e: Exception) {}
                                }
                            }
                            else -> {
                                if (isTesterProgramActive == 1) {
                                    onOpenTester()
                                }
                            }
                        }
                    }
                )
            }

            val isTesterBannerVisible = activeBanners.any { it.actionType == "tester_program" }

            // Horizontal Action Buttons: Settings, Tester/Rewards (toggle), Rate Us, Share
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Settings Button
                HomeScreenActionButton(
                    icon = Icons.Default.Settings,
                    label = "Settings",
                    onClick = onSettingsClick
                )

                // Toggle: Agar tester banner visible hai toh Rewards button, warna Tester button
                if (isTesterBannerVisible) {
                    if (SettingsManager.isRewardsEnabled()) {
                        Spacer(Modifier.width(16.dp))
                        HomeScreenActionButton(
                            icon = Icons.Default.Star,
                            label = "Rewards",
                            onClick = onRewardsClick
                        )
                    }
                } else {
                    if (isTesterProgramActive == 1) {
                        Spacer(Modifier.width(16.dp))
                        HomeScreenActionButton(
                            icon = Icons.Default.Person,
                            label = "Tester",
                            onClick = onOpenTester
                        )
                    } else if (SettingsManager.isRewardsEnabled()) {
                        Spacer(Modifier.width(16.dp))
                        HomeScreenActionButton(
                            icon = Icons.Default.Star,
                            label = "Rewards",
                            onClick = onRewardsClick
                        )
                    }
                }

                Spacer(Modifier.width(16.dp))

                // Rate Us Button
                HomeScreenActionButton(
                    icon = Icons.Default.ThumbUp,
                    label = "Rate Us",
                    onClick = {
                        val packageName = context.packageName
                        val rateIntent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("market://details?id=$packageName")
                        ).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        try {
                            context.startActivity(rateIntent)
                        } catch (e: Exception) {
                            context.startActivity(
                                Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
                                )
                            )
                        }
                    }
                )

                Spacer(Modifier.width(16.dp))

                // Share Button
                HomeScreenActionButton(
                    icon = Icons.Default.Share,
                    label = "Share",
                    onClick = {
                        val packageName = context.packageName
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(
                                Intent.EXTRA_TEXT,
                                "I am playing Color Pipes Puzzle! A super fun and cheerful pipe puzzle adventure. Can you beat my level? Download now: https://play.google.com/store/apps/details?id=$packageName"
                            )
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share Game via"))
                    }
                )
            }

            // Live Payout Marquee Ticker on Home Page (Positioned below Setting, Rate Us, Share buttons)
            val homeTickerConfig = SettingsManager.settings?.payoutTicker
            if (homeTickerConfig?.showHome != false && (homeTickerConfig?.enabled != false)) {
                Spacer(Modifier.height(8.dp))
                LivePayoutMarqueeTicker(config = homeTickerConfig, modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp))
            }
        }
    }
}

@Composable
fun MenuListItem(
    icon: ImageVector,
    title: String,
    iconColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth(0.85f)
            .height(56.dp)
            .bouncyClickable {
                onClick()
            }
    ) {
        // 3D red-orange bottom shadow base layer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .align(Alignment.BottomCenter)
                .background(Color(0xFFC84B02), RoundedCornerShape(16.dp)) // Dark red-orange underlay
        )
        // Top layer (Bright Orange gradient)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .align(Alignment.TopCenter)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFFFF9F1C), Color(0xFFF15A24)) // Bright orange-yellow to rich orange
                    ),
                    shape = RoundedCornerShape(16.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(10.dp))
                
                // Text with thick dark shadow outline simulation
                Box {
                    // Shadow layer (Shifted slightly more to the right)
                    Text(
                        text = title,
                        color = Color(0xFF6E2200), // Dark brown/red shadow
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = LuckiestGuyFontFamily,
                        letterSpacing = 1.2.sp,
                        modifier = Modifier.offset(x = 2.5.dp, y = 2.dp)
                    )
                    // Front layer
                    Text(
                        text = title,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = LuckiestGuyFontFamily,
                        letterSpacing = 1.2.sp
                    )
                }
            }
        }
    }
}

@Composable
fun CustomScoreBanner(stars: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .shadow(2.dp, RoundedCornerShape(20.dp))
            .background(Color.White, RoundedCornerShape(20.dp))
            .border(1.5.dp, Color(0xFFCBD5E1), RoundedCornerShape(20.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Star,
            contentDescription = "Star",
            tint = Color(0xFFFFD93D),
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = "$stars",
            color = Color(0xFF262F3C),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = LuckiestGuyFontFamily
        )
    }
}

@Composable
fun HeaderBar(stars: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.CenterEnd
    ) {
        CustomScoreBanner(stars = stars)
    }
}

@Composable
fun button3D(size: Dp, onClick: () -> Unit, color: Color, animate: Boolean = false) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val infiniteTransition = rememberInfiniteTransition(label = "playPulse")
    val scaleAnim by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = if (animate && !isPressed) 1.15f else 1f,
        animationSpec = infiniteRepeatable(tween(800, easing = EaseInOutSine), RepeatMode.Reverse), label = "scale"
    )
    
    val translationY by animateDpAsState(if (isPressed) 10.dp else 0.dp, label = "button3DBounce")

    val shadowColor = Color(0xFFF9A825) // Matching SunnyYellow shadow

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(size)
                .scale(scaleAnim)
                .clickable(interactionSource = interactionSource, indication = null, onClick = {
                    onClick()
                }),
            contentAlignment = Alignment.TopCenter
        ) {
            Box(modifier = Modifier.size(size).padding(top = 10.dp).background(shadowColor, CircleShape))
            Box(
                modifier = Modifier
                    .size(size - 10.dp)
                    .offset(y = translationY)
                    .background(Brush.verticalGradient(listOf(SunnyYellow.copy(alpha = 0.8f), SunnyYellow)), CircleShape)
                    .border(5.dp, MintWhite.copy(alpha = 0.4f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.PlayArrow, null, tint = TextDark, modifier = Modifier.size(size / 2))
                    Text("PLAY", color = TextDark, fontWeight = FontWeight.Black, fontSize = 24.sp, fontFamily = LuckiestGuyFontFamily)
                }
            }
        }
    }
}

@Composable
fun CurrentLevelBadge(
    level: Int,
    isUnlocked: Boolean,
    isCompleted: Boolean,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .scale(pulseScale)
            .zoomClickable {
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        // 3D Shadow Base Underlay (Dark Emerald) with Yellow Border
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(Color(0xFF064E3B), RoundedCornerShape(16.dp))
                .border(2.dp, Color(0xFFFBBF24), RoundedCornerShape(16.dp))
        )
        // Top Layer (Vibrant Green with Glowing Asmani-Yellow-Green Border)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.90f)
                .offset(y = (-3).dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF22C55E), Color(0xFF15803D))
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
                .border(
                    width = 2.5.dp,
                    brush = Brush.horizontalGradient(listOf(Color(0xFF38BDF8), Color(0xFFFBBF24), Color(0xFF4ADE80))),
                    shape = RoundedCornerShape(16.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box {
                    Text(
                        text = "$level",
                        color = Color(0xFF064E3B),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = LuckiestGuyFontFamily,
                        modifier = Modifier.offset(x = 1.dp, y = 1.5.dp)
                    )
                    Text(
                        text = "$level",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = LuckiestGuyFontFamily
                    )
                }
                Spacer(Modifier.height(1.dp))
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Play",
                    tint = Color(0xFFFEF08A),
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Composable
fun StaticLevelBadge(
    level: Int,
    isUnlocked: Boolean,
    isCompleted: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .then(
                if (isUnlocked) Modifier.zoomClickable {
                    onClick()
                } else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        if (!isUnlocked) {
            // Locked level node: Sleek dark slate block with lock icon
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color(0xFF0F172A), RoundedCornerShape(16.dp))
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.90f)
                    .offset(y = (-3).dp)
                    .background(Color(0xFF1E293B), RoundedCornerShape(16.dp))
                    .border(1.5.dp, Color(0xFF334155), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "$level",
                        color = Color(0xFF64748B),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = LuckiestGuyFontFamily
                    )
                    Spacer(Modifier.height(2.dp))
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Locked",
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        } else {
            // Unlocked & Completed level node: 2-layer Vibrant Orange 3D Card
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color(0xFF9A3412), RoundedCornerShape(16.dp))
                    .border(2.dp, Color(0xFF22C55E), RoundedCornerShape(16.dp))
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.90f)
                    .offset(y = (-3).dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFFFB923C), Color(0xFFEA580C))
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .border(
                        width = 2.dp,
                        brush = Brush.horizontalGradient(listOf(Color(0xFF0284C7), Color(0xFFFBBF24), Color(0xFF22C55E))),
                        shape = RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box {
                        Text(
                            text = "$level",
                            color = Color(0xFF7C2D12),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = LuckiestGuyFontFamily,
                            modifier = Modifier.offset(x = 1.dp, y = 1.5.dp)
                        )
                        Text(
                            text = "$level",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = LuckiestGuyFontFamily
                        )
                    }
                    if (isCompleted) {
                        Spacer(Modifier.height(1.dp))
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Completed",
                            tint = Color(0xFF86EFAC),
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LevelBadge(
    level: Int,
    isUnlocked: Boolean,
    isCompleted: Boolean,
    isCurrent: Boolean,
    onClick: () -> Unit
) {
    if (isCurrent) {
        CurrentLevelBadge(level, isUnlocked, isCompleted, onClick)
    } else {
        StaticLevelBadge(level, isUnlocked, isCompleted, onClick)
    }
}

data class QuadColors(
    val baseGradient: List<Color>,
    val shadowColor: Color,
    val borderColor: Color,
    val activeGlow: Color
)

@Composable
fun LevelSelectScreen(
    userLevel: Int,
    stars: Int,
    coins: Int,
    unlockedLevels: Set<Int>, completedLevels: Set<Int>, 
    selectedBackgroundSkin: BackgroundSkin,
    onLevelClick: (Int) -> Unit, onBack: () -> Unit,
    onRewardClaim: () -> Unit,
    claimedRewardLevels: Set<Int> = emptySet()
) {
    val context = LocalContext.current
    
    ArrowPuzzleProBackground(selectedBackgroundSkin) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // HEADER ROW (Back Icon + Levels Title + HUD counters)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp, start = 24.dp, end = 24.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .bouncyClickable {
                                onBack()
                            }
                            .background(Color(0xFFFFF9C4), RoundedCornerShape(14.dp))
                            .border(
                                width = 2.5.dp,
                                brush = Brush.horizontalGradient(listOf(Color(0xFF0284C7), Color(0xFF22C55E))),
                                shape = RoundedCornerShape(14.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.Black,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(Modifier.width(16.dp))
                    Text(
                        text = "LEVELS",
                        style = TextStyle(
                            color = Color.Black,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = LuckiestGuyFontFamily,
                            letterSpacing = 1.sp
                        )
                    )
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Diamond Counter HUD
                    Box(
                        modifier = Modifier
                            .height(38.dp)
                            .background(Color(0xFFFFF9C4), RoundedCornerShape(19.dp))
                            .border(
                                width = 2.5.dp,
                                brush = Brush.horizontalGradient(listOf(Color(0xFF0284C7), Color(0xFF22C55E))),
                                shape = RoundedCornerShape(19.dp)
                            )
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.diamond),
                                contentDescription = "Diamond",
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "$coins",
                                color = Color(0xFF0F172A),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = LuckiestGuyFontFamily
                            )
                        }
                    }
                }
            }

            val gridState = androidx.compose.foundation.lazy.grid.rememberLazyGridState(
                initialFirstVisibleItemIndex = maxOf(0, userLevel - 7)
            )

            // Dynamic Chapter Badge based on scrolling position
            val firstVisibleIndex = gridState.firstVisibleItemIndex + 1
            val chapterName = when {
                firstVisibleIndex <= 500 -> "Beginners Track"
                firstVisibleIndex <= 1000 -> "Clever Paths"
                firstVisibleIndex <= 2000 -> "Untangle Master"
                else -> "Grandmaster Maze"
            }

            Box(
                modifier = Modifier
                    .padding(bottom = 12.dp)
                    .background(Color(0xFFFFF9E6), RoundedCornerShape(14.dp))
                    .border(
                        width = 2.5.dp,
                        brush = Brush.horizontalGradient(listOf(Color(0xFF0284C7), Color(0xFF22C55E))),
                        shape = RoundedCornerShape(14.dp)
                    )
                    .padding(horizontal = 20.dp, vertical = 7.dp)
            ) {
                Text(
                    text = chapterName.uppercase(),
                    color = Color(0xFF2563EB),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = LuckiestGuyFontFamily,
                    letterSpacing = 1.sp
                )
            }

            // LEVEL GRID CONTAINER (Card with dark slate interior and tri-color border)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(0.92f)
                    .padding(vertical = 4.dp)
                    .shadow(8.dp, RoundedCornerShape(24.dp), clip = false)
                    .background(Color(0xCC0F172A), RoundedCornerShape(24.dp))
                    .border(
                        width = 3.5.dp,
                        brush = Brush.horizontalGradient(listOf(Color(0xFF0284C7), Color(0xFFFBBF24), Color(0xFF22C55E))),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .clip(RoundedCornerShape(24.dp))
            ) {
                LazyVerticalGrid(
                    state = gridState,
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    items(TOTAL_LEVELS) { index ->
                        val level = index + 1
                        val isUnlocked = unlockedLevels.contains(level)
                        val isCompleted = completedLevels.contains(level)
                        val isCurrent = level == userLevel
                        
                        LevelBadge(
                            level = level,
                            isUnlocked = isUnlocked,
                            isCompleted = isCompleted,
                            isCurrent = isCurrent,
                            onClick = {
                                if (isUnlocked) {
                                    onLevelClick(level)
                                } else {
                                    SoundManager.playWrongSound()
                                    Toast.makeText(context, "Level Locked!", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }
                }
            }

            // Reward Card at the very bottom (Stylized 3D card)
            if (SettingsManager.isRewardsEnabled()) {
                val activeReward = SettingsManager.getRewardSettings()
                    .filter { it.status == 1 && it.requiredLevel !in claimedRewardLevels }
                    .minByOrNull { it.requiredLevel }
                if (activeReward != null) {
                    Spacer(Modifier.height(12.dp))
                    AnimatedRewardCard(
                        completedCount = completedLevels.size,
                        claimedRewardLevels = claimedRewardLevels,
                        onClaim = onRewardClaim
                    )
                }
            }
        }
    }
}

@Composable
fun AnimatedRewardCard(completedCount: Int, claimedRewardLevels: Set<Int> = emptySet(), onClaim: () -> Unit) {
    val reward = SettingsManager.getRewardSettings()
        .filter { it.status == 1 && it.requiredLevel !in claimedRewardLevels }
        .minByOrNull { it.requiredLevel }
    val requiredLevel = reward?.requiredLevel ?: 100
    val rewardAmount = reward?.rewardAmount ?: 1000
    val message = reward?.message ?: "Complete $requiredLevel levels & get $rewardAmount"

    val isReady = completedCount >= requiredLevel

    val infiniteTransition = rememberInfiniteTransition(label = "rewardPulse")
    val floatAnim by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = -10f,
        animationSpec = infiniteRepeatable(tween(2000, easing = EaseInOutSine), RepeatMode.Reverse), label = "float"
    )

    val progress = (completedCount.toFloat() / requiredLevel).coerceIn(0f, 1f)
    
    Box(
        modifier = Modifier
            .padding(horizontal = 24.dp)
            .offset(y = floatAnim.dp)
            .fillMaxWidth()
            .height(90.dp)
            .background(Color(0xFFFFF9E6), RoundedCornerShape(20.dp))
            .border(
                width = 3.dp, 
                color = Color(0xFF2C1B47), 
                shape = RoundedCornerShape(20.dp)
            )
            .bouncyClickable(enabled = isReady, onClick = onClaim)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        // Internal 3D shadow for the card
        Box(modifier = Modifier.fillMaxWidth().height(4.dp).align(Alignment.BottomCenter).background(Color(0xFF2C1B47).copy(alpha = 0.05f), RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp)))
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Icon Circle (Rupee replaced with Diamond icon)
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(if (isReady) Color(0xFF4CAF50) else Color(0xFFC4BEAF), CircleShape)
                    .border(2.5.dp, Color(0xFF2C1B47), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.diamond),
                    contentDescription = "Diamond",
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isReady) "READY TO CLAIM!" else "LOCKED",
                    color = if (isReady) Color(0xFF2E7D32) else Color(0xFF6E6E6E),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = LuckiestGuyFontFamily
                )
                Text(
                    text = message,
                    color = Color(0xFF2C1B47),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = LuckiestGuyFontFamily
                )
                Spacer(Modifier.height(8.dp))
                
                // Progress Bar
                Box(modifier = Modifier.fillMaxWidth().height(8.dp).background(Color(0xFFC4BEAF).copy(alpha = 0.5f), CircleShape).border(1.dp, Color(0xFF2C1B47), CircleShape)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress)
                            .fillMaxHeight()
                            .background(
                                Brush.horizontalGradient(if (isReady) listOf(Color(0xFF4CAF50), Color(0xFF2E7D32)) else listOf(Color(0xFF8338EC), Color(0xFF7B2CBF))),
                                CircleShape
                            )
                    )
                }
            }
        }
    }
}

@Composable
fun RewardCenterScreen(
    userCurrentLevel: Int,
    claimedRewardLevels: Set<Int>,
    rewardSettings: List<RewardSetting>,
    onBack: () -> Unit,
    onClaimMethodSelected: (method: String, amount: Int, reqLevel: Int) -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Claim Reward, 1: History
    
    var historyList by remember { mutableStateOf<List<RewardHistoryItem>>(emptyList()) }
    var isInitialHistoryLoading by remember { mutableStateOf(true) }

    // Silently pre-fetch settings & history on screen open to prevent tab-switch flicker/blink
    LaunchedEffect(Unit) {
        SettingsManager.init(context)
        UserManager.getRewardHistory(context) { list ->
            historyList = list
            isInitialHistoryLoading = false
        }
    }

    // Refresh history silently when entering History tab without clearing existing items
    LaunchedEffect(selectedTab) {
        if (selectedTab == 1) {
            UserManager.getRewardHistory(context) { list ->
                historyList = list
                isInitialHistoryLoading = false
            }
        }
    }

    val liveRewardSettings = SettingsManager.settings?.rewardSettings ?: rewardSettings.ifEmpty { SettingsManager.getRewardSettings() }
    val activeRewards = liveRewardSettings.filter { it.status == 1 }.sortedBy { it.requiredLevel }

    // User can select between active milestones if there are multiple
    var selectedMilestoneLevel by remember(activeRewards, claimedRewardLevels, userCurrentLevel) {
        mutableStateOf(
            activeRewards.firstOrNull { it.requiredLevel !in claimedRewardLevels && userCurrentLevel >= it.requiredLevel }?.requiredLevel
                ?: activeRewards.firstOrNull { it.requiredLevel !in claimedRewardLevels }?.requiredLevel
                ?: activeRewards.firstOrNull()?.requiredLevel
                ?: 5
        )
    }

    val currentSelectedMilestone = activeRewards.find { it.requiredLevel == selectedMilestoneLevel }
        ?: activeRewards.firstOrNull()

    val isMilestoneActive = currentSelectedMilestone != null && currentSelectedMilestone.status == 1
    val isSelectedClaimed = currentSelectedMilestone != null && currentSelectedMilestone.requiredLevel in claimedRewardLevels
    val isReadyToClaim = isMilestoneActive && userCurrentLevel >= currentSelectedMilestone!!.requiredLevel && !isSelectedClaimed
    val isAllClaimed = activeRewards.isNotEmpty() && activeRewards.all { it.requiredLevel in claimedRewardLevels }

    val infiniteTransition = rememberInfiniteTransition(label = "rewardCenterPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.985f,
        targetValue = 1.015f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- TOP BAR (Matching size buttons on Left & Right) ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Back Button (Matching all game screens)
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .bouncyClickable {
                            onBack()
                        }
                        .shadow(4.dp, RoundedCornerShape(14.dp), clip = false)
                        .background(Color(0xFFFFF9C4), RoundedCornerShape(14.dp))
                        .border(
                            width = 2.5.dp,
                            brush = Brush.horizontalGradient(listOf(Color(0xFF0284C7), Color(0xFF22C55E))),
                            shape = RoundedCornerShape(14.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(0xFF1E152A),
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Current Level Button (Same 46.dp size as Back Button)
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .shadow(4.dp, RoundedCornerShape(14.dp), clip = false)
                        .background(Color(0xFFFFF9C4), RoundedCornerShape(14.dp))
                        .border(
                            width = 2.5.dp,
                            brush = Brush.horizontalGradient(listOf(Color(0xFF0284C7), Color(0xFF22C55E))),
                            shape = RoundedCornerShape(14.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "LVL",
                            color = Color(0xFF0284C7),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = LuckiestGuyFontFamily,
                            lineHeight = 11.sp
                        )
                        Text(
                            text = "$userCurrentLevel",
                            color = Color(0xFF1E152A),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = LuckiestGuyFontFamily,
                            lineHeight = 14.sp
                        )
                    }
                }
            }

            // --- TITLE (Centered Below Top Buttons) ---
            Text(
                text = "REWARD CENTER",
                style = TextStyle(
                    color = Color(0xFF0284C7),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = LuckiestGuyFontFamily,
                    letterSpacing = 1.sp,
                    shadow = Shadow(
                        color = Color(0xFF03224C),
                        offset = Offset(0f, 3f),
                        blurRadius = 2f
                    )
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 2.dp, bottom = 8.dp),
                maxLines = 1
            )

            // --- TAB SWITCHER (Soft Cream + Tri-color Border) ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .shadow(4.dp, RoundedCornerShape(24.dp), clip = false)
                    .background(Color(0xFFFFF9E6), RoundedCornerShape(24.dp))
                    .border(
                        width = 2.5.dp,
                        brush = Brush.horizontalGradient(
                            listOf(Color(0xFF0284C7), Color(0xFFFACC15), Color(0xFF22C55E))
                        ),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .padding(3.dp)
            ) {
                Row(modifier = Modifier.fillMaxSize()) {
                    // Tab 1: Claim Reward
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(
                                if (selectedTab == 0) Brush.horizontalGradient(
                                    listOf(Color(0xFF4ADE80), Color(0xFF16A34A))
                                ) else Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent)),
                                RoundedCornerShape(20.dp)
                            )
                            .bouncyClickable {
                                selectedTab = 0
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🎁 CLAIM REWARD",
                            color = if (selectedTab == 0) Color.White else Color(0xFF64748B),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = LuckiestGuyFontFamily,
                            maxLines = 1
                        )
                    }

                    // Tab 2: History
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(
                                if (selectedTab == 1) Brush.horizontalGradient(
                                    listOf(Color(0xFF0284C7), Color(0xFF2563EB))
                                ) else Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent)),
                                RoundedCornerShape(20.dp)
                            )
                            .bouncyClickable {
                                selectedTab = 1
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "📜 HISTORY",
                            color = if (selectedTab == 1) Color.White else Color(0xFF64748B),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = LuckiestGuyFontFamily,
                            maxLines = 1
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // --- TAB CONTENT ---
            Crossfade(
                targetState = selectedTab,
                animationSpec = tween(220),
                label = "tabTransition",
                modifier = Modifier.fillMaxSize()
            ) { tab ->
                if (tab == 0) {
                    // ==================== TAB 1: CLAIM REWARD ====================
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (isMilestoneActive && currentSelectedMilestone != null) {
                            // MULTI-MILESTONE SELECTOR TABS (If more than 1 milestone configured)
                            if (activeRewards.size > 1) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    activeRewards.forEach { ms ->
                                        val isMsClaimed = ms.requiredLevel in claimedRewardLevels
                                        val isMsUnlocked = userCurrentLevel >= ms.requiredLevel
                                        val isSelected = ms.requiredLevel == currentSelectedMilestone.requiredLevel

                                        Box(
                                            modifier = Modifier
                                                .shadow(2.dp, RoundedCornerShape(12.dp), clip = false)
                                                .background(
                                                    if (isSelected) Color(0xFFFFF9C4)
                                                    else if (isMsClaimed) Color(0xFFDCFCE7)
                                                    else if (isMsUnlocked) Color(0xFFFEF3C7)
                                                    else Color.White,
                                                    RoundedCornerShape(12.dp)
                                                )
                                                .border(
                                                    2.dp,
                                                    if (isSelected) Color(0xFF0284C7)
                                                    else if (isMsClaimed) Color(0xFF16A34A)
                                                    else if (isMsUnlocked) Color(0xFFF59E0B)
                                                    else Color(0xFFCBD5E1),
                                                    RoundedCornerShape(12.dp)
                                                )
                                                .bouncyClickable {
                                                    selectedMilestoneLevel = ms.requiredLevel
                                                }
                                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Text(
                                                text = "LVL ${ms.requiredLevel} (₹${ms.rewardAmount}) " +
                                                        if (isMsClaimed) "✓"
                                                        else if (isMsUnlocked) "🟢 Ready"
                                                        else "🔒",
                                                color = Color(0xFF1E293B),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Black,
                                                fontFamily = LuckiestGuyFontFamily,
                                                softWrap = false
                                            )
                                        }
                                    }
                                }
                            }

                            val reqLevel = currentSelectedMilestone.requiredLevel
                            val rewardAmt = currentSelectedMilestone.rewardAmount
                            val progress = (userCurrentLevel.toFloat() / reqLevel).coerceIn(0f, 1f)

                            // ==================== BEAUTIFIED HERO MILESTONE CARD ====================
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .graphicsLayer {
                                        if (isReadyToClaim) {
                                            scaleX = pulseScale
                                            scaleY = pulseScale
                                        }
                                    }
                                    .shadow(8.dp, RoundedCornerShape(24.dp), clip = false)
                                    .background(Color(0xFFFFF9E6), RoundedCornerShape(24.dp))
                                    .border(
                                        width = 3.5.dp,
                                        brush = Brush.horizontalGradient(
                                            listOf(Color(0xFF0284C7), Color(0xFFFACC15), Color(0xFF22C55E))
                                        ),
                                        shape = RoundedCornerShape(24.dp)
                                    )
                                    .padding(16.dp)
                            ) {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    // Top Row: Badge Icon + Title + Cash Pill
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            modifier = Modifier.weight(1f),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            // Badge Icon
                                            Box(
                                                modifier = Modifier
                                                    .size(44.dp)
                                                    .background(Color(0xFFFFF9C4), RoundedCornerShape(14.dp))
                                                    .border(
                                                        2.dp,
                                                        Brush.horizontalGradient(
                                                            listOf(Color(0xFF0284C7), Color(0xFF22C55E))
                                                        ),
                                                        RoundedCornerShape(14.dp)
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = if (isSelectedClaimed) "✓" else if (isReadyToClaim) "🏆" else "🔒",
                                                    fontSize = 20.sp
                                                )
                                            }

                                            Column(
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text(
                                                    text = if (isSelectedClaimed) "LEVEL $reqLevel CLAIMED"
                                                    else if (isReadyToClaim) "🎉 LEVEL $reqLevel UNLOCKED!"
                                                    else "LEVEL $reqLevel MILESTONE",
                                                    color = Color(0xFF1E293B),
                                                    fontSize = 15.sp,
                                                    fontWeight = FontWeight.Black,
                                                    fontFamily = LuckiestGuyFontFamily,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = if (isReadyToClaim) "Target Completed • Claim Now"
                                                    else if (isSelectedClaimed) "Claim Pending in History"
                                                    else "Play puzzles to unlock bonus",
                                                    color = Color(0xFF64748B),
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }

                                        Spacer(Modifier.width(8.dp))

                                        // Cash Pill
                                        Box(
                                            modifier = Modifier
                                                .wrapContentWidth()
                                                .background(
                                                    brush = Brush.verticalGradient(
                                                        if (isReadyToClaim) listOf(Color(0xFF4ADE80), Color(0xFF16A34A))
                                                        else if (isSelectedClaimed) listOf(Color(0xFF60A5FA), Color(0xFF2563EB))
                                                        else listOf(Color(0xFFFF9F1C), Color(0xFFF15A24))
                                                    ),
                                                    shape = RoundedCornerShape(14.dp)
                                                )
                                                .border(2.dp, Color.White.copy(alpha = 0.8f), RoundedCornerShape(14.dp))
                                                .padding(horizontal = 10.dp, vertical = 6.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "₹$rewardAmt CASH",
                                                color = Color.White,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Black,
                                                fontFamily = LuckiestGuyFontFamily,
                                                softWrap = false,
                                                maxLines = 1
                                            )
                                        }
                                    }

                                    // Progress Bar
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(5.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = if (isSelectedClaimed) "Reward Status:"
                                                else if (isReadyToClaim) "Milestone Progress:"
                                                else "Progress:",
                                                color = Color(0xFF1E293B),
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1
                                            )
                                            Text(
                                                text = "${userCurrentLevel.coerceAtMost(reqLevel)} / $reqLevel Levels (${(progress * 100).toInt()}%)",
                                                color = Color(0xFF0284C7),
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Black,
                                                fontFamily = LuckiestGuyFontFamily,
                                                maxLines = 1
                                            )
                                        }

                                        // Progress Track
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(16.dp)
                                                .background(Color(0xFFE2E8F0), CircleShape)
                                                .border(1.5.dp, Color(0xFFCBD5E1), CircleShape)
                                                .padding(2.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth(progress)
                                                    .fillMaxHeight()
                                                    .background(
                                                        brush = Brush.horizontalGradient(
                                                            listOf(Color(0xFF0284C7), Color(0xFF22C55E))
                                                        ),
                                                        shape = CircleShape
                                                    )
                                            )
                                        }
                                    }

                                    // Instruction / Action Bubble
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(
                                                if (isReadyToClaim) Color(0xFFDCFCE7)
                                                else if (isSelectedClaimed) Color(0xFFDBEAFE)
                                                else Color(0xFFFEF3C7),
                                                RoundedCornerShape(12.dp)
                                            )
                                            .border(
                                                1.dp,
                                                if (isReadyToClaim) Color(0xFF86EFAC)
                                                else if (isSelectedClaimed) Color(0xFF93C5FD)
                                                else Color(0xFFFDE68A),
                                                RoundedCornerShape(12.dp)
                                            )
                                            .padding(horizontal = 10.dp, vertical = 7.dp)
                                    ) {
                                        Text(
                                            text = if (isSelectedClaimed)
                                                "✅ Claim submitted! Check History tab for status."
                                            else if (isReadyToClaim)
                                                "👉 Select any withdrawal method below to claim ₹$rewardAmt!"
                                            else
                                                "🎯 Reach Level $reqLevel to unlock ₹$rewardAmt instant cash withdrawal.",
                                            color = Color(0xFF1E293B),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            lineHeight = 15.sp
                                        )
                                    }
                                }
                            }

                            // SECTION HEADER
                            Text(
                                text = "WITHDRAWAL METHODS",
                                color = Color(0xFF1E293B),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = LuckiestGuyFontFamily,
                                letterSpacing = 0.8.sp,
                                modifier = Modifier.padding(top = 4.dp)
                            )

                            // METHODS LIST
                            val methods = listOf(
                                Triple("UPI / Paytm", "Fast Bank & Wallet Transfer", Color(0xFF10B981)),
                                Triple("Google Play Code", "Play Store Redeem Gift Card", Color(0xFF3B82F6)),
                                Triple("Amazon Voucher", "Instant Shopping Gift Voucher", Color(0xFFF59E0B)),
                                Triple("Free Fire Diamonds", "Direct In-Game Top-Up", Color(0xFFEF4444))
                            )

                            methods.forEach { (methodName, desc, color) ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .shadow(3.dp, RoundedCornerShape(18.dp), clip = false)
                                        .background(Color.White, RoundedCornerShape(18.dp))
                                        .border(
                                            width = 2.dp,
                                            brush = if (isReadyToClaim) {
                                                Brush.horizontalGradient(listOf(Color(0xFF0284C7), Color(0xFF22C55E)))
                                            } else {
                                                Brush.horizontalGradient(listOf(Color(0xFFE2E8F0), Color(0xFFCBD5E1)))
                                            },
                                            shape = RoundedCornerShape(18.dp)
                                        )
                                        .bouncyClickable(enabled = isReadyToClaim) {
                                            if (isReadyToClaim) {
                                                onClaimMethodSelected(methodName, rewardAmt, reqLevel)
                                            }
                                        }
                                        .padding(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            // Method Icon Capsule
                                            Box(
                                                modifier = Modifier
                                                    .size(42.dp)
                                                    .background(
                                                        if (isReadyToClaim) Color(0xFFFFF9C4)
                                                        else Color(0xFFF1F5F9),
                                                        CircleShape
                                                    )
                                                    .border(
                                                        1.5.dp,
                                                        if (isReadyToClaim) Color(0xFF0284C7) else Color(0xFFCBD5E1),
                                                        CircleShape
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = if (isReadyToClaim) "₹" else "🔒",
                                                    color = if (isReadyToClaim) Color(0xFF0284C7) else Color(0xFF94A3B8),
                                                    fontSize = 18.sp,
                                                    fontWeight = FontWeight.Black
                                                )
                                            }

                                            Column(
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text(
                                                    text = methodName,
                                                    color = Color(0xFF1E293B),
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Black,
                                                    fontFamily = LuckiestGuyFontFamily,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = if (isReadyToClaim) "₹$rewardAmt Cash • $desc"
                                                    else if (isSelectedClaimed) "✅ Level $reqLevel submitted"
                                                    else "🔒 Reach Level $reqLevel to Unlock",
                                                    color = if (isReadyToClaim) Color(0xFF16A34A) else Color(0xFF64748B),
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }

                                        if (isReadyToClaim) {
                                            Spacer(Modifier.width(6.dp))
                                            Box(
                                                modifier = Modifier
                                                    .wrapContentWidth()
                                                    .background(
                                                        brush = Brush.verticalGradient(
                                                            listOf(Color(0xFF4ADE80), Color(0xFF16A34A))
                                                        ),
                                                        shape = RoundedCornerShape(12.dp)
                                                    )
                                                    .border(1.5.dp, Color(0xFF0284C7), RoundedCornerShape(12.dp))
                                                    .padding(horizontal = 12.dp, vertical = 7.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "CLAIM ₹$rewardAmt",
                                                    color = Color.White,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Black,
                                                    fontFamily = LuckiestGuyFontFamily,
                                                    softWrap = false,
                                                    maxLines = 1
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            // Empty State / CPA Mode
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .shadow(6.dp, RoundedCornerShape(22.dp), clip = false)
                                    .background(Color(0xFFFFF9E6), RoundedCornerShape(22.dp))
                                    .border(
                                        3.dp,
                                        Brush.horizontalGradient(
                                            listOf(Color(0xFF0284C7), Color(0xFFFACC15), Color(0xFF22C55E))
                                        ),
                                        RoundedCornerShape(22.dp)
                                    )
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(
                                        text = "🌟 SPECIAL BONUS EVENTS",
                                        color = Color(0xFF0284C7),
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Black,
                                        fontFamily = LuckiestGuyFontFamily
                                    )
                                    Text(
                                        text = "Special reward bonus events will open soon! Keep playing and mastering puzzle levels.",
                                        color = Color(0xFF1E293B),
                                        fontSize = 13.sp,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(80.dp))
                    }
                } else {
                    // ==================== TAB 2: HISTORY ====================
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (isInitialHistoryLoading && historyList.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = Color(0xFF0284C7))
                            }
                        } else if (historyList.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .shadow(6.dp, RoundedCornerShape(22.dp), clip = false)
                                    .background(Color(0xFFFFF9E6), RoundedCornerShape(22.dp))
                                    .border(
                                        3.dp,
                                        Brush.horizontalGradient(
                                            listOf(Color(0xFF0284C7), Color(0xFFFACC15), Color(0xFF22C55E))
                                        ),
                                        RoundedCornerShape(22.dp)
                                    )
                                    .padding(28.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "🎁",
                                        fontSize = 36.sp
                                    )
                                    Text(
                                        text = "NO CLAIMS YET",
                                        color = Color(0xFF0284C7),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Black,
                                        fontFamily = LuckiestGuyFontFamily
                                    )
                                    Text(
                                        text = "Complete milestone levels to unlock rewards and submit your payout claims.",
                                        color = Color(0xFF64748B),
                                        fontSize = 12.sp,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        } else {
                            historyList.forEach { item ->
                                val isCompleted = item.status.equals("completed", ignoreCase = true) || item.status.equals("approved", ignoreCase = true)
                                val isRejected = item.status.equals("rejected", ignoreCase = true)

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .shadow(3.dp, RoundedCornerShape(16.dp), clip = false)
                                        .background(Color.White, RoundedCornerShape(16.dp))
                                        .border(
                                            2.dp,
                                            when {
                                                isCompleted -> Color(0xFF22C55E)
                                                isRejected -> Color(0xFFEF4444)
                                                else -> Color(0xFFF59E0B)
                                            },
                                            RoundedCornerShape(16.dp)
                                        )
                                        .padding(14.dp)
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column(
                                                verticalArrangement = Arrangement.spacedBy(3.dp),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text(
                                                    text = if (item.type.equals("tester", ignoreCase = true)) "TESTER (CYCLE ${item.cycle}) • ${item.method.uppercase()}" else item.method.uppercase(),
                                                    color = Color(0xFF1E293B),
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Black,
                                                    fontFamily = LuckiestGuyFontFamily,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = "Account: ${item.account}",
                                                    color = Color(0xFF0284C7),
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                if (item.createdAt.isNotEmpty()) {
                                                    Text(
                                                        text = item.createdAt,
                                                        color = Color(0xFF64748B),
                                                        fontSize = 10.sp,
                                                        maxLines = 1
                                                    )
                                                }
                                            }

                                            Column(
                                                horizontalAlignment = Alignment.End,
                                                verticalArrangement = Arrangement.spacedBy(3.dp)
                                            ) {
                                                Text(
                                                    text = "₹${item.amount}",
                                                    color = Color(0xFF1E293B),
                                                    fontSize = 15.sp,
                                                    fontWeight = FontWeight.Black,
                                                    fontFamily = LuckiestGuyFontFamily,
                                                    softWrap = false
                                                )
                                                Box(
                                                    modifier = Modifier
                                                        .background(
                                                            when {
                                                                isCompleted -> Color(0xFFDCFCE7)
                                                                isRejected -> Color(0xFFFEE2E2)
                                                                else -> Color(0xFFFEF3C7)
                                                            },
                                                            RoundedCornerShape(8.dp)
                                                        )
                                                        .border(
                                                            1.dp,
                                                            when {
                                                                isCompleted -> Color(0xFF86EFAC)
                                                                isRejected -> Color(0xFFFCA5A5)
                                                                else -> Color(0xFFFDE68A)
                                                            },
                                                            RoundedCornerShape(8.dp)
                                                        )
                                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                                ) {
                                                    Text(
                                                        text = when {
                                                            isCompleted -> "COMPLETED"
                                                            isRejected -> "REJECTED"
                                                            else -> "PENDING"
                                                        },
                                                        color = when {
                                                            isCompleted -> Color(0xFF15803D)
                                                            isRejected -> Color(0xFFB91C1C)
                                                            else -> Color(0xFFB45309)
                                                        },
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Black,
                                                        fontFamily = LuckiestGuyFontFamily
                                                    )
                                                }
                                            }
                                        }

                                        if (!item.voucherCode.isNullOrBlank()) {
                                            val voucherCode = item.voucherCode
                                            val clipboard = LocalClipboardManager.current
                                            val haptic = LocalHapticFeedback.current
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(Color(0xFFFFF9E6), RoundedCornerShape(10.dp))
                                                    .border(
                                                        1.5.dp,
                                                        Brush.horizontalGradient(
                                                            listOf(Color(0xFF0284C7), Color(0xFF22C55E))
                                                        ),
                                                        RoundedCornerShape(10.dp)
                                                    )
                                                    .padding(8.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(text = "🎁 REDEEM CODE / VOUCHER", color = Color(0xFF0284C7), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                                        Text(text = voucherCode, color = Color(0xFF1E293B), fontSize = 13.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                                                    }
                                                    Button(
                                                        onClick = {
                                                            clipboard.setText(AnnotatedString(voucherCode))
                                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                            Toast.makeText(context, "Copied: $voucherCode", Toast.LENGTH_SHORT).show()
                                                        },
                                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                                        shape = RoundedCornerShape(8.dp)
                                                    ) {
                                                        Text(text = "📋 COPY", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                            }
                                        }

                                        if (!item.adminNotes.isNullOrBlank()) {
                                            Text(text = "Note: ${item.adminNotes}", color = Color(0xFF64748B), fontSize = 10.sp, fontStyle = FontStyle.Italic)
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(20.dp))
                    }
                }
            }
        }
    }
}

// ==================== ASYNC NETWORK IMAGE & BANNER SLIDER ====================

@Composable
fun AsyncNetworkImage(
    url: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    var bitmap by remember(url) { mutableStateOf<android.graphics.Bitmap?>(null) }
    var isLoading by remember(url) { mutableStateOf(true) }

    LaunchedEffect(url) {
        if (url.isNotEmpty()) {
            withContext(Dispatchers.IO) {
                try {
                    val conn = (java.net.URL(url).openConnection() as java.net.HttpURLConnection).apply {
                        connectTimeout = 10000
                        readTimeout = 10000
                        instanceFollowRedirects = true
                        setRequestProperty("User-Agent", "Mozilla/5.0 (Android; Mobile)")
                    }
                    conn.inputStream.use { stream ->
                        val bmp = BitmapFactory.decodeStream(stream)
                        withContext(Dispatchers.Main) {
                            bitmap = bmp
                            isLoading = false
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        isLoading = false
                    }
                }
            }
        } else {
            isLoading = false
        }
    }

    if (bitmap != null) {
        Image(
            bitmap = bitmap!!.asImageBitmap(),
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale
        )
    } else {
        Box(
            modifier = modifier
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xFF8338EC), Color(0xFF3A86FF), Color(0xFF06D6A0))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "🎮",
                fontSize = 28.sp
            )
        }
    }
}

@Composable
fun HomeBannerSlider(
    banners: List<com.color.pipe.puzzle.model.BannerItem>,
    onBannerClick: (com.color.pipe.puzzle.model.BannerItem) -> Unit
) {
    if (banners.isEmpty()) {
        return
    }

    var currentIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(banners.size) {
        if (banners.size > 1) {
            while (true) {
                delay(3800)
                currentIndex = (currentIndex + 1) % banners.size
            }
        }
    }

    val currentBanner = banners[currentIndex.coerceIn(0, banners.size - 1)]

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(170.dp)
                .bouncyClickable {
                    onBannerClick(currentBanner)
                }
        ) {
            // 3D Bottom Shadow
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .offset(y = 4.dp)
                    .background(Color(0xFF1E152A), RoundedCornerShape(20.dp))
            )

            // Top Banner Body
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color(0xFF7209B7), Color(0xFF3A0CA3), Color(0xFF4361EE))
                        )
                    )
                    .border(width = 3.dp, brush = Brush.horizontalGradient(colors = listOf(Color(0xFF00F5D4), Color(0xFFFFD166), Color(0xFF06D6A0))), shape = RoundedCornerShape(20.dp))
            ) {
                if (currentBanner.imageUrl.isNotEmpty()) {
                    AsyncNetworkImage(
                        url = currentBanner.imageUrl,
                        contentDescription = currentBanner.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                // If banner has title, description OR buttonText, show sleek bottom gradient overlay
                val hasTitle = currentBanner.title.isNotBlank()
                val hasDesc = currentBanner.description.isNotBlank()
                val hasBtn = currentBanner.buttonText.isNotBlank()
                val hasTextOrBtn = hasTitle || hasDesc || hasBtn

                if (hasTextOrBtn) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.35f),
                                        Color.Black.copy(alpha = 0.90f)
                                    ),
                                    startY = 180f
                                )
                            )
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        contentAlignment = Alignment.BottomStart
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = if (hasBtn) 8.dp else 0.dp),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                if (hasTitle) {
                                    Text(
                                        text = currentBanner.title,
                                        color = Color.White,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = LuckiestGuyFontFamily,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                if (hasDesc) {
                                    Text(
                                        text = currentBanner.description,
                                        color = Color(0xFFF1F5F9),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            // Dynamic Action Button from Admin
                            if (hasBtn) {
                                Box(
                                    modifier = Modifier
                                        .background(
                                            brush = Brush.verticalGradient(
                                                colors = listOf(Color(0xFF06D6A0), Color(0xFF049669))
                                            ),
                                            shape = RoundedCornerShape(14.dp)
                                        )
                                        .border(1.5.dp, Color(0xFFDCFCE7), RoundedCornerShape(14.dp))
                                        .padding(horizontal = 14.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = currentBanner.buttonText,
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Black,
                                        fontFamily = LuckiestGuyFontFamily
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Indicator Dots (Only if multiple banners)
        if (banners.size > 1) {
            Spacer(Modifier.height(6.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                banners.forEachIndexed { idx, _ ->
                    Box(
                        modifier = Modifier
                            .size(if (idx == currentIndex) 16.dp else 6.dp, 6.dp)
                            .background(
                                color = if (idx == currentIndex) Color(0xFFFFD166) else Color.White.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(3.dp)
                            )
                    )
                }
            }
        }
    }
}

// ==================== LIVE PAYOUT MARQUEE TICKER ====================

@Composable
fun LivePayoutMarqueeTicker(
    config: com.color.pipe.puzzle.model.PayoutTickerConfig?,
    modifier: Modifier = Modifier
) {
    if (config == null || !config.enabled || config.items.isEmpty()) return

    val items = config.items
    val speed = config.speed.coerceIn(10, 300)
    val scrollState = rememberScrollState()

    LaunchedEffect(items, speed) {
        while (true) {
            val max = scrollState.maxValue
            if (max > 0) {
                val current = scrollState.value
                val remaining = max - current
                if (remaining > 0) {
                    val durationMs = (speed * 1000L * remaining / max.coerceAtLeast(1)).toInt().coerceAtLeast(100)
                    scrollState.animateScrollTo(
                        value = max,
                        animationSpec = tween(
                            durationMillis = durationMs,
                            easing = LinearEasing
                        )
                    )
                }
                scrollState.scrollTo(0)
            } else {
                kotlinx.coroutines.delay(300)
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(38.dp)
            .clip(RoundedCornerShape(19.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(
                        Color(0xFF1E152A).copy(alpha = 0.94f),
                        Color(0xFF2D1F47).copy(alpha = 0.94f)
                    )
                ),
                RoundedCornerShape(19.dp)
            )
            .border(1.5.dp, Color(0xFF38BDF8).copy(alpha = 0.5f), RoundedCornerShape(19.dp))
            .padding(start = 4.dp, end = 10.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Live badge fixed on left
            Box(
                modifier = Modifier
                    .background(
                        Brush.horizontalGradient(listOf(Color(0xFF0EA5E9), Color(0xFF0284C7))),
                        RoundedCornerShape(14.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(Color(0xFF86EFAC), CircleShape)
                    )
                    Text(
                        text = "LIVE",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = LuckiestGuyFontFamily
                    )
                }
            }

            Spacer(Modifier.width(8.dp))

            // Smooth Marquee Stream
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxHeight()
                        .horizontalScroll(scrollState, enabled = false),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    val streamList = items + items + items
                    streamList.forEach { item ->
                        val methodBg = when {
                            item.method.contains("UPI", ignoreCase = true) -> Color(0xFF059669)
                            item.method.contains("Google", ignoreCase = true) || item.method.contains("Play", ignoreCase = true) -> Color(0xFF0284C7)
                            item.method.contains("Amazon", ignoreCase = true) -> Color(0xFFD97706)
                            item.method.contains("Paytm", ignoreCase = true) -> Color(0xFF0369A1)
                            else -> Color(0xFF7C3AED)
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = item.name,
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "claimed",
                                color = Color(0xFFCBD5E1),
                                fontSize = 11.sp
                            )
                            Box(
                                modifier = Modifier
                                    .background(methodBg, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "₹${item.amount} ${item.method}",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = LuckiestGuyFontFamily
                                )
                            }
                            if (item.time.isNotEmpty()) {
                                Text(
                                    text = item.time,
                                    color = Color(0xFF94A3B8),
                                    fontSize = 10.sp
                                )
                            }
                            Text(
                                text = "•",
                                color = Color(0xFF64748B),
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                // Left Edge Fade
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(14.dp)
                        .align(Alignment.CenterStart)
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF1E152A), Color.Transparent)
                            )
                        )
                )

                // Right Edge Fade
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(20.dp)
                        .align(Alignment.CenterEnd)
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color.Transparent, Color(0xFF2D1F47))
                            )
                        )
                )
            }
        }
    }
}

// ==================== 7-DAY TESTER PROGRAM SCREENS ====================

@Composable
fun TesterRegistrationScreen(
    onBack: () -> Unit,
    onSuccess: (isInstantApproved: Boolean) -> Unit
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }

    // Account / Email Picker Launcher
    val accountPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val pickedEmail = result.data?.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
            if (!pickedEmail.isNullOrBlank()) {
                email = pickedEmail.trim()
            }
        }
    }

    val launchEmailPicker = {
        try {
            val intent = AccountManager.newChooseAccountIntent(
                null, null, arrayOf("com.google"), null, null, null, null
            )
            accountPickerLauncher.launch(intent)
        } catch (e: Exception) {
            try {
                val genericIntent = AccountManager.newChooseAccountIntent(
                    null, null, null, null, null, null, null
                )
                accountPickerLauncher.launch(genericIntent)
            } catch (ex: Exception) {
                Toast.makeText(context, "Could not open account picker", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val isMobileValid = mobile.length == 10 && (mobile.startsWith("6") || mobile.startsWith("7") || mobile.startsWith("8") || mobile.startsWith("9"))
    val isEmailValid = email.isNotBlank() && android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.bg),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.25f))
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 14.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- TOP BAR ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Back Button (Matching all game screens)
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .bouncyClickable {
                            onBack()
                        }
                        .shadow(4.dp, RoundedCornerShape(14.dp), clip = false)
                        .background(Color(0xFFFFF9C4), RoundedCornerShape(14.dp))
                        .border(
                            width = 2.5.dp,
                            brush = Brush.horizontalGradient(listOf(Color(0xFF0284C7), Color(0xFF22C55E))),
                            shape = RoundedCornerShape(14.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(0xFF1E152A),
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Title
                Text(
                    text = "TESTER PROGRAM",
                    style = TextStyle(
                        color = Color(0xFF0284C7),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = LuckiestGuyFontFamily,
                        letterSpacing = 0.8.sp,
                        shadow = Shadow(
                            color = Color(0xFF03224C),
                            offset = Offset(0f, 3f),
                            blurRadius = 2f
                        )
                    ),
                    maxLines = 1
                )

                // Placeholder for balance
                Spacer(Modifier.size(46.dp))
            }

            // Live Payout Marquee Ticker on Registration Screen
            val regTicker = SettingsManager.settings?.payoutTicker
            if (regTicker?.showRegister != false && (regTicker?.enabled != false)) {
                LivePayoutMarqueeTicker(config = regTicker, modifier = Modifier.padding(bottom = 12.dp))
            }

            // Dynamic Hero Card from Admin Settings
            val tProgram = SettingsManager.settings?.testerProgram
            val badgeText = tProgram?.badgeText?.ifBlank { "EARN ₹150 GUARANTEED" } ?: "EARN ₹150 GUARANTEED"
            val badgeColor = runCatching {
                Color(android.graphics.Color.parseColor(tProgram?.badgeColor ?: "#059669"))
            }.getOrDefault(Color(0xFF059669))
            val heroTitle = tProgram?.title?.ifBlank { "Join the 7-Day Testing Team" } ?: "Join the 7-Day Testing Team"
            val heroDesc = tProgram?.description?.ifBlank { "Play 10 puzzle levels every day for 7 days, submit short daily bug reviews, and claim direct UPI/Paytm payout on Day 7!" } ?: "Play 10 puzzle levels every day for 7 days, submit short daily bug reviews, and claim direct UPI/Paytm payout on Day 7!"
            val perk1T = tProgram?.perk1Title?.ifBlank { "10 Levels/Day" } ?: "10 Levels/Day"
            val perk1S = tProgram?.perk1Subtitle?.ifBlank { "Daily Target" } ?: "Daily Target"
            val perk2T = tProgram?.perk2Title?.ifBlank { "Bug Reports" } ?: "Bug Reports"
            val perk2S = tProgram?.perk2Subtitle?.ifBlank { "Quick Review" } ?: "Quick Review"
            val perk3T = tProgram?.perk3Title?.ifBlank { "Instant Cash" } ?: "Instant Cash"
            val perk3S = tProgram?.perk3Subtitle?.ifBlank { "₹150 Reward" } ?: "₹150 Reward"

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(6.dp, RoundedCornerShape(24.dp), clip = false)
                    .background(Color(0xFFFFF9E6), RoundedCornerShape(24.dp))
                    .border(
                        3.dp,
                        Brush.horizontalGradient(listOf(Color(0xFF0284C7), Color(0xFFFACC15), Color(0xFF22C55E))),
                        RoundedCornerShape(24.dp)
                    )
                    .padding(18.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .background(badgeColor, RoundedCornerShape(12.dp))
                            .padding(horizontal = 14.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = badgeText,
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = LuckiestGuyFontFamily,
                            letterSpacing = 0.5.sp
                        )
                    }

                    Text(
                        text = heroTitle,
                        color = Color(0xFF1E293B),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = LuckiestGuyFontFamily,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = heroDesc,
                        color = Color(0xFF64748B),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )

                    HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 1.dp)

                    // Benefits row (Dynamic from Admin)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        TesterPerkItem(perk1T, perk1S)
                        TesterPerkItem(perk2T, perk2S)
                        TesterPerkItem(perk3T, perk3S)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Registration Form Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(6.dp, RoundedCornerShape(24.dp), clip = false)
                    .background(Color(0xFFFFF9E6), RoundedCornerShape(24.dp))
                    .border(
                        3.dp,
                        Brush.horizontalGradient(listOf(Color(0xFF0284C7), Color(0xFFFACC15), Color(0xFF22C55E))),
                        RoundedCornerShape(24.dp)
                    )
                    .padding(20.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "YOUR TESTER DETAILS",
                        color = Color(0xFF0284C7),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = LuckiestGuyFontFamily,
                        letterSpacing = 0.5.sp
                    )

                    // Full Name Field
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(text = "Full Name:", color = Color(0xFF475569), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            placeholder = { Text("e.g. Rahul Sharma", color = Color(0xFF94A3B8)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedBorderColor = Color(0xFF0284C7),
                                unfocusedBorderColor = Color(0xFFCBD5E1),
                                focusedTextColor = Color(0xFF1E293B),
                                unfocusedTextColor = Color(0xFF1E293B)
                            )
                        )
                    }

                    // Mobile Number Field with 10-Digit Validation
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Mobile Number (WhatsApp/UPI):", color = Color(0xFF475569), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = "${mobile.length}/10 digits",
                                color = if (isMobileValid) Color(0xFF10B981) else Color(0xFF94A3B8),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        OutlinedTextField(
                            value = mobile,
                            onValueChange = { newVal ->
                                val digitsOnly = newVal.filter { it.isDigit() }.take(10)
                                mobile = digitsOnly
                            },
                            placeholder = { Text("e.g. 9876543210 (10 digits)", color = Color(0xFF94A3B8)) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            isError = mobile.isNotEmpty() && !isMobileValid,
                            trailingIcon = {
                                if (isMobileValid) {
                                    Text("✅", fontSize = 16.sp, modifier = Modifier.padding(end = 8.dp))
                                } else if (mobile.isNotEmpty()) {
                                    Text("⚠️", fontSize = 16.sp, modifier = Modifier.padding(end = 8.dp))
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedBorderColor = if (isMobileValid) Color(0xFF10B981) else Color(0xFF0284C7),
                                unfocusedBorderColor = if (isMobileValid) Color(0xFF10B981) else Color(0xFFCBD5E1),
                                focusedTextColor = Color(0xFF1E293B),
                                unfocusedTextColor = Color(0xFF1E293B),
                                errorBorderColor = Color(0xFFEF4444)
                            )
                        )
                        if (mobile.isNotEmpty()) {
                            if (isMobileValid) {
                                Text("✓ Valid 10-digit mobile number", color = Color(0xFF10B981), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            } else {
                                Text("⚠️ Must be 10 digits starting with 6, 7, 8, or 9", color = Color(0xFFEF4444), fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }

                    // Email Field with Account Picker
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Email Address (Google/Play):", color = Color(0xFF475569), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFFFFF9C4), RoundedCornerShape(8.dp))
                                    .border(1.dp, Color(0xFF0284C7), RoundedCornerShape(8.dp))
                                    .clickable { launchEmailPicker() }
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text("📧", fontSize = 11.sp)
                                    Text("PICK EMAIL", color = Color(0xFF0284C7), fontSize = 10.5.sp, fontWeight = FontWeight.Black, fontFamily = LuckiestGuyFontFamily)
                                }
                            }
                        }
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            placeholder = { Text("e.g. rahul@gmail.com", color = Color(0xFF94A3B8)) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            trailingIcon = {
                                IconButton(onClick = { launchEmailPicker() }) {
                                    Icon(
                                        imageVector = Icons.Default.AccountCircle,
                                        contentDescription = "Pick Email",
                                        tint = Color(0xFF0284C7)
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedBorderColor = if (isEmailValid) Color(0xFF10B981) else Color(0xFF0284C7),
                                unfocusedBorderColor = if (isEmailValid) Color(0xFF10B981) else Color(0xFFCBD5E1),
                                focusedTextColor = Color(0xFF1E293B),
                                unfocusedTextColor = Color(0xFF1E293B)
                            )
                        )
                        if (email.isNotEmpty() && !android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
                            Text("⚠️ Please enter a valid email address format", color = Color(0xFFEF4444), fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        }
                    }

                    val isProgramActive = tProgram?.isActive ?: 1

                    if (isProgramActive == 0) {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFEF4444).copy(alpha = 0.12f))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Warning, null, tint = Color(0xFFEF4444), modifier = Modifier.size(22.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "Registrations are temporarily paused by the admin.",
                                    color = Color(0xFFB91C1C),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    // Submit Button with Validation (Green 3D Gradient)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .zoomClickable(enabled = !isSubmitting && isProgramActive != 0) {
                                if (isProgramActive == 0) {
                                    Toast.makeText(context, "Registration is currently paused by admin", Toast.LENGTH_SHORT).show()
                                    return@zoomClickable
                                }
                                if (name.trim().isEmpty()) {
                                    Toast.makeText(context, "Please enter your full name", Toast.LENGTH_SHORT).show()
                                    return@zoomClickable
                                }
                                if (!isMobileValid) {
                                    Toast.makeText(context, "Please enter a valid 10-digit mobile number", Toast.LENGTH_SHORT).show()
                                    return@zoomClickable
                                }
                                if (!isEmailValid) {
                                    Toast.makeText(context, "Please enter a valid email address", Toast.LENGTH_SHORT).show()
                                    return@zoomClickable
                                }
                                isSubmitting = true
                                SettingsManager.registerTester(context, name.trim(), mobile.trim(), email.trim()) { success, resp ->
                                    android.os.Handler(android.os.Looper.getMainLooper()).post {
                                        try {
                                            isSubmitting = false
                                            if (success) {
                                                val isInstant = resp?.contains("active") == true || resp?.contains("\"instant_approved\":true") == true || resp?.contains("\"instant_approved\":1") == true
                                                Toast.makeText(context, if (isInstant) "Welcome to the Testing Team!" else "Application Submitted!", Toast.LENGTH_SHORT).show()
                                                onSuccess(isInstant)
                                            } else {
                                                Toast.makeText(context, resp ?: "Registration failed", Toast.LENGTH_SHORT).show()
                                            }
                                        } catch (e: Exception) {
                                            isSubmitting = false
                                        }
                                    }
                                }
                            }
                            .background(
                                brush = Brush.verticalGradient(
                                    if (isProgramActive == 0) listOf(Color(0xFF94A3B8), Color(0xFF64748B))
                                    else listOf(Color(0xFF4ADE80), Color(0xFF16A34A))
                                ),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .border(
                                width = 2.dp,
                                brush = Brush.horizontalGradient(
                                    listOf(Color(0xFF0284C7), Color(0xFFFACC15), Color(0xFF22C55E))
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isProgramActive == 0) "PROGRAM PAUSED" else if (isSubmitting) "SUBMITTING..." else "SUBMIT & START TESTING",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = LuckiestGuyFontFamily,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(30.dp))
        }
    }
}

@Composable
private fun TesterPerkItem(title: String, subtitle: String) {
    Box(
        modifier = Modifier
            .background(Color.White, RoundedCornerShape(14.dp))
            .border(
                1.5.dp,
                Brush.horizontalGradient(
                    listOf(Color(0xFF0284C7).copy(alpha = 0.4f), Color(0xFF22C55E).copy(alpha = 0.4f))
                ),
                RoundedCornerShape(14.dp)
            )
            .padding(horizontal = 10.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                color = Color(0xFF0284C7),
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                fontFamily = LuckiestGuyFontFamily
            )
            Text(
                text = subtitle,
                color = Color(0xFF64748B),
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun TesterPendingScreen(
    onBack: () -> Unit,
    onRefresh: () -> Unit
) {
    var isChecking by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.bg),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth(0.94f)
                    .shadow(8.dp, RoundedCornerShape(26.dp), clip = false)
                    .background(Color(0xFFFFF9E6), RoundedCornerShape(26.dp))
                    .border(
                        width = 3.5.dp,
                        brush = Brush.horizontalGradient(
                            listOf(Color(0xFF0284C7), Color(0xFFFACC15), Color(0xFF22C55E))
                        ),
                        shape = RoundedCornerShape(26.dp)
                    )
                    .padding(24.dp)
            ) {
                Text(text = "⏳", fontSize = 52.sp)

                Spacer(Modifier.height(14.dp))

                Text(
                    text = "APPLICATION UNDER REVIEW",
                    color = Color(0xFF2563EB),
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = LuckiestGuyFontFamily,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Your tester application has been submitted and is waiting for administrator approval. Check back shortly or tap refresh below.",
                    color = Color(0xFF64748B),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )

                Spacer(Modifier.height(24.dp))

                // Check Approval Status Button (Green 3D Gradient)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .zoomClickable {
                            isChecking = true
                            onRefresh()
                        }
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color(0xFF4ADE80), Color(0xFF16A34A))
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .border(
                            width = 2.dp,
                            brush = Brush.horizontalGradient(
                                listOf(Color(0xFF0284C7), Color(0xFFFACC15), Color(0xFF22C55E))
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "CHECK APPROVAL STATUS",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = LuckiestGuyFontFamily,
                        letterSpacing = 0.5.sp
                    )
                }

                Spacer(Modifier.height(12.dp))

                TextButton(onClick = {
                    onBack()
                }) {
                    Text(
                        text = "← Back to Home",
                        color = Color(0xFF0284C7),
                        fontWeight = FontWeight.Black,
                        fontFamily = LuckiestGuyFontFamily,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
fun TesterRejectedScreen(
    onBack: () -> Unit,
    onReapply: () -> Unit,
    onRefresh: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.bg),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth(0.94f)
                    .shadow(8.dp, RoundedCornerShape(26.dp), clip = false)
                    .background(Color(0xFFFFF9E6), RoundedCornerShape(26.dp))
                    .border(
                        width = 3.5.dp,
                        brush = Brush.horizontalGradient(
                            listOf(Color(0xFF0284C7), Color(0xFFFACC15), Color(0xFF22C55E))
                        ),
                        shape = RoundedCornerShape(26.dp)
                    )
                    .padding(24.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(Color(0xFFFEE2E2), CircleShape)
                        .border(2.5.dp, Color(0xFFEF4444), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Rejected",
                        tint = Color(0xFFDC2626),
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(Modifier.height(14.dp))

                Text(
                    text = "APPLICATION NOT APPROVED",
                    color = Color(0xFFDC2626),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = LuckiestGuyFontFamily,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Your application for the Tester Program was reviewed and was not approved at this time. You can update your details and re-apply.",
                    color = Color(0xFF64748B),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )

                Spacer(Modifier.height(20.dp))

                // Re-apply button (Green 3D Gradient)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .zoomClickable {
                            onReapply()
                        }
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color(0xFF4ADE80), Color(0xFF16A34A))
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .border(
                            width = 2.dp,
                            brush = Brush.horizontalGradient(
                                listOf(Color(0xFF0284C7), Color(0xFFFACC15), Color(0xFF22C55E))
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "RE-APPLY AS TESTER",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = LuckiestGuyFontFamily,
                        letterSpacing = 0.5.sp
                    )
                }

                Spacer(Modifier.height(10.dp))

                // Check status again button (Yellow-Cream Button)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .zoomClickable {
                            onRefresh()
                        }
                        .background(Color(0xFFFFF9C4), RoundedCornerShape(14.dp))
                        .border(
                            2.dp,
                            Brush.horizontalGradient(listOf(Color(0xFF0284C7), Color(0xFF22C55E))),
                            RoundedCornerShape(14.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = Color(0xFF1E152A), modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "CHECK STATUS AGAIN",
                            color = Color(0xFF1E152A),
                            fontWeight = FontWeight.Black,
                            fontFamily = LuckiestGuyFontFamily,
                            fontSize = 13.sp
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))

                TextButton(onClick = {
                    onBack()
                }) {
                    Text(
                        text = "← Back to Home",
                        color = Color(0xFF0284C7),
                        fontWeight = FontWeight.Black,
                        fontFamily = LuckiestGuyFontFamily,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
fun TesterDashboardScreen(
    status: com.color.pipe.puzzle.model.TesterStatusResponse?,
    onBack: () -> Unit,
    onPlayDay: (dayNumber: Int, startLevel: Int, currentLevelToPlay: Int, targetLevels: Int, completedLevels: Int) -> Unit,
    onSubmitReviewClick: (dayNumber: Int) -> Unit,
    onClaimRewardClick: () -> Unit,
    onRestartCycle: () -> Unit,
    onEndTesterProgram: () -> Unit,
    onRefresh: () -> Unit,
    onOpenHistoryClick: () -> Unit = {}
) {
    val currentDay = status?.currentDay ?: 1
    val isTodayCompleted = status?.isTodayCompleted == true
    val lastCompletedDay = status?.lastCompletedDay ?: 0
    val days = status?.days ?: emptyList()
    val rewardAmount = status?.rewardAmount ?: 150
    val totalDays = status?.totalDays ?: 7
    val hasClaimed = status?.hasClaimed == true
    val canClaim = status?.canClaim == true
    val isFinished = status?.isFinished == true
    val claimStatus = (status?.claimInfo?.get("status") as? String) ?: "pending"
    val claimMethod = (status?.claimInfo?.get("payment_method") as? String) ?: "UPI"
    val claimAccount = (status?.claimInfo?.get("account_details") as? String) ?: ""
    val currentCycle = status?.currentCycle ?: 1

    var showEndConfirmationDialog by remember { mutableStateOf(false) }

    ArrowPuzzleProBackground(BackgroundSkin.Classic) {
        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 12.dp)) {
            // Header Row
            Row(modifier = Modifier.fillMaxWidth().padding(top = 24.dp, bottom = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Box(modifier = Modifier.size(44.dp).bouncyClickable { onBack() }) {
                    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFB58A0D), CircleShape).offset(y = 3.dp))
                    Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFFFFD166), Color(0xFFF7B538))), CircleShape).border(2.dp, Color.White, CircleShape), contentAlignment = Alignment.Center) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFF1E152A), modifier = Modifier.size(24.dp))
                    }
                }
                Text(text = if (isFinished) "PROGRAM COMPLETED" else "TESTER JOURNEY", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black, fontFamily = LuckiestGuyFontFamily, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(44.dp).bouncyClickable {
                        onOpenHistoryClick()
                    }) {
                        Box(modifier = Modifier.fillMaxSize().background(Color(0xFF7C3AED), CircleShape).offset(y = 3.dp))
                        Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFFA78BFA), Color(0xFF7C3AED))), CircleShape).border(2.dp, Color.White, CircleShape), contentAlignment = Alignment.Center) {
                            Text(text = "📜", fontSize = 18.sp)
                        }
                    }
                    Box(modifier = Modifier.size(44.dp).bouncyClickable { onRefresh() }) {
                        Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0D9488), CircleShape).offset(y = 3.dp))
                        Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF2DD4BF), Color(0xFF0D9488))), CircleShape).border(2.dp, Color.White, CircleShape), contentAlignment = Alignment.Center) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = "Refresh", tint = Color.White, modifier = Modifier.size(24.dp))
                        }
                    }
                }
            }

            // Live Payout Marquee Ticker on Dashboard Screen
            val dashTicker = status?.payoutTicker ?: SettingsManager.settings?.payoutTicker
            if (dashTicker?.showDashboard != false && (dashTicker?.enabled != false)) {
                LivePayoutMarqueeTicker(config = dashTicker, modifier = Modifier.padding(bottom = 12.dp))
            }

            if (isFinished) {
                Column(modifier = Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Box(modifier = Modifier.fillMaxWidth().background(Brush.verticalGradient(listOf(Color(0xFF1E152A).copy(alpha = 0.95f), Color(0xFF2D1F47).copy(alpha = 0.95f))), RoundedCornerShape(24.dp)).border(2.dp, Color(0xFFFFD166), RoundedCornerShape(24.dp)).padding(22.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            Text(text = "🏁 ✨", fontSize = 44.sp, textAlign = TextAlign.Center)
                            Text(text = "TESTER PROGRAM EXITED", color = Color(0xFFFFD166), fontSize = 20.sp, fontWeight = FontWeight.Black, fontFamily = LuckiestGuyFontFamily, textAlign = TextAlign.Center)
                            Text(text = "You have exited the Tester Program. Your reward claim status remains safe. If you wish to participate again, simply tap 'Join Next Round' to instantly start your next cycle without re-filling any forms!", color = Color(0xFFE2E8F0), fontSize = 12.sp, textAlign = TextAlign.Center, lineHeight = 17.sp)
                            
                            if (hasClaimed) {
                                Box(modifier = Modifier.fillMaxWidth().background(when (claimStatus) { "sent" -> Color(0xFF064E3B); "rejected" -> Color(0xFF7F1D1D); else -> Color(0xFF78350F) }, RoundedCornerShape(16.dp)).border(2.dp, when (claimStatus) { "sent" -> Color(0xFF10B981); "rejected" -> Color(0xFFEF4444); else -> Color(0xFFF59E0B) }, RoundedCornerShape(16.dp)).padding(14.dp)) {
                                    Column(verticalArrangement = Arrangement.spacedBy(5.dp), horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                        Text(text = when (claimStatus) { "sent" -> "✅ PAYOUT TRANSFERRED"; "rejected" -> "❌ PAYOUT REJECTED"; else -> "⏳ PAYOUT UNDER ADMIN REVIEW" }, color = when (claimStatus) { "sent" -> Color(0xFF6EE7B7); "rejected" -> Color(0xFFFCA5A5); else -> Color(0xFFFDE68A) }, fontSize = 13.sp, fontWeight = FontWeight.Black, fontFamily = LuckiestGuyFontFamily)
                                        Text(text = "Reward Amount: Upto ₹$rewardAmount", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                        if (claimAccount.isNotEmpty()) Text(text = "$claimMethod: $claimAccount", color = Color(0xFFCBD5E1), fontSize = 11.sp)
                                        Text(text = when (claimStatus) { "sent" -> "Upto ₹$rewardAmount has been successfully sent to your $claimMethod account."; "rejected" -> "Your payout claim was rejected. Please contact support."; else -> "Admin is verifying your reviews and will transfer upto ₹$rewardAmount shortly." }, color = Color(0xFFE2E8F0), fontSize = 11.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 2.dp))
                                    }
                                }
                            }

                            // Re-join Next Round Button (Instant Cycle X start without form)
                            Box(modifier = Modifier.fillMaxWidth().height(52.dp).bouncyClickable {
                                onRestartCycle()
                            }) {
                                Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF8B5CF6), Color(0xFF6D28D9))), RoundedCornerShape(16.dp)).border(2.dp, Color(0xFFDDD6FE), RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text(text = "🚀 JOIN NEXT ROUND (CYCLE ${currentCycle + 1})", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Black, fontFamily = LuckiestGuyFontFamily)
                                    }
                                }
                            }

                            // Regular Game Button
                            Box(modifier = Modifier.fillMaxWidth().height(48.dp).bouncyClickable { 
                                onBack() 
                            }) {
                                Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF10B981), Color(0xFF059669))), RoundedCornerShape(16.dp)).border(2.dp, Color(0xFFA7F3D0), RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
                                    Text(text = "🎮 CONTINUE PLAYING REGULAR GAME", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Black, fontFamily = LuckiestGuyFontFamily)
                                }
                            }
                        }
                    }
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f), verticalArrangement = Arrangement.spacedBy(14.dp), contentPadding = PaddingValues(bottom = 24.dp)) {
                    items(days.size) { idx ->
                        val day = days[idx]
                        val isDayCompleted = day.isCompleted
                        val isUnlocksTomorrow = day.unlocksTomorrow || (day.dayNumber == lastCompletedDay + 1 && isTodayCompleted && !isDayCompleted)
                        val isCurrentPlayableDay = (day.dayNumber == currentDay) && !isTodayCompleted && !isDayCompleted
                        val target = day.targetLevels.coerceAtLeast(1)
                        val completed = day.levelsCompleted.coerceAtMost(target)
                        val dayReadyToReview = (completed >= target) && !day.feedbackSubmitted && isCurrentPlayableDay
                        val startLvl = if (day.startLevel > 0) day.startLevel else 1
                        val currentLvlToPlay = (startLvl + completed).coerceAtMost(startLvl + target - 1)

                        // Clean title to avoid duplicate "Day X: Day X..." text
                        val cleanTitle = remember(day.title, day.dayNumber) {
                            var t = day.title.trim()
                            if (t.startsWith("Day ${day.dayNumber}:", ignoreCase = true)) {
                                t = t.substring("Day ${day.dayNumber}:".length).trim()
                            } else if (t.startsWith("Day ${day.dayNumber} -", ignoreCase = true)) {
                                t = t.substring("Day ${day.dayNumber} -".length).trim()
                            } else if (t.startsWith("Day ${day.dayNumber}", ignoreCase = true)) {
                                t = t.substring("Day ${day.dayNumber}".length).trim()
                            }
                            if (t.isEmpty()) "Testing Phase" else t
                        }

                        val cardBg = when {
                            isDayCompleted -> Brush.verticalGradient(listOf(Color(0xFF064E3B), Color(0xFF022C22)))
                            isCurrentPlayableDay -> Brush.verticalGradient(listOf(Color(0xFF2E1065), Color(0xFF1E152A)))
                            isUnlocksTomorrow -> Brush.verticalGradient(listOf(Color(0xFF451A03), Color(0xFF1C1917)))
                            else -> Brush.verticalGradient(listOf(Color(0xFF1E293B).copy(alpha = 0.85f), Color(0xFF0F172A).copy(alpha = 0.85f)))
                        }

                        val cardBorder = when {
                            isDayCompleted -> Color(0xFF10B981)
                            isCurrentPlayableDay -> Color(0xFFA855F7)
                            isUnlocksTomorrow -> Color(0xFFF59E0B)
                            else -> Color(0xFF475569).copy(alpha = 0.6f)
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(cardBg, RoundedCornerShape(18.dp))
                                .border(2.dp, cardBorder, RoundedCornerShape(18.dp))
                                .padding(16.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                // Header: Day Badge + Clean Title on left, Status Badge on right
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        modifier = Modifier.weight(1f).padding(end = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .background(
                                                    when {
                                                        isDayCompleted -> Color(0xFF10B981)
                                                        isCurrentPlayableDay -> Color(0xFF8B5CF6)
                                                        isUnlocksTomorrow -> Color(0xFFF59E0B)
                                                        else -> Color(0xFF475569)
                                                    },
                                                    RoundedCornerShape(8.dp)
                                                )
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = "DAY ${day.dayNumber}",
                                                color = Color.White,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Black,
                                                fontFamily = LuckiestGuyFontFamily
                                            )
                                        }

                                        Text(
                                            text = cleanTitle,
                                            color = if (!isDayCompleted && !isCurrentPlayableDay && !isUnlocksTomorrow) Color(0xFFE2E8F0) else Color.White,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    // Status Badge on Right (never squeezed)
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                when {
                                                    isDayCompleted -> Color(0xFF065F46)
                                                    dayReadyToReview -> Color(0xFF78350F)
                                                    isCurrentPlayableDay -> Color(0xFF4C1D95)
                                                    isUnlocksTomorrow -> Color(0xFF78350F)
                                                    else -> Color(0xFF1E293B)
                                                },
                                                RoundedCornerShape(8.dp)
                                            )
                                            .border(
                                                1.dp,
                                                when {
                                                    isDayCompleted -> Color(0xFF34D399)
                                                    dayReadyToReview -> Color(0xFFFFD166)
                                                    isCurrentPlayableDay -> Color(0xFFA78BFA)
                                                    isUnlocksTomorrow -> Color(0xFFFBBF24)
                                                    else -> Color(0xFF64748B)
                                                }.copy(alpha = 0.7f),
                                                RoundedCornerShape(8.dp)
                                            )
                                            .padding(horizontal = 8.dp, vertical = 4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = when {
                                                isDayCompleted -> "COMPLETED"
                                                dayReadyToReview -> "REVIEW READY"
                                                isCurrentPlayableDay -> "IN PROGRESS"
                                                isUnlocksTomorrow -> "TOMORROW"
                                                else -> "LOCKED"
                                            },
                                            color = when {
                                                isDayCompleted -> Color(0xFF34D399)
                                                dayReadyToReview -> Color(0xFFFFD166)
                                                isCurrentPlayableDay -> Color(0xFFA78BFA)
                                                isUnlocksTomorrow -> Color(0xFFFBBF24)
                                                else -> Color(0xFF94A3B8)
                                            },
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Black,
                                            fontFamily = LuckiestGuyFontFamily,
                                            maxLines = 1
                                        )
                                    }
                                }

                                if (day.instructions.isNotEmpty()) {
                                    Text(
                                        text = "Focus: ${day.instructions}",
                                        color = if (!isDayCompleted && !isCurrentPlayableDay && !isUnlocksTomorrow) Color(0xFF94A3B8) else Color(0xFFCBD5E1),
                                        fontSize = 12.sp,
                                        lineHeight = 16.sp
                                    )
                                }

                                val progressFloat = (completed.toFloat() / target).coerceIn(0f, 1f)
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Target: $target Levels (Lvl $startLvl - ${startLvl + target - 1})",
                                            color = Color(0xFFCBD5E1),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = "$completed / $target (${(progressFloat * 100).toInt()}%)",
                                            color = if (isDayCompleted) Color(0xFF34D399) else Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(10.dp)
                                            .background(Color(0xFF0F172A), CircleShape)
                                            .border(1.dp, Color(0xFF334155).copy(alpha = 0.5f), CircleShape)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth(progressFloat)
                                                .fillMaxHeight()
                                                .background(
                                                    Brush.horizontalGradient(
                                                        if (isDayCompleted) listOf(Color(0xFF34D399), Color(0xFF059669))
                                                        else listOf(Color(0xFFFFD166), Color(0xFFFB8500))
                                                    ),
                                                    CircleShape
                                                )
                                        )
                                    }
                                }

                                if (day.feedbackSubmitted && day.feedbackText.isNotEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color(0xFF065F46).copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                                            .padding(8.dp)
                                    ) {
                                        Text(
                                            text = "Your Review: ${day.feedbackText}",
                                            color = Color(0xFFD1FAE5),
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                if (isCurrentPlayableDay) {
                                    if (dayReadyToReview) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(46.dp)
                                                .bouncyClickable {
                                                    onSubmitReviewClick(day.dayNumber)
                                                }
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .background(
                                                        Brush.verticalGradient(listOf(Color(0xFFFFD166), Color(0xFFF59E0B))),
                                                        RoundedCornerShape(12.dp)
                                                    )
                                                    .border(2.dp, Color.White, RoundedCornerShape(12.dp)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "SUBMIT DAY ${day.dayNumber} BUG REVIEW",
                                                    color = Color(0xFF1E152A),
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Black,
                                                    fontFamily = LuckiestGuyFontFamily
                                                )
                                            }
                                        }
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(44.dp)
                                                .bouncyClickable {
                                                    onPlayDay(day.dayNumber, startLvl, currentLvlToPlay, target, completed)
                                                }
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .background(
                                                        Brush.verticalGradient(listOf(Color(0xFF06D6A0), Color(0xFF049669))),
                                                        RoundedCornerShape(12.dp)
                                                    )
                                                    .border(2.dp, Color(0xFFDCFCE7), RoundedCornerShape(12.dp)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "PLAY TODAY'S LEVELS ($completed/$target)",
                                                    color = Color.White,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Black,
                                                    fontFamily = LuckiestGuyFontFamily
                                                )
                                            }
                                        }
                                    }
                                } else if (isUnlocksTomorrow) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(42.dp)
                                            .background(Color(0xFF334155).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                            .border(1.dp, Color(0xFFF59E0B).copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "DAY ${day.dayNumber} UNLOCKS TOMORROW (00:00 AM)",
                                            color = Color(0xFFFBBF24),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = LuckiestGuyFontFamily
                                        )
                                    }
                                }
                            }
                        }

                        // Native Ad Card placed after Day 2
                        if (day.dayNumber == 2 || idx == 1) {
                            NativeAdSlot("tester_dashboard_native")
                        }
                    }
                    item {
                        Spacer(Modifier.height(8.dp))
                        if (canClaim) {
                            Box(modifier = Modifier.fillMaxWidth().height(58.dp).bouncyClickable { onClaimRewardClick() }) {
                                Box(modifier = Modifier.fillMaxSize().offset(y = 4.dp).background(Color(0xFFB45309), RoundedCornerShape(16.dp)))
                                Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFFFFD166), Color(0xFFF59E0B))), RoundedCornerShape(16.dp)).border(2.dp, Color.White, RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text(text = "🎁", fontSize = 22.sp)
                                        Text(text = "OPEN MYSTERY REWARD BOX", color = Color(0xFF1E152A), fontSize = 15.sp, fontWeight = FontWeight.Black, fontFamily = LuckiestGuyFontFamily)
                                    }
                                }
                            }
                        } else if (hasClaimed) {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(modifier = Modifier.fillMaxWidth().background(when (claimStatus) { "sent" -> Color(0xFF064E3B); "rejected" -> Color(0xFF7F1D1D); else -> Color(0xFF78350F) }, RoundedCornerShape(16.dp)).border(2.dp, when (claimStatus) { "sent" -> Color(0xFF10B981); "rejected" -> Color(0xFFEF4444); else -> Color(0xFFF59E0B) }, RoundedCornerShape(16.dp)).padding(14.dp), contentAlignment = Alignment.Center) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(text = when (claimStatus) { "sent" -> "✅ Payout Sent! Upto ₹$rewardAmount transferred to your $claimMethod."; "rejected" -> "❌ Payout Rejected. Contact admin support."; else -> "⏳ Payout claim submitted! Admin will transfer upto ₹$rewardAmount shortly." }, color = when (claimStatus) { "sent" -> Color(0xFFD1FAE5); "rejected" -> Color(0xFFFEE2E2); else -> Color(0xFFFEF3C7) }, fontSize = 12.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                                        if (claimAccount.isNotEmpty()) Text(text = "Account: $claimAccount", color = Color(0xFFCBD5E1), fontSize = 11.sp)
                                    }
                                }
                                Box(modifier = Modifier.fillMaxWidth().height(52.dp).bouncyClickable { onRestartCycle() }) {
                                    Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF8B5CF6), Color(0xFF6D28D9))), RoundedCornerShape(14.dp)).border(2.dp, Color(0xFFDDD6FE), RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) {
                                        Text(text = "🔄 START NEXT ROUND (CYCLE ${currentCycle + 1})", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Black, fontFamily = LuckiestGuyFontFamily)
                                    }
                                }
                                Box(modifier = Modifier.fillMaxWidth().height(48.dp).bouncyClickable { showEndConfirmationDialog = true }) {
                                    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF1E152A).copy(alpha = 0.8f), RoundedCornerShape(14.dp)).border(1.5.dp, Color(0xFFF43F5E), RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) {
                                        Text(text = "🏁 END TESTER PROGRAM", color = Color(0xFFFDA4AF), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showEndConfirmationDialog) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.8f)).clickable(enabled = false) {}, contentAlignment = Alignment.Center) {
            Box(modifier = Modifier.fillMaxWidth(0.88f).background(Color(0xFF1E152A), RoundedCornerShape(20.dp)).border(2.dp, Color(0xFFF43F5E), RoundedCornerShape(20.dp)).padding(20.dp)) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(text = "🏁 End Tester Program?", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black, fontFamily = LuckiestGuyFontFamily)
                    Text(text = "You will exit the active tester journey. Your submitted reward claim and review progress will remain safe. You can rejoin at any time to start Cycle ${currentCycle + 1} instantly without filling any forms.", color = Color(0xFFCBD5E1), fontSize = 13.sp, textAlign = TextAlign.Center, lineHeight = 18.sp)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(onClick = { showEndConfirmationDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)), modifier = Modifier.weight(1f).height(44.dp), shape = RoundedCornerShape(12.dp)) { Text(text = "Cancel", color = Color.White, fontWeight = FontWeight.Bold) }
                        Button(onClick = { showEndConfirmationDialog = false; onEndTesterProgram() }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48)), modifier = Modifier.weight(1f).height(44.dp), shape = RoundedCornerShape(12.dp)) { Text(text = "Yes, End", color = Color.White, fontWeight = FontWeight.Bold) }
                    }
                }
            }
        }
    }
}

// ==================== TESTER POPUPS ====================

@Composable
fun TesterDailyReviewPopup(
    dayNumber: Int,
    onSubmit: (rating: Int, feedbackText: String) -> Unit,
    onClose: () -> Unit
) {
    var rating by remember { mutableIntStateOf(5) }
    
    // QA Questionnaire State
    val bugOptions = listOf(
        "No, perfectly smooth! 👍",
        "Minor glitch / Pipe touch issue ⚠️",
        "Game froze / crashed ❌",
        "Other issue 📝"
    )
    var selectedBugOption by remember { mutableStateOf(bugOptions[0]) }
    var bugDescription by remember { mutableStateOf("") }
    
    val perfOptions = listOf(
        "Super smooth (60 FPS) ⚡",
        "Occasional lag 🐢",
        "Heavy stutter 📉"
    )
    var selectedPerfOption by remember { mutableStateOf(perfOptions[0]) }
    
    val diffOptions = listOf(
        "Perfect & fun! 🎯",
        "Too easy 🥱",
        "Too difficult 🤯"
    )
    var selectedDiffOption by remember { mutableStateOf(diffOptions[0]) }
    
    var suggestions by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f))
            .clickable(enabled = false) {},
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.88f)
                .background(Color(0xFFFFFDF5), RoundedCornerShape(24.dp))
                .border(3.5.dp, Color(0xFF2C1B47), RoundedCornerShape(24.dp))
                .padding(horizontal = 18.dp, vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📋 DAY $dayNumber QA REPORT",
                        color = Color(0xFF2C1B47),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = LuckiestGuyFontFamily
                    )
                    IconButton(onClick = onClose, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF64748B))
                    }
                }

                Text(
                    text = "Awesome work completing Day $dayNumber! As an official tester, please answer these quick QA questions to unlock tomorrow:",
                    color = Color(0xFF475569),
                    fontSize = 11.5.sp,
                    lineHeight = 16.sp,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.fillMaxWidth()
                )

                // Overall Rating
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "⭐ Overall Experience Rating",
                            color = Color(0xFF1E293B),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            for (star in 1..5) {
                                Text(
                                    text = if (star <= rating) "⭐" else "☆",
                                    fontSize = 30.sp,
                                    modifier = Modifier.clickable { rating = star }
                                )
                            }
                        }
                    }
                }

                // Question 1: Bugs & Glitches
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "1. Did you notice any bugs or pipe glitches?",
                        color = Color(0xFF1E293B),
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                    bugOptions.forEach { opt ->
                        val isSelected = selectedBugOption == opt
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    if (isSelected) Color(0xFFEEF2FF) else Color(0xFFF8FAFC),
                                    RoundedCornerShape(10.dp)
                                )
                                .border(
                                    1.5.dp,
                                    if (isSelected) Color(0xFF6366F1) else Color(0xFFE2E8F0),
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable { selectedBugOption = opt }
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { selectedBugOption = opt },
                                colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF6366F1))
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = opt,
                                color = if (isSelected) Color(0xFF4338CA) else Color(0xFF334155),
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                    if (selectedBugOption != "No, perfectly smooth! 👍") {
                        OutlinedTextField(
                            value = bugDescription,
                            onValueChange = { bugDescription = it },
                            placeholder = { Text("Describe the bug or level where it happened...", color = Color(0xFF94A3B8), fontSize = 11.5.sp) },
                            modifier = Modifier.fillMaxWidth().height(80.dp),
                            shape = RoundedCornerShape(10.dp),
                            textStyle = TextStyle(fontSize = 12.sp, color = Color(0xFF1E293B)),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF6366F1),
                                unfocusedBorderColor = Color(0xFFCBD5E1),
                                focusedTextColor = Color(0xFF1E293B),
                                unfocusedTextColor = Color(0xFF1E293B)
                            )
                        )
                    }
                }

                // Question 2: Smoothness / FPS
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "2. How was the gameplay smoothness / FPS?",
                        color = Color(0xFF1E293B),
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        perfOptions.forEach { opt ->
                            val isSelected = selectedPerfOption == opt
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(
                                        if (isSelected) Color(0xFFECFDF5) else Color(0xFFF8FAFC),
                                        RoundedCornerShape(10.dp)
                                    )
                                    .border(
                                        1.5.dp,
                                        if (isSelected) Color(0xFF10B981) else Color(0xFFE2E8F0),
                                        RoundedCornerShape(10.dp)
                                    )
                                    .clickable { selectedPerfOption = opt }
                                    .padding(vertical = 8.dp, horizontal = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = opt,
                                    color = if (isSelected) Color(0xFF065F46) else Color(0xFF475569),
                                    fontSize = 10.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                // Question 3: Difficulty & Fun
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "3. Level difficulty & puzzle enjoyment:",
                        color = Color(0xFF1E293B),
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        diffOptions.forEach { opt ->
                            val isSelected = selectedDiffOption == opt
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(
                                        if (isSelected) Color(0xFFFEF3C7) else Color(0xFFF8FAFC),
                                        RoundedCornerShape(10.dp)
                                    )
                                    .border(
                                        1.5.dp,
                                        if (isSelected) Color(0xFFF59E0B) else Color(0xFFE2E8F0),
                                        RoundedCornerShape(10.dp)
                                    )
                                    .clickable { selectedDiffOption = opt }
                                    .padding(vertical = 8.dp, horizontal = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = opt,
                                    color = if (isSelected) Color(0xFF92400E) else Color(0xFF475569),
                                    fontSize = 10.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                // Optional Suggestions
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "💡 Any ideas, tips or improvements? (Optional)",
                        color = Color(0xFF475569),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    OutlinedTextField(
                        value = suggestions,
                        onValueChange = { suggestions = it },
                        placeholder = { Text("e.g. Add more pipe skins, sound effects...", color = Color(0xFF94A3B8), fontSize = 11.5.sp) },
                        modifier = Modifier.fillMaxWidth().height(65.dp),
                        shape = RoundedCornerShape(10.dp),
                        textStyle = TextStyle(fontSize = 12.sp, color = Color(0xFF1E293B)),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF8338EC),
                            unfocusedBorderColor = Color(0xFFCBD5E1),
                            focusedTextColor = Color(0xFF1E293B),
                            unfocusedTextColor = Color(0xFF1E293B)
                        )
                    )
                }

                Spacer(Modifier.height(4.dp))

                // Submit Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .bouncyClickable {
                            val formattedReview = buildString {
                                append("Rating: $rating/5 | [Bugs]: $selectedBugOption")
                                if (selectedBugOption != "No, perfectly smooth! 👍" && bugDescription.isNotBlank()) {
                                    append(" ($bugDescription)")
                                }
                                append(" | [Performance]: $selectedPerfOption")
                                append(" | [Difficulty]: $selectedDiffOption")
                                if (suggestions.isNotBlank()) {
                                    append(" | [Ideas]: ${suggestions.trim()}")
                                }
                            }
                            onSubmit(rating, formattedReview)
                        }
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(listOf(Color(0xFF06D6A0), Color(0xFF049669))),
                                RoundedCornerShape(14.dp)
                            )
                            .border(2.dp, Color(0xFFDCFCE7), RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "✅ SUBMIT QA REPORT & UNLOCK DAY",
                            color = Color.White,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = LuckiestGuyFontFamily
                        )
                    }
                }

                TextButton(onClick = onClose) {
                    Text(text = "Close", color = Color(0xFF64748B), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
fun TesterRewardClaimPopup(
    rewardAmount: Int,
    payoutMethods: List<com.color.pipe.puzzle.model.TesterPayoutMethodItem> = emptyList(),
    onUnwrap: (onDone: () -> Unit) -> Unit = { it() },
    onSubmit: (method: String, account: String) -> Unit,
    onClose: () -> Unit
) {
    var isBoxOpened by remember { mutableStateOf(false) }

    val triggerUnwrap = {
        SoundManager.playCorrectSound()
        onUnwrap {
            isBoxOpened = true
        }
    }

    val availableMethods = remember(payoutMethods) {
        if (payoutMethods.isNotEmpty()) payoutMethods else listOf(
            com.color.pipe.puzzle.model.TesterPayoutMethodItem("UPI", "Enter UPI ID (e.g. name@okhdfcbank)"),
            com.color.pipe.puzzle.model.TesterPayoutMethodItem("Paytm", "Enter 10-digit Paytm Wallet/Mobile Number"),
            com.color.pipe.puzzle.model.TesterPayoutMethodItem("Google Play", "Enter Gmail Address for Redeem Code"),
            com.color.pipe.puzzle.model.TesterPayoutMethodItem("Amazon Pay", "Enter Amazon registered Mobile or Email")
        )
    }

    var selectedMethod by remember(availableMethods) {
        mutableStateOf(availableMethods.firstOrNull()?.name ?: "UPI")
    }
    var accountDetails by remember { mutableStateOf("") }

    val currentPlaceholder = availableMethods.find { it.name == selectedMethod }?.inputPlaceholder
        ?: "Enter your $selectedMethod account details"

    // Infinite pulsing animation for unopened mystery box
    val infiniteTransition = rememberInfiniteTransition(label = "giftBoxPulse")
    val boxScale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "boxScale"
    )
    val boxRotation by infiniteTransition.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "boxRotation"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f))
            .clickable(enabled = false) {},
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .background(Color(0xFFFFF9E6), RoundedCornerShape(26.dp))
                .border(4.dp, Color(0xFF2C1B47), RoundedCornerShape(26.dp))
                .padding(22.dp)
        ) {
            if (!isBoxOpened) {
                // ==================== STAGE 1: UNOPENED MYSTERY GIFT BOX ====================
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF8338EC), RoundedCornerShape(12.dp))
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "🎁 MYSTERY REWARD UNBOXING",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = LuckiestGuyFontFamily
                        )
                    }

                    Text(
                        text = "Congratulations on completing 7 Days of Testing!\nTap the gift box to unwrap your guaranteed reward!",
                        color = Color(0xFF334155),
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp,
                        fontWeight = FontWeight.Medium
                    )

                    // Interactive Mystery Gift Box
                    Box(
                        modifier = Modifier
                            .size(150.dp)
                            .scale(boxScale)
                            .rotate(boxRotation)
                            .bouncyClickable {
                                triggerUnwrap()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.radialGradient(
                                        listOf(Color(0xFFFFD166).copy(alpha = 0.5f), Color.Transparent)
                                    ),
                                    CircleShape
                                )
                        )
                        Text(text = "🎁", fontSize = 72.sp, textAlign = TextAlign.Center)
                    }

                    // Tap to Open Button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .bouncyClickable {
                                triggerUnwrap()
                            }
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.horizontalGradient(listOf(Color(0xFFFF006E), Color(0xFF8338EC))),
                                    RoundedCornerShape(16.dp)
                                )
                                .border(2.dp, Color.White, RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "✨ TAP TO UNWRAP BOX ✨",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = LuckiestGuyFontFamily
                            )
                        }
                    }

                    TextButton(onClick = onClose) {
                        Text(text = "Close", color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                // ==================== STAGE 2: REVEALED EXACT REWARD & CLAIM FORM ====================
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Celebration Banner with Exact Amount
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(listOf(Color(0xFF10B981), Color(0xFF047857))),
                                RoundedCornerShape(18.dp)
                            )
                            .border(2.dp, Color(0xFF6EE7B7), RoundedCornerShape(18.dp))
                            .padding(vertical = 12.dp, horizontal = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "🎉 REWARD UNLOCKED!",
                                color = Color(0xFFD1FAE5),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = LuckiestGuyFontFamily
                            )
                            Text(
                                text = "YOU WON ₹$rewardAmount",
                                color = Color.White,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = LuckiestGuyFontFamily
                            )
                            Text(
                                text = "Guaranteed 7-Day Testing Cash Reward",
                                color = Color(0xFFA7F3D0),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Text(
                        text = "Select your payment method to receive your ₹$rewardAmount payout:",
                        color = Color(0xFF475569),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )

                    // Dynamic Method Selector (in 2-item rows)
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        availableMethods.chunked(2).forEach { rowMethods ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                rowMethods.forEach { item ->
                                    val isSel = selectedMethod == item.name
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(44.dp)
                                            .background(
                                                if (isSel) Color(0xFF8338EC) else Color(0xFFE2E8F0),
                                                RoundedCornerShape(12.dp)
                                            )
                                            .border(
                                                2.dp,
                                                if (isSel) Color(0xFF2C1B47) else Color(0xFFCBD5E1),
                                                RoundedCornerShape(12.dp)
                                            )
                                            .clickable { selectedMethod = item.name },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = item.name,
                                            color = if (isSel) Color.White else Color(0xFF475569),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = LuckiestGuyFontFamily,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                                if (rowMethods.size == 1) {
                                    Spacer(Modifier.weight(1f))
                                }
                            }
                        }
                    }

                    // Dynamic Account Input with custom placeholder
                    OutlinedTextField(
                        value = accountDetails,
                        onValueChange = { accountDetails = it },
                        placeholder = {
                            Text(
                                text = currentPlaceholder,
                                color = Color(0xFF94A3B8),
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF8338EC),
                            unfocusedBorderColor = Color(0xFFCBD5E1),
                            focusedTextColor = Color(0xFF1E293B),
                            unfocusedTextColor = Color(0xFF1E293B)
                        )
                    )

                    // Submit Button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .bouncyClickable {
                                if (accountDetails.trim().isEmpty()) return@bouncyClickable
                                onSubmit(selectedMethod, accountDetails.trim())
                            }
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(listOf(Color(0xFF06D6A0), Color(0xFF049669))),
                                    RoundedCornerShape(14.dp)
                                )
                                .border(2.dp, Color(0xFFDCFCE7), RoundedCornerShape(14.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "💰 CLAIM ₹$rewardAmount NOW",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = LuckiestGuyFontFamily
                            )
                        }
                    }

                    TextButton(onClick = onClose) {
                        Text(text = "Cancel", color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun RewardHistoryPopup(
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val haptic = LocalHapticFeedback.current

    var historyList by remember { mutableStateOf<List<RewardHistoryItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var refreshTrigger by remember { mutableIntStateOf(0) }

    fun loadHistory() {
        isLoading = true
        UserManager.getRewardHistory(context) { list ->
            historyList = list
            isLoading = false
        }
    }

    LaunchedEffect(refreshTrigger) {
        loadHistory()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f))
            .clickable(enabled = false) {},
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.85f)
                .background(Color(0xFF1E152A), RoundedCornerShape(24.dp))
                .border(2.5.dp, Color(0xFFA78BFA), RoundedCornerShape(24.dp))
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Topbar Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(text = "📜", fontSize = 22.sp)
                        Text(
                            text = "REWARD & VOUCHER WALLET",
                            color = Color(0xFFFFD166),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = LuckiestGuyFontFamily
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { refreshTrigger++ }, modifier = Modifier.size(32.dp)) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = "Refresh", tint = Color(0xFF38BDF8), modifier = Modifier.size(20.dp))
                        }
                        IconButton(onClick = onClose, modifier = Modifier.size(32.dp)) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                    }
                }

                if (isLoading) {
                    Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFFFFD166))
                    }
                } else if (historyList.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(text = "🎁", fontSize = 48.sp)
                            Text(
                                text = "No Reward Claims Yet",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = LuckiestGuyFontFamily
                            )
                            Text(
                                text = "Complete levels or 7 days of tester journey to earn rewards & gift vouchers!",
                                color = Color(0xFFCBD5E1),
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                            Button(
                                onClick = { refreshTrigger++ },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.padding(top = 8.dp)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                                Spacer(Modifier.width(6.dp))
                                Text("Refresh Wallet", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(historyList.size) { index ->
                            val item = historyList[index]
                            val isPaid = item.status.equals("sent", ignoreCase = true) || item.status.equals("completed", ignoreCase = true)
                            val isRejected = item.status.equals("rejected", ignoreCase = true)

                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF2D1F47)),
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(
                                    1.5.dp,
                                    when {
                                        isPaid -> Color(0xFF10B981)
                                        isRejected -> Color(0xFFEF4444)
                                        else -> Color(0xFFF59E0B)
                                    }
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Row 1: Title + Status Badge
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                            Text(
                                                text = if (item.type.equals("tester", ignoreCase = true)) "🎯 7-Day Tester Reward (Cycle ${item.cycle})" else "⭐ Milestone Reward",
                                                color = Color.White,
                                                fontSize = 13.5.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            if (item.createdAt.isNotBlank()) {
                                                Text(
                                                    text = item.createdAt,
                                                    color = Color(0xFF94A3B8),
                                                    fontSize = 10.sp
                                                )
                                            }
                                        }

                                        // Status Pill
                                        Box(
                                            modifier = Modifier
                                                .background(
                                                    when {
                                                        isPaid -> Color(0xFF065F46)
                                                        isRejected -> Color(0xFF7F1D1D)
                                                        else -> Color(0xFF78350F)
                                                    },
                                                    RoundedCornerShape(8.dp)
                                                )
                                                .border(
                                                    1.dp,
                                                    when {
                                                        isPaid -> Color(0xFF10B981)
                                                        isRejected -> Color(0xFFEF4444)
                                                        else -> Color(0xFFF59E0B)
                                                    },
                                                    RoundedCornerShape(8.dp)
                                                )
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = when {
                                                    isPaid -> "PAID / SENT ✅"
                                                    isRejected -> "REJECTED ❌"
                                                    else -> "PENDING ⏳"
                                                },
                                                color = when {
                                                    isPaid -> Color(0xFF6EE7B7)
                                                    isRejected -> Color(0xFFFCA5A5)
                                                    else -> Color(0xFFFDE68A)
                                                },
                                                fontSize = 10.5.sp,
                                                fontWeight = FontWeight.Black
                                            )
                                        }
                                    }

                                    // Row 2: Amount Detail
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Amount: ",
                                            color = Color(0xFFCBD5E1),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = "Upto ₹${item.amount}",
                                            color = Color(0xFFFFD166),
                                            fontSize = 14.5.sp,
                                            fontWeight = FontWeight.Black,
                                            fontFamily = LuckiestGuyFontFamily
                                        )
                                    }

                                    // Row 3: Account & Method Details (Full width pill to prevent overlapping)
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color(0xFF1E152A).copy(alpha = 0.7f), RoundedCornerShape(10.dp))
                                            .border(1.dp, Color(0xFF475569).copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                            .padding(horizontal = 10.dp, vertical = 7.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .background(Color(0xFF8B5CF6).copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                                                    .border(1.dp, Color(0xFFA78BFA), RoundedCornerShape(6.dp))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = item.method,
                                                    color = Color(0xFFDDD6FE),
                                                    fontSize = 10.5.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                            Text(
                                                text = item.account,
                                                color = Color(0xFFE2E8F0),
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.Medium,
                                                modifier = Modifier.weight(1f),
                                                overflow = TextOverflow.Ellipsis,
                                                maxLines = 2
                                            )
                                        }
                                    }

                                    // Row 4: Voucher Code Card (if available)
                                    if (!item.voucherCode.isNullOrBlank()) {
                                        val voucherCode = item.voucherCode
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(Color(0xFF0F172A), RoundedCornerShape(12.dp))
                                                .border(
                                                    1.5.dp,
                                                    Brush.horizontalGradient(listOf(Color(0xFFFFD166), Color(0xFFF59E0B))),
                                                    RoundedCornerShape(12.dp)
                                                )
                                                .padding(10.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = "🎁 REDEEM CODE / VOUCHER",
                                                        color = Color(0xFFFFD166),
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Text(
                                                        text = voucherCode,
                                                        color = Color(0xFF38BDF8),
                                                        fontSize = 13.5.sp,
                                                        fontWeight = FontWeight.Black,
                                                        fontFamily = FontFamily.Monospace
                                                    )
                                                }

                                                Button(
                                                    onClick = {
                                                        clipboardManager.setText(AnnotatedString(voucherCode))
                                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                        Toast.makeText(context, "Redeem Code Copied: $voucherCode", Toast.LENGTH_SHORT).show()
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                                    shape = RoundedCornerShape(8.dp)
                                                ) {
                                                    Text(text = "📋 COPY", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }

                                    // Admin Notes (if any)
                                    if (!item.adminNotes.isNullOrBlank()) {
                                        Text(
                                            text = "Note: ${item.adminNotes}",
                                            color = Color(0xFF94A3B8),
                                            fontSize = 10.5.sp,
                                            fontStyle = FontStyle.Italic
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Close Button
                Button(
                    onClick = onClose,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(text = "Close", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun LevelNode(level: Int, isUnlocked: Boolean, isCompleted: Boolean, isCurrent: Boolean, onClick: () -> Unit) {
    val color = when {
        isCurrent -> SunnyYellow
        isCompleted -> VibrantGreen
        else -> Color(0xFF546E7A)
    }
    
    val shadowColor = when {
        isCurrent -> Color(0xFFF9A825)
        isCompleted -> VibrantGreenShadow
        else -> Color(0xFF263238)
    }

    val infiniteTransition = rememberInfiniteTransition(label = "levelPulse")
    val scaleAnim by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = if (isCurrent) 1.15f else 1f,
        animationSpec = infiniteRepeatable(tween(800, easing = EaseInOutSine), RepeatMode.Reverse), label = "scale"
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(if (isCurrent) 100.dp else 80.dp)
                .scale(scaleAnim)
                .clickable(enabled = isUnlocked, onClick = {
                    onClick()
                }),
            contentAlignment = Alignment.TopCenter
        ) {
            Box(modifier = Modifier.size(if (isCurrent) 100.dp else 80.dp).padding(top = 8.dp).background(shadowColor, CircleShape))
            Box(
                modifier = Modifier
                    .size(if (isCurrent) 90.dp else 72.dp)
                    .background(
                        if (isUnlocked) Brush.verticalGradient(listOf(color.copy(alpha = 0.8f), color))
                        else SolidColor(Color(0xFF37474F)), 
                        CircleShape
                    )
                    .border(4.dp, MintWhite.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (!isUnlocked) {
                    Icon(Icons.Default.Lock, null, tint = MintWhite.copy(alpha = 0.2f))
                } else if (isCurrent) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.PlayArrow, null, tint = TextDark, modifier = Modifier.size(32.dp))
                        Text("LEVEL $level", color = TextDark, fontWeight = FontWeight.Black, fontSize = 12.sp, fontFamily = LuckiestGuyFontFamily)
                    }
                } else {
                    Text("$level", color = MintWhite, fontWeight = FontWeight.Black, fontSize = 24.sp, fontFamily = LuckiestGuyFontFamily)
                }
            }
        }
        if (isCompleted) {
            Text("⭐⭐⭐", fontSize = 12.sp)
        }
    }
}

fun getColorsForOption(name: String): Pair<Color, Color> {
    return when (name.uppercase()) {
        "RED" -> Pair(Color(0xFFFF3B30), Color(0xFFB71C1C))
        "BLUE" -> Pair(Color(0xFF007AFF), Color(0xFF0D47A1))
        "GREEN" -> Pair(Color(0xFF34C759), Color(0xFF1B5E20))
        "YELLOW" -> Pair(Color(0xFFFFCC00), Color(0xFFF57F17))
        "PURPLE" -> Pair(Color(0xFFAF52DE), Color(0xFF4A148C))
        "ORANGE" -> Pair(Color(0xFFFF9500), Color(0xFFE65100))
        "PINK" -> Pair(Color(0xFFFF2D55), Color(0xFF880E4F))
        "WHITE" -> Pair(Color(0xFFFFFFFF), Color(0xFF9E9E9E))
        "GRAY" -> Pair(Color(0xFF8E8E93), Color(0xFF37474F))
        "BLACK" -> Pair(Color(0xFF1C1C1E), Color(0xFF000000))
        else -> Pair(Color(0xFFFFD54F), Color(0xFFE65100))
    }
}

@Composable
fun MiniGameIcon(modifier: Modifier = Modifier) {
    Box(modifier = modifier.size(48.dp)) {
        // Main square icon
        Box(
            modifier = Modifier
                .size(40.dp)
                .align(Alignment.BottomStart)
                .shadow(2.dp, RoundedCornerShape(10.dp))
                .background(Color(0xFFFF5E3A), RoundedCornerShape(10.dp))
                .border(2.dp, Color.White, RoundedCornerShape(10.dp))
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(14.dp)
                        .background(Color(0xFF262F3C), RoundedCornerShape(4.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "WHITE",
                        color = Color.Green,
                        fontSize = 6.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.height(2.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    Box(modifier = Modifier.weight(1f).height(8.dp).background(Color(0xFFFF9500), RoundedCornerShape(2.dp)))
                    Box(modifier = Modifier.weight(1f).height(8.dp).background(Color(0xFF007AFF), RoundedCornerShape(2.dp)))
                }
            }
        }

        // Red Target Badge on the top-right
        Box(
            modifier = Modifier
                .size(16.dp)
                .align(Alignment.TopEnd),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(Color(0xFFFF3B30), radius = 8.dp.toPx())
                drawCircle(Color.Black, radius = 5.dp.toPx(), style = Stroke(width = 1.5.dp.toPx()))
                drawCircle(Color(0xFFFF3B30), radius = 2.dp.toPx())
            }
        }
    }
}

@Composable
fun CustomOptionCard(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    fillColor: Color? = null,
    borderColor: Color? = null,
    textColor: Color? = null
) {
    val scope = rememberCoroutineScope()
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    var isAnimatingClick by remember { mutableStateOf(false) }
    val isVisualPressed = isPressed || isAnimatingClick

    val scale by animateFloatAsState(
        targetValue = if (isVisualPressed) 0.92f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "optionPress"
    )
    val buttonOffsetY by animateDpAsState(if (isVisualPressed) 6.dp else 0.dp, label = "optionOffsetY")

    val bg = fillColor ?: Color.White
    val border = borderColor ?: Color(0xFFCBD5E1)
    val finalTextColor = textColor ?: (if (fillColor != null && fillColor != Color.White) Color.Black else Color(0xFF262F3C))

    val shadowColor = remember(bg) {
        if (bg == Color.White) {
            Color(0xFFCBD5E1)
        } else {
            bg.copy(
                red = (bg.red * 0.7f).coerceIn(0f, 1f),
                green = (bg.green * 0.7f).coerceIn(0f, 1f),
                blue = (bg.blue * 0.7f).coerceIn(0f, 1f)
            )
        }
    }

    Box(
        modifier = modifier
            .height(54.dp)
            .scale(scale)
    ) {
        // Shadow/bottom layer
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(shadowColor, RoundedCornerShape(32.dp))
        )

        // Top active face layer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .offset(y = buttonOffsetY)
                .background(bg, RoundedCornerShape(32.dp))
                .border(2.dp, border, RoundedCornerShape(32.dp))
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = {
                        scope.launch {
                            isAnimatingClick = true
                            delay(100)
                            isAnimatingClick = false
                            delay(120)
                            onClick()
                        }
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = finalTextColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = LuckiestGuyFontFamily,
                textAlign = TextAlign.Center
            )
        }
    }
}

fun generateMismatchedColors(options: List<String>): List<String> {
    val colors = options.toList()
    var shuffled = colors.shuffled()
    var attempts = 0
    while (attempts < 100) {
        var hasMatch = false
        for (i in options.indices) {
            if (options[i] == shuffled[i]) {
                hasMatch = true
                break
            }
        }
        if (!hasMatch) break
        shuffled = colors.shuffled()
        attempts++
    }
    return shuffled
}

fun getMismatchedColorsForName(colorName: String): Pair<Color, Color> {
    return when (colorName.uppercase()) {
        "RED" -> Pair(Color(0xFFE53935), Color.White)
        "PINK" -> Pair(Color(0xFFFF4081), Color.White)
        "WHITE" -> Pair(Color(0xFFFFFFFF), Color.Black)
        "GRAY" -> Pair(Color(0xFF9E9E9E), Color.Black)
        "BLACK" -> Pair(Color(0xFF1C1C1E), Color.White)
        else -> Pair(Color.White, Color.Black)
    }
}

enum class GameArrowDirection(val dr: Int, val dc: Int) {
    UP(-1, 0),
    DOWN(1, 0),
    LEFT(0, -1),
    RIGHT(0, 1);
    
    fun opposite(): GameArrowDirection {
        return when (this) {
            UP -> DOWN
            DOWN -> UP
            LEFT -> RIGHT
            RIGHT -> LEFT
        }
    }
}

data class GameGridCell(val r: Int, val c: Int) {
    operator fun plus(dir: GameArrowDirection): GameGridCell {
        return GameGridCell(r + dir.dr, c + dir.dc)
    }
}

data class GameArrow(
    val id: Int,
    val path: List<GameGridCell>, // Ordered from tail to head
    val direction: GameArrowDirection, // Direction of head at path.last()
    val color: Color
) {
    fun getOccupiedCells(): Set<GameGridCell> {
        return path.toSet()
    }

    fun getHeadCell(): GameGridCell {
        return path.last()
    }
}

data class GameArrowLevelConfig(
    val id: Int,
    val gridSize: Int,
    val arrows: List<GameArrow>,
    val targetMoves: Int,
    val shapeType: Int = 0,
    val xSize: Int = gridSize,
    val ySize: Int = gridSize
)

fun isLevelSolvable(arrows: List<GameArrow>, xSize: Int, ySize: Int): Boolean {
    val remaining = arrows.toMutableList()
    var progress = true
    while (remaining.isNotEmpty() && progress) {
        progress = false
        val solvable = remaining.find { !isArrowBlocked(it, remaining, xSize, ySize) }
        if (solvable != null) {
            remaining.remove(solvable)
            progress = true
        }
    }
    return remaining.isEmpty()
}

fun isCellValidForShape(r: Int, c: Int, gridSize: Int, shapeType: Int): Boolean {
    return ShapeTemplates.isCellValid(r, c, gridSize, shapeType)
}

fun generateSimpleArrowLevel(levelId: Int, gridSize: Int, numArrows: Int): GameArrowLevelConfig {
    val arrows = mutableListOf<GameArrow>()
    val occupied = Array(gridSize) { BooleanArray(gridSize) { false } }
    var arrowIdCounter = 1
    for (r in 0 until gridSize step 2) {
        if (arrows.size >= numArrows) break
        val path = (0 until gridSize).map { GameGridCell(r, it) }
        arrows.add(
            GameArrow(
                id = arrowIdCounter++,
                path = path,
                direction = GameArrowDirection.RIGHT,
                color = Color(0xFFFF5E3A)
            )
        )
    }
    return GameArrowLevelConfig(levelId, gridSize, arrows, arrows.size + 3)
}

data class GenerationStage(
    val minLength: Int,
    val maxLength: Int,
    val maxTurns: Int,
    val attempts: Int
)

data class LevelJson(
    val XSize: Int,
    val YSize: Int,
    val ShapeType: Int = 0,
    val Arrows: List<ArrowJson>
)

data class ArrowJson(
    val Points: List<Int>
)

fun loadArrowLevel(context: Context, levelId: Int): GameArrowLevelConfig? {
    if (levelId == 84 || levelId == 85 || levelId == 86) return null
    return try {
        val mappedLevelId = ((levelId - 1) % TOTAL_LEVELS) + 1
        val jsonString = context.assets.open("levels/level_$mappedLevelId.json").bufferedReader().use { it.readText() }
        val levelJson = Gson().fromJson(jsonString, LevelJson::class.java) ?: return null
        
        val gridSize = maxOf(levelJson.XSize, levelJson.YSize)
        val premiumColors = listOf(
            Color(0xFFFF5E3A), Color(0xFF00A2FF), Color(0xFF34C759),
            Color(0xFFFFD93D), Color(0xFFAF52DE), Color(0xFFFF2D55),
            Color(0xFFE5BA73), Color(0xFFCCFF00), Color(0xFFFF9500)
        )
        
        val arrows = levelJson.Arrows.mapIndexed { index, arrowJson ->
            val points = arrowJson.Points
            val path = points.map { idx ->
                val r = (levelJson.YSize - 1) - (idx / levelJson.XSize)
                val c = idx % levelJson.XSize
                GameGridCell(r, c)
            }.reversed()
            
            val direction = if (path.size >= 2) {
                val last = path.last()
                val prev = path[path.size - 2]
                val dr = last.r - prev.r
                val dc = last.c - prev.c
                when {
                    dr < 0 -> GameArrowDirection.UP
                    dr > 0 -> GameArrowDirection.DOWN
                    dc < 0 -> GameArrowDirection.LEFT
                    else -> GameArrowDirection.RIGHT
                }
            } else {
                GameArrowDirection.RIGHT
            }
            
            val color = premiumColors[index % premiumColors.size]
            GameArrow(
                id = index + 1,
                path = path,
                direction = direction,
                color = color
            )
        }
        
        val shapeType = levelJson.ShapeType
        
        GameArrowLevelConfig(
            id = levelId,
            gridSize = gridSize,
            arrows = arrows,
            targetMoves = arrows.size + 3,
            shapeType = shapeType,
            xSize = levelJson.XSize,
            ySize = levelJson.YSize
        )
    } catch (e: Exception) {
        null
    }
}

fun generateArrowLevel(context: Context, levelId: Int, seedOffset: Int = 0): GameArrowLevelConfig {
    val loaded = loadArrowLevel(context, levelId)
    if (loaded != null) {
        return loaded
    }

    val gridSize = when (levelId) {
        84, 85, 86 -> 14
        else -> when {
            levelId == 1 -> 5
            levelId == 2 -> 10
            levelId == 3 -> 15
            levelId == 4 -> 20
            levelId <= 20 -> (20 + (levelId - 5) * 5 / 15)
            levelId <= 50 -> (30 + (levelId - 21) * 20 / 29)
            levelId <= 150 -> (50 + (levelId - 51) * 10 / 99)
            levelId <= 400 -> (60 + (levelId - 151) * 5 / 249)
            levelId <= 800 -> (70 + (levelId - 401) * 10 / 399)
            else -> (80 + (levelId - 801) * 20 / 199)
        }
    }
    
    val shapeType = when (levelId) {
        84 -> 8
        85 -> 9
        86 -> 37
        else -> 0
    }
    val premiumColors = listOf(
        Color(0xFFFF5E3A),
        Color(0xFF00A2FF),
        Color(0xFF34C759),
        Color(0xFFFFD93D),
        Color(0xFFAF52DE),
        Color(0xFFFF2D55),
        Color(0xFFE5BA73),
        Color(0xFFCCFF00),
        Color(0xFFFF9500)
    )
    val directions = GameArrowDirection.values()

    val expectedCovered = (0 until gridSize).sumOf { r ->
        (0 until gridSize).count { c ->
            isCellValidForShape(r, c, gridSize, shapeType)
        }
    }

    for (attempt in 0 until 1000) {
        val seed = levelId.toLong() * 10000L + seedOffset * 37L + attempt
        val config = generateSingleArrowLevelAttempt(levelId, gridSize, seed, premiumColors, directions, shapeType)
        if (config != null) {
            val hasLength1 = config.arrows.any { it.path.size < 2 }
            val coveredCount = config.arrows.sumOf { it.path.size }
            val isCovered = coveredCount == expectedCovered
            val solvable = isLevelSolvable(config.arrows, gridSize, gridSize)
            if (!hasLength1 && isCovered && solvable) {
                return config.copy(shapeType = shapeType)
            }
        }
    }
    
    // Fallback if all attempts fail
    return generateArrowLevelFallback(levelId, gridSize, premiumColors, shapeType)
}

fun generateSingleArrowLevelAttempt(
    levelId: Int,
    gridSize: Int,
    seed: Long,
    premiumColors: List<Color>,
    directions: Array<GameArrowDirection>,
    shapeType: Int
): GameArrowLevelConfig? {
    val random = java.util.Random(seed)
    
    // 1. Partition the grid into paths of length >= 2
    val visited = Array(gridSize) { r -> BooleanArray(gridSize) { c -> !isCellValidForShape(r, c, gridSize, shapeType) } }
    val paths = mutableListOf<List<GameGridCell>>()
    val isolatedCells = mutableListOf<GameGridCell>()
    
    for (r in 0 until gridSize) {
        for (c in 0 until gridSize) {
            if (!visited[r][c]) {
                val startCell = GameGridCell(r, c)
                val path = mutableListOf<GameGridCell>()
                path.add(startCell)
                visited[r][c] = true
                
                var current = startCell
                val targetLength = when {
                    gridSize <= 4 -> random.nextInt(3) + 4    // Lengths 4 to 6
                    gridSize <= 8 -> random.nextInt(7) + 8    // Lengths 8 to 14
                    gridSize <= 15 -> random.nextInt(18) + 18 // Lengths 18 to 35
                    else -> random.nextInt(46) + 35           // Lengths 35 to 80
                }
                var turns = 0
                val maxTurns = when {
                    gridSize <= 4 -> 2
                    gridSize <= 8 -> 5
                    else -> 12
                }
                var lastDir: GameArrowDirection? = null
                
                for (step in 1 until targetLength) {
                    val possibleNeighbors = directions.filter { dir ->
                        val next = current + dir
                        next.r in 0 until gridSize && next.c in 0 until gridSize && !visited[next.r][next.c]
                    }
                    if (possibleNeighbors.isEmpty()) break
                    
                    val validNeighbors = possibleNeighbors.filter { dir ->
                        lastDir == null || dir == lastDir || turns < maxTurns
                    }
                    if (validNeighbors.isEmpty()) break
                    
                    val chosenDir = validNeighbors[random.nextInt(validNeighbors.size)]
                    if (lastDir != null && chosenDir != lastDir) {
                        turns++
                    }
                    lastDir = chosenDir
                    current = current + chosenDir
                    path.add(current)
                    visited[current.r][current.c] = true
                }
                
                if (path.size == 1) {
                    isolatedCells.add(startCell)
                } else {
                    paths.add(path)
                }
            }
        }
    }
    
    // Merge isolated cells into adjacent paths
    val deferredIsolated = mutableListOf<GameGridCell>()
    for (cell in isolatedCells) {
        var merged = false
        val neighbors = directions.map { cell + it }
            .filter { it.r in 0 until gridSize && it.c in 0 until gridSize }
        
        for (neighbor in neighbors) {
            val adjPath = paths.find { neighbor in it } ?: continue
            val idx = adjPath.indexOf(neighbor)
            
            if (idx == 0) {
                val newPath = listOf(cell) + adjPath
                paths.remove(adjPath)
                paths.add(newPath)
                merged = true
                break
            } else if (idx == adjPath.size - 1) {
                val newPath = adjPath + cell
                paths.remove(adjPath)
                paths.add(newPath)
                merged = true
                break
            } else if (idx >= 1 && adjPath.size - idx >= 3) {
                val path1 = adjPath.subList(0, idx + 1) + cell
                val path2 = adjPath.subList(idx + 1, adjPath.size)
                paths.remove(adjPath)
                paths.add(path1)
                paths.add(path2)
                merged = true
                break
            } else if (idx >= 2 && adjPath.size - idx == 1) {
                val path1 = adjPath.subList(0, idx)
                val path2 = listOf(cell) + adjPath.subList(idx, adjPath.size)
                paths.remove(adjPath)
                paths.add(path1)
                paths.add(path2)
                merged = true
                break
            }
        }
        
        if (!merged) {
            deferredIsolated.add(cell)
        }
    }
    
    // Pair up or fallback merge the remaining deferred isolated cells
    val processedIsolated = mutableSetOf<GameGridCell>()
    for (cell in deferredIsolated) {
        if (cell in processedIsolated) continue
        
        val adjIsolatedNeighbor = directions.map { cell + it }
            .filter { it.r in 0 until gridSize && it.c in 0 until gridSize }
            .find { it in deferredIsolated && it !in processedIsolated }
            
        if (adjIsolatedNeighbor != null) {
            paths.add(listOf(cell, adjIsolatedNeighbor))
            processedIsolated.add(cell)
            processedIsolated.add(adjIsolatedNeighbor)
        } else {
            var merged = false
            val neighbors = directions.map { cell + it }
                .filter { it.r in 0 until gridSize && it.c in 0 until gridSize }
                
            for (neighbor in neighbors) {
                val adjPath = paths.find { neighbor in it } ?: continue
                val newPath = adjPath + cell
                paths.remove(adjPath)
                paths.add(newPath)
                merged = true
                break
            }
            if (!merged) return null
        }
    }
    
    // Ensure all paths have length >= 2
    if (paths.any { it.size < 2 }) return null
    
    // 2. Solve the partition in reverse to assign directions (simulating slides)
    val remainingPaths = paths.toMutableList()
    val gridOccupied = Array(gridSize) { BooleanArray(gridSize) { true } }
    val assignedArrows = mutableListOf<GameArrow>()
    var arrowIdCounter = 1
    
    // Set non-shape cells as unoccupied so they don't block the reverse solve
    for (r in 0 until gridSize) {
        for (c in 0 until gridSize) {
            if (!isCellValidForShape(r, c, gridSize, shapeType)) {
                gridOccupied[r][c] = false
            }
        }
    }
    
    while (remainingPaths.isNotEmpty()) {
        var removedAny = false
        
        for (path in remainingPaths) {
            val candidates = mutableListOf<Pair<GameGridCell, GameArrowDirection>>()
            if (path.size >= 2) {
                val head1 = path.last()
                val dir1 = directions.find { (path[path.size - 2] + it) == head1 }
                if (dir1 != null) {
                    candidates.add(head1 to dir1)
                }
                
                val head2 = path.first()
                val dir2 = directions.find { (path[1] + it) == head2 }
                if (dir2 != null) {
                    candidates.add(head2 to dir2)
                }
            }
            
            for (candidate in candidates) {
                val head = candidate.first
                val dir = candidate.second
                
                var clear = true
                var current = head + dir
                while (current.r in 0 until gridSize && current.c in 0 until gridSize) {
                    if (gridOccupied[current.r][current.c]) {
                        clear = false
                        break
                    }
                    current += dir
                }
                
                if (clear) {
                    val isLastHead = (head == path.last())
                    val arrowPath = if (isLastHead) path else path.reversed()
                    
                    assignedArrows.add(
                        GameArrow(
                            id = arrowIdCounter++,
                            path = arrowPath,
                            direction = dir,
                            color = premiumColors[random.nextInt(premiumColors.size)]
                        )
                    )
                    
                    path.forEach { cell ->
                        gridOccupied[cell.r][cell.c] = false
                    }
                    
                    remainingPaths.remove(path)
                    removedAny = true
                    break
                }
            }
            if (removedAny) break
        }
        
        if (!removedAny) {
            return null // Stuck (cycle detected), retry with different seed
        }
    }
    
    val targetMoves = assignedArrows.size + 3
    return GameArrowLevelConfig(levelId, gridSize, assignedArrows, targetMoves, shapeType)
}

fun generateArrowLevelFallback(
    levelId: Int,
    gridSize: Int,
    premiumColors: List<Color>,
    shapeType: Int
): GameArrowLevelConfig {
    val arrows = mutableListOf<GameArrow>()
    var arrowIdCounter = 1
    for (r in 0 until gridSize) {
        val rowCells = mutableListOf<GameGridCell>()
        for (c in 0 until gridSize) {
            if (isCellValidForShape(r, c, gridSize, shapeType)) {
                rowCells.add(GameGridCell(r, c))
            } else {
                if (rowCells.size >= 2) {
                    arrows.add(
                        GameArrow(
                            id = arrowIdCounter++,
                            path = rowCells.toList(),
                            direction = GameArrowDirection.RIGHT,
                            color = premiumColors[r % premiumColors.size]
                        )
                    )
                }
                rowCells.clear()
            }
        }
        if (rowCells.size >= 2) {
            arrows.add(
                GameArrow(
                    id = arrowIdCounter++,
                    path = rowCells.toList(),
                    direction = GameArrowDirection.RIGHT,
                    color = premiumColors[r % premiumColors.size]
                )
            )
        }
    }
    return GameArrowLevelConfig(levelId, gridSize, arrows, arrows.size + 3, shapeType)
}


fun isArrowBlocked(arrow: GameArrow, activeArrows: List<GameArrow>, xSize: Int, ySize: Int): Boolean {
    val otherArrowsOccupied = activeArrows.filter { it.id != arrow.id }
        .flatMap { it.getOccupiedCells() }
        .toSet()
        
    var current = arrow.getHeadCell() + arrow.direction
    while (current.r in 0 until ySize && current.c in 0 until xSize) {
        if (current in otherArrowsOccupied) {
            return true
        }
        current += arrow.direction
    }
    return false
}

@Composable
fun ConfettiCanvas(particlesProvider: () -> List<ConfettiParticle>) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val particles = particlesProvider()
        particles.forEach { c ->
            if (c.isCircle) {
                drawCircle(
                    color = c.color,
                    radius = c.size / 2,
                    center = Offset(c.x, c.y)
                )
            } else {
                rotate(degrees = c.rotation, pivot = Offset(c.x, c.y)) {
                    drawRect(
                        color = c.color,
                        topLeft = Offset(c.x - c.size / 2, c.y - c.size / 2),
                        size = Size(c.size, c.size * 1.4f)
                    )
                }
            }
        }
    }
}


// ==========================================
// PUZZLE BOARD (Home Screen 1:1 White Card)
// ==========================================
@Composable
fun PuzzleBoard(
    modifier: Modifier = Modifier,
    level: Int = 1,
    restartTrigger: Int = 0,
    hintTrigger: Int = 0,
    onMoveMade: () -> Unit = {},
    onWin: () -> Unit = {},
    onLifeLost: () -> Unit = {}
) {
    // 3D Card Container (1:1 Perfect Square)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f),
        contentAlignment = Alignment.Center
    ) {
        // 3D Bottom Depth Shadow Layer (3D Candy Depth)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset(y = 5.dp)
                .background(
                    color = Color(0xFF1D4ED8), // 3D Blue bottom depth
                    shape = RoundedCornerShape(24.dp)
                )
        )

        // Piche wala Card (Yellow Body with 1.dp Blue Frame)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .shadow(
                    elevation = 6.dp,
                    shape = RoundedCornerShape(24.dp),
                    spotColor = Color(0x33000000)
                )
                .background(
                    color = Color(0xFFFEF9C3), // Light yellow inner color
                    shape = RoundedCornerShape(24.dp)
                )
                .border(
                    width = 2.dp, // Blue frame: 2.dp (1.dp thicker)
                    color = Color(0xFF2563EB),
                    shape = RoundedCornerShape(24.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            // Main Card (Puzzle Board): 1.5.dp chota (padding = 1.5.dp), 50% Dark Black
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 3.5.dp, end = 3.5.dp, top = 3.dp, bottom = 2.dp)
                    .shadow(
                        elevation = 3.dp,
                        shape = RoundedCornerShape(22.dp),
                        spotColor = Color(0x22000000)
                    )
                    .background(
                        color = Color.Black.copy(alpha = 0.5f), // 50% Dark Black
                        shape = RoundedCornerShape(22.dp)
                    )
                    .border(
                        width = 2.dp,
                        brush = Brush.horizontalGradient(listOf(Color(0xFF0284C7), Color(0xFFFACC15), Color(0xFF22C55E))),
                        shape = RoundedCornerShape(22.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    FlowGameBoard(
                        level = level,
                        restartTrigger = restartTrigger,
                        hintTrigger = hintTrigger,
                        onMoveMade = onMoveMade,
                        onWin = onWin,
                        onLifeLost = onLifeLost
                    )
                }
            }
        }
    }
}

@Composable
fun ArrowPuzzleScreen(
    level: Int,
    coins: Int,
    onCoinsChange: (Int) -> Unit,
    restartTrigger: Int,
    resumeTrigger: Int,
    selectedBackgroundSkin: BackgroundSkin,
    onBack: () -> Unit,
    onPauseClick: () -> Unit,
    onWin: (Int, Int, Int) -> Unit,
    onGameOver: () -> Unit,
    skin: BottleSkin,
    isTimerPaused: Boolean,
    onShowAd: (String, () -> Unit, () -> Unit) -> Unit
) {
    val (difficulty, difficultyColor) = when {
        level <= 500 -> Pair("Easy", Color(0xFF22C55E))
        level <= 1000 -> Pair("Medium", Color(0xFFF59E0B))
        level <= 2000 -> Pair("Hard", Color(0xFF8B5CF6))
        level <= 3500 -> Pair("Expert", Color(0xFFEF4444))
        level <= 5000 -> Pair("Master", Color(0xFF9333EA))
        else -> Pair("Grandmaster", Color(0xFF06B6D4))
    }

    var resetCount by remember(level, restartTrigger) { mutableStateOf(0) }
    var hintCount by remember(level, restartTrigger) { mutableStateOf(0) }
    var freeHintsRemaining by remember(level, restartTrigger) { mutableStateOf(3) }
    var movesCount by remember(level, restartTrigger, resetCount) { mutableStateOf(0) }
    var timeRemaining by remember(level, restartTrigger, resetCount, resumeTrigger) { mutableStateOf(72) }
    var timeElapsed by remember(level, restartTrigger, resetCount) { mutableIntStateOf(0) }
    var livesCount by remember(level, restartTrigger, resetCount, resumeTrigger) { mutableStateOf(3) }

    LaunchedEffect(resumeTrigger) {
        if (resumeTrigger > 0) {
            livesCount = 3
            if (timeRemaining <= 10) {
                timeRemaining = 60
            }
        }
    }

    LaunchedEffect(isTimerPaused, restartTrigger, resetCount) {
        if (!isTimerPaused) {
            while (timeRemaining > 0) {
                kotlinx.coroutines.delay(1000L)
                timeRemaining--
                timeElapsed++
            }
            if (timeRemaining <= 0) {
                onGameOver()
            }
        }
    }

    ArrowPuzzleProBackground(skin = selectedBackgroundSkin) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 15.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Section (Header + Action Buttons + Stats)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 5.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Top Header Row: Close (Left), Level Badge (Center), Pause (Right)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Close / Back Button
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .bouncyClickable { onBack() }
                            .background(Color(0xFFFFF9C4), RoundedCornerShape(14.dp))
                            .border(
                                width = 2.5.dp,
                                brush = Brush.horizontalGradient(listOf(Color(0xFF0284C7), Color(0xFF22C55E))),
                                shape = RoundedCornerShape(14.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color(0xFF2C1B47),
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Level Badge
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFFFF9C4), RoundedCornerShape(18.dp))
                            .border(
                                width = 2.5.dp,
                                brush = Brush.horizontalGradient(listOf(Color(0xFF0284C7), Color(0xFFFACC15), Color(0xFF22C55E))),
                                shape = RoundedCornerShape(18.dp)
                            )
                            .padding(horizontal = 20.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Level $level",
                                color = Color(0xFF2C1B47),
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp,
                                fontFamily = LuckiestGuyFontFamily
                            )
                            Text(
                                text = difficulty,
                                color = difficultyColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                fontFamily = LuckiestGuyFontFamily
                            )
                        }
                    }

                    // Pause Button
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .bouncyClickable { onPauseClick() }
                            .background(Color(0xFFFFF9C4), RoundedCornerShape(14.dp))
                            .border(
                                width = 2.5.dp,
                                brush = Brush.horizontalGradient(listOf(Color(0xFF0284C7), Color(0xFF22C55E))),
                                shape = RoundedCornerShape(14.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Pause,
                            contentDescription = "Pause",
                            tint = Color(0xFF2C1B47),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // Action Buttons Row: Exactly 2 buttons (RESET Orange, HINT (5) Green)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // RESET Button
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .bouncyClickable {
                                resetCount++
                                movesCount = 0
                                timeRemaining = 72
                            }
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(Color(0xFFFF9F1C), Color(0xFFF15A24))
                                ),
                                shape = RoundedCornerShape(14.dp)
                            )
                            .border(
                                width = 2.5.dp,
                                brush = Brush.horizontalGradient(listOf(Color(0xFF0284C7), Color(0xFF22C55E))),
                                shape = RoundedCornerShape(14.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Reset",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "RESET",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = LuckiestGuyFontFamily
                            )
                        }
                    }

                    // HINT Button (Limit 3, when exhausted shows round Ad badge with purple border at inner top right)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .bouncyClickable {
                                if (freeHintsRemaining > 0) {
                                    freeHintsRemaining--
                                    hintCount++
                                } else {
                                    onShowAd("free_hint_rewarded", {}) {
                                        hintCount++
                                    }
                                }
                            }
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(Color(0xFF4ADE80), Color(0xFF16A34A))
                                ),
                                shape = RoundedCornerShape(14.dp)
                            )
                            .border(
                                width = 2.5.dp,
                                brush = Brush.horizontalGradient(listOf(Color(0xFF0284C7), Color(0xFFFF9F1C))),
                                shape = RoundedCornerShape(14.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = "Hint",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = if (freeHintsRemaining > 0) "HINT ($freeHintsRemaining)" else "HINT",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = LuckiestGuyFontFamily
                            )
                        }

                        // When limit 3 is exhausted, inner top right round Ad badge with purple/bengani border
                        if (freeHintsRemaining == 0) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .offset(x = (-4).dp, y = 4.dp)
                                    .size(20.dp)
                                    .background(Color(0xFFFFF9C4), CircleShape)
                                    .border(1.5.dp, Color(0xFF7C3AED), CircleShape), // Bengani / Purple border
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Ad",
                                    color = Color(0xFF6D28D9), // Bengani text
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = LuckiestGuyFontFamily
                                )
                            }
                        }
                    }
                }

                // Stats Row: Moves: 0 (Left), Time: 72s (Center), 3 Hearts (Right)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Moves: $movesCount",
                        color = Color.Black,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = LuckiestGuyFontFamily
                    )

                    Text(
                        text = "Time: ${timeRemaining}s",
                        color = Color.Black,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = LuckiestGuyFontFamily
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(3) { index ->
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = "Life",
                                tint = if (index < livesCount) Color(0xFFEF4444) else Color.White.copy(alpha = 0.3f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // Center Area: 3D Puzzle Board Card running FlowGameBoard (Color Pipes Game!) moved 25.dp further up & 10.dp bigger
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .offset(y = (-75).dp),
                contentAlignment = Alignment.Center
            ) {
                PuzzleBoard(
                    modifier = Modifier
                        .fillMaxWidth(),
                    level = level,
                    restartTrigger = restartTrigger + resetCount,
                    hintTrigger = hintCount,
                    onMoveMade = { movesCount++ },
                    onWin = { onWin(3, movesCount, timeElapsed) },
                    onLifeLost = {
                        if (livesCount > 1) {
                            livesCount--
                            SoundManager.playWrongSound()
                        } else if (livesCount == 1) {
                            livesCount = 0
                            SoundManager.playWrongSound()
                            onGameOver()
                        }
                    }
                )
            }

            // Bottom Spacing
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

data class ConfettiParticle(
    var x: Float,
    var y: Float,
    val vx: Float,
    var vy: Float,
    var rotation: Float,
    val rotationSpeed: Float,
    val color: Color,
    val size: Float,
    val isCircle: Boolean
)

enum class BottleSkin(val displayName: String, val price: Int) {
    Standard("Classic Arrow", 0)
}

fun getSmoothSubPath(nodes: List<Offset>, progress: Float): List<Offset> {
    if (nodes.size <= 1 || progress >= 1f) return nodes
    if (progress <= 0f) return listOf(nodes.first())
    
    val totalSegments = nodes.size - 1
    val currentLength = progress * totalSegments
    val fullSegments = currentLength.toInt()
    val remainder = currentLength - fullSegments
    
    val result = mutableListOf<Offset>()
    for (i in 0..fullSegments) {
        result.add(nodes[i])
    }
    if (remainder > 0f && fullSegments < totalSegments) {
        val lastNode = nodes[fullSegments]
        val nextNode = nodes[fullSegments + 1]
        val interpolationPoint = Offset(
            lastNode.x + (nextNode.x - lastNode.x) * remainder,
            lastNode.y + (nextNode.y - lastNode.y) * remainder
        )
        result.add(interpolationPoint)
    }
    return result
}

fun getSlidingPathPoints(
    originalPath: List<GameGridCell>,
    direction: GameArrowDirection,
    t: Float,
    gridSize: Int,
    w: Float,
    cellW: Float,
    cellH: Float
): List<Offset> {
    if (originalPath.isEmpty()) return emptyList()
    
    val d = t + w
    val k = originalPath.size
    
    val nodes = mutableListOf<Offset>()
    originalPath.forEach { cell ->
        nodes.add(Offset((cell.c + 0.5f) * cellW, (cell.r + 0.5f) * cellH))
    }
    
    val lastCell = originalPath.last()
    val numExtensions = 100
    for (i in 1..numExtensions) {
        val nextC = lastCell.c + direction.dc * i
        val nextR = lastCell.r + direction.dr * i
        nodes.add(Offset((nextC + 0.5f) * cellW, (nextR + 0.5f) * cellH))
    }
    
    val sMin = d
    val sMax = d + (k - 1)
    
    val points = mutableListOf<Offset>()
    
    fun getPointAt(s: Float): Offset {
        val idx = s.toInt().coerceIn(0, nodes.size - 2)
        val f = s - s.toInt()
        val p1 = nodes[idx]
        val p2 = nodes[idx + 1]
        return Offset(
            p1.x * (1f - f) + p2.x * f,
            p1.y * (1f - f) + p2.y * f
        )
    }
    
    points.add(getPointAt(sMin))
    
    val startInt = (sMin + 0.0001f).toInt() + 1
    val endInt = (sMax - 0.0001f).toInt()
    for (j in startInt..endInt) {
        if (j >= 0 && j < nodes.size) {
            points.add(nodes[j])
        }
    }
    
    points.add(getPointAt(sMax))
    
    return points
}

fun DrawScope.drawStyledArrow(
    arrow: GameArrow,
    points: List<Offset>,
    isHint: Boolean,
    isWrong: Boolean = false,
    cellWidth: Float,
    hintScale: Float = 1f
) {
    if (points.isEmpty()) return
    
    val strokeWidth = (cellWidth * 0.20f).coerceAtLeast(3.0f)
    val headSize = (cellWidth * 0.45f).coerceAtLeast(8.0f)
    val shadowOffsetX = (cellWidth * 0.02f).coerceAtLeast(0.3f)
    val shadowOffsetY = (cellWidth * 0.03f).coerceAtLeast(0.5f)
    val cornerRadius = (cellWidth * 0.05f).coerceAtLeast(0.5f)
    
    val arrowColor = if (isWrong) Color(0xFFE24343) else Color(0xFFFF9F1C)
    
    val minX = points.minOf { it.x }
    val maxX = points.maxOf { it.x }
    val minY = points.minOf { it.y }
    val maxY = points.maxOf { it.y }
    val centerX = (minX + maxX) / 2f
    val centerY = (minY + maxY) / 2f
    
    val pulseScale = if (isHint) {
        hintScale
    } else {
        1f
    }

    withTransform({
        if (isHint) {
            scale(pulseScale, pulseScale, Offset(centerX, centerY))
        }
    }) {
        val shadowPath = Path().apply {
            moveTo(points.first().x + shadowOffsetX, points.first().y + shadowOffsetY)
            for (i in 1 until points.size) {
                lineTo(points[i].x + shadowOffsetX, points[i].y + shadowOffsetY)
            }
        }
        drawPath(
            path = shadowPath,
            color = Color.Black.copy(alpha = 0.15f),
            style = Stroke(
                width = strokeWidth,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round,
                pathEffect = PathEffect.cornerPathEffect(cornerRadius)
            )
        )

        val bodyPath = Path().apply {
            moveTo(points.first().x, points.first().y)
            for (i in 1 until points.size) {
                lineTo(points[i].x, points[i].y)
            }
        }
        drawPath(
            path = bodyPath,
            color = arrowColor,
            style = Stroke(
                width = strokeWidth,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round,
                pathEffect = PathEffect.cornerPathEffect(cornerRadius)
            )
        )

        val headPoint = points.last()
        val headPath = Path()
        val headW = headSize * 0.6f
        val headL = headSize * 0.7f
        when (arrow.direction) {
            GameArrowDirection.UP -> {
                headPath.moveTo(headPoint.x, headPoint.y - headL)
                headPath.lineTo(headPoint.x - headW, headPoint.y + headL * 0.3f)
                headPath.lineTo(headPoint.x + headW, headPoint.y + headL * 0.3f)
                headPath.close()
            }
            GameArrowDirection.DOWN -> {
                headPath.moveTo(headPoint.x, headPoint.y + headL)
                headPath.lineTo(headPoint.x - headW, headPoint.y - headL * 0.3f)
                headPath.lineTo(headPoint.x + headW, headPoint.y - headL * 0.3f)
                headPath.close()
            }
            GameArrowDirection.LEFT -> {
                headPath.moveTo(headPoint.x - headL, headPoint.y)
                headPath.lineTo(headPoint.x + headL * 0.3f, headPoint.y - headW)
                headPath.lineTo(headPoint.x + headL * 0.3f, headPoint.y + headW)
                headPath.close()
            }
            GameArrowDirection.RIGHT -> {
                headPath.moveTo(headPoint.x + headL, headPoint.y)
                headPath.lineTo(headPoint.x - headL * 0.3f, headPoint.y - headW)
                headPath.lineTo(headPoint.x - headL * 0.3f, headPoint.y + headW)
                headPath.close()
            }
        }

        val shadowHeadPath = Path()
        shadowHeadPath.addPath(headPath, Offset(shadowOffsetX, shadowOffsetY))
        drawPath(shadowHeadPath, color = Color.Black.copy(alpha = 0.12f))

        drawPath(headPath, color = arrowColor)
    }
}

@Composable
fun QuizHeader(level: Int, lives: Int, timeLeft: Int, onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .background(Color.Transparent)
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(4.dp).align(Alignment.BottomCenter).background(Color.Black.copy(alpha = 0.1f)))

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = {
                onBack()
            }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = TextDark) }
            Text("LEVEL $level", color = TextDark, fontWeight = FontWeight.Black, fontSize = 20.sp)
            
            Spacer(Modifier.weight(1f))
            
            Box(modifier = Modifier.width(120.dp).height(32.dp).background(Color.Black.copy(alpha = 0.1f), CircleShape).padding(4.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(timeLeft / 20f)
                        .fillMaxHeight()
                        .background(SunnyYellow, CircleShape)
                )
                Text("$timeLeft", color = TextDark, modifier = Modifier.align(Alignment.Center), fontSize = 12.sp, fontWeight = FontWeight.Black)
            }
            
            Spacer(Modifier.width(16.dp))
            
            Row {
                repeat(3) { i ->
                    Icon(
                        Icons.Default.Bolt, null,
                        tint = if (i < lives) SunnyYellow else Color.Black.copy(alpha = 0.1f),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun NativeAdSlot(screenName: String = "tester_dashboard_native", height: Dp = 150.dp) {
    val context = LocalContext.current
    val isEnabled = SettingsManager.getAdTypeForScreen(screenName) == "native" || SettingsManager.isNativeEnabled(screenName)
    if (!isEnabled) return

    var loadedNativeAd by remember { mutableStateOf<NativeAd?>(null) }

    DisposableEffect(screenName) {
        AdManager.loadNativeAd(context) { ad ->
            loadedNativeAd = ad
        }
        onDispose {
            try {
                loadedNativeAd?.destory()
            } catch (e: Exception) {}
            loadedNativeAd = null
        }
    }

    loadedNativeAd?.let { nativeAd ->
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .background(Color(0xFF1E293B).copy(alpha = 0.95f), RoundedCornerShape(18.dp))
                .border(2.dp, Color(0xFF8338EC).copy(alpha = 0.6f), RoundedCornerShape(18.dp))
                .clip(RoundedCornerShape(18.dp))
                .padding(8.dp)
        ) {
            AndroidView(
                factory = { ctx ->
                    val adView = LayoutInflater.from(ctx).inflate(R.layout.ad_unified, null) as ATNativeAdView
                    populateNativeAdView(nativeAd, adView)
                    adView
                },
                update = { adView ->
                    populateNativeAdView(nativeAd, adView)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
            )
        }
    }
}
fun populateNativeAdView(nativeAd: NativeAd, adView: ATNativeAdView) {
    if (nativeAd.isNativeExpress) {
        nativeAd.renderAdContainer(adView, null)
        nativeAd.prepare(adView, null)
    } else {
        val material = nativeAd.adMaterial ?: return
        
        val titleView = adView.findViewById<TextView>(R.id.ad_headline)
        val ctaButton = adView.findViewById<Button>(R.id.ad_call_to_action)
        val mediaViewContainer = adView.findViewById<FrameLayout>(R.id.ad_media)
        val iconView = adView.findViewById<ImageView>(R.id.ad_app_icon)
        val bodyView = adView.findViewById<TextView>(R.id.ad_body)

        titleView?.text = material.title ?: ""
        ctaButton?.text = material.callToActionText ?: "INSTALL"
        if (bodyView != null) {
            val desc = material.descriptionText
            if (!desc.isNullOrEmpty()) {
                bodyView.text = desc
                bodyView.visibility = View.VISIBLE
            } else {
                bodyView.visibility = View.GONE
            }
        }

        if (mediaViewContainer != null) {
            val adMediaView = material.getAdMediaView(mediaViewContainer)
            if (adMediaView != null) {
                mediaViewContainer.removeAllViews()
                mediaViewContainer.addView(adMediaView)
            }
        }

        if (iconView != null) {
            val iconUrl = material.iconImageUrl
            if (!iconUrl.isNullOrEmpty()) {
                GlobalScope.launch(Dispatchers.Main) {
                    val bitmap = withContext(Dispatchers.IO) {
                        try {
                            val stream = URL(iconUrl).openStream()
                            BitmapFactory.decodeStream(stream)
                        } catch (e: Exception) {
                            null
                        }
                    }
                    if (bitmap != null) {
                        iconView.setImageBitmap(bitmap)
                        iconView.visibility = View.VISIBLE
                    } else {
                        iconView.visibility = View.GONE
                    }
                }
            } else {
                iconView.visibility = View.GONE
            }
        }

        val prepareInfo = ATNativePrepareInfo()
        prepareInfo.setParentView(adView)
        prepareInfo.setTitleView(titleView)
        prepareInfo.setCtaView(ctaButton)
        if (mediaViewContainer != null) {
            prepareInfo.setMainImageView(mediaViewContainer)
        }
        if (iconView != null) {
            prepareInfo.setIconView(iconView)
        }
        if (bodyView != null) {
            prepareInfo.setDescView(bodyView)
        }

        val clickableViews = mutableListOf<View>()
        if (ctaButton != null) clickableViews.add(ctaButton)
        clickableViews.add(adView)
        if (titleView != null) clickableViews.add(titleView)
        if (mediaViewContainer != null) clickableViews.add(mediaViewContainer)

        prepareInfo.setClickViewList(clickableViews)

        val selfRenderView = if (adView.childCount > 0) adView.getChildAt(0) else null

        nativeAd.renderAdContainer(adView, selfRenderView)
        nativeAd.prepare(adView, prepareInfo)
    }
}
// --- POPUPS ---

@Composable
fun FullScreenPopupContainer(
    gradientColors: List<Color> = emptyList(),
    showBorder: Boolean = false,
    cardColor: Color = Color(0xFFFFF9E6),
    borderColor: Color = Color(0xFF2C1B47),
    borderWidth: Dp = 6.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.65f))
            .clickable(enabled = false) {}
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .wrapContentHeight()
                .shadow(8.dp, RoundedCornerShape(24.dp), clip = false)
                .background(cardColor, RoundedCornerShape(24.dp))
                .border(borderWidth, borderColor, RoundedCornerShape(24.dp))
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            content()
        }
    }
}

@Composable
fun CorrectAnswerPopup(onNext: () -> Unit) {
    FullScreenPopupContainer(
        gradientColors = emptyList()
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(Color(0xFFE8F5E9), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = Color(0xFF34C759),
                modifier = Modifier.size(64.dp)
            )
        }
        Spacer(Modifier.height(32.dp))
        Text(
            text = "CORRECT!",
            color = Color(0xFF34C759),
            fontSize = 42.sp,
            fontWeight = FontWeight.Black,
            fontFamily = LuckiestGuyFontFamily
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Great job! Get ready for the next move.",
            color = Color(0xFF262F3C),
            fontSize = 18.sp,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(48.dp))
        PopupThreeDButton(
            text = "CONTINUE",
            onClick = onNext,
            baseColor = Color(0xFF00A2FF),
            modifier = Modifier.fillMaxWidth(),
            isLarge = true
        )
    }
}

@Composable
fun WrongAnswerPopup(onRetry: () -> Unit, onBack: () -> Unit) {
    FullScreenPopupContainer(
        gradientColors = emptyList()
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(Color(0xFFFFEBEE), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = null,
                tint = Color(0xFFFF2D55),
                modifier = Modifier.size(64.dp)
            )
        }
        Spacer(Modifier.height(32.dp))
        Text(
            text = "WRONG!",
            color = Color(0xFFFF2D55),
            fontSize = 42.sp,
            fontWeight = FontWeight.Black,
            fontFamily = LuckiestGuyFontFamily
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Oops! Let's try again.",
            color = Color(0xFF262F3C),
            fontSize = 18.sp,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(48.dp))
        PopupThreeDButton(
            text = "RETRY",
            onClick = onRetry,
            baseColor = Color(0xFF34C759),
            modifier = Modifier.fillMaxWidth(),
            isLarge = true
        )
        Spacer(Modifier.height(16.dp))
        PopupThreeDButton(
            text = "EXIT",
            onClick = onBack,
            baseColor = Color(0xFF94A3B8),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun TimeOutPopup(onRetry: () -> Unit, onBack: () -> Unit) {
    FullScreenPopupContainer(
        gradientColors = emptyList()
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(Color(0xFFFFF3E0), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.TimerOff,
                contentDescription = null,
                tint = Color(0xFFFF9500),
                modifier = Modifier.size(64.dp)
            )
        }
        Spacer(Modifier.height(32.dp))
        Text(
            text = "TIME LIMIT!",
            color = Color(0xFFFF9500),
            fontSize = 42.sp,
            fontWeight = FontWeight.Black,
            fontFamily = LuckiestGuyFontFamily
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Your time has run out.",
            color = Color(0xFF262F3C),
            fontSize = 18.sp,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(48.dp))
        PopupThreeDButton(
            text = "PLAY AGAIN",
            onClick = onRetry,
            baseColor = Color(0xFF34C759),
            modifier = Modifier.fillMaxWidth(),
            isLarge = true
        )
        Spacer(Modifier.height(16.dp))
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.Default.Home,
                contentDescription = "Home",
                tint = Color(0xFF00A2FF),
                modifier = Modifier.size(40.dp)
            )
        }
    }
}

@Composable
fun SpeedometerGauge(fraction: Float, modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxWidth(0.6f)
    ) {
        Canvas(modifier = Modifier.fillMaxWidth().aspectRatio(2f)) {
            val w = size.width
            val h = size.height
            val cx = w / 2
            val cy = h

            val strokeWidth = 14.dp.toPx()
            val radius = h - strokeWidth

            // Draw background arc
            drawArc(
                color = Color(0xFFE2E8F0),
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = Offset(cx - radius, cy - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Draw colorful filled arc matching fraction
            val sweepAngle = 180f * fraction

            drawArc(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color(0xFFFFA000), Color(0xFFFF5722))
                ),
                startAngle = 180f,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = Offset(cx - radius, cy - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Draw needle pointer
            val needleLength = radius - 10.dp.toPx()
            val needleAngleRad = Math.toRadians((180f + sweepAngle).toDouble())
            val needleX = cx + (needleLength * Math.cos(needleAngleRad)).toFloat()
            val needleY = cy + (needleLength * Math.sin(needleAngleRad)).toFloat()

            // Center hub circle
            drawCircle(
                color = Color(0xFF1E293B),
                radius = 10.dp.toPx(),
                center = Offset(cx, cy)
            )

            // Needle line
            drawLine(
                color = Color(0xFF1E293B),
                start = Offset(cx, cy),
                end = Offset(needleX, needleY),
                strokeWidth = 4.dp.toPx(),
                cap = StrokeCap.Round
            )
        }

        Spacer(Modifier.height(6.dp))

        // Gauge label marks
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "0",
                color = Color(0xFF94A3B8),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "500",
                color = Color(0xFF94A3B8),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun SpeechBubble(text: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Upward pointing speech bubble tail
        Box(
            modifier = Modifier
                .size(16.dp)
                .offset(y = 10.dp)
                .rotate(45f)
                .background(Color.White)
                .border(2.dp, Color(0xFFE2E8F0))
        )

        // Bubble main text block
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(2.dp, RoundedCornerShape(20.dp))
                .background(Color.White, RoundedCornerShape(20.dp))
                .border(2.dp, Color(0xFFE2E8F0), RoundedCornerShape(20.dp))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = Color(0xFF1E293B),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun GameOverPopup(score: Int, onWatchAd: () -> Unit, onRestart: () -> Unit, onBack: () -> Unit) {
    val wobbleTransition = rememberInfiniteTransition(label = "sadWobble")
    val wobbleAngle by wobbleTransition.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "wobble"
    )

    val pulseScale by wobbleTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.65f))
            .clickable(enabled = false) {}
            .padding(horizontal = 18.dp, vertical = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth(0.97f)
                .shadow(8.dp, RoundedCornerShape(24.dp), clip = false)
                .background(Color(0xFFFFF9E6), RoundedCornerShape(24.dp))
                .border(
                    width = 3.5.dp,
                    brush = Brush.horizontalGradient(listOf(Color(0xFF0284C7), Color(0xFFFACC15), Color(0xFF22C55E))),
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(start = 15.dp, end = 15.dp, top = 20.dp, bottom = 24.dp)
        ) {
            // Header Row: OUT OF LIVES! (Left) and Close Cut Button (Right)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Lives",
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "OUT OF LIVES!",
                        style = TextStyle(
                            color = Color(0xFF2563EB),
                            fontSize = 21.5.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = LuckiestGuyFontFamily
                        )
                    )
                }

                // Cut/Close button matching gameplay screen back/cut button
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .bouncyClickable {
                            onBack()
                        }
                        .background(Color(0xFFFFF9C4), RoundedCornerShape(14.dp))
                        .border(
                            2.5.dp,
                            Brush.horizontalGradient(listOf(Color(0xFF0284C7), Color(0xFF22C55E))),
                            RoundedCornerShape(14.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.Black,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            // Central App Logo with Wobble & Pulse Animation (size 113.dp)
            Box(
                modifier = Modifier
                    .size(113.dp)
                    .graphicsLayer {
                        rotationZ = wobbleAngle
                        scaleX = pulseScale
                        scaleY = pulseScale
                    }
                    .padding(vertical = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo_image),
                    contentDescription = "App Logo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }
            
            Spacer(Modifier.height(6.dp))

            Text(
                text = "Don't Give Up!",
                color = Color(0xFF1E293B),
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                fontFamily = LuckiestGuyFontFamily,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = "Watch a quick video to get +3 Extra Lives and continue solving, or restart the puzzle.",
                color = Color(0xFF475569),
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
            )
            
            Spacer(Modifier.height(14.dp))
            
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(13.dp)
            ) {
                // 1. WATCH AD FOR +3 LIVES (Green Button with purple FREE Ad at top & zoom animation)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .zoomClickable {
                            onWatchAd()
                        }
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color(0xFF4ADE80), Color(0xFF16A34A))
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .border(
                            width = 2.5.dp,
                            brush = Brush.horizontalGradient(
                                listOf(Color(0xFF0284C7), Color(0xFFFACC15), Color(0xFF22C55E))
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "FREE Ad",
                            color = Color(0xFF4C1D95), // Bengni / Deep Purple Color
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = LuckiestGuyFontFamily,
                            letterSpacing = 0.5.sp
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Watch Ad",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                            Text(
                                text = "RESUME (+3 LIVES)",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = LuckiestGuyFontFamily
                            )
                        }
                    }
                }

                // 2. RESTART Button (Orange Button card with full zoom animation on click)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .zoomClickable {
                            onRestart()
                        }
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color(0xFFFF9F1C), Color(0xFFF15A24))
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .border(
                            width = 2.5.dp,
                            brush = Brush.horizontalGradient(
                                listOf(Color(0xFF0284C7), Color(0xFFFACC15), Color(0xFF22C55E))
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Restart",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "RESTART LEVEL",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = LuckiestGuyFontFamily
                        )
                    }
                }

                // 3. QUIT TO MAP Button (Red Button card with full zoom animation on click)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .zoomClickable {
                            onBack()
                        }
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color(0xFFEF4444), Color(0xFFDC2626))
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .border(
                            width = 2.5.dp,
                            brush = Brush.horizontalGradient(
                                listOf(Color(0xFF0284C7), Color(0xFFFACC15), Color(0xFF22C55E))
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "Quit To Map",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "QUIT TO MAP",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = LuckiestGuyFontFamily
                        )
                    }
                }
            }
        }
    }
}
@Composable
fun PausedPopup(
    onResume: () -> Unit,
    onRestart: () -> Unit,
    onTutorial: () -> Unit,
    onQuit: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.65f))
            .clickable(enabled = false) {}
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .shadow(8.dp, RoundedCornerShape(24.dp), clip = false)
                .background(Color(0xFFFFF9E6), RoundedCornerShape(24.dp))
                .border(
                    width = 3.5.dp,
                    brush = Brush.horizontalGradient(listOf(Color(0xFF0284C7), Color(0xFFFACC15), Color(0xFF22C55E))),
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 36.dp)
        ) {
            // Header Row: GAME PAUSED (Left) and Cut Button (Right)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "GAME PAUSED",
                    style = TextStyle(
                        color = Color(0xFF2563EB), // Blue text
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = LuckiestGuyFontFamily
                    )
                )

                // Cut/Close button matching gameplay screen back/cut button
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .bouncyClickable {
                            onResume()
                        }
                        .background(Color(0xFFFFF9C4), RoundedCornerShape(14.dp))
                        .border(
                            2.5.dp,
                            Brush.horizontalGradient(listOf(Color(0xFF0284C7), Color(0xFF22C55E))),
                            RoundedCornerShape(14.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.Black,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(13.dp)
            ) {
                // 1. RESUME (Green Button card with full zoom animation on click)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .zoomClickable {
                            onResume()
                        }
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color(0xFF4ADE80), Color(0xFF16A34A))
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .border(
                            width = 2.5.dp,
                            brush = Brush.horizontalGradient(
                                listOf(Color(0xFF0284C7), Color(0xFFFACC15), Color(0xFF22C55E))
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Resume",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "RESUME",
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = LuckiestGuyFontFamily
                        )
                    }
                }

                // 2. RESTART (Orange Button card with full zoom animation on click)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .zoomClickable {
                            onRestart()
                        }
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color(0xFFFF9F1C), Color(0xFFF15A24))
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .border(
                            width = 2.5.dp,
                            brush = Brush.horizontalGradient(
                                listOf(Color(0xFF0284C7), Color(0xFFFACC15), Color(0xFF22C55E))
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Restart",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "RESTART",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = LuckiestGuyFontFamily
                        )
                    }
                }

                // 3. HOW TO PLAY (Purple Button card with full zoom animation on click)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .zoomClickable {
                            onTutorial()
                        }
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color(0xFF8B5CF6), Color(0xFF6D28D9))
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .border(
                            width = 2.5.dp,
                            brush = Brush.horizontalGradient(
                                listOf(Color(0xFF0284C7), Color(0xFFFACC15), Color(0xFF22C55E))
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "How To Play",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "HOW TO PLAY",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = LuckiestGuyFontFamily
                        )
                    }
                }

                // 4. QUIT TO MAP (Red Button card with full zoom animation on click)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .zoomClickable {
                            onQuit()
                        }
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color(0xFFEF4444), Color(0xFFDC2626))
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .border(
                            width = 2.5.dp,
                            brush = Brush.horizontalGradient(
                                listOf(Color(0xFF0284C7), Color(0xFFFACC15), Color(0xFF22C55E))
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "Quit To Map",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "QUIT TO MAP",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = LuckiestGuyFontFamily
                        )
                    }
                }
            }
            Spacer(Modifier.height(4.dp))
        }
    }
}

@Composable
fun TutorialPopup(selectedBackgroundSkin: BackgroundSkin, onClose: () -> Unit) {
    var step by remember { mutableIntStateOf(1) }
    var dragAmountTotal by remember { mutableFloatStateOf(0f) }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragStart = { dragAmountTotal = 0f },
                    onDragEnd = {
                        if (dragAmountTotal > 120f) {
                            if (step > 1) {
                                step--
                            }
                        } else if (dragAmountTotal < -120f) {
                            if (step < 4) {
                                step++
                            }
                        }
                    },
                    onHorizontalDrag = { change, dragAmount ->
                        change.consume()
                        dragAmountTotal += dragAmount
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        // Dark Dim Background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.65f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {}
        )

        // Main Dialog Card (Standard Cream Card with Tri-Color Gradient Border)
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight()
                .shadow(16.dp, RoundedCornerShape(32.dp), clip = false)
                .background(Color(0xFFFFF9E6), RoundedCornerShape(32.dp))
                .border(
                    width = 3.5.dp,
                    brush = Brush.horizontalGradient(
                        listOf(Color(0xFF0284C7), Color(0xFFFACC15), Color(0xFF22C55E))
                    ),
                    shape = RoundedCornerShape(32.dp)
                )
                .padding(horizontal = 20.dp, vertical = 22.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header Row with Title (Left) and Close Button (Right) - Zero Overlap
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        horizontalAlignment = Alignment.Start,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.weight(1f).padding(end = 8.dp)
                    ) {
                        Text(
                            text = "HOW TO PLAY",
                            style = TextStyle(
                                color = Color(0xFF0284C7),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = LuckiestGuyFontFamily,
                                shadow = Shadow(
                                    color = Color(0xFF0369A1),
                                    offset = Offset(0f, 3f),
                                    blurRadius = 0f
                                )
                            )
                        )
                        
                        // Badge Pill for Step
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFFFF9C4), RoundedCornerShape(12.dp))
                                .border(1.5.dp, Color(0xFF22C55E), RoundedCornerShape(12.dp))
                                .padding(horizontal = 10.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "STEP $step OF 4 • COLOR PIPES GUIDE",
                                color = Color(0xFF15803D),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = LuckiestGuyFontFamily,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }

                    // Top Right Close / Cut Button (Matching Gameplay Screen Back Button)
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .bouncyClickable {
                                onClose()
                            }
                            .background(Color(0xFFFFF9C4), RoundedCornerShape(14.dp))
                            .border(
                                width = 2.5.dp,
                                brush = Brush.horizontalGradient(listOf(Color(0xFF0284C7), Color(0xFF22C55E))),
                                shape = RoundedCornerShape(14.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color(0xFF2C1B47),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                // Solved Flow Pipe Preview Card (Matching Win Popup LevelCompleteDialog)
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .background(Color(0xFF0F172A), RoundedCornerShape(16.dp))
                        .border(
                            width = 2.5.dp,
                            brush = Brush.horizontalGradient(listOf(Color(0xFF0284C7), Color(0xFF22C55E))),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val totalW = size.width
                        val totalH = size.height
                        val spacing = 3.dp.toPx()
                        val gridSize = 4
                        val cellW = (totalW - (gridSize - 1) * spacing) / gridSize
                        val cellH = (totalH - (gridSize - 1) * spacing) / gridSize
                        val stepX = cellW + spacing
                        val stepY = cellH + spacing

                        fun getCenter(r: Int, c: Int): Offset {
                            return Offset(c * stepX + cellW / 2f, r * stepY + cellH / 2f)
                        }

                        // 1. Draw grid cell backgrounds
                        for (r in 0 until gridSize) {
                            for (c in 0 until gridSize) {
                                val topLeft = Offset(c * stepX, r * stepY)
                                drawRoundRect(
                                    color = Color(0xFF1E293B),
                                    topLeft = topLeft,
                                    size = Size(cellW, cellH),
                                    cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
                                )
                            }
                        }

                        val pipeStroke = cellW * 0.44f
                        val dotRadius = cellW * 0.36f

                        when (step) {
                            1 -> {
                                val p1 = getCenter(1, 0)
                                val p2 = getCenter(1, 3)
                                drawLine(
                                    color = Color(0xFFFF3366),
                                    start = p1,
                                    end = p2,
                                    strokeWidth = pipeStroke,
                                    cap = StrokeCap.Round
                                )
                                drawCircle(color = Color(0xFFFF3366), radius = dotRadius, center = p1)
                                drawCircle(color = Color.White.copy(alpha = 0.6f), radius = dotRadius * 0.38f, center = p1)
                                drawCircle(color = Color(0xFFFF3366), radius = dotRadius, center = p2)
                                drawCircle(color = Color.White.copy(alpha = 0.6f), radius = dotRadius * 0.38f, center = p2)

                                val pulse = 1f + 0.1f * kotlin.math.sin(System.currentTimeMillis() / 150.0).toFloat()
                                drawCircle(
                                    color = Color(0xFFFF3366).copy(alpha = 0.35f),
                                    radius = dotRadius * 1.3f * pulse,
                                    center = p2
                                )
                            }
                            2 -> {
                                val r1 = getCenter(1, 0)
                                val r2 = getCenter(1, 3)
                                drawLine(
                                    color = Color(0xFFFF3366),
                                    start = r1,
                                    end = r2,
                                    strokeWidth = pipeStroke,
                                    cap = StrokeCap.Round
                                )
                                drawCircle(color = Color(0xFFFF3366), radius = dotRadius, center = r1)
                                drawCircle(color = Color.White.copy(alpha = 0.6f), radius = dotRadius * 0.38f, center = r1)
                                drawCircle(color = Color(0xFFFF3366), radius = dotRadius, center = r2)
                                drawCircle(color = Color.White.copy(alpha = 0.6f), radius = dotRadius * 0.38f, center = r2)

                                val b1 = getCenter(0, 2)
                                val b2 = getCenter(3, 2)
                                drawCircle(color = Color(0xFF00B4D8), radius = dotRadius, center = b1)
                                drawCircle(color = Color.White.copy(alpha = 0.6f), radius = dotRadius * 0.38f, center = b1)
                                drawCircle(color = Color(0xFF00B4D8), radius = dotRadius, center = b2)
                                drawCircle(color = Color.White.copy(alpha = 0.6f), radius = dotRadius * 0.38f, center = b2)

                                val xCenter = getCenter(1, 2)
                                val xSize = cellW * 0.32f
                                drawLine(
                                    color = Color(0xFFFACC15),
                                    start = Offset(xCenter.x - xSize, xCenter.y - xSize),
                                    end = Offset(xCenter.x + xSize, xCenter.y + xSize),
                                    strokeWidth = 3.5.dp.toPx(),
                                    cap = StrokeCap.Round
                                )
                                drawLine(
                                    color = Color(0xFFFACC15),
                                    start = Offset(xCenter.x + xSize, xCenter.y - xSize),
                                    end = Offset(xCenter.x - xSize, xCenter.y + xSize),
                                    strokeWidth = 3.5.dp.toPx(),
                                    cap = StrokeCap.Round
                                )
                            }
                            3 -> {
                                val colors = listOf(
                                    Color(0xFFFF3366),
                                    Color(0xFF00B4D8),
                                    Color(0xFF10B981),
                                    Color(0xFFFFB703)
                                )
                                for (r in 0..3) {
                                    val start = getCenter(r, 0)
                                    val end = getCenter(r, 3)
                                    val col = colors[r]
                                    drawLine(
                                        color = col,
                                        start = start,
                                        end = end,
                                        strokeWidth = pipeStroke,
                                        cap = StrokeCap.Round
                                    )
                                    drawCircle(color = col, radius = dotRadius, center = start)
                                    drawCircle(color = Color.White.copy(alpha = 0.6f), radius = dotRadius * 0.38f, center = start)
                                    drawCircle(color = col, radius = dotRadius, center = end)
                                    drawCircle(color = Color.White.copy(alpha = 0.6f), radius = dotRadius * 0.38f, center = end)
                                }
                            }
                            else -> {
                                val colors = listOf(
                                    Color(0xFFFF3366),
                                    Color(0xFF00B4D8),
                                    Color(0xFF10B981),
                                    Color(0xFFFFB703)
                                )
                                for (r in 0..3) {
                                    val start = getCenter(r, 0)
                                    val end = getCenter(r, 3)
                                    val col = colors[r]
                                    drawLine(
                                        color = col,
                                        start = start,
                                        end = end,
                                        strokeWidth = pipeStroke,
                                        cap = StrokeCap.Round
                                    )
                                    drawCircle(color = col, radius = dotRadius, center = start)
                                    drawCircle(color = Color.White.copy(alpha = 0.6f), radius = dotRadius * 0.38f, center = start)
                                    drawCircle(color = col, radius = dotRadius, center = end)
                                    drawCircle(color = Color.White.copy(alpha = 0.6f), radius = dotRadius * 0.38f, center = end)
                                }

                                val w = size.width
                                val h = size.height
                                val starPath = Path().apply {
                                    val cx = w * 0.5f
                                    val cy = h * 0.5f
                                    val spikes = 5
                                    val outerRadius = 32.dp.toPx()
                                    val innerRadius = 14.dp.toPx()
                                    var rot = Math.PI / 2 * 3
                                    val stepAngle = Math.PI / spikes
                                    
                                    moveTo(cx, cy - outerRadius)
                                    for (i in 0 until spikes) {
                                        var x = cx + kotlin.math.cos(rot).toFloat() * outerRadius
                                        var y = cy + kotlin.math.sin(rot).toFloat() * outerRadius
                                        lineTo(x, y)
                                        rot += stepAngle
                                        
                                        x = cx + kotlin.math.cos(rot).toFloat() * innerRadius
                                        y = cy + kotlin.math.sin(rot).toFloat() * innerRadius
                                        lineTo(x, y)
                                        rot += stepAngle
                                    }
                                    close()
                                }
                                drawPath(starPath, color = Color(0xFFFACC15))
                                drawPath(starPath, color = Color(0xFF78350F), style = Stroke(width = 2.5.dp.toPx()))
                                
                                val checkPath = Path().apply {
                                    moveTo(w * 0.44f, h * 0.52f)
                                    lineTo(w * 0.48f, h * 0.57f)
                                    lineTo(w * 0.57f, h * 0.42f)
                                }
                                drawPath(checkPath, color = Color(0xFF15803D), style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
                            }
                        }
                    }
                }

                // Step Title & Description Box
                val stepTitle = when (step) {
                    1 -> "CONNECT MATCHING COLORS"
                    2 -> "DO NOT CROSS PIPES"
                    3 -> "COVER THE WHOLE BOARD"
                    else -> "WIN STARS & DIAMONDS"
                }
                val stepDesc = when (step) {
                    1 -> "Drag from one colored dot to another with matching color to create a continuous glowing pipe."
                    2 -> "Pipes cannot cross or overlap each other. If pipes cross, the previous connection will break."
                    3 -> "Connect all color pairs and cover every single cell on the board to solve the puzzle perfectly."
                    else -> "Complete levels to earn 3 Stars, collect diamond coins, and claim cash milestone rewards!"
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = stepTitle,
                        color = Color(0xFF0284C7),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = LuckiestGuyFontFamily,
                        textAlign = TextAlign.Center
                    )
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(2.dp, RoundedCornerShape(16.dp), clip = false)
                            .background(Color.White, RoundedCornerShape(16.dp))
                            .border(1.5.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stepDesc,
                            color = Color(0xFF334155),
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center,
                            lineHeight = 17.sp
                        )
                    }
                }

                // Step Indicator Pills
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(4) { i ->
                        val isActive = i + 1 == step
                        Box(
                            modifier = Modifier
                                .height(8.dp)
                                .width(if (isActive) 24.dp else 8.dp)
                                .background(
                                    if (isActive) Brush.horizontalGradient(listOf(Color(0xFF0284C7), Color(0xFF22C55E)))
                                    else Brush.horizontalGradient(listOf(Color(0xFFCBD5E1), Color(0xFFCBD5E1))),
                                    RoundedCornerShape(4.dp)
                                )
                        )
                    }
                }

                // Navigation Buttons Row (Matching Pause Popup Buttons UI)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (step > 1) {
                        // Previous Button (Orange Gradient matching Pause Popup style)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .zoomClickable {
                                    step--
                                }
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(Color(0xFFFF9F1C), Color(0xFFF15A24))
                                    ),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .border(
                                    width = 2.5.dp,
                                    brush = Brush.horizontalGradient(
                                        listOf(Color(0xFF0284C7), Color(0xFFFACC15), Color(0xFF22C55E))
                                    ),
                                    shape = RoundedCornerShape(16.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Previous",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "PREV",
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = LuckiestGuyFontFamily
                                )
                            }
                        }
                    }

                    // Next / Start Playing Button (Green Gradient matching Pause Popup Resume style)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .zoomClickable {
                                if (step < 4) {
                                    step++
                                } else {
                                    onClose()
                                }
                            }
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(Color(0xFF4ADE80), Color(0xFF16A34A))
                                ),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .border(
                                width = 2.5.dp,
                                brush = Brush.horizontalGradient(
                                    listOf(Color(0xFF0284C7), Color(0xFFFACC15), Color(0xFF22C55E))
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = if (step < 4) Icons.AutoMirrored.Filled.ArrowForward else Icons.Default.PlayArrow,
                                contentDescription = if (step < 4) "Next" else "Start",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = if (step < 4) "NEXT" else "START",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = LuckiestGuyFontFamily
                            )
                        }
                    }
                }
            }
        }
    }
}
@Composable
fun SkinShopPopup(
    coins: Int,
    onCoinsChange: (Int) -> Unit,
    unlockedBottleSkins: Set<String>,
    onUnlockedBottleSkinsChange: (Set<String>) -> Unit,
    selectedBottleSkin: BottleSkin,
    onSelectedBottleSkinChange: (BottleSkin) -> Unit,
    unlockedBackgroundSkins: Set<String>,
    onUnlockedBackgroundSkinsChange: (Set<String>) -> Unit,
    selectedBackgroundSkin: BackgroundSkin,
    onSelectedBackgroundSkinChange: (BackgroundSkin) -> Unit,
    onClose: () -> Unit,
    onShowAd: (String, () -> Unit, () -> Unit) -> Unit
) {
    // Empty stub to completely remove bottle skins shop from compiler
}


@Composable
fun OutlinedThreeDButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    baseColor: Color = SoftPurple
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.92f else 1.0f, label = "outBtnScale")
    
    Box(
        modifier = modifier
            .height(52.dp)
            .scale(scale)
            .border(2.dp, baseColor, RoundedCornerShape(26.dp))
            .clip(RoundedCornerShape(26.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(color = baseColor.copy(alpha = 0.1f)),
                onClick = {
                    onClick()
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text.uppercase(),
            color = baseColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Black,
            fontFamily = LuckiestGuyFontFamily,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
fun SunburstRaysBackground(modifier: Modifier = Modifier) {
    val rayTransition = rememberInfiniteTransition(label = "sunburst")
    val rotationAngle by rayTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sunburstRotation"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = maxOf(size.width, size.height)
        val numRays = 16
        val angleStep = 360f / numRays
        
        rotate(rotationAngle, pivot = center) {
            for (i in 0 until numRays) {
                if (i % 2 == 0) {
                    val startAngle = i * angleStep
                    val sweepAngle = angleStep
                    
                    val a1 = Math.toRadians(startAngle.toDouble())
                    val a2 = Math.toRadians((startAngle + sweepAngle).toDouble())
                    val x1 = (center.x + radius * kotlin.math.cos(a1)).toFloat()
                    val y1 = (center.y + radius * kotlin.math.sin(a1)).toFloat()
                    val x2 = (center.x + radius * kotlin.math.cos(a2)).toFloat()
                    val y2 = (center.y + radius * kotlin.math.sin(a2)).toFloat()

                    val path = Path().apply {
                        moveTo(center.x, center.y)
                        lineTo(x1, y1)
                        lineTo(x2, y2)
                        close()
                    }
                    drawPath(
                        path = path,
                        color = Color(0xFFFFCC00).copy(alpha = 0.08f)
                    )
                }
            }
        }
    }
}

@Composable
fun StarIcon(earned: Boolean, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f
        val rOuter = w / 2f
        val rInner = w / 4.5f
        
        val starPath = Path().apply {
            var angle = -Math.PI / 2
            val dAngle = Math.PI / 5
            for (step in 0 until 10) {
                val r = if (step % 2 == 0) rOuter else rInner
                val x = (cx + r * kotlin.math.cos(angle)).toFloat()
                val y = (cy + r * kotlin.math.sin(angle)).toFloat()
                if (step == 0) moveTo(x, y) else lineTo(x, y)
                angle += dAngle
            }
            close()
        }
        
        if (earned) {
            drawPath(
                path = starPath,
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFFFE082), Color(0xFFFFB300))
                )
            )
            drawPath(
                path = starPath,
                color = Color(0xFFE65100),
                style = Stroke(width = 2.5.dp.toPx(), join = StrokeJoin.Round)
            )
            val highlightPath = Path().apply {
                var angle = -Math.PI / 2
                val dAngle = Math.PI / 5
                val r = rOuter * 0.85f
                val rIn = rInner * 0.85f
                for (step in 0 until 6) {
                    val currentR = if (step % 2 == 0) r else rIn
                    val x = (cx + currentR * kotlin.math.cos(angle)).toFloat()
                    val y = (cy + currentR * kotlin.math.sin(angle)).toFloat()
                    if (step == 0) moveTo(x, y) else lineTo(x, y)
                    angle += dAngle
                }
            }
            drawPath(
                path = highlightPath,
                color = Color.White.copy(alpha = 0.4f),
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
            )
        } else {
            drawPath(
                path = starPath,
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFE2E8F0), Color(0xFF94A3B8))
                )
            )
            drawPath(
                path = starPath,
                color = Color(0xFF64748B),
                style = Stroke(width = 2.5.dp.toPx(), join = StrokeJoin.Round)
            )
        }
    }
}

@Composable
fun LevelCompletePopup(
    level: Int,
    starsEarned: Int,
    moves: Int,
    timeInSeconds: Int,
    coinsEarned: Int,
    activeMilestoneReward: RewardSetting? = null,
    showAdBadge: Boolean = false,
    onClaimReward: () -> Unit = {},
    onHome: () -> Unit,
    onNext: () -> Unit,
    onRestart: () -> Unit
) {
    val context = LocalContext.current
    var isClicked by remember { mutableStateOf(false) }

    var activeConfetti by remember { mutableStateOf<List<ConfettiParticle>>(emptyList()) }
    LaunchedEffect(Unit) {
        val random = java.util.Random()
        var lastTime = 0L
        while (true) {
            withFrameMillis { time ->
                val dt = if (lastTime == 0L) 0.016f else (time - lastTime) / 1000f
                lastTime = time
                val timeScale = (dt / 0.016f).coerceIn(0.2f, 3.0f)
                
                if (activeConfetti.size < 60) {
                    val colors = listOf(
                        Color(0xFFFF4500), Color(0xFFFFD700), Color(0xFFADFF2F),
                        Color(0xFF00FFFF), Color(0xFFFF00FF), Color(0xFF1E90FF)
                    )
                    val newParticles = (0 until 10).map {
                        ConfettiParticle(
                            x = random.nextFloat() * 1080f,
                            y = -50f,
                            vx = (random.nextFloat() - 0.5f) * 6f,
                            vy = random.nextFloat() * 6f + 5f,
                            rotation = random.nextFloat() * 360f,
                            rotationSpeed = (random.nextFloat() - 0.5f) * 10f,
                            color = colors[random.nextInt(colors.size)],
                            size = random.nextFloat() * 20f + 15f,
                            isCircle = random.nextBoolean()
                        )
                    }
                    activeConfetti = activeConfetti + newParticles
                }
                activeConfetti = activeConfetti.map { c ->
                    c.copy(
                        x = c.x + c.vx * timeScale,
                        y = c.y + c.vy * timeScale,
                        rotation = c.rotation + c.rotationSpeed * timeScale
                    )
                }.filter { it.y < 2200f }
            }
        }
    }

    val flowLevel = remember(level) { FlowPuzzleLevels.getLevel(level) }
    val gridSize = flowLevel.gridSize
    val pairs = flowLevel.pairs

    val difficulty = when {
        level <= 5 -> "Easy"
        level <= 15 -> "Medium"
        else -> "Hard"
    }

    val minutes = timeInSeconds / 60
    val seconds = timeInSeconds % 60
    val timeFormatted = String.format("%02d:%02d", minutes, seconds)

    val infiniteTransition = rememberInfiniteTransition(label = "completeAnim")
    val buttonScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "btnPulse"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.65f))
            .clickable(enabled = false) {}
            .padding(horizontal = 14.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        SunburstRaysBackground()

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 38.dp)
                    .shadow(8.dp, RoundedCornerShape(24.dp), clip = false)
                    .background(Color(0xFFFFF9E6), RoundedCornerShape(24.dp))
                    .border(
                        width = 3.5.dp,
                        brush = Brush.horizontalGradient(listOf(Color(0xFF0284C7), Color(0xFFFACC15), Color(0xFF22C55E))),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .padding(start = 16.dp, end = 16.dp, top = 46.dp, bottom = 40.dp)
            ) {
                // Header Row: LEVEL COMPLETE (Left) and Close/Home Button (Right)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "LEVEL COMPLETE",
                        style = TextStyle(
                            color = Color(0xFF2563EB), // Blue text
                            fontSize = 23.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = LuckiestGuyFontFamily
                        ),
                        maxLines = 1,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    Spacer(Modifier.width(6.dp))

                    // Cut/Close button matching gameplay screen back/cut button
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .bouncyClickable {
                                onHome()
                            }
                            .background(Color(0xFFFFF9C4), RoundedCornerShape(14.dp))
                            .border(
                                2.5.dp,
                                Brush.horizontalGradient(listOf(Color(0xFF0284C7), Color(0xFF22C55E))),
                                RoundedCornerShape(14.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.Black,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                // Solved Flow Pipe Preview Card
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .background(Color(0xFF0F172A), RoundedCornerShape(16.dp))
                        .border(
                            width = 2.5.dp,
                            brush = Brush.horizontalGradient(listOf(Color(0xFF0284C7), Color(0xFFFACC15), Color(0xFF22C55E))),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val totalW = size.width
                        val totalH = size.height
                        val spacing = 3.dp.toPx()
                        val cellW = (totalW - (gridSize - 1) * spacing) / gridSize
                        val cellH = (totalH - (gridSize - 1) * spacing) / gridSize
                        val stepX = cellW + spacing
                        val stepY = cellH + spacing

                        fun getCenter(r: Int, c: Int): Offset {
                            return Offset(c * stepX + cellW / 2f, r * stepY + cellH / 2f)
                        }

                        // 1. Draw grid cell backgrounds
                        for (r in 0 until gridSize) {
                            for (c in 0 until gridSize) {
                                val topLeft = Offset(c * stepX, r * stepY)
                                drawRoundRect(
                                    color = Color(0xFF1E293B),
                                    topLeft = topLeft,
                                    size = Size(cellW, cellH),
                                    cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
                                )
                            }
                        }

                        // 2. Draw solved connecting pipes
                        val pipeStroke = cellW * 0.44f
                        for (pair in pairs) {
                            val sol = pair.solution
                            if (sol.size > 1) {
                                val path = Path()
                                val first = getCenter(sol[0].row, sol[0].col)
                                path.moveTo(first.x, first.y)
                                for (idx in 1 until sol.size) {
                                    val next = getCenter(sol[idx].row, sol[idx].col)
                                    path.lineTo(next.x, next.y)
                                }
                                drawPath(
                                    path = path,
                                    color = pair.color,
                                    style = Stroke(
                                        width = pipeStroke,
                                        cap = StrokeCap.Round,
                                        join = StrokeJoin.Round
                                    )
                                )
                            }
                        }

                        // 3. Draw endpoint dots with core highlights
                        val dotRadius = cellW * 0.36f
                        for (pair in pairs) {
                            val p1Center = getCenter(pair.p1.row, pair.p1.col)
                            val p2Center = getCenter(pair.p2.row, pair.p2.col)

                            drawCircle(color = pair.color, radius = dotRadius, center = p1Center)
                            drawCircle(color = Color.White.copy(alpha = 0.6f), radius = dotRadius * 0.38f, center = p1Center)

                            drawCircle(color = pair.color, radius = dotRadius, center = p2Center)
                            drawCircle(color = Color.White.copy(alpha = 0.6f), radius = dotRadius * 0.38f, center = p2Center)
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Stats Box
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF0F9FF), RoundedCornerShape(16.dp))
                        .border(
                            2.dp,
                            Brush.horizontalGradient(listOf(Color(0xFF0284C7).copy(alpha = 0.5f), Color(0xFF22C55E).copy(alpha = 0.5f))),
                            RoundedCornerShape(16.dp)
                        )
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Level", color = Color(0xFF0369A1), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("Level $level ($difficulty)", color = Color(0xFF0C4A6E), fontWeight = FontWeight.Black, fontSize = 14.sp, fontFamily = LuckiestGuyFontFamily)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Time Taken", color = Color(0xFF0369A1), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(timeFormatted, color = Color(0xFF0C4A6E), fontWeight = FontWeight.Black, fontSize = 14.sp, fontFamily = LuckiestGuyFontFamily)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Diamond Coins", color = Color(0xFF0369A1), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("+ $coinsEarned 💎", color = Color(0xFFD97706), fontWeight = FontWeight.Black, fontSize = 14.sp, fontFamily = LuckiestGuyFontFamily)
                    }
                }

                Spacer(Modifier.height(14.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (activeMilestoneReward != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .zoomClickable {
                                    onClaimReward()
                                }
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(Color(0xFFFDE047), Color(0xFFF59E0B), Color(0xFFD97706))
                                    ),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .border(
                                    width = 2.5.dp,
                                    brush = Brush.horizontalGradient(
                                        listOf(Color(0xFF0284C7), Color(0xFFFACC15), Color(0xFF22C55E))
                                    ),
                                    shape = RoundedCornerShape(16.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "🎁 CLAIM UPTO ₹ REWARD!",
                                style = TextStyle(
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = LuckiestGuyFontFamily,
                                    letterSpacing = 0.5.sp,
                                    shadow = Shadow(
                                        color = Color(0xFF78350F),
                                        offset = Offset(2f, 3f),
                                        blurRadius = 2f
                                    )
                                )
                            )
                        }
                    }

                    // 1. NEXT LEVEL (Green Button with full zoom animation on click)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .zoomClickable {
                                if (!isClicked) {
                                    isClicked = true
                                    onNext()
                                }
                            }
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(Color(0xFF4ADE80), Color(0xFF16A34A))
                                ),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .border(
                                width = 2.5.dp,
                                brush = Brush.horizontalGradient(
                                    listOf(Color(0xFF0284C7), Color(0xFFFACC15), Color(0xFF22C55E))
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Next Level",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "NEXT LEVEL",
                                color = Color.White,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = LuckiestGuyFontFamily
                            )
                        }
                        // Chota Gol Card with Baingani (Purple) "Ad" text
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = (-8).dp, y = 8.dp)
                                .size(24.dp)
                                .shadow(2.dp, CircleShape, clip = false)
                                .background(Color.White, CircleShape)
                                .border(1.5.dp, Color(0xFF8B5CF6), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Ad",
                                color = Color(0xFF8B5CF6),
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = LuckiestGuyFontFamily
                            )
                        }
                    }

                    // 2. Action Buttons Row: RESTART and QUIT TO MAP
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // RESTART Button (Orange)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                            .zoomClickable {
                                onRestart()
                            }
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(Color(0xFFFF9F1C), Color(0xFFF15A24))
                                ),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .border(
                                width = 2.5.dp,
                                brush = Brush.horizontalGradient(
                                    listOf(Color(0xFF0284C7), Color(0xFFFACC15), Color(0xFF22C55E))
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Restart",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "RESTART",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = LuckiestGuyFontFamily
                                )
                            }
                        }

                        // QUIT TO MAP Button (Red)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                            .zoomClickable {
                                onHome()
                            }
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(Color(0xFFEF4444), Color(0xFFDC2626))
                                ),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .border(
                                width = 2.5.dp,
                                brush = Brush.horizontalGradient(
                                    listOf(Color(0xFF0284C7), Color(0xFFFACC15), Color(0xFF22C55E))
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Home,
                                    contentDescription = "Quit To Map",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "QUIT TO MAP",
                                    color = Color.White,
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = LuckiestGuyFontFamily
                                )
                            }
                        }
                    }
                }
            }

            // 3 Stars Row centered on the top border
            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .zIndex(2f)
            ) {
                StarIcon(
                    earned = starsEarned >= 1,
                    modifier = Modifier
                        .size(60.dp)
                        .graphicsLayer { rotationZ = -14f }
                )
                StarIcon(
                    earned = starsEarned >= 2,
                    modifier = Modifier
                        .size(76.dp)
                )
                StarIcon(
                    earned = starsEarned >= 3,
                    modifier = Modifier
                        .size(60.dp)
                        .graphicsLayer { rotationZ = 14f }
                )
            }
        }
        ConfettiCanvas(particlesProvider = { activeConfetti })
    }
}
@Composable
fun RewardClaimPopup(amount: Int, onClaim: () -> Unit) {
    FullScreenPopupContainer(
        gradientColors = emptyList()
    ) {
        Text(
            text = "🎁 MILESTONE REWARD!",
            style = TextStyle(
                color = Color(0xFFFFD166),
                fontSize = 26.sp,
                fontWeight = FontWeight.Black,
                fontFamily = LuckiestGuyFontFamily,
                shadow = Shadow(
                    color = Color(0xFF2C1B47),
                    offset = Offset(2f, 4f),
                    blurRadius = 2f
                )
            )
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Great job! You reached a reward milestone.",
            color = Color(0xFF2C1B47),
            textAlign = TextAlign.Center,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(20.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFFEF3C7), RoundedCornerShape(24.dp))
                .border(3.dp, Color(0xFFF59E0B), RoundedCornerShape(24.dp))
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "+$amount ",
                    color = Color(0xFFD97706),
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = LuckiestGuyFontFamily
                )
                Image(
                    painter = painterResource(id = R.drawable.diamond),
                    contentDescription = "Diamond",
                    modifier = Modifier.size(32.dp)
                )
                Text(
                    text = " REWARD",
                    color = Color(0xFFD97706),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = LuckiestGuyFontFamily
                )
            }
        }
        Spacer(Modifier.height(28.dp))
        PopupThreeDButton(
            text = "CLAIM NOW",
            onClick = onClaim,
            isLarge = true,
            baseColor = Color(0xFF10B981),
            textColor = Color.White,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun PaymentSuccessPopup(onDone: () -> Unit) {
    FullScreenPopupContainer(
        gradientColors = emptyList(),
        cardColor = Color(0xFFF0FDF4),
        borderColor = Color(0xFF166534)
    ) {
        Box(
            modifier = Modifier
                .size(90.dp)
                .background(Color(0xFFDCFCE7), CircleShape)
                .border(3.dp, Color(0xFF86EFAC), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = Color(0xFF16A34A),
                modifier = Modifier.size(56.dp)
            )
        }
        Spacer(Modifier.height(24.dp))
        Text(
            text = "REWARD SUBMITTED!",
            style = TextStyle(
                color = Color(0xFF16A34A),
                fontSize = 26.sp,
                fontWeight = FontWeight.Black,
                fontFamily = LuckiestGuyFontFamily,
                textAlign = TextAlign.Center,
                shadow = Shadow(
                    color = Color(0xFF14532D),
                    offset = Offset(2f, 3f),
                    blurRadius = 2f
                )
            )
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = "Your reward request has been initiated.",
            color = Color(0xFF1E293B),
            textAlign = TextAlign.Center,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Verification and payout are processed automatically. Keep playing to earn more!",
            color = Color(0xFF64748B),
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            fontSize = 13.sp
        )
        Spacer(Modifier.height(32.dp))
        PopupThreeDButton(
            text = "AWESOME!",
            onClick = onDone,
            isLarge = true,
            baseColor = Color(0xFF0284C7),
            textColor = Color.White,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun PaymentMethodSelectPopup(onMethodSelected: (String) -> Unit, onClose: () -> Unit) {
    FullScreenPopupContainer(
        gradientColors = emptyList()
    ) {
        Text(
            text = "CHOOSE REWARD",
            style = TextStyle(
                color = Color(0xFF2C1B47),
                fontSize = 26.sp,
                fontWeight = FontWeight.Black,
                fontFamily = LuckiestGuyFontFamily
            )
        )
        Spacer(Modifier.height(24.dp))
        Column(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            PopupThreeDButton("Google Play Code", { onMethodSelected("Google Play") }, baseColor = Color(0xFF2563EB), textColor = Color.White, modifier = Modifier.fillMaxWidth())
            PopupThreeDButton("Amazon Voucher", { onMethodSelected("Amazon Voucher") }, baseColor = Color(0xFFF59E0B), textColor = Color.White, modifier = Modifier.fillMaxWidth())
            PopupThreeDButton("UPI / Paytm Transfer", { onMethodSelected("UPI") }, baseColor = Color(0xFF10B981), textColor = Color.White, modifier = Modifier.fillMaxWidth())
            PopupThreeDButton("Free Fire Diamond", { onMethodSelected("FF Diamond") }, baseColor = Color(0xFFE11D48), textColor = Color.White, modifier = Modifier.fillMaxWidth())
        }
        Spacer(Modifier.height(24.dp))
        PopupThreeDButton(
            text = "CANCEL",
            onClick = onClose,
            baseColor = Color(0xFF64748B),
            textColor = Color.White,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun EmailSubmitPopup(
    method: String,
    rewardAmount: Int = 50,
    onSubmit: (String) -> Unit,
    onBack: () -> Unit
) {
    var detail by remember { mutableStateOf("") }
    val isUpi = method.lowercase().contains("upi")
    val isFf = method.lowercase().contains("ff") || method.lowercase().contains("diamond")
    val isAmazon = method.lowercase().contains("amazon")
    val isGooglePlay = method.lowercase().contains("google") || method.lowercase().contains("play")
    
    val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
    val upiRegex = "^[a-zA-Z0-9.\\-_]{2,256}@[a-zA-Z]{2,64}$".toRegex()
    val ffRegex = "^[0-9]{6,14}$".toRegex()

    val isValid = remember(detail, method) {
        if (isUpi) upiRegex.matches(detail.trim())
        else if (isFf) ffRegex.matches(detail.trim())
        else emailRegex.matches(detail.trim())
    }

    val methodTitle = when {
        isUpi -> "UPI / PAYTM"
        isGooglePlay -> "GOOGLE PLAY CODE"
        isAmazon -> "AMAZON VOUCHER"
        isFf -> "FREE FIRE DIAMONDS"
        else -> method.uppercase()
    }
    val methodColor = when {
        isUpi -> Color(0xFF10B981)
        isGooglePlay -> Color(0xFF3B82F6)
        isAmazon -> Color(0xFFF59E0B)
        isFf -> Color(0xFFEF4444)
        else -> Color(0xFF10B981)
    }
    val methodIcon = when {
        isUpi -> "₹"
        isGooglePlay -> "🎁"
        isAmazon -> "🛍️"
        isFf -> "💎"
        else -> "₹"
    }
    val placeholderText = when {
        isUpi -> "e.g. yourname@okhdfcbank"
        isGooglePlay -> "e.g. yourname@gmail.com"
        isAmazon -> "e.g. yourname@gmail.com"
        isFf -> "e.g. 123456789"
        else -> "Enter account details"
    }
    val hintLabel = when {
        isUpi -> "Enter your UPI ID / VPA:"
        isGooglePlay -> "Enter email to receive Google Play Code:"
        isAmazon -> "Enter email to receive Amazon Voucher:"
        isFf -> "Enter your Free Fire Player UID:"
        else -> "Enter payment details:"
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.72f))
            .clickable(enabled = false) {}
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        // 3D Card Container
        Box(
            modifier = Modifier.fillMaxWidth(0.92f)
        ) {
            // 3D Bottom Depth Layer
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .offset(y = 8.dp)
                    .background(Color(0xFF1E1035), RoundedCornerShape(32.dp))
            )

            // Main Card Surface
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFFFFFDF5), Color(0xFFFEF9EE))
                        ),
                        shape = RoundedCornerShape(32.dp)
                    )
                    .border(4.dp, Color(0xFF2C1B47), RoundedCornerShape(32.dp))
                    .padding(22.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Top Bar: 3D Back Button + Title
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // 3D Circular Back Button
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .bouncyClickable {
                                    onBack()
                                }
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .offset(y = 3.dp)
                                    .background(Color(0xFFB58A0D), CircleShape)
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        brush = Brush.verticalGradient(
                                            colors = listOf(Color(0xFFFFD166), Color(0xFFF7B538))
                                        ),
                                        shape = CircleShape
                                    )
                                    .border(2.dp, Color(0xFFFFF9E6), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = Color(0xFF2C1B47),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        // Title
                        Text(
                            text = "ENTER DETAILS",
                            style = TextStyle(
                                color = Color(0xFF2C1B47),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = LuckiestGuyFontFamily,
                                letterSpacing = 1.sp,
                                shadow = Shadow(
                                    color = Color(0x30000000),
                                    offset = Offset(0f, 2f),
                                    blurRadius = 2f
                                )
                            )
                        )

                        Spacer(Modifier.size(40.dp))
                    }

                    // Selected Method Highlight Card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(methodColor.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
                            .border(2.dp, methodColor.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .background(methodColor, CircleShape)
                                        .border(2.dp, Color.White, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = methodIcon,
                                        fontSize = 18.sp,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Column {
                                    Text(
                                        text = methodTitle,
                                        color = Color(0xFF1E293B),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Black,
                                        fontFamily = LuckiestGuyFontFamily
                                    )
                                    Text(
                                        text = "Selected Method",
                                        color = Color(0xFF64748B),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            // Amount Badge
                            Box(
                                modifier = Modifier
                                    .background(
                                        brush = Brush.horizontalGradient(
                                            listOf(Color(0xFF10B981), Color(0xFF059669))
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .border(1.5.dp, Color(0xFFD1FAE5), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "UPTO ₹$rewardAmount CASH",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = LuckiestGuyFontFamily
                                )
                            }
                        }
                    }

                    // Input Field Section
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = hintLabel,
                            color = Color(0xFF475569),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 4.dp)
                        )

                        OutlinedTextField(
                            value = detail,
                            onValueChange = { detail = it },
                            placeholder = {
                                Text(
                                    text = placeholderText,
                                    color = Color(0xFF94A3B8),
                                    fontSize = 13.sp
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White, RoundedCornerShape(18.dp)),
                            shape = RoundedCornerShape(18.dp),
                            singleLine = true,
                            isError = detail.isNotEmpty() && !isValid,
                            textStyle = TextStyle(
                                color = Color(0xFF1E293B),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = Color.White,
                                focusedContainerColor = Color.White,
                                unfocusedBorderColor = Color(0xFFCBD5E1),
                                focusedBorderColor = methodColor,
                                cursorColor = methodColor,
                                errorBorderColor = Color(0xFFEF4444)
                            )
                        )

                        if (detail.isNotEmpty()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.padding(start = 6.dp, top = 2.dp)
                            ) {
                                Text(
                                    text = if (isValid) "✓ Valid details entered" else "⚠ Please check the entered format",
                                    color = if (isValid) Color(0xFF10B981) else Color(0xFFEF4444),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(4.dp))

                    // 3D Bouncy Submit Button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .bouncyClickable(enabled = isValid) {
                                if (isValid) {
                                    onSubmit(detail.trim())
                                }
                            }
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .offset(y = 5.dp)
                                .background(
                                    if (isValid) Color(0xFF065F46) else Color(0xFF64748B),
                                    RoundedCornerShape(20.dp)
                                )
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    brush = if (isValid) {
                                        Brush.verticalGradient(
                                            listOf(Color(0xFF34D399), Color(0xFF059669))
                                        )
                                    } else {
                                        Brush.verticalGradient(
                                            listOf(Color(0xFF94A3B8), Color(0xFF64748B))
                                        )
                                    },
                                    shape = RoundedCornerShape(20.dp)
                                )
                                .border(2.dp, if (isValid) Color(0xFFA7F3D0) else Color(0xFFCBD5E1), RoundedCornerShape(20.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "CLAIM UPTO ₹$rewardAmount CASH",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = LuckiestGuyFontFamily,
                                letterSpacing = 1.sp,
                                style = TextStyle(
                                    shadow = Shadow(
                                        color = Color(0x60000000),
                                        offset = Offset(0f, 2f),
                                        blurRadius = 2f
                                    )
                                )
                            )
                        }
                    }

                    // Security Tagline
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(text = "🔒", fontSize = 12.sp)
                        Text(
                            text = "Verified payout • Direct to your account",
                            color = Color(0xFF64748B),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun WoodenCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Box(
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(y = 10.dp)
                .background(Color(0x7F180B04), RoundedCornerShape(28.dp))
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF8B5A2B)),
            border = BorderStroke(4.dp, Color(0xFF5C3A21)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFFA0522D), Color(0xFF8B4513))
                        )
                    )
                    .border(2.dp, Color(0xFFFFE4C4).copy(alpha = 0.2f), RoundedCornerShape(24.dp))
                    .padding(24.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    content()
                }
            }
        }
    }
}

@Composable
fun ColorPipesPopupSwitch(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    val transition = updateTransition(targetState = checked, label = "switchState")
    val thumbOffset by transition.animateDp(
        transitionSpec = { tween(180) },
        label = "thumbOffset"
    ) { isChecked ->
        if (isChecked) 24.dp else 0.dp
    }
    
    val trackBrush = if (checked) {
        Brush.horizontalGradient(listOf(Color(0xFF4ADE80), Color(0xFF16A34A)))
    } else {
        Brush.horizontalGradient(listOf(Color(0xFFE2E8F0), Color(0xFFCBD5E1)))
    }

    Box(
        modifier = Modifier
            .width(54.dp)
            .height(30.dp)
            .clip(CircleShape)
            .background(trackBrush)
            .border(
                1.5.dp,
                if (checked) Color(0xFF15803D) else Color(0xFF94A3B8),
                CircleShape
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                SoundManager.playSwitchSound()
                onCheckedChange(!checked)
            }
            .padding(3.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .offset(x = thumbOffset)
                .shadow(2.dp, CircleShape)
                .background(Color.White, CircleShape)
                .border(1.dp, Color(0xFFE2E8F0), CircleShape)
        )
    }
}

@Composable
fun SoundSettingsPopup(
    showResetButton: Boolean,
    onResetLevels: () -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    var tempSfxEnabled by remember { mutableStateOf(SoundManager.isSfxEnabled()) }
    var tempVolume by remember { mutableFloatStateOf(SoundManager.getVolume()) }
    var tempVibrationEnabled by remember { mutableStateOf(SoundManager.isVibrationEnabled()) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.65f))
            .clickable(enabled = false) {}
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .shadow(8.dp, RoundedCornerShape(24.dp), clip = false)
                .background(Color(0xFFFFF9E6), RoundedCornerShape(24.dp))
                .border(
                    width = 3.5.dp,
                    brush = Brush.horizontalGradient(
                        listOf(Color(0xFF0284C7), Color(0xFFFACC15), Color(0xFF22C55E))
                    ),
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(start = 18.dp, end = 18.dp, top = 20.dp, bottom = 26.dp)
        ) {
            // Header Row: SETTINGS (Left) and Cut/Close Button (Right)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SETTINGS",
                    style = TextStyle(
                        color = Color(0xFF2563EB), // Sky/Blue text matching PausedPopup
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = LuckiestGuyFontFamily
                    )
                )

                // Cut/Close button matching all popups
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .bouncyClickable {
                            onClose()
                        }
                        .background(Color(0xFFFFF9C4), RoundedCornerShape(14.dp))
                        .border(
                            2.5.dp,
                            Brush.horizontalGradient(listOf(Color(0xFF0284C7), Color(0xFF22C55E))),
                            RoundedCornerShape(14.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.Black,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(13.dp)
            ) {
                // 1. Sound FX Item
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White, RoundedCornerShape(16.dp))
                        .border(
                            2.dp,
                            Brush.horizontalGradient(
                                listOf(Color(0xFF0284C7).copy(alpha = 0.5f), Color(0xFF22C55E).copy(alpha = 0.5f))
                            ),
                            RoundedCornerShape(16.dp)
                        )
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = "Sound FX",
                        tint = Color(0xFF0284C7),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = "Sound FX",
                        color = Color(0xFF1E293B),
                        fontFamily = LuckiestGuyFontFamily,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.weight(1f)
                    )
                    ColorPipesPopupSwitch(
                        checked = tempSfxEnabled,
                        onCheckedChange = {
                            tempSfxEnabled = it
                            SoundManager.setSfxEnabled(it)
                        }
                    )
                }

                // 2. Vibration Item
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White, RoundedCornerShape(16.dp))
                        .border(
                            2.dp,
                            Brush.horizontalGradient(
                                listOf(Color(0xFF0284C7).copy(alpha = 0.5f), Color(0xFF22C55E).copy(alpha = 0.5f))
                            ),
                            RoundedCornerShape(16.dp)
                        )
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Vibration",
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = "Vibration",
                        color = Color(0xFF1E293B),
                        fontFamily = LuckiestGuyFontFamily,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.weight(1f)
                    )
                    ColorPipesPopupSwitch(
                        checked = tempVibrationEnabled,
                        onCheckedChange = {
                            tempVibrationEnabled = it
                            SoundManager.setVibrationEnabled(it)
                            if (it) {
                                triggerVibration(context, 45, android.view.HapticFeedbackConstants.VIRTUAL_KEY)
                            }
                        }
                    )
                }

                // 3. Volume Slider Item
                Column(
                    horizontalAlignment = Alignment.Start,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White, RoundedCornerShape(16.dp))
                        .border(
                            2.dp,
                            Brush.horizontalGradient(
                                listOf(Color(0xFF0284C7).copy(alpha = 0.5f), Color(0xFF22C55E).copy(alpha = 0.5f))
                            ),
                            RoundedCornerShape(16.dp)
                        )
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Volume",
                            color = Color(0xFF1E293B),
                            fontFamily = LuckiestGuyFontFamily,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "${(tempVolume * 100).toInt()}%",
                            color = Color(0xFF0284C7),
                            fontFamily = LuckiestGuyFontFamily,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Slider(
                        value = tempVolume,
                        onValueChange = {
                            tempVolume = it
                            SoundManager.setVolume(it)
                        },
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFF0284C7),
                            activeTrackColor = Color(0xFF22C55E),
                            inactiveTrackColor = Color(0xFFCBD5E1)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (showResetButton) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .zoomClickable {
                                onResetLevels()
                            }
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(Color(0xFFF87171), Color(0xFFDC2626))
                                ),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .border(
                                width = 2.5.dp,
                                brush = Brush.horizontalGradient(
                                    listOf(Color(0xFF0284C7), Color(0xFFFACC15), Color(0xFF22C55E))
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "RESET PROGRESS",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = LuckiestGuyFontFamily
                        )
                    }
                }

                // 4. SAVE & CLOSE Button (Green gradient zoomClickable button)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .zoomClickable {
                            SoundManager.setSfxEnabled(tempSfxEnabled)
                            SoundManager.setVolume(tempVolume)
                            SoundManager.setVibrationEnabled(tempVibrationEnabled)
                            onClose()
                        }
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color(0xFF4ADE80), Color(0xFF16A34A))
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .border(
                            width = 2.5.dp,
                            brush = Brush.horizontalGradient(
                                listOf(Color(0xFF0284C7), Color(0xFFFACC15), Color(0xFF22C55E))
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "SAVE & CLOSE",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = LuckiestGuyFontFamily
                    )
                }
            }
        }
    }
}

@Composable
fun ExitPopup(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.65f))
            .clickable(enabled = false) {}
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .shadow(8.dp, RoundedCornerShape(24.dp), clip = false)
                .background(Color(0xFFFFF9E6), RoundedCornerShape(24.dp))
                .border(
                    width = 3.5.dp,
                    brush = Brush.horizontalGradient(
                        listOf(Color(0xFF0284C7), Color(0xFFFACC15), Color(0xFF22C55E))
                    ),
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(start = 18.dp, end = 18.dp, top = 20.dp, bottom = 24.dp)
        ) {
            // Header Row: Title on Left and Cut/Close Button on Right
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "LEAVING SO SOON?",
                    style = TextStyle(
                        color = Color(0xFF2563EB),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = LuckiestGuyFontFamily,
                        letterSpacing = 0.2.sp,
                        shadow = Shadow(
                            color = Color(0x33000000),
                            offset = Offset(0f, 2f),
                            blurRadius = 2f
                        )
                    )
                )

                // Cut/Close button matching all popups
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .bouncyClickable {
                            onDismiss()
                        }
                        .background(Color(0xFFFFF9C4), RoundedCornerShape(14.dp))
                        .border(
                            2.5.dp,
                            Brush.horizontalGradient(listOf(Color(0xFF0284C7), Color(0xFF22C55E))),
                            RoundedCornerShape(14.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.Black,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            // Logo Image Container
            Box(
                modifier = Modifier
                    .size(105.dp)
                    .background(Color.White, RoundedCornerShape(22.dp))
                    .border(
                        2.dp,
                        Brush.horizontalGradient(
                            listOf(Color(0xFF0284C7).copy(alpha = 0.5f), Color(0xFF22C55E).copy(alpha = 0.5f))
                        ),
                        RoundedCornerShape(22.dp)
                    )
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo_image),
                    contentDescription = "Logo",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = "Are you sure you want to exit?",
                color = Color(0xFF1E293B),
                fontWeight = FontWeight.Black,
                fontSize = 18.sp,
                fontFamily = LuckiestGuyFontFamily,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = "Your level & stars are safely saved!",
                color = Color(0xFF64748B),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(22.dp))

            // Action Buttons: STAY vs EXIT
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // STAY Button (Green 3D Gradient)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .zoomClickable {
                            onDismiss()
                        }
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color(0xFF4ADE80), Color(0xFF16A34A))
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .border(
                            width = 2.dp,
                            brush = Brush.horizontalGradient(
                                listOf(Color(0xFF0284C7), Color(0xFFFACC15), Color(0xFF22C55E))
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "STAY",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = LuckiestGuyFontFamily
                    )
                }

                // EXIT Button (Red 3D Gradient)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .zoomClickable {
                            onConfirm()
                        }
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color(0xFFF87171), Color(0xFFDC2626))
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .border(
                            width = 2.dp,
                            brush = Brush.horizontalGradient(
                                listOf(Color(0xFF0284C7), Color(0xFFFACC15), Color(0xFF22C55E))
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "EXIT",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = LuckiestGuyFontFamily
                    )
                }
            }
        }
    }
}

fun getFreeInternalStorageMB(): Long {
    return try {
        val path = Environment.getDataDirectory()
        val stat = StatFs(path.path)
        val blockSize = stat.blockSizeLong
        val availableBlocks = stat.availableBlocksLong
        (availableBlocks * blockSize) / (1024L * 1024L)
    } catch (e: Exception) { 
        0L 
    }
}

@Composable
fun TamperedProgressScreen() {
    val infiniteTransition = rememberInfiniteTransition(label = "tamperPulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE2E8F0)) // Solid opaque dark-white
            .clickable(enabled = false) {}
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .shadow(8.dp, RoundedCornerShape(24.dp), clip = false)
                .background(Color(0xFFFFF9E6), RoundedCornerShape(24.dp))
                .border(
                    width = 3.5.dp,
                    brush = Brush.horizontalGradient(
                        listOf(Color(0xFF0284C7), Color(0xFFFACC15), Color(0xFF22C55E))
                    ),
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(start = 20.dp, end = 20.dp, top = 22.dp, bottom = 24.dp)
        ) {
            Text(
                text = "TAMPER DETECTED!",
                style = TextStyle(
                    color = Color(0xFFDC2626),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = LuckiestGuyFontFamily,
                    letterSpacing = 0.5.sp,
                    shadow = Shadow(
                        color = Color(0x33000000),
                        offset = Offset(0f, 2f),
                        blurRadius = 2f
                    )
                ),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(18.dp))

            Box(
                modifier = Modifier
                    .size(92.dp)
                    .background(Color.White, RoundedCornerShape(22.dp))
                    .border(
                        2.dp,
                        Brush.horizontalGradient(
                            listOf(Color(0xFF0284C7).copy(alpha = 0.5f), Color(0xFF22C55E).copy(alpha = 0.5f))
                        ),
                        RoundedCornerShape(22.dp)
                    )
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Block,
                    contentDescription = "Tampering Blocked",
                    tint = Color(0xFFDC2626),
                    modifier = Modifier
                        .size(54.dp)
                        .scale(scale)
                )
            }

            Spacer(Modifier.height(18.dp))

            Text(
                text = "Security Violation",
                color = Color(0xFF1E293B),
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                fontFamily = LuckiestGuyFontFamily,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = "Suspicious modification of game data has been detected. For security reasons, gameplay access is restricted. Please reinstall the application to continue.",
                color = Color(0xFF64748B),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
fun GenericVpnBlockedScreen(onRetry: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "vpnPulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE2E8F0)) // Solid opaque dark-white covering the full screen
            .clickable(enabled = false) {}
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .shadow(8.dp, RoundedCornerShape(24.dp), clip = false)
                .background(Color(0xFFFFF9E6), RoundedCornerShape(24.dp))
                .border(
                    width = 3.5.dp,
                    brush = Brush.horizontalGradient(
                        listOf(Color(0xFF0284C7), Color(0xFFFACC15), Color(0xFF22C55E))
                    ),
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(start = 20.dp, end = 20.dp, top = 22.dp, bottom = 24.dp)
        ) {
            // Header Title
            Text(
                text = "ACCESS BLOCKED!",
                style = TextStyle(
                    color = Color(0xFF2563EB),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = LuckiestGuyFontFamily,
                    letterSpacing = 0.5.sp,
                    shadow = Shadow(
                        color = Color(0x33000000),
                        offset = Offset(0f, 2f),
                        blurRadius = 2f
                    )
                ),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(18.dp))

            // Warning Visual Container
            Box(
                modifier = Modifier
                    .size(92.dp)
                    .background(Color.White, RoundedCornerShape(22.dp))
                    .border(
                        2.dp,
                        Brush.horizontalGradient(
                            listOf(Color(0xFF0284C7).copy(alpha = 0.5f), Color(0xFF22C55E).copy(alpha = 0.5f))
                        ),
                        RoundedCornerShape(22.dp)
                    )
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Warning",
                    tint = Color(0xFFEF4444),
                    modifier = Modifier
                        .size(54.dp)
                        .scale(scale)
                )
            }

            Spacer(Modifier.height(18.dp))

            Text(
                text = "VPN / Proxy Detected",
                color = Color(0xFF1E293B),
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                fontFamily = LuckiestGuyFontFamily,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = "Please disable your VPN, proxy, AdBlocker, or Private DNS configuration to continue using this application.",
                color = Color(0xFF64748B),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )

            Spacer(Modifier.height(24.dp))

            // RETRY Button (Green 3D Gradient)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .zoomClickable {
                        onRetry()
                    }
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFF4ADE80), Color(0xFF16A34A))
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .border(
                        width = 2.dp,
                        brush = Brush.horizontalGradient(
                            listOf(Color(0xFF0284C7), Color(0xFFFACC15), Color(0xFF22C55E))
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "RETRY",
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = LuckiestGuyFontFamily,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

@Composable
fun StepItem(stepNumber: String, text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFE0F2FE), RoundedCornerShape(16.dp))
            .border(1.dp, Color(0xFFBAE6FD), RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(Color(0xFF00A2FF), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stepNumber,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                fontFamily = LuckiestGuyFontFamily
            )
        }
        Spacer(Modifier.width(16.dp))
        Text(
            text = text,
            color = Color(0xFF262F3C),
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ImportantTaskPopup(
    requiredMb: Int,
    onInstallClick: () -> Unit,
    onCancel: () -> Unit
) {
    FullScreenPopupContainer(
        gradientColors = emptyList(),
        cardColor = Color(0xFFEAF8FF),
        borderColor = Color(0xFF00508F)
    ) {
        Text(
            text = "IMPORTANT TASK",
            style = TextStyle(
                color = Color(0xFF00A2FF),
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = LuckiestGuyFontFamily,
                shadow = Shadow(
                    color = Color(0xFF002D54),
                    offset = Offset(2f, 4f),
                    blurRadius = 2f
                )
            )
        )
        Spacer(Modifier.height(16.dp))
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = null,
            tint = Color(0xFF00A2FF),
            modifier = Modifier.size(56.dp)
        )
        Spacer(Modifier.height(24.dp))
        
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StepItem(stepNumber = "1", text = "Click 'Install & Unlock' below.")
            StepItem(stepNumber = "2", text = "Install the app shown in the ad.")
            StepItem(stepNumber = "3", text = "Open the app & use it for at least 1 minute to unlock!")
        }
        
        Spacer(Modifier.height(48.dp))
        PopupThreeDButton(
            text = "Install & Unlock 📥",
            onClick = onInstallClick,
            baseColor = VibrantGreen,
            isLarge = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))
        PopupThreeDButton(
            text = "Cancel",
            onClick = onCancel,
            baseColor = Color(0xFF94A3B8),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun TaskFailedPopup(
    requiredMb: Int,
    onOkay: () -> Unit
) {
    FullScreenPopupContainer(
        gradientColors = emptyList(),
        cardColor = Color(0xFFFFEBEB),
        borderColor = Color(0xFFB91C1C)
    ) {
        Text(
            text = "TASK FAILED!",
            style = TextStyle(
                color = Color(0xFFFF5E3A),
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = LuckiestGuyFontFamily,
                shadow = Shadow(
                    color = Color(0xFF7F1D1D),
                    offset = Offset(2f, 4f),
                    blurRadius = 2f
                )
            )
        )
        Spacer(Modifier.height(24.dp))
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = null,
            tint = Color(0xFFFF5E3A),
            modifier = Modifier.size(64.dp)
        )
        Spacer(Modifier.height(24.dp))
        Text(
            text = "The promoted app was not installed or opened. Please try again to unlock your level.",
            color = Color(0xFF262F3C),
            textAlign = TextAlign.Center,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(48.dp))
        PopupThreeDButton(
            text = "Okay",
            onClick = onOkay,
            baseColor = SunsetOrange,
            isLarge = true,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun NoInternetScreen(onRetry: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE2E8F0)) // Solid opaque dark-white covering the full screen
            .clickable(enabled = false) {}
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .shadow(8.dp, RoundedCornerShape(24.dp), clip = false)
                .background(Color(0xFFFFF9E6), RoundedCornerShape(24.dp))
                .border(
                    width = 3.5.dp,
                    brush = Brush.horizontalGradient(
                        listOf(Color(0xFF0284C7), Color(0xFFFACC15), Color(0xFF22C55E))
                    ),
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(start = 20.dp, end = 20.dp, top = 22.dp, bottom = 24.dp)
        ) {
            // Header Title
            Text(
                text = "NO CONNECTION!",
                style = TextStyle(
                    color = Color(0xFF2563EB),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = LuckiestGuyFontFamily,
                    letterSpacing = 0.5.sp,
                    shadow = Shadow(
                        color = Color(0x33000000),
                        offset = Offset(0f, 2f),
                        blurRadius = 2f
                    )
                ),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(18.dp))

            // Warning / No-Internet Visual Container
            Box(
                modifier = Modifier
                    .size(92.dp)
                    .background(Color.White, RoundedCornerShape(22.dp))
                    .border(
                        2.dp,
                        Brush.horizontalGradient(
                            listOf(Color(0xFF0284C7).copy(alpha = 0.5f), Color(0xFF22C55E).copy(alpha = 0.5f))
                        ),
                        RoundedCornerShape(22.dp)
                    )
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "No Internet Warning",
                    tint = Color(0xFFEF4444),
                    modifier = Modifier
                        .size(54.dp)
                        .scale(scale)
                )
            }

            Spacer(Modifier.height(18.dp))

            Text(
                text = "No Internet Connection",
                color = Color(0xFF1E293B),
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                fontFamily = LuckiestGuyFontFamily,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = "Please check your internet connection to continue playing and claiming rewards.",
                color = Color(0xFF64748B),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )

            Spacer(Modifier.height(24.dp))

            // RETRY Button (Green 3D Gradient)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .zoomClickable {
                        onRetry()
                    }
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFF4ADE80), Color(0xFF16A34A))
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .border(
                        width = 2.dp,
                        brush = Brush.horizontalGradient(
                            listOf(Color(0xFF0284C7), Color(0xFFFACC15), Color(0xFF22C55E))
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "RETRY",
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = LuckiestGuyFontFamily,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

@Composable
fun HintExplanationPopup(
    optionIndex: Int,
    colorName: String,
    onClose: () -> Unit
) {
    FullScreenPopupContainer(
        gradientColors = listOf(Color(0xFFF1F5F9), Color(0xFFE2E8F0))
    ) {
        Box(
            modifier = Modifier
                .size(90.dp)
                .background(SunnyYellow, CircleShape)
                .border(3.dp, Color(0xFFE2B800), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Lightbulb,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(54.dp)
            )
        }

        Spacer(Modifier.height(16.dp))

        Text(
            text = "HINT REVEALED!",
            color = SunnyYellow,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = LuckiestGuyFontFamily,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .background(Color.White, RoundedCornerShape(16.dp))
                .border(2.dp, Color(0xFFCBD5E1), RoundedCornerShape(16.dp))
                .padding(20.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Correct Answer is:",
                    color = Color(0xFF64748B),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                
                Spacer(Modifier.height(8.dp))
                
                Text(
                    text = "OPTION $optionIndex",
                    color = Color(0xFF2563EB),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = LuckiestGuyFontFamily,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(4.dp))

                val cName = colorName.uppercase()
                Text(
                    text = "($cName)",
                    color = getColorFromName(cName),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = LuckiestGuyFontFamily,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(16.dp))

                Text(
                    text = "Stroop Rule: You must select the COLOR of the text, not what the word reads! The text color is $cName, which corresponds to Option $optionIndex.",
                    color = Color(0xFF334155),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        PopupThreeDButton(
            text = "CONTINUE",
            onClick = {
                onClose()
            },
            baseColor = VibrantGreen,
            icon = Icons.Default.PlayArrow,
            isLarge = true,
            modifier = Modifier.fillMaxWidth(0.8f)
        )
    }
}


private fun triggerVibration(context: Context, milliseconds: Long, hapticConstant: Int) {
    if (!SoundManager.isVibrationEnabled()) {
        return
    }
    try {
        val vibrator = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? android.os.VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? android.os.Vibrator
        }

        if (vibrator != null && vibrator.hasVibrator()) {
            val amplitude = when {
                milliseconds >= 80 -> 255
                milliseconds <= 35 -> 140
                else -> 200
            }

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                val effect = android.os.VibrationEffect.createOneShot(milliseconds, amplitude)
                vibrator.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(milliseconds)
            }
        }
        
        // Also trigger view haptic feedback as companion for instant tactile response
        val view = (context as? android.app.Activity)?.window?.decorView ?: (context as? android.content.ContextWrapper)?.baseContext?.let {
            (it as? android.app.Activity)?.window?.decorView
        }
        if (view != null) {
            view.isHapticFeedbackEnabled = true
            view.performHapticFeedback(
                hapticConstant,
                android.view.HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING or android.view.HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING
            )
        }
    } catch (e: Exception) {
        android.util.Log.e("ArrowVibration", "Error in triggerVibration: ${e.message}")
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewArrowPuzzlePro() {
    ColorPipesApp()
}


