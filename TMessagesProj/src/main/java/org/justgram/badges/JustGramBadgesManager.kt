package org.justgram.badges

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.View
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import org.telegram.messenger.AndroidUtilities
import org.telegram.messenger.ApplicationLoader
import org.telegram.messenger.DialogObject
import org.telegram.messenger.FileLog
import org.telegram.messenger.LocaleController
import org.telegram.messenger.MessagesController
import org.telegram.messenger.R
import org.telegram.messenger.UserConfig
import org.telegram.messenger.UserObject
import org.telegram.tgnet.TLRPC
import org.telegram.ui.ActionBar.AlertDialog
import org.telegram.ui.ActionBar.BaseFragment
import org.telegram.ui.ActionBar.Theme
import org.telegram.ui.Components.AnimatedEmojiDrawable
import org.telegram.ui.Components.Premium.PremiumGradient
import uz.unnarsx.cherrygram.donates.BadgeHelper
import uz.unnarsx.cherrygram.donates.DonatesManager
import uz.unnarsx.cherrygram.misc.Constants
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.ConcurrentHashMap

object JustGramBadgesManager : CoroutineScope by CoroutineScope(SupervisorJob() + Dispatchers.IO) {

    private const val TAG = "JustGramBadges"
    private const val JUSTGRAM_BADGES_FILE = "justgram_badges.json"
    private const val EXTERAGRAM_BADGES_FILE = "exteragram_badges.json"
    private const val JUSTGRAM_SUPPORTERS_FILE = "justgram_supporters.txt"

    // Default remote endpoints for client badges (all JustGram users see them)
    private const val JUSTGRAM_BADGES_URL = "https://raw.githubusercontent.com/JustGramApp/badges/main/badges.json"
    private const val EXTERAGRAM_BADGES_URL = "https://raw.githubusercontent.com/exteraClan/exteraGram-Badges/main/badges.json"
    private const val JUSTGRAM_SUPPORTERS_URL_MAIN = "https://raw.githubusercontent.com/Funtyra52/JustGram/main/Supporters.txt"
    private const val JUSTGRAM_SUPPORTERS_URL_MASTER = "https://raw.githubusercontent.com/Funtyra52/JustGram/master/Supporters.txt"

    // JustGram Special IDs
    val justGramSupporters = ConcurrentHashMap.newKeySet<Long>()
    private val justGramDevs = ConcurrentHashMap.newKeySet<Long>()
    private val justGramVips = ConcurrentHashMap.newKeySet<Long>()
    private val justGramCustomBadges = ConcurrentHashMap<Long, BadgeInfo>()

    // ExteraGram IDs
    private val exteraGramUsers = ConcurrentHashMap.newKeySet<Long>()

    // Memory cache for active badges per user
    private val userBadgesCache = ConcurrentHashMap<Long, List<BadgeInfo>>()

    init {
        // Built-in fallback IDs so badges work offline out of the box
        justGramDevs.add(5142967559L) // Creator & Lead Developer
        justGramDevs.add(282287840L) // Contributor
        justGramDevs.add(1087968824L) // Dev
        justGramSupporters.add(5142967559L)
        justGramSupporters.add(282287840L)
        justGramSupporters.add(1087968824L)
        exteraGramUsers.add(282287840L)
    }

    fun init(context: Context) {
        launch {
            loadLocalBadges(context)
            fetchRemoteBadges(context)
        }
    }

    private fun loadLocalBadges(context: Context) {
        try {
            val suppFile = File(context.filesDir, JUSTGRAM_SUPPORTERS_FILE)
            if (suppFile.exists()) {
                val content = suppFile.readText()
                parseSupporters(content)
            }
        } catch (e: Throwable) {
            FileLog.e(TAG, e)
        }

        try {
            val justFile = File(context.filesDir, JUSTGRAM_BADGES_FILE)
            if (justFile.exists()) {
                val content = justFile.readText()
                parseJustGramBadges(content)
            }
        } catch (e: Throwable) {
            FileLog.e(TAG, e)
        }

        try {
            val exteraFile = File(context.filesDir, EXTERAGRAM_BADGES_FILE)
            if (exteraFile.exists()) {
                val content = exteraFile.readText()
                parseExteraGramBadges(content)
            }
        } catch (e: Throwable) {
            FileLog.e(TAG, e)
        }
    }

