package com.dndassistant.utilities

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import androidx.appcompat.widget.AppCompatImageView

class SquareImageView(context: Context, attributes: AttributeSet?): AppCompatImageView(context, attributes) {
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }
}

// Source - https://stackoverflow.com/a/6327095
// Posted by prasobh, modified by community. See post 'Timeline' for change history
// Retrieved 2026-05-10, License - CC BY-SA 4.0

fun Context.toPx(dp: Int): Float = TypedValue.applyDimension(
    TypedValue.COMPLEX_UNIT_DIP,
    dp.toFloat(),
    resources.displayMetrics)
