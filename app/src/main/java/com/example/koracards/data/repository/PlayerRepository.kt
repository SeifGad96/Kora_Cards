package com.example.koracards.data.repository

import android.content.Context
import com.example.koracards.data.model.Player
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Loads and manages the bundled football player dataset.
 *
 * - JSON is parsed off the main thread via [Dispatchers.IO].
 * - [getRandomPlayers] enforces no-repeat assignment within a match session
 *   (PRD §7.1: "No player will be assigned as a card twice in the same match session").
 */
@Singleton
class PlayerRepository @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    private val json = Json { ignoreUnknownKeys = true }

    /** Full in-memory player list, loaded lazily once and cached. */
    @Volatile
    private var cachedPlayers: List<Player>? = null

    /**
     * Returns all players from the bundled dataset.
     * Parses the JSON on [Dispatchers.IO] the first time; subsequent calls return the cache.
     */
    suspend fun getAllPlayers(): List<Player> = withContext(Dispatchers.IO) {
        cachedPlayers ?: loadFromAssets().also { cachedPlayers = it }
    }

    /**
     * Returns [count] unique random players that are not in [usedIds].
     * Throws [IllegalStateException] if the dataset has too few remaining players.
     */
    suspend fun getRandomPlayers(count: Int, usedIds: Set<String> = emptySet()): List<Player> {
        val all = getAllPlayers()
        val available = all.filter { it.id !in usedIds }
        check(available.size >= count) {
            "Not enough unused players: need $count, have ${available.size}"
        }
        return available.shuffled().take(count)
    }

    /**
     * Returns [count] decoy players for an MCQ round.
     * Excludes [correctPlayerId] and [usedIds] to ensure distinct options.
     */
    suspend fun getMcqDecoys(
        correctPlayerId: String,
        usedIds: Set<String> = emptySet(),
        count: Int = 2
    ): List<Player> {
        val all = getAllPlayers()
        val excluded = usedIds + correctPlayerId
        val available = all.filter { it.id !in excluded }
        check(available.size >= count) {
            "Not enough players for MCQ decoys: need $count, have ${available.size}"
        }
        return available.shuffled().take(count)
    }

    private fun loadFromAssets(): List<Player> {
        val jsonString = context.assets.open("players.json")
            .bufferedReader()
            .use { it.readText() }
        return json.decodeFromString<List<Player>>(jsonString)
    }
}