    fun parseSupporters(text: String) {
        try {
            val lines = text.lines()
            for (rawLine in lines) {
                val cleanLine = rawLine.substringBefore("#").substringBefore("//").trim()
                if (cleanLine.isEmpty()) continue
                // Extract first numeric id from line
                val match = Regex("""\b\d{5,15}\b""").find(cleanLine)
                val id = match?.value?.toLongOrNull() ?: cleanLine.toLongOrNull()
                if (id != null && id != 0L) {
                    justGramSupporters.add(id)
                }
            }
        } catch (e: Throwable) {
            FileLog.e(TAG, e)
        }
    }

    fun reloadSupporters(context: Context, onComplete: ((Int) -> Unit)? = null) {
        launch {
            try {
                var content = fetchUrl(JUSTGRAM_SUPPORTERS_URL_MAIN)
                if (content.isNullOrEmpty()) {
                    content = fetchUrl(JUSTGRAM_SUPPORTERS_URL_MASTER)
                }
                if (!content.isNullOrEmpty()) {
                    justGramSupporters.clear()
                    justGramSupporters.add(282287840L)
                    justGramSupporters.add(1087968824L)
                    parseSupporters(content)
                    saveToFile(context, JUSTGRAM_SUPPORTERS_FILE, content)
                    userBadgesCache.clear()
                }
            } catch (e: Throwable) {
                FileLog.e(TAG, e)
            }
            withContext(Dispatchers.Main) {
                onComplete?.invoke(justGramSupporters.size)
            }
        }
    }

    fun fetchRemoteBadges(context: Context) {
        launch {
            // Fetch JustGram Supporters from GitHub repo
            try {
                var content = fetchUrl(JUSTGRAM_SUPPORTERS_URL_MAIN)
                if (content.isNullOrEmpty()) {
                    content = fetchUrl(JUSTGRAM_SUPPORTERS_URL_MASTER)
                }
                if (!content.isNullOrEmpty()) {
                    justGramSupporters.clear()
                    justGramSupporters.add(282287840L)
                    justGramSupporters.add(1087968824L)
                    parseSupporters(content)
                    saveToFile(context, JUSTGRAM_SUPPORTERS_FILE, content)
                }
            } catch (e: Throwable) {
                FileLog.e(TAG, e)
            }

            // Fetch JustGram badges
            try {
                val content = fetchUrl(JUSTGRAM_BADGES_URL)
                if (!content.isNullOrEmpty()) {
                    parseJustGramBadges(content)
                    saveToFile(context, JUSTGRAM_BADGES_FILE, content)
                }
            } catch (e: Throwable) {
                FileLog.e(TAG, e)
            }

            // Fetch ExteraGram badges
            try {
                val content = fetchUrl(EXTERAGRAM_BADGES_URL)
                if (!content.isNullOrEmpty()) {
                    parseExteraGramBadges(content)
                    saveToFile(context, EXTERAGRAM_BADGES_FILE, content)
                }
            } catch (e: Throwable) {
                FileLog.e(TAG, e)
            }

            userBadgesCache.clear()
        }
    }

    private fun fetchUrl(urlString: String): String? {
        var conn: HttpURLConnection? = null
        return try {
            val url = URL(urlString)
            conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 6000
            conn.readTimeout = 6000
            conn.requestMethod = "GET"
            conn.useCaches = false
            if (conn.responseCode == 200) {
                conn.inputStream.bufferedReader().use { it.readText() }
            } else {
                null
            }
        } catch (e: Throwable) {
            null
        } finally {
            try {
                conn?.disconnect()
            } catch (ignore: Throwable) {}
        }
    }

    private fun saveToFile(context: Context, filename: String, data: String) {
        try {
            val file = File(context.filesDir, filename)
            FileOutputStream(file).use { it.write(data.toByteArray()) }
        } catch (e: Throwable) {
            FileLog.e(TAG, e)
        }
    }

