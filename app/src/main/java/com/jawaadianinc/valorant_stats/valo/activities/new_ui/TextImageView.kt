package com.jawaadianinc.valorant_stats.valo.activities.new_ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView


class TextImageView : AppCompatTextView {
    private var paint: Paint? = null

    constructor(context: Context?) : super(context!!) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(
        context!!, attrs
    ) {
        init()
    }

    private fun init() {
        paint = Paint()
        paint!!.color = Color.BLACK // Set the text color
        paint!!.textSize = textSize
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawText(text.toString(), 0f, baseline.toFloat(), paint!!)
    }
}
