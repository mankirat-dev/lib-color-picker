package com.ss.color_picker

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import com.google.android.material.textfield.TextInputEditText
import com.ss.color_picker.databinding.ColorPickerViewBinding
import kotlin.math.abs
import kotlin.math.roundToInt

class ColorPickerView : ConstraintLayout {

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    //Inflate xml resource, pass "this" as the parent, we use <merge> tag in xml to avoid
    //redundant parent, otherwise a LinearLayout will be added to this LinearLayout ending up
    //with two view groups
    private val binding by lazy { ColorPickerViewBinding.inflate(LayoutInflater.from(context), this) }

    var is3D: Boolean = false
        set(value) {
            field = value
            binding.btnSingleColor.isVisible = !is3D
            binding.group3DButtons.visibility = if (is3D) View.VISIBLE else View.INVISIBLE
            refreshColorUI()
        }

    var singleColor: Int = Color.WHITE
        set(value) {
            field = value
            binding.btnSingleColor.color = singleColor
            refreshColorUI(singleColor)
        }
    var startColor: Int = Color.WHITE
        set(value) {
            field = value
            binding.btnStartColor.color = startColor
            refreshColorUI(startColor)
        }
    var centerColor: Int = Color.WHITE
        set(value) {
            field = value
            binding.btnCenterColor.color = centerColor
            refreshColorUI(centerColor)
        }
    var endColor: Int = Color.WHITE
        set(value) {
            field = value
            binding.btnEndColor.color = endColor
            refreshColorUI(endColor)
        }

    private var oldIs3DSupported: Boolean = true
    private var oldIs3D: Boolean = is3D
    private var oldSingleColor: Int = singleColor
    private var oldStartColor: Int = startColor
    private var oldCenterColor: Int = centerColor
    private var oldEndColor: Int = endColor

    var onChangeListener: ((is3D: Boolean, singleColor: Int, startColor: Int, centerColor: Int, endColor: Int) -> Unit)? = null

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        binding.colorView.onChangeListener = {
            changeSelectedColor(it)
        }
        binding.switch3D.setOnCheckedChangeListener { _, value -> is3D = value }
        binding.btnReset.setOnClickListener { initialSetUp(oldIs3DSupported, oldIs3D, oldSingleColor, oldStartColor, oldCenterColor, oldEndColor) }
        binding.btnStartColor.setOnClickListener(::setSelectedColor)
        binding.btnCenterColor.setOnClickListener(::setSelectedColor)
        binding.btnEndColor.setOnClickListener(::setSelectedColor)
    }

    override fun onDetachedFromWindow() {
        binding.colorView.onChangeListener = null
        binding.switch3D.setOnCheckedChangeListener(null)
        binding.btnReset.setOnClickListener(null)
        binding.btnStartColor.setOnClickListener(null)
        binding.btnCenterColor.setOnClickListener(null)
        binding.btnEndColor.setOnClickListener(null)
        super.onDetachedFromWindow()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val params = binding.btnCenterColor.layoutParams
        if (params !is LayoutParams) return
        val margin = (binding.btnCenterColor.measuredHeight / 10f).roundToInt()
        params.setMargins(0, margin, 0, margin)

    }

    private fun init() {
        binding.root
        binding.btnSingleColor.isActive = true

        binding.etHexCode.setOnEditorActionListener(::onHexCodeEditingDone)
        binding.etHexCode.doOnTextChanged { text, _, before, count ->
            val isUserChange = abs(count - before) == 1
            if (isUserChange) {
                try {
                    changeSelectedColor(Color.parseColor("#$text"))
                } catch (_: IllegalArgumentException) {

                }
            }

        }
        binding.etAlpha.doOnTextChanged { text, start, before, count ->
            if (text.isNullOrBlank()) return@doOnTextChanged
            val alphaPercent: Float
            try {
                alphaPercent = java.lang.Float.valueOf(text.toString())
            } catch (e: NumberFormatException) {
                e.printStackTrace()
                return@doOnTextChanged
            }

            binding.colorView.setAlphaPercent(alphaPercent)
        }
        initialSetUp()
    }

    private fun initialSetUp(
        is3DSupported: Boolean = true, is3D: Boolean = true, singleColor: Int = Color.BLACK, startColor: Int = Color.BLACK, centerColor: Int = Color.BLACK, endColor: Int = Color.BLACK
    ) {
        oldIs3DSupported = is3DSupported
        oldIs3D = is3D
        oldSingleColor = singleColor
        oldStartColor = startColor
        oldCenterColor = centerColor
        oldEndColor = endColor

        binding.switch3D.isVisible = is3DSupported
        binding.switch3D.isChecked = is3DSupported && is3D
        setSelectedColor(binding.btnStartColor)
        this.is3D = is3DSupported && is3D
        this.singleColor = singleColor
        this.startColor = startColor
        this.centerColor = centerColor
        this.endColor = endColor
    }

    private fun shouldConsumeEvent(actionId: Int, event: KeyEvent?): Boolean {
        val isSearchOrDoneAction = actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE
        val isEnterKeyDown = event != null && event.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_ENTER
        val isShiftNotPressed = event == null || !event.isShiftPressed
        return (isSearchOrDoneAction || isEnterKeyDown) && isShiftNotPressed
    }

    private fun onHexCodeEditingDone(textView: TextView, actionId: Int, event: KeyEvent?): Boolean {
        return if (shouldConsumeEvent(actionId, event)) {
            binding.tilHexCode.clearFocus()
            true
        } else true
    }

    private fun getSelectedColor(): Int {
        return if (is3D) {
            when {
                binding.btnStartColor.isActive -> startColor
                binding.btnCenterColor.isActive -> centerColor
                binding.btnEndColor.isActive -> endColor
                else -> startColor
            }
        } else {
            singleColor
        }
    }

    private fun setSelectedColor(view: View) {
        if (view !is ColorButton) return
        binding.btnStartColor.isActive = false
        binding.btnCenterColor.isActive = false
        binding.btnEndColor.isActive = false
        view.isActive = true
        refreshColorUI()
    }

    private fun changeSelectedColor(color: Int) {
        if (is3D) {
            when {
                binding.btnStartColor.isActive -> startColor = color
                binding.btnCenterColor.isActive -> centerColor = color
                binding.btnEndColor.isActive -> endColor = color
                else -> startColor = color
            }
        } else {
            singleColor = color
        }
    }


    private fun refreshColorUI(color: Int = getSelectedColor()) {
        if (!binding.etHexCode.isKeyboardOpened()) binding.etHexCode.setText(String.format("%08X", color))
        val alphaPercent = (color.alpha() / 255f) * 100f
        //binding.etAlpha.setText(String.format("%.2f", alphaPercent))
        onChangeListener?.invoke(is3D, singleColor, startColor, centerColor, endColor)
    }

    /** ----------------- EXTENSIONS ----------------- **/

    private fun TextInputEditText.isKeyboardOpened(): Boolean = context.getSystemService(InputMethodManager::class.java).isActive((this as View))

    private fun Int.alpha(): Int = Color.alpha(this)
}