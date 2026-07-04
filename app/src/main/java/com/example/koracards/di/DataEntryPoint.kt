package com.example.koracards.di

import com.example.koracards.data.repository.PlayerRepository
import com.example.koracards.data.repository.PreferencesRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Hilt entry point for accessing repositories from non-Hilt-managed contexts if needed.
 *
 * [PlayerRepository] and [PreferencesRepository] are both annotated with [@Singleton]
 * and [@Inject constructor], so Hilt auto-binds them — no explicit [@Provides] needed.
 *
 * This file documents the binding contract and can be extended if manual bindings
 * become necessary (e.g., for interface-to-impl binding).
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface DataEntryPoint {
    fun playerRepository(): PlayerRepository
    fun preferencesRepository(): PreferencesRepository
}
