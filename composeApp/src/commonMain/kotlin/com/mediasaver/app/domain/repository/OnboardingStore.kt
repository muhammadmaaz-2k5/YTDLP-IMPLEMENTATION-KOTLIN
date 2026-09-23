package com.mediasaver.app.domain.repository

/** Tracks whether the user has completed the first-launch onboarding flow. */
interface OnboardingStore {
    suspend fun hasCompletedOnboarding(): Boolean
    suspend fun setOnboardingCompleted()
}