    private fun parseJustGramBadges(jsonString: String) {
        try {
            val json = JSONObject(jsonString)
            if (json.has("devs")) {
                val arr = json.getJSONArray("devs")
                for (i in 0 until arr.length()) {
                    justGramDevs.add(arr.getLong(i))
                }
            }
            if (json.has("vips")) {
                val arr = json.getJSONArray("vips")
                for (i in 0 until arr.length()) {
                    justGramVips.add(arr.getLong(i))
                }
            }
            if (json.has("custom")) {
                val customArr = json.getJSONArray("custom")
                for (i in 0 until customArr.length()) {
                    val obj = customArr.getJSONObject(i)
                    val uid = obj.optLong("user_id", 0L)
                    if (uid != 0L) {
                        val docId = obj.optLong("emoji_id", 0L)
                        val colorStr = obj.optString("color", "")
                        val color = if (colorStr.isNotEmpty()) {
                            try { Color.parseColor(colorStr) } catch (_: Throwable) { null }
                        } else null
                        val title = obj.optString("title", "JustGram")
                        val desc = obj.optString("description", "JustGram verified user")
                        justGramCustomBadges[uid] = BadgeInfo(
                            type = BadgeType.JUSTGRAM,
                            title = title,
                            description = desc,
                            emojiDocumentId = docId,
                            fallbackDrawableRes = R.drawable.justgram_badge,
                            customColor = color,
                            showParticles = obj.optBoolean("particles", false),
                            isVerified = true
                        )
                    }
                }
            }
        } catch (e: Throwable) {
            FileLog.e(TAG, e)
        }
    }

    private fun parseExteraGramBadges(jsonString: String) {
        try {
            if (jsonString.trim().startsWith("[")) {
                val arr = JSONArray(jsonString)
                for (i in 0 until arr.length()) {
                    exteraGramUsers.add(arr.getLong(i))
                }
            } else {
                val json = JSONObject(jsonString)
                val users = json.optJSONArray("users") ?: json.optJSONArray("donators")
                if (users != null) {
                    for (i in 0 until users.length()) {
                        exteraGramUsers.add(users.getLong(i))
                    }
                }
            }
        } catch (e: Throwable) {
            FileLog.e(TAG, e)
        }
    }

    @JvmStatic
    fun getUserBadges(userId: Long, user: TLRPC.User?): List<BadgeInfo> {
        if (userId == 0L) return emptyList()

        val cached = userBadgesCache[userId]
        if (cached != null) return cached

        val list = ArrayList<BadgeInfo>()

        // 1. JustGram Badges (Priority 1)
        val custom = justGramCustomBadges[userId]
        if (custom != null) {
            list.add(custom)
        } else if (justGramDevs.contains(userId)) {
            list.add(
                BadgeInfo(
                    type = BadgeType.JUSTGRAM,
                    title = "JustGram Developer",
                    description = "Разработчик / Создатель JustGram",
                    emojiDocumentId = 0L,
                    fallbackDrawableRes = R.drawable.justgram_badge,
                    customColor = null,
                    showParticles = true,
                    isVerified = true
                )
            )
        } else if (justGramSupporters.contains(userId) || justGramVips.contains(userId)) {
            list.add(
                BadgeInfo(
                    type = BadgeType.JUSTGRAM,
                    title = "JustGram Supporter",
                    description = "Поддержал развитие JustGram (Supporter)",
                    emojiDocumentId = 0L,
                    fallbackDrawableRes = R.drawable.justgram_badge,
                    customColor = null,
                    showParticles = false,
                    isVerified = true
                )
            )
        }

        // 2. CherryGram Badges (Priority 2)
        val isCGDonated = try { DonatesManager.didUserDonate(userId) } catch (_: Throwable) { false }
        val isCGMarketplace = try { DonatesManager.didUserDonateForMarketplace(userId) } catch (_: Throwable) { false }
        val isCGOwner = userId == Constants.Cherrygram_Owner
        if (isCGDonated || isCGMarketplace || isCGOwner) {
            val emojiId = if (isCGOwner) Constants.CHERRY_EMOJI_ID_VERIFIED_BRA else Constants.CHERRY_EMOJI_ID_VERIFIED
            val color = try { BadgeHelper.getEmojiStatusColor(userId, Color.parseColor("#FF4B2B"), false) } catch (_: Throwable) { Color.parseColor("#FF4B2B") }
            list.add(
                BadgeInfo(
                    type = BadgeType.CHERRYGRAM,
                    title = "CherryGram Supporter",
                    description = "Участник сообщества CherryGram",
                    emojiDocumentId = emojiId,
                    fallbackDrawableRes = R.drawable.cherrygram_badge,
                    customColor = color,
                    showParticles = isCGMarketplace || isCGOwner,
                    isVerified = true
                )
            )
        }

        // 3. ExteraGram Badges (Priority 3)
        if (exteraGramUsers.contains(userId)) {
            list.add(
                BadgeInfo(
                    type = BadgeType.EXTERAGRAM,
                    title = "exteraGram Supporter",
                    description = "Участник сообщества exteraGram",
                    emojiDocumentId = 0L,
                    fallbackDrawableRes = R.drawable.exteragram_badge,
                    customColor = Color.parseColor("#9B51E0"),
                    showParticles = false,
                    isVerified = true
                )
            )
        }

        // 4. Telegram Premium (Priority 4)
        val hasPremium = user?.premium == true
        val emojiStatusDocId = if (user != null) DialogObject.getEmojiStatusDocumentId(user.emoji_status) else 0L
        if (hasPremium || emojiStatusDocId != 0L) {
            list.add(
                BadgeInfo(
                    type = BadgeType.PREMIUM,
                    title = "Telegram Premium",
                    description = "Подписка Telegram Premium",
                    emojiDocumentId = emojiStatusDocId,
                    fallbackDrawableRes = R.drawable.star_premium,
                    customColor = null,
                    showParticles = user?.emoji_status != null && DialogObject.isEmojiStatusCollectible(user.emoji_status),
                    isVerified = false
                )
            )
        }

        // Sort by priority (JustGram -> CherryGram -> ExteraGram -> Premium)
        list.sort()
        userBadgesCache[userId] = list
        return list
    }

