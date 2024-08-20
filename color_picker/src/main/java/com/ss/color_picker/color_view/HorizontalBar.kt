package com.ss.color_picker.color_view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import androidx.core.content.ContextCompat
import com.ss.color_picker.R
import kotlin.math.roundToInt


class HorizontalBar : View, OnTouchListener {

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() = Unit

    var maximumValue: Float = 100f
        set(value) {
            field = value
            refreshThumbPosition()
            invalidate()
        }
    var progress: Float = 0f
        set(value) {
            field = value.coerceIn(0f, maximumValue)
            refreshThumbPosition()
            invalidate()
        }
    var trackImage: Drawable? = null
        set(value) {
            field = value
            trackBackground?.bounds?.let { trackImage?.bounds = it }
            invalidate()
        }

    private val trackBackground = ContextCompat.getDrawable(context, R.drawable.bg_color_alpha)
    private val thumbIcon = ContextCompat.getDrawable(context, R.drawable.color_thumb)
    var onProgressChange: ((progressValue: Float) -> Unit)? = null

    override fun onSizeChanged(width: Int, height: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(width, height, oldw, oldh)
        refreshBackgroundSize()
        refreshThumbPosition()
        //Because onDraw function is called after the onSizeChanged that's why I did not called invalidate() here.
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        trackBackground?.draw(canvas)
        trackImage?.draw(canvas)
        thumbIcon?.draw(canvas)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        setOnTouchListener(this)
    }

    override fun onDetachedFromWindow() {
        setOnTouchListener(null)
        super.onDetachedFromWindow()
    }

    override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {
        if (!motionEvent.isRequiredMotionEvent()) return false
        val totalWidth = view.measuredWidth
        val x = motionEvent.x
        progress = (x / totalWidth) * maximumValue
        onProgressChange?.invoke(progress)
        return true
    }

    private fun refreshBackgroundSize() {
        val backgroundTopMargin = height * 0.33f
        val bounds = Rect(0, backgroundTopMargin.roundToInt(), measuredWidth, measuredHeight)
        trackBackground?.bounds = bounds
        trackImage?.bounds = bounds
    }

    private fun refreshThumbPosition() {
        val thumbHalfWidth = measuredWidth * 0.055f
        val thumbHeight = measuredHeight * 0.58f
        val centerX = (progress / maximumValue) * width
        thumbIcon?.bounds = Rect((centerX - thumbHalfWidth).roundToInt(), 0, (centerX + thumbHalfWidth).roundToInt(), thumbHeight.roundToInt())
    }

}


fun MotionEvent.isRequiredMotionEvent(): Boolean {
    return action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_MOVE
}