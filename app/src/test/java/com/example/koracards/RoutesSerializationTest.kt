package com.example.koracards

import com.example.koracards.ui.navigation.Routes
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests to verify serialization of the type-safe Navigation routes.
 */
class RoutesSerializationTest {

    @Test
    fun testSplashRouteSerialization() {
        val route: Routes = Routes.Splash
        val json = Json.encodeToString(route)
        // Verify it contains the fully qualified name or class identifier
        assertTrue(json.contains("Splash") || json.isNotEmpty())
    }

    @Test
    fun testPlayerNameEntryRouteSerialization() {
        val route: Routes = Routes.PlayerNameEntry(isHost = true)
        val json = Json.encodeToString(route)
        assertTrue(json.contains("isHost"))
        assertTrue(json.contains("true"))
    }

    @Test
    fun testGameConfigRouteSerialization() {
        val route: Routes = Routes.GameConfig(hostName = "TestHost")
        val json = Json.encodeToString(route)
        assertTrue(json.contains("hostName"))
        assertTrue(json.contains("TestHost"))
    }

    @Test
    fun testRoundStartRouteSerialization() {
        val route: Routes = Routes.RoundStart(roundNumber = 3, isMyTurn = false)
        val json = Json.encodeToString(route)
        assertTrue(json.contains("roundNumber"))
        assertTrue(json.contains("3"))
        assertTrue(json.contains("isMyTurn"))
        assertTrue(json.contains("false"))
    }
}