    @JvmStatic
    fun createCompoundDrawable(
        parentView: View?,
        badges: List<BadgeInfo>,
        sizeDp: Int = 18
    ): BadgeCompoundDrawable? {
        if (badges.isEmpty()) return null
        val context = parentView?.context ?: ApplicationLoader.applicationContext
        val drawables = ArrayList<Drawable>()

        for (badge in badges) {
            try {
                if (badge.emojiDocumentId != 0L) {
                    val animatedEmoji = AnimatedEmojiDrawable.SwapAnimatedEmojiDrawable(
                        parentView,
                        AndroidUtilities.dp(sizeDp.toFloat()),
                        AnimatedEmojiDrawable.CACHE_TYPE_EMOJI_STATUS
                    )
                    animatedEmoji.set(badge.emojiDocumentId, false)
                    animatedEmoji.setParticles(badge.showParticles, false)
                    badge.customColor?.let { animatedEmoji.setColor(it) }
                    drawables.add(animatedEmoji)
                } else if (badge.fallbackDrawableRes != 0) {
                    val d = ContextCompat.getDrawable(context, badge.fallbackDrawableRes)?.mutate()
                    if (d != null) {
                        badge.customColor?.let { d.setTint(it) }
                        drawables.add(d)
                    }
                }
            } catch (e: Throwable) {
                FileLog.e(TAG, e)
            }
        }

        if (drawables.isEmpty()) return null
        return BadgeCompoundDrawable(drawables, gapDp = 2)
    }

    @JvmStatic
    fun showBadgesInfoDialog(fragment: BaseFragment?, userId: Long, user: TLRPC.User?) {
        if (fragment == null || fragment.parentActivity == null) return
        val badges = getUserBadges(userId, user)
        if (badges.isEmpty()) return

        val builder = AlertDialog.Builder(fragment.parentActivity, fragment.resourceProvider)
        val name = if (user != null) UserObject.getUserName(user) else "Пользователь"
        builder.setTitle("Бейджи профиля: $name")

        val sb = StringBuilder()
        for (i in badges.indices) {
            val b = badges[i]
            val tag = when (b.type) {
                BadgeType.JUSTGRAM -> "⭐ JustGram"
                BadgeType.CHERRYGRAM -> "🍒 CherryGram"
                BadgeType.EXTERAGRAM -> "🟣 exteraGram"
                BadgeType.PREMIUM -> "✨ Telegram Premium"
            }
            sb.append("${i + 1}. $tag — ${b.title}\n")
            sb.append("   ${b.description}\n\n")
        }

        builder.setMessage(sb.toString().trimEnd())
        builder.setPositiveButton(LocaleController.getString(R.string.OK), null)
        fragment.showDialog(builder.create())
    }
}
