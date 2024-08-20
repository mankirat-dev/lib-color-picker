package com.ss.color_picker.color_view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ComposeShader
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.Rect
import android.graphics.Shader
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import com.ss.color_picker.R
import kotlin.math.roundToInt

class HorizontalVerticalBar : View, View.OnTouchListener {

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

    var maximumValue: Pair<Float, Float> = Pair(100f, 100f)
        set(value) {
            field = value
            refreshThumbPosition()
            invalidate()
        }
    var progress: Pair<Float, Float> = Pair(100f, 100f)
        set(value) {
            field = value.coerceIn(Pair(0f, 0f), maximumValue)
            refreshThumbPosition()
            invalidate()
        }

    private val thumbIcon = ContextCompat.getDrawable(context, R.drawable.color_thumb_circle)
    var onProgressChange: ((progressValue: Pair<Float, Float>) -> Unit)? = null
    private val paint: Paint = Paint()

    override fun onSizeChanged(width: Int, height: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(width, height, oldw, oldh)
        refreshThumbPosition()
        //Because onDraw function is called after the onSizeChanged that's why I did not called invalidate() here.
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawRect(0f, 0f, measuredWidth.toFloat(), measuredHeight.toFloat(), paint)
        setLayerType(LAYER_TYPE_SOFTWARE, paint)
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
        val totalHeight = view.measuredHeight
        val x = motionEvent.x
        val y = motionEvent.y
        progress = Pair((x / totalWidth) * maximumValue.first, (1f - (y / totalHeight) * maximumValue.second))
        onProgressChange?.invoke(Pair(progress.first, progress.second))
        return true
    }

    private fun refreshThumbPosition() {
        val thumbHalfSize = measuredWidth * 0.05f
        val centerX = (progress.first / maximumValue.first) * measuredWidth
        val centerY = (1f - (progress.second / maximumValue.second)) * measuredHeight
        thumbIcon?.bounds = Rect(
            (centerX - thumbHalfSize).roundToInt(),
            (centerY - thumbHalfSize).roundToInt(),
            (centerX + thumbHalfSize).roundToInt(),
            (centerY + thumbHalfSize).roundToInt(),
        )
    }

    /**
     * x0=0, y0=0, x1=0, y1=height (these are coordinates, assume similar to graph).
     * Color.WHITE is the start color (at (x0,y0)).
     * Color.BLACK is the end color (at (x1,y1)).
     * In between there is a vertical linear gradient.
     * (Vertical gradient is not needed to be initialized again as its value is same for every rgb)
     *
     *
     * x0=0, y0=0, x1=width, y1=0 (these are coordinates, assume similar to graph).
     * Color.WHITE is the start color (at (x0,y0)).
     * rgbValue is the end color (at (x1,y1)).
     * In between there is a horizontal linear gradient.
     */
    fun setHue(hue360: Float) {
        val rgbValue = Color.HSVToColor(floatArrayOf(hue360, 1f, 1f)) // hue (0-360), saturation (0-1), value (0-1)
        val verticalShader = LinearGradient(0f, 0f, 0f, measuredHeight.toFloat(), Color.WHITE, Color.BLACK, Shader.TileMode.CLAMP)
        val horizontalShader = LinearGradient(0f, 0f, measuredWidth.toFloat(), 0f, Color.WHITE, rgbValue, Shader.TileMode.CLAMP)
        val composeShader = ComposeShader(verticalShader, horizontalShader, PorterDuff.Mode.MULTIPLY)
        paint.shader = composeShader
        invalidate()
    }


}

fun Pair<Float, Float>.coerceIn(minimumValue: Pair<Float, Float>, maximumValue: Pair<Float, Float>): Pair<Float, Float> =
    Pair(this.first.coerceIn(minimumValue.first, maximumValue.first), this.second.coerceIn(minimumValue.second, maximumValue.second))
