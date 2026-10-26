package com.puneetfsn.kutumbcare.di

/**
 * Remembers when the dossier went to the background so the PIN screen can be
 * restored after a short grace period.
 *
 * The grace window keeps routine interruptions from being annoying: rotating the
 * phone, answering a call or picking a file to export a backup does not force the
 * PIN again, while handing the phone to someone else does.
 */
class BackgroundClock(
    private val clock: () -> Long = { System.currentTimeMillis() },
    val graceMillis: Long = DEFAULT_GRACE_MILLIS
) {
    private var backgroundedAt: Long? = null

    fun onBackground() {
        backgroundedAt = clock()
    }

    /** Returns true when the app has been away long enough to re-lock. */
    fun shouldRelock(): Boolean {
        val since = backgroundedAt ?: return false
        backgroundedAt = null
        return clock() - since >= graceMillis
    }

    fun reset() {
        backgroundedAt = null
    }

    companion object {
        const val DEFAULT_GRACE_MILLIS = 30_000L
    }
}
