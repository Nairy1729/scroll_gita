package com.scrollthegita.core.model

/**
 * A shloka counts as read only after its page has settled for [SETTLED_READ_MILLIS].
 * Fast swipes and prefetched neighbors do not count. The count is private and is not shown as a streak.
 */
object ReadingPolicy {
    const val SETTLED_READ_MILLIS = 2_000L
    const val MINDFUL_PAUSE_EVERY = 7
}

object ReflectionPolicy {
    const val MAX_BODY_LENGTH = 5_000
}
