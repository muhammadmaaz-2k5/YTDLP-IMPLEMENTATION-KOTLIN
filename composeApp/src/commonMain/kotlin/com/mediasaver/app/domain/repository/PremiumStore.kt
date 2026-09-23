package com.mediasaver.app.domain.repository

import com.mediasaver.app.domain.model.PremiumPlan

/**
 * Tracks the locally-unlocked premium plan.
 *
 * There is no billing backend wired up — this app has no server component and isn't distributed
 * through Play Store (see README), so Google Play Billing isn't a viable integration here.
 * [setActivePlan] therefore does not charge anything; it only persists the user's selection so
 * the UI can reflect it. The Premium screen makes this explicit to the user rather than implying
 * a real transaction occurred.
 */
interface PremiumStore {
    suspend fun activePlan(): PremiumPlan?
    suspend fun setActivePlan(plan: PremiumPlan)
    suspend fun clear()

    /** Epoch-ms expiry of a rewarded-ad-earned temporary unlock, or null if none/expired. See [grantTemporaryUnlock]. */
    suspend fun temporaryUnlockExpiresAt(): Long?

    /** Grants a temporary premium unlock — the real, honest "reward" behind the Premium screen's rewarded ad (no fake purchase). */
    suspend fun grantTemporaryUnlock(expiresAtEpochMs: Long)
}
