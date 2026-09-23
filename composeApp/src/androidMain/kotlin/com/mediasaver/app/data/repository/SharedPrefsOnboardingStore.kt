package com.mediasaver.app.data.repository

import android.content.Context
import com.mediasaver.app.domain.repository.OnboardingStore

/** [OnboardingStore] backed by a single boolean in Android's default SharedPreferences. */
class SharedPrefsOnboardingStore(context: Context) : OnboardingStore {

    private val prefs = context.getSharedPreferences("mediasaver_prefs", Context.MODE_PRIVATE)

    override suspend fun hasCompletedOnboarding(): Boolean =
        prefs.getBoolean(KEY_ONBOARDING_DONE, false)

    override suspend fun setOnboardingCompleted() {
        prefs.edit().putBoolean(KEY_ONBOARDING_DONE, true).apply()
    }

    private companion object {
        const val KEY_ONBOARDING_DONE = "onboarding_done"
    }
}
