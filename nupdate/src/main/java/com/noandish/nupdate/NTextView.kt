package com.noandish.nupdate

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.appcompat.widget.AppCompatTextView
import android.util.AttributeSet
import androidx.annotation.ColorInt


class NTextView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : AppCompatTextView(context, attrs) {
    @ColorInt
    var strokeColor: Int = Color.TRANSPARENT
    var strokeWidth = 5f

    init {
        if (attrs != null) {
            val ta = context.obtainStyledAttributes(attrs, R.styleable.NTextView)
            val mTypeface = ta.getString(R.styleable.NTextView_typeface)

            if (mTypeface != null)
                typeface = TypefaceCache.addTypeface(mTypeface, context)

            strokeWidth = ta.getDimension(R.styleable.NTextView_strokeWidth, strokeWidth)
            strokeColor = ta.getColor(R.styleable.NTextView_strokeColor, strokeColor)
            ta.recycle()
        }
    }


    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (strokeColor != Color.TRANSPARENT) {
            val textColor = textColors

            val paint = this.paint

            paint.style = Paint.Style.STROKE
//            paint.strokeJoin = strokeJoin
            paint.strokeMiter = strokeWidth    //10f
            this.setTextColor(strokeColor)
            paint.strokeWidth = strokeWidth    //15f

            super.onDraw(canvas)
            paint.style = Paint.Style.FILL

            setTextColor(textColor)
            super.onDraw(canvas)

        }
    }

    companion object {
        private const val TAG = "NTextView"
    }
}