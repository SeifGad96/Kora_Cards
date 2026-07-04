package com.example.koracards.game

/**
 * Which role this device plays in the current match.
 *
 * - **Host** — runs the WebSocket server, drives all game logic.
 * - **Guest** — connects as WebSocket client, receives state updates from Host.
 */
enum class PlayerRole { Host, Guest }
