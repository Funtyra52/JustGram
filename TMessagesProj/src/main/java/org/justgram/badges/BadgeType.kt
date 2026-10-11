package org.justgram.badges

enum class BadgeType(val priority: Int, val displayName: String) {
    JUSTGRAM(1, "JustGram"),
    CHERRYGRAM(2, "CherryGram"),
    EXTERAGRAM(3, "ExteraGram"),
    PREMIUM(4, "Telegram Premium")
}
