package com.example.koracards.network

import com.example.koracards.data.model.Player
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * All events exchanged between the host and guest over the WebSocket connection.
 *
 * Serialized using [NetworkJson] with `classDiscriminator = "type"`.
 * Every subclass must carry a unique `@SerialName` that matches the discriminator value.
 *
 * PRD reference: §6.3 — Network Message Protocol
 */
@Serializable
sealed class GameMessage {

    /** Host sends the agreed [GameConfig] to the guest after setup. */
    @Serializable
    @SerialName("ConfigSynced")
    data class ConfigSynced(val config: GameConfig) : GameMessage()

    /**
     * Sent by host at the start of each round. Each player receives the *other* player's card
     * only at round end (card reveal) — this message delivers only the recipient's own card.
     * Both [hostCard] and [guestCard] are included so both devices can set up their local state.
     */
    @Serializable
    @SerialName("RoundStarted")
    data class RoundStarted(
        val hostCard: Player,
        val guestCard: Player
    ) : GameMessage()

    /** Sent when the active questioning player changes. */
    @Serializable
    @SerialName("TurnChanged")
    data class TurnChanged(val activePlayerId: String) : GameMessage()

    /** Sent by the card-holder after judging the opponent's guess as correct or wrong. */
    @Serializable
    @SerialName("GuessResult")
    data class GuessResult(
        val isCorrect: Boolean,
        val faultCount: Int
    ) : GameMessage()

    /**
     * Host triggers MCQ — sends role-specific option lists to both devices in one message.
     *
     * Each device selects its own list based on [PlayerRole]:
     * - Host picks from [hostOptions] (trying to guess the guest's card).
     * - Guest picks from [guestOptions] (trying to guess the host's card).
     *
     * Each list contains 1 correct answer + 2 decoys drawn from the player dataset.
     */
    @Serializable
    @SerialName("McqTriggered")
    data class McqTriggered(
        val hostOptions: List<Player>,
        val guestOptions: List<Player>
    ) : GameMessage()

    /** Sent by each player after selecting their MCQ answer. */
    @Serializable
    @SerialName("McqAnswerSubmitted")
    data class McqAnswerSubmitted(
        val playerId: String,
        val answer: String
    ) : GameMessage()

    /** Host broadcasts when the match reaches a tie — Sudden Death begins. */
    @Serializable
    @SerialName("SuddenDeathTriggered")
    object SuddenDeathTriggered : GameMessage()

    /** Sent at the end of a round. [scores] reflect cumulative match score after this round. */
    @Serializable
    @SerialName("RoundEnded")
    data class RoundEnded(
        val winnerId: String,
        val scores: Scores
    ) : GameMessage()

    /** Host signals both devices to play the card-flip reveal animation. */
    @Serializable
    @SerialName("CardRevealTrigger")
    object CardRevealTrigger : GameMessage()

    /** Sent when one player wins enough rounds to win the match. */
    @Serializable
    @SerialName("MatchWon")
    data class MatchWon(val winnerId: String) : GameMessage()

    /**
     * Exchanged after both players enter their nicknames on the Player Name Entry screen.
     * Host sends this once both names are confirmed.
     */
    @Serializable
    @SerialName("NicknameSync")
    data class NicknameSync(
        val hostName: String,
        val guestName: String
    ) : GameMessage()
}

// ---------------------------------------------------------------------------
// Supporting data classes used inside GameMessage subtypes
// ---------------------------------------------------------------------------

/**
 * Game settings chosen by the host on the Game Config screen.
 * Sent to the guest via [GameMessage.ConfigSynced].
 *
 * PRD reference: §4.6 — Game Configuration
 */
@Serializable
data class GameConfig(
    /** Number of rounds a player must win to win the match (1–10). */
    val roundsToWin: Int = 3,
    /** Maximum wrong guesses per round before the round is lost (1–5). */
    val maxFaults: Int = 3,
    /** Turn timer duration in seconds, or null if the timer is disabled. */
    val turnTimerSeconds: Int? = null,
    /** Whether the MCQ fallback is enabled. */
    val mcqEnabled: Boolean = true,
    /** Number of questions asked before MCQ is triggered (when [mcqEnabled] = true). */
    val mcqQuestionThreshold: Int = 3
)

/**
 * Cumulative match score at the time a message is sent.
 * Carried by [GameMessage.RoundEnded].
 */
@Serializable
data class Scores(
    val hostScore: Int = 0,
    val guestScore: Int = 0
)
