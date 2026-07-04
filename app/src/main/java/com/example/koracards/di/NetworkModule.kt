package com.example.koracards.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Hilt module for the M3 networking layer.
 *
 * [com.example.koracards.network.NetworkManager] is a `@Singleton` with an `@Inject constructor`,
 * so Hilt provisions it automatically — no explicit `@Provides` binding is needed here.
 *
 * This module exists as a documented extension point for future bindings such as:
 * - A fake/mock [com.example.koracards.network.NetworkManager] in instrumented tests
 * - Alternative transport implementations
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule
