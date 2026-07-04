package com.example.koracards.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a single football player card entry from the bundled dataset.
 *
 * Bilingual fields follow the PRD §4.4 localization rules:
 *  - [id], [name], [age], [shirtNumber]: always in English (never translated)
 *  - [teamEn]/[teamAr], [nationalityEn]/[nationalityAr],
 *    [positionEn]/[positionAr], [preferredFootEn]/[preferredFootAr],
 *    [leagueEn]/[leagueAr]: translated per active language
 */
@Serializable
data class Player(
    val id: String,
    val name: String,
    @SerialName("photo_url") val photoUrl: String,
    val age: Int,
    @SerialName("shirt_number") val shirtNumber: Int,
    @SerialName("team_en") val teamEn: String,
    @SerialName("team_ar") val teamAr: String,
    @SerialName("nationality_en") val nationalityEn: String,
    @SerialName("nationality_ar") val nationalityAr: String,
    @SerialName("position_en") val positionEn: String,
    @SerialName("position_ar") val positionAr: String,
    @SerialName("preferred_foot_en") val preferredFootEn: String,
    @SerialName("preferred_foot_ar") val preferredFootAr: String,
    @SerialName("league_en") val leagueEn: String,
    @SerialName("league_ar") val leagueAr: String
) {
    /** Returns the team name in the requested language. */
    fun team(isArabic: Boolean) = if (isArabic) teamAr else teamEn

    /** Returns the nationality in the requested language. */
    fun nationality(isArabic: Boolean) = if (isArabic) nationalityAr else nationalityEn

    /** Returns the position in the requested language. */
    fun position(isArabic: Boolean) = if (isArabic) positionAr else positionEn

    /** Returns the preferred foot label in the requested language. */
    fun preferredFoot(isArabic: Boolean) = if (isArabic) preferredFootAr else preferredFootEn

    /** Returns the league name in the requested language. */
    fun league(isArabic: Boolean) = if (isArabic) leagueAr else leagueEn
}
