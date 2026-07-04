package com.example.koracards.ui.navigation

import kotlinx.serialization.Serializable

/**
 * Type-safe navigation routes for KoraCards.
 *
 * The sealed interface and all subclasses are [@Serializable] so that
 * kotlinx.serialization can encode/decode them polymorphically — used by
 * the Navigation Compose type-safe routes and unit tests.
 */
@Serializable
sealed interface Routes {
    @Serializable
    data object Splash : Routes

    @Serializable
    data object Home : Routes

    @Serializable
    data object HostWaiting : Routes

    @Serializable
    data object JoinGame : Routes

    @Serializable
    data class PlayerNameEntry(val isHost: Boolean) : Routes

    @Serializable
    data class GameConfig(val hostName: String) : Routes

    @Serializable
    data class RoundStart(val roundNumber: Int, val isMyTurn: Boolean) : Routes

    @Serializable
    data object GameplayMyCard : Routes

    @Serializable
    data object GameplayOpponentGuessing : Routes

    @Serializable
    data object MCQ : Routes

    @Serializable
    data object CardReveal : Routes

    @Serializable
    data class RoundResult(val winnerName: String, val hostScore: Int, val guestScore: Int) : Routes

    @Serializable
    data object SuddenDeathIntro : Routes

    @Serializable
    data class MatchWinner(val winnerName: String, val hostScore: Int, val guestScore: Int) : Routes

    @Serializable
    data object Settings : Routes

    @Serializable
    data object ConnectionLost : Routes
}
