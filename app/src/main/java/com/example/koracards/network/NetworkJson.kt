package com.example.koracards.network

import kotlinx.serialization.json.Json

/**
 * Shared [Json] instance for all WebSocket message serialization.
 *
 * - [classDiscriminator] = "type" — the polymorphic type tag in every serialized [GameMessage]
 * - [encodeDefaults] = true — ensures optional fields with defaults are always written
 * - [ignoreUnknownKeys] = true — forward-compatible: unknown fields from newer app versions are ignored
 */
val NetworkJson = Json {
    classDiscriminator = "type"
    encodeDefaults = true
    ignoreUnknownKeys = true
}
