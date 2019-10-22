package com.noandish.nupdate

import android.content.Context
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import androidx.annotation.ColorInt
import androidx.appcompat.widget.AppCompatButton


class NButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : AppCompatButton(context, attrs) {
    @ColorInt
    var strokeColor: Int = Color.TRANSPARENT

    var strokeWidth = 5f

    init {
        if (attrs != null) {
            val ta = context.obtainStyledAttributes(attrs, R.styleable.NButton)
            val typeFace = ta.getString(R.styleable.NButton_typefaceButton)
            if (typeFace != null)
                typeface = Typeface.createFromAsset(context.assets, typeFace)
            strokeWidth = ta.getDimension(R.styleable.NButton_strokeWidthButton, strokeWidth)
            strokeColor = ta.getColor(R.styleable.NButton_strokeColorButton, strokeColor)
            ta.recycle()
        }

        if (background is ColorDrawable) {
            val colorBackground = (background as ColorDrawable).color
            setBackgroundResource(android.R.drawable.btn_default)
            background.setColorFilter(colorBackground, PorterDuff.Mode.MULTIPLY)
        }
    }


    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (strokeColor != Color.TRANSPARENT) {
            val textColor = textColors

            val paint = this.paint

            paint.style = Paint.Style.STROKE
//            paint.strokeJoin = strokeJoin
            paint.strokeMiter = 10f    //10f
            this.setTextColor(strokeColor)
            paint.strokeWidth = strokeWidth    //15f

            super.onDraw(canvas)
            paint.style = Paint.Style.FILL

            setTextColor(textColor)
            super.onDraw(canvas)

        }
    }

    companion object {
        private const val TAG = "NButton"
    }
}