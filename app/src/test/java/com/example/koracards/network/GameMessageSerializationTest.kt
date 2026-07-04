package com.example.koracards.network

import com.example.koracards.data.model.Player
import kotlinx.serialization.encodeToString
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for [GameMessage] serialization round-trips.
 *
 * Verifies that every [GameMessage] subtype can be encoded to JSON and decoded back
 * to an equal instance using [NetworkJson]. Also checks the type discriminator field.
 */
class GameMessageSerializationTest {

    // Helper — round-trips a GameMessage through NetworkJson
    private fun roundTrip(message: GameMessage): GameMessage {
        val json = NetworkJson.encodeToString<GameMessage>(message)
        return NetworkJson.decodeFromString<GameMessage>(json)
    }

    // ----- GameMessage subtypes -----

    @Test
    fun `ConfigSynced round-trip preserves all GameConfig fields`() {
        val msg = GameMessage.ConfigSynced(
            GameConfig(roundsToWin = 5, maxFaults = 2, turnTimerSeconds = 30, mcqEnabled = false, mcqQuestionThreshold = 5)
        )
        assertEquals(msg, roundTrip(msg))
    }

    @Test
    fun `RoundStarted round-trip preserves both Player cards`() {
        val host = makePlayer("p1", "Cristiano Ronaldo")
        val guest = makePlayer("p2", "Lionel Messi")
        val msg = GameMessage.RoundStarted(hostCard = host, guestCard = guest)
        assertEquals(msg, roundTrip(msg))
    }

    @Test
    fun `TurnChanged round-trip preserves activePlayerId`() {
        val msg = GameMessage.TurnChanged("player_host")
        assertEquals(msg, roundTrip(msg))
    }

    @Test
    fun `GuessResult round-trip - correct guess`() {
        val msg = GameMessage.GuessResult(isCorrect = true, faultCount = 0)
        assertEquals(msg, roundTrip(msg))
    }

    @Test
    fun `GuessResult round-trip - wrong guess with faults`() {
        val msg = GameMessage.GuessResult(isCorrect = false, faultCount = 3)
        assertEquals(msg, roundTrip(msg))
    }

    @Test
    fun `McqTriggered round-trip preserves hostOptions and guestOptions`() {
        val msg = GameMessage.McqTriggered(
            hostOptions = listOf(makePlayer("a", "Player A"), makePlayer("b", "Player B"), makePlayer("c", "Player C")),
            guestOptions = listOf(makePlayer("d", "Player D"), makePlayer("e", "Player E"), makePlayer("f", "Player F"))
        )
        assertEquals(msg, roundTrip(msg))
    }

    @Test
    fun `McqAnswerSubmitted round-trip preserves playerId and answer`() {
        val msg = GameMessage.McqAnswerSubmitted(playerId = "player_host", answer = "Messi")
        assertEquals(msg, roundTrip(msg))
    }

    @Test
    fun `SuddenDeathTriggered round-trip - object type`() {
        val msg = GameMessage.SuddenDeathTriggered
        assertEquals(msg, roundTrip(msg))
    }

    @Test
    fun `RoundEnded round-trip preserves winnerId and scores`() {
        val msg = GameMessage.RoundEnded(winnerId = "player_guest", scores = Scores(hostScore = 1, guestScore = 2))
        assertEquals(msg, roundTrip(msg))
    }

    @Test
    fun `CardRevealTrigger round-trip - object type`() {
        val msg = GameMessage.CardRevealTrigger
        assertEquals(msg, roundTrip(msg))
    }

    @Test
    fun `MatchWon round-trip preserves winnerId`() {
        val msg = GameMessage.MatchWon(winnerId = "player_host")
        assertEquals(msg, roundTrip(msg))
    }

    @Test
    fun `NicknameSync round-trip preserves both names`() {
        val msg = GameMessage.NicknameSync(hostName = "Seif", guestName = "Ahmed")
        assertEquals(msg, roundTrip(msg))
    }

    // ----- Type discriminator -----

    @Test
    fun `serialized JSON contains type discriminator field`() {
        val json = NetworkJson.encodeToString<GameMessage>(GameMessage.NicknameSync("A", "B"))
        assertTrue("Expected 'type' key in JSON: $json", json.contains("\"type\""))
        assertTrue("Expected 'NicknameSync' discriminator value in JSON: $json", json.contains("NicknameSync"))
    }

    // ----- Supporting data classes -----

    @Test
    fun `GameConfig defaults are correct per PRD`() {
        val config = GameConfig()
        assertEquals(3, config.roundsToWin)
        assertEquals(3, config.maxFaults)
        assertNull("Turn timer should default to null (disabled)", config.turnTimerSeconds)
        assertTrue("MCQ should be enabled by default", config.mcqEnabled)
        assertEquals(3, config.mcqQuestionThreshold)
    }

    @Test
    fun `Scores defaults to zero-zero`() {
        val scores = Scores()
        assertEquals(0, scores.hostScore)
        assertEquals(0, scores.guestScore)
    }

    // ----- Helper -----

    private fun makePlayer(id: String, name: String = "Test Player") = Player(
        id = id,
        name = name,
        photoUrl = "",
        age = 25,
        shirtNumber = 10,
        teamEn = "Real Madrid",
        teamAr = "ريال مدريد",
        nationalityEn = "Spanish",
        nationalityAr = "إسباني",
        positionEn = "Forward",
        positionAr = "مهاجم",
        preferredFootEn = "Right",
        preferredFootAr = "يمين",
        leagueEn = "La Liga",
        leagueAr = "الدوري الإسباني"
    )
}
