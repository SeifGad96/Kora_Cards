package com.example.koracards.data.repository

import com.example.koracards.data.model.Language
import com.example.koracards.data.model.Player
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for [Player] data-model logic and the core deduplication / random-draw
 * algorithms used by [PlayerRepository].
 *
 * Tests run purely on the JVM — no Android context required.
 */
class PlayerRepositoryTest {

    // ─────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────

    private fun makePlayer(id: String, shirtNumber: Int = 99) = Player(
        id = id,
        name = "Player $id",
        photoUrl = "players/test_$id.webp",
        age = 25,
        shirtNumber = shirtNumber,
        teamEn = "Team EN",        teamAr = "فريق",
        nationalityEn = "EGY",     nationalityAr = "مصري",
        positionEn = "FWD",        positionAr = "مهاجم",
        preferredFootEn = "Right", preferredFootAr = "يمين",
        leagueEn = "EPL",          leagueAr = "الدوري الإنجليزي"
    )

    private fun List<Player>.assertDistinctIds() =
        assertEquals("Expected all IDs to be distinct", size, map { it.id }.toSet().size)

    // ─────────────────────────────────────────────────────────────
    // Player data class — bilingual helpers
    // ─────────────────────────────────────────────────────────────

    @Test
    fun `player language helpers return English fields when isArabic=false`() {
        val p = makePlayer("1")
        assertEquals("Team EN", p.team(isArabic = false))
        assertEquals("EGY",     p.nationality(isArabic = false))
        assertEquals("FWD",     p.position(isArabic = false))
        assertEquals("Right",   p.preferredFoot(isArabic = false))
        assertEquals("EPL",     p.league(isArabic = false))
    }

    @Test
    fun `player language helpers return Arabic fields when isArabic=true`() {
        val p = makePlayer("1")
        assertEquals("فريق",              p.team(isArabic = true))
        assertEquals("مصري",             p.nationality(isArabic = true))
        assertEquals("مهاجم",            p.position(isArabic = true))
        assertEquals("يمين",             p.preferredFoot(isArabic = true))
        assertEquals("الدوري الإنجليزي", p.league(isArabic = true))
    }

    @Test
    fun `player equality is based on all fields (data class)`() {
        val p1a = makePlayer("1")
        val p1b = makePlayer("1")
        val p2  = makePlayer("2")
        assertEquals(p1a, p1b)
        assertNotEquals(p1a, p2)
    }

    @Test
    fun `player copy preserves unchanged fields`() {
        val original = makePlayer("1")
        val updated  = original.copy(age = 30)
        assertEquals(30, updated.age)
        assertEquals(original.id, updated.id)
        assertEquals(original.name, updated.name)
    }

    // ─────────────────────────────────────────────────────────────
    // Language enum
    // ─────────────────────────────────────────────────────────────

    @Test
    fun `Language fromCode returns Arabic for 'ar'`() =
        assertEquals(Language.Arabic, Language.fromCode("ar"))

    @Test
    fun `Language fromCode returns English for 'en'`() =
        assertEquals(Language.English, Language.fromCode("en"))

    @Test
    fun `Language fromCode defaults to Arabic for unknown code`() =
        assertEquals(Language.Arabic, Language.fromCode("xx"))

    @Test
    fun `Language isArabic reflects enum variant`() {
        assertTrue(Language.Arabic.isArabic)
        assertFalse(Language.English.isArabic)
    }

    // ─────────────────────────────────────────────────────────────
    // pickRandom — mirrors PlayerRepository.getRandomPlayers logic
    // ─────────────────────────────────────────────────────────────

    @Test
    fun `pickRandom returns requested count`() = runTest {
        val pool = (1..20).map { makePlayer(it.toString()) }
        val result = pickRandom(pool, count = 5, usedIds = emptySet())
        assertEquals(5, result.size)
    }

