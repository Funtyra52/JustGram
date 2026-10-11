package org.justgram.badges

import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.drawable.Drawable
import org.telegram.messenger.AndroidUtilities
import org.telegram.ui.Components.AnimatedEmojiDrawable
import org.telegram.ui.Components.AnimatedEmojiSpan

class BadgeCompoundDrawable(
    val drawables: List<Drawable>,
    private val gapDp: Int = 2
) : Drawable(), Drawable.Callback, AnimatedEmojiSpan.InvalidateHolder {

    private val gap: Int
        get() = AndroidUtilities.dp(gapDp.toFloat())

    init {
        for (drawable in drawables) {
            drawable.callback = this
        }
    }

    fun attach() {
        for (drawable in drawables) {
            if (drawable is AnimatedEmojiDrawable.SwapAnimatedEmojiDrawable) {
                drawable.attach()
            }
        }
    }

    fun detach() {
        for (drawable in drawables) {
            if (drawable is AnimatedEmojiDrawable.SwapAnimatedEmojiDrawable) {
                drawable.detach()
            }
        }
    }

    override fun getIntrinsicWidth(): Int {
        if (drawables.isEmpty()) return 0
        var total = 0
        for (i in drawables.indices) {
            val d = drawables[i]
            val w = if (d.intrinsicWidth > 0) d.intrinsicWidth else AndroidUtilities.dp(18f)
            total += w
            if (i < drawables.size - 1) {
                total += gap
            }
        }
        return total
    }

    override fun getIntrinsicHeight(): Int {
        var maxHeight = 0
        for (d in drawables) {
            val h = if (d.intrinsicHeight > 0) d.intrinsicHeight else AndroidUtilities.dp(18f)
            if (h > maxHeight) {
                maxHeight = h
            }
        }
        return if (maxHeight > 0) maxHeight else AndroidUtilities.dp(18f)
    }

    override fun draw(canvas: Canvas) {
        val b = bounds
        var currentX = b.left
        val height = b.height()

        for (d in drawables) {
            val itemWidth = if (d.intrinsicWidth > 0) d.intrinsicWidth else height
            val itemHeight = if (d.intrinsicHeight > 0) d.intrinsicHeight else height
            val topOffset = b.top + ((height - itemHeight) / 2)
            d.setBounds(currentX, topOffset, currentX + itemWidth, topOffset + itemHeight)
            d.draw(canvas)
            currentX += itemWidth + gap
        }
    }

    override fun onBoundsChange(bounds: Rect) {
        super.onBoundsChange(bounds)
        var currentX = bounds.left
        val height = bounds.height()

        for (d in drawables) {
            val itemWidth = if (d.intrinsicWidth > 0) d.intrinsicWidth else height
            val itemHeight = if (d.intrinsicHeight > 0) d.intrinsicHeight else height
            val topOffset = bounds.top + ((height - itemHeight) / 2)
            d.setBounds(currentX, topOffset, currentX + itemWidth, topOffset + itemHeight)
            currentX += itemWidth + gap
        }
    }

    override fun setAlpha(alpha: Int) {
        for (d in drawables) {
            d.alpha = alpha
        }
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        for (d in drawables) {
            d.colorFilter = colorFilter
        }
    }

    @Deprecated("Deprecated in Java")
    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT

    override fun invalidateDrawable(who: Drawable) {
        invalidateSelf()
    }

    override fun scheduleDrawable(who: Drawable, what: Runnable, `when`: Long) {
        scheduleSelf(what, `when`)
    }

    override fun unscheduleDrawable(who: Drawable, what: Runnable) {
        unscheduleSelf(what)
    }

    override fun invalidate() {
        invalidateSelf()
    }
}
