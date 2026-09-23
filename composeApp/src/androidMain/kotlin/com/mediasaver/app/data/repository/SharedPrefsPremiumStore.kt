package com.mediasaver.app.data.repository

import android.content.Context
import com.mediasaver.app.domain.model.PremiumPlan
import com.mediasaver.app.domain.repository.PremiumStore

/** [PremiumStore] backed by a single plan-id string in Android's default SharedPreferences. */
class SharedPrefsPremiumStore(context: Context) : PremiumStore {

    private val prefs = context.getSharedPreferences("mediasaver_prefs", Context.MODE_PRIVATE)

    override suspend fun activePlan(): PremiumPlan? =
        PremiumPlan.fromId(prefs.getString(KEY_ACTIVE_PLAN, null))

    override suspend fun setActivePlan(plan: PremiumPlan) {
        prefs.edit().putString(KEY_ACTIVE_PLAN, plan.id).apply()
    }

    override suspend fun clear() {
        prefs.edit().remove(KEY_ACTIVE_PLAN).apply()
    }

    override suspend fun temporaryUnlockExpiresAt(): Long? {
        val expiry = prefs.getLong(KEY_TEMP_UNLOCK_EXPIRY, 0L)
        return expiry.takeIf { it > System.currentTimeMillis() }
    }

    override suspend fun grantTemporaryUnlock(expiresAtEpochMs: Long) {
        prefs.edit().putLong(KEY_TEMP_UNLOCK_EXPIRY, expiresAtEpochMs).apply()
    }

    private companion object {
        const val KEY_ACTIVE_PLAN = "premium_active_plan"
        const val KEY_TEMP_UNLOCK_EXPIRY = "premium_temp_unlock_expiry"
    }
}
