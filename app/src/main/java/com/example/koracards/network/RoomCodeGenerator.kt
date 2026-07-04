package com.example.koracards.network

/**
 * Generates a random 4-character alphanumeric room code for each game session.
 *
 * - Uses uppercase letters and digits only (avoids ambiguous chars like 0/O, 1/I)
 * - Called once per host session; displayed prominently on the Host Waiting screen
 * - The guest enters this code on the Join Game screen to filter UDP broadcasts
 */
object RoomCodeGenerator {

    private val ALLOWED_CHARS = ('A'..'Z') + ('2'..'9') // excludes 0, 1 to avoid O/I confusion
    private const val CODE_LENGTH = 4

    /** Returns a new random 4-character room code, e.g. `"K7RB"`. */
    fun generate(): String = (1..CODE_LENGTH)
        .map { ALLOWED_CHARS.random() }
        .joinToString("")
}