    @Test
    fun `pickRandom returns distinct players`() = runTest {
        val pool = (1..20).map { makePlayer(it.toString()) }
        pickRandom(pool, count = 10, usedIds = emptySet()).assertDistinctIds()
    }

    @Test
    fun `pickRandom excludes used ids`() = runTest {
        val pool = (1..20).map { makePlayer(it.toString()) }
        val usedIds = (1..18).map { it.toString() }.toSet()
        val result = pickRandom(pool, count = 2, usedIds = usedIds)
        result.forEach { assertFalse("${it.id} should not be in usedIds", it.id in usedIds) }
    }

    @Test
    fun `pickRandom with empty usedIds picks from entire pool`() = runTest {
        val pool = (1..10).map { makePlayer(it.toString()) }
        val result = pickRandom(pool, count = 10, usedIds = emptySet())
        assertEquals(10, result.size)
    }

    @Test(expected = IllegalStateException::class)
    fun `pickRandom throws when pool is too small`() = runTest {
        val pool = (1..5).map { makePlayer(it.toString()) }
        val usedIds = (1..4).map { it.toString() }.toSet()
        pickRandom(pool, count = 2, usedIds = usedIds) // only 1 available
    }

    // ─────────────────────────────────────────────────────────────
    // pickDecoys — mirrors PlayerRepository.getMcqDecoys logic
    // ─────────────────────────────────────────────────────────────

    @Test
    fun `pickDecoys returns requested count`() = runTest {
        val pool = (1..20).map { makePlayer(it.toString()) }
        val decoys = pickDecoys(pool, correctPlayerId = "1", usedIds = emptySet(), count = 2)
        assertEquals(2, decoys.size)
    }

    @Test
    fun `pickDecoys excludes correct player`() = runTest {
        val pool = (1..20).map { makePlayer(it.toString()) }
        val decoys = pickDecoys(pool, correctPlayerId = "1", usedIds = emptySet(), count = 5)
        decoys.forEach { assertNotEquals("Correct player must not appear in decoys", "1", it.id) }
    }

    @Test
    fun `pickDecoys excludes used ids`() = runTest {
        val pool = (1..20).map { makePlayer(it.toString()) }
        val usedIds = setOf("2", "3", "4")
        val decoys = pickDecoys(pool, correctPlayerId = "1", usedIds = usedIds, count = 2)
        decoys.forEach {
            assertFalse("${it.id} in usedIds", it.id in usedIds)
            assertNotEquals("1", it.id)
        }
    }

    @Test
    fun `pickDecoys returns distinct players`() = runTest {
        val pool = (1..20).map { makePlayer(it.toString()) }
        pickDecoys(pool, correctPlayerId = "1", usedIds = emptySet(), count = 2).assertDistinctIds()
    }

    @Test(expected = IllegalStateException::class)
    fun `pickDecoys throws when not enough decoys available`() = runTest {
        val pool = (1..3).map { makePlayer(it.toString()) }
        // correct="1", usedIds={"2"} → only "3" is available, need 2
        pickDecoys(pool, correctPlayerId = "1", usedIds = setOf("2"), count = 2)
    }

    // ─────────────────────────────────────────────────────────────
    // Pure helpers (mirror repository logic, no context needed)
    // ─────────────────────────────────────────────────────────────

    private fun pickRandom(
        players: List<Player>,
        count: Int,
        usedIds: Set<String>
    ): List<Player> {
        val available = players.filter { it.id !in usedIds }
        check(available.size >= count) {
            "Not enough unused players: need $count, have ${available.size}"
        }
        return available.shuffled().take(count)
    }

    private fun pickDecoys(
        players: List<Player>,
        correctPlayerId: String,
        usedIds: Set<String>,
        count: Int
    ): List<Player> {
        val excluded = usedIds + correctPlayerId
        val available = players.filter { it.id !in excluded }
        check(available.size >= count) {
            "Not enough players for MCQ decoys: need $count, have ${available.size}"
        }
        return available.shuffled().take(count)
    }
}
