package com.ss.color_picker.color_view

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.ss.color_picker.R
import com.ss.color_picker.databinding.ColorViewBinding
import kotlin.math.roundToInt


class ColorView : ConstraintLayout {

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    private val binding by lazy { ColorViewBinding.inflate(LayoutInflater.from(context), this) }

    var color: Int = Color.RED
    var onChangeListener: ((color: Int) -> Unit)? = null
    private var lastHue = 0f

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        setMarginBetweenBars()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        setMarginBetweenBars()
    }

    private fun init() {
        binding.root
        binding.seekBarHue.maximumValue = 359f
        binding.seekBarHue.trackImage = ContextCompat.getDrawable(context, R.drawable.bg_color_hue)
        binding.seekBarHue.onProgressChange = ::onHueChange
        binding.seekBarSaturationLightness.maximumValue = Pair(1f, 1f)
        binding.seekBarSaturationLightness.onProgressChange = ::onSaturationLightnessChange
        binding.seekBarAlpha.maximumValue = 255f
        binding.seekBarAlpha.onProgressChange = ::onAlphaChange
        changeColor(Color.BLUE)
    }

    private fun changeColor(color: Int) {
        this.color = color
        lastHue = color.hue()
        binding.seekBarHue.progress = lastHue
        binding.seekBarSaturationLightness.setHue(lastHue)
        binding.seekBarSaturationLightness.progress = Pair(color.saturation(), color.lightness())
        binding.seekBarAlpha.trackImage = getAlphaDrawable(color.hsv())
        binding.seekBarAlpha.progress = color.alpha().toFloat()
    }

    private fun setColorInternally(color: Int) {
        this.color = color
        onChangeListener?.invoke(color)
    }

    /**minimum value = 0f maximum value = 360f*/
    private fun onHueChange(hue360: Float) {
        lastHue = hue360
        val hsv = floatArrayOf(hue360, color.saturation(), color.lightness())
        binding.seekBarSaturationLightness.setHue(hue360)
        binding.seekBarAlpha.trackImage = getAlphaDrawable(hsv)
        setColorInternally(Color.HSVToColor(color.alpha(), hsv))
    }

    /**minimum value = 0f maximum value = 1f*/
    private fun onSaturationLightnessChange(saturationLightness: Pair<Float, Float>) {
        val hsv = floatArrayOf(lastHue, saturationLightness.first, saturationLightness.second)
        binding.seekBarAlpha.trackImage = getAlphaDrawable(hsv)
        val color = Color.HSVToColor(color.alpha(), hsv)
        setColorInternally(color)
    }

    /**minimum value = 0f maximum value = 255f*/
    private fun onAlphaChange(alphaHex: Float) {
        setColorInternally((alphaHex.roundToInt() shl 24) or (color and 0x00FFFFFF))
    }

    private fun getAlphaDrawable(hsv: FloatArray) = GradientDrawable(
        GradientDrawable.Orientation.LEFT_RIGHT,
        intArrayOf(Color.TRANSPARENT, Color.HSVToColor(hsv)),
    )

    private fun setMarginBetweenBars() {
        val params = binding.seekBarSaturationLightness.layoutParams
        if (params !is LayoutParams) return
        val marginTop = (binding.seekBarAlpha.measuredHeight * 0.333f).roundToInt()
        val marginBottom = (binding.seekBarAlpha.measuredHeight * 0.08333f).roundToInt()
        params.setMargins(0, marginTop, 0, marginBottom)
        binding.seekBarSaturationLightness.layoutParams = params
    }

    fun setAlphaPercent(alphaPercent: Float) = onAlphaChange(((alphaPercent / 100f) * 255f).coerceIn(0f, 255f))


    /** ----------------- EXTENSIONS ----------------- **/

    private fun Int.hsv(): FloatArray {
        val hsv = FloatArray(3)
        Color.colorToHSV(this, hsv)
        return hsv
    }

    private fun Int.hue(): Float = this.hsv()[0]
    private fun Int.saturation(): Float = this.hsv()[1]
    private fun Int.lightness(): Float = this.hsv()[2]
    private fun Int.alpha(): Int = Color.alpha(this)
}
