package com.example.koracards.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.koracards.game.GamePhase
import com.example.koracards.game.GameViewModel
import com.example.koracards.network.ConnectionState
import com.example.koracards.ui.screens.LobbyViewModel
import com.example.koracards.ui.screens.*

/**
 * AppNavHost sets up the navigation graph for all 16 screens using type-safe routes.
 *
 * [isArabic] is propagated to every screen so each composable renders in one language only.
 */
@Composable
fun AppNavHost(
    isArabic: Boolean,
    onToggleLanguage: () -> Unit,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    val gameViewModel: GameViewModel = hiltViewModel()
    val lobbyViewModel: LobbyViewModel = hiltViewModel()
    val gameState by gameViewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(gameState.phase) {
        when (gameState.phase) {
            GamePhase.QA -> {
                val current = navController.currentDestination?.route
                // Only navigate to gameplay from screens that are part of the active game flow.
                // Block navigation if we're on a lobby/home/settings screen — a stale QA phase
                // from a previous match session could otherwise fire and skip the lobby screens.
                val isOnNonGameScreen = current == Routes.Home::class.qualifiedName ||
                    current == Routes.Splash::class.qualifiedName ||
                    current == Routes.Settings::class.qualifiedName ||
                    current == Routes.HostWaiting::class.qualifiedName ||
                    current == Routes.JoinGame::class.qualifiedName
                if (!isOnNonGameScreen &&
                    current != Routes.GameplayMyCard::class.qualifiedName &&
                    current != Routes.GameplayOpponentGuessing::class.qualifiedName) {
                    val dest = if (gameState.isMyTurn) Routes.GameplayMyCard
                               else Routes.GameplayOpponentGuessing
                    navController.navigate(dest)
                }
            }
            GamePhase.MCQ       -> navController.navigate(Routes.MCQ)
            GamePhase.SuddenDeath -> navController.navigate(Routes.SuddenDeathIntro)
            GamePhase.CardReveal  -> navController.navigate(Routes.CardReveal)
            GamePhase.RoundResult -> {
                val winnerName = if (gameState.roundWinnerId == gameState.myPlayerId)
                    gameState.myNickname else gameState.opponentNickname
                navController.navigate(Routes.RoundResult(winnerName, gameState.hostScore, gameState.guestScore))
            }
            GamePhase.MatchOver -> {
                val winnerName = if (gameState.matchWinnerId == gameState.myPlayerId)
                    gameState.myNickname else gameState.opponentNickname
                navController.navigate(Routes.MatchWinner(winnerName, gameState.hostScore, gameState.guestScore))
            }
            else -> Unit
        }
    }

    LaunchedEffect(gameState.connectionState) {
        if (gameState.connectionState is ConnectionState.Lost && gameState.phase != GamePhase.Lobby) {
            navController.navigate(Routes.ConnectionLost)
        }
    }

    NavHost(
        navController = navController,
        startDestination = Routes.Splash,
        modifier = modifier
    ) {
        composable<Routes.Splash> {
            SplashScreen(
                isArabic = isArabic,
                onNavigateToHome = { navController.navigate(Routes.Home) }
            )
        }
        composable<Routes.Home> {
            HomeScreen(
                isArabic = isArabic,
                // Host Game now goes directly to HostWaiting (show room code + wait for guest).
                // PlayerNameEntry and GameConfig are visited AFTER the guest connects.
                onHostGame = { navController.navigate(Routes.HostWaiting) },
                onJoinGame = { navController.navigate(Routes.JoinGame) },
                onSettings = { navController.navigate(Routes.Settings) }
            )
        }
        composable<Routes.HostWaiting> {
            HostWaitingScreen(
                isArabic = isArabic,
                lobbyViewModel = lobbyViewModel,
                onGuestConnected = { navController.navigate(Routes.PlayerNameEntry(isHost = true)) },
                onBackToHome = { navController.popBackStack(Routes.Home, inclusive = false) }
            )
        }
        composable<Routes.JoinGame> {
            JoinGameScreen(
                isArabic = isArabic,
                lobbyViewModel = lobbyViewModel,
                onConnected = { navController.navigate(Routes.PlayerNameEntry(isHost = false)) },
                onBack = { navController.popBackStack() }
            )
        }
        composable<Routes.PlayerNameEntry> { backStackEntry ->
            val route = backStackEntry.toRoute<Routes.PlayerNameEntry>()
            PlayerNameEntryScreen(
                isArabic = isArabic,
                isHost = route.isHost,
                lobbyViewModel = lobbyViewModel,
                gameViewModel = gameViewModel,
                onHostProceedToConfig = { hostName -> navController.navigate(Routes.GameConfig(hostName)) },
                onGuestWait = { /* no-op */ },
                onBack = { navController.popBackStack() }
            )
        }
        composable<Routes.GameConfig> { backStackEntry ->
            val route = backStackEntry.toRoute<Routes.GameConfig>()
            GameConfigScreen(
                isArabic = isArabic,
                hostName = route.hostName,
                lobbyViewModel = lobbyViewModel,
                gameViewModel = gameViewModel,
                // No-op: GameConfigScreen calls gameViewModel.startRound() which transitions
                // GamePhase to QA. The LaunchedEffect(gameState.phase) above handles navigation
                // automatically — no need to push a new destination here.
                onStartGame = { /* phase-driven navigation handles this */ },
                onBack = { navController.popBackStack() }
            )
        }
        composable<Routes.RoundStart> { backStackEntry ->
            val route = backStackEntry.toRoute<Routes.RoundStart>()
            RoundStartScreen(
                isArabic = isArabic,
                roundNumber = route.roundNumber,
                isMyTurn = route.isMyTurn,
                onProceedToGameplay = {
                    if (route.isMyTurn) {
                        navController.navigate(Routes.GameplayMyCard)
                    } else {
                        navController.navigate(Routes.GameplayOpponentGuessing)
                    }
                }
            )
        }
        composable<Routes.GameplayMyCard> {
            GameplayMyCardScreen(
                isArabic = isArabic,
                gameViewModel = gameViewModel
            )
        }
        composable<Routes.GameplayOpponentGuessing> {
            GameplayOpponentGuessingScreen(
                isArabic = isArabic,
                gameViewModel = gameViewModel
            )
        }
        composable<Routes.MCQ> {
            MCQScreen(
                isArabic = isArabic,
                gameViewModel = gameViewModel
            )
        }
        composable<Routes.CardReveal> {
            CardRevealScreen(
                isArabic = isArabic,
                gameViewModel = gameViewModel
            )
        }
        composable<Routes.RoundResult> { backStackEntry ->
            val route = backStackEntry.toRoute<Routes.RoundResult>()
            RoundResultScreen(
                isArabic = isArabic,
                winnerName = route.winnerName,
                hostScore = route.hostScore,
                guestScore = route.guestScore,
                gameViewModel = gameViewModel
            )
        }
        composable<Routes.SuddenDeathIntro> {
            SuddenDeathIntroScreen(
                isArabic = isArabic
            )
        }
        composable<Routes.MatchWinner> { backStackEntry ->
            val route = backStackEntry.toRoute<Routes.MatchWinner>()
            MatchWinnerScreen(
                isArabic = isArabic,
                winnerName = route.winnerName,
                hostScore = route.hostScore,
                guestScore = route.guestScore,
                gameViewModel = gameViewModel,
                onBackToHome = {
                    gameViewModel.resetMatch()
                    navController.popBackStack(Routes.Home, inclusive = false)
                }
            )
        }
        composable<Routes.Settings> {
            SettingsScreen(
                isArabic = isArabic,
                onToggleLanguage = onToggleLanguage,
                onBack = { navController.popBackStack() }
            )
        }
        composable<Routes.ConnectionLost> {
            ConnectionLostScreen(
                isArabic = isArabic,
                gameViewModel = gameViewModel,
                onGoHome = {
                    gameViewModel.resetMatch()
                    navController.popBackStack(Routes.Home, inclusive = false)
                }
            )
        }
    }
}
