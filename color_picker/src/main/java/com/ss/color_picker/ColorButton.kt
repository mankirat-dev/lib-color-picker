package com.ss.color_picker

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.ss.color_picker.databinding.ColorButtonBinding

class ColorButton : ConstraintLayout {

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(attrs)
    }

    //Inflate xml resource, pass "this" as the parent, we use <merge> tag in xml to avoid
    //redundant parent, otherwise a LinearLayout will be added to this LinearLayout ending up
    //with two view groups
    private val binding by lazy { ColorButtonBinding.inflate(LayoutInflater.from(context), this) }

    var text: String? = null
        set(value) {
            field = value
            binding.tvTitle.text = text
        }

    var color: Int = Color.WHITE
        set(value) {
            field = value
            binding.viewColor.setBackgroundColor(color)
        }

    var isActive: Boolean = false
        set(value) {
            field = value
            setBackgroundResource(if (isActive) R.drawable.bg_color_button_selected else R.drawable.bg_color_button_unselected)
        }

    private fun init(attrs: AttributeSet? = null) {
        binding.root
        initTypeface(attrs)
    }

    private fun initTypeface(attrs: AttributeSet?) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.ColorButton, 0, 0)
        try {
            text = typedArray.getString(R.styleable.ColorButton_color_button_text)
        } finally {
            typedArray.recycle()
        }
    }
}