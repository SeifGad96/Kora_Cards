package com.example.koracards.game

/**
 * High-level phase of the game state machine.
 *
 * Transitions (simplified):
 * ```
 * Lobby → QA → MCQ → SuddenDeath → QA
 *               ↓                    ↓
 *           CardReveal ← ─ ─ ─ ─ ─ ─┘
 *               ↓
 *          RoundResult  →  QA (next round)
 *               ↓
 *           MatchOver
 * ```
 */
enum class GamePhase {
    /** Waiting for setup — nicknames, game config, guest connection. */
    Lobby,

    /** Normal Q&A turn loop — players alternate asking yes/no questions. */
    QA,

    /** MCQ fallback — each player receives 3 options to identify their opponent's card. */
    MCQ,

    /** Sudden Death — both MCQ answers were correct; new cards dealt, no fault limit. */
    SuddenDeath,

    /** Card flip animation — both devices reveal the mystery card simultaneously. */
    CardReveal,

    /** Round over — score updated, host triggers next round. */
    RoundResult,

    /** A player has reached [com.example.koracards.network.GameConfig.roundsToWin]. */
    MatchOver
}
