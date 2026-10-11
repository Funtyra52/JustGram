package org.justgram.badges

import androidx.annotation.DrawableRes
import androidx.annotation.Keep

@Keep
data class BadgeInfo(
    val type: BadgeType,
    val title: String,
    val description: String,
    val emojiDocumentId: Long = 0L,
    @DrawableRes val fallbackDrawableRes: Int = 0,
    val customColor: Int? = null,
    val showParticles: Boolean = false,
    val isVerified: Boolean = false
) : Comparable<BadgeInfo> {
    override fun compareTo(other: BadgeInfo): Int {
        return this.type.priority.compareTo(other.type.priority)
    }
}
