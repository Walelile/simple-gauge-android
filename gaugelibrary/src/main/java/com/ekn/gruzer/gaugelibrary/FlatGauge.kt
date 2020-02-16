/*******************************************************************************
 * Copyright 2018 Evstafiev Konstantin
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ekn.gruzer.gaugelibrary

import android.content.Context
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.view.View
import java.util.*

class FlatGauge: View {
    private val ranges: ArrayList<Range> = ArrayList()
    var gaugeValue = 0.0
    var minValue = 0.0
    var maxValue = 100.0

    private lateinit var needlePaint: Paint
    private lateinit var backGroundPaint: Paint
    private lateinit var textPaint: Paint
    private lateinit var rectF: RectF

    private val TAG = "FlatGauge"
    private val needleStart = 30f
    private val needleEnd = 150f
    private var currentAngle = 30f
    private val startValue = 0f
    private val valueRange = 100f
    private var needleAngleNext: Int? = null

    private var fgPaddingLeft = 0f
    private var fgPaddingTop = 0f
    private var fgPaddingRight = 0f
    private var fgPaddingBottom = 0f

    //private val handler = Handler()
    var isEnableBackGroundShadow = false
    var isEnableNeedleShadow = false
    //private var rectF: RectF? = null


    private val runnable = Runnable {
        invalidate()
    }

    constructor(context: Context?) : super(context) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        parseAttr(attrs)
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        parseAttr(attrs)
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        parseAttr(attrs)
        init()
    }

    private fun init() {
        // Lib setting
        rectF = RectF(
                0 + fgPaddingLeft,
                0 + fgPaddingTop,
                measuredWidth - fgPaddingRight,
                measuredHeight - fgPaddingBottom)

        backGroundPaint = Paint()
        backGroundPaint.setColor(Color.parseColor("#EAEAEA"))
        backGroundPaint.setAntiAlias(true)
        backGroundPaint.style = Paint.Style.FILL

        needlePaint = Paint()
        needlePaint.setColor(Color.BLACK)
        needlePaint.setAntiAlias(true)
        needlePaint.setStyle(Paint.Style.FILL_AND_STROKE)
        needlePaint.setStrokeWidth(5f)

        //add BG Shadow (high CPU usage)
        //drawShadow();

    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        rectF.right = measuredWidth - fgPaddingRight
        rectF.bottom = measuredHeight - fgPaddingBottom
    }


    private fun parseAttr(attrs: AttributeSet?) {
        attrs?.let {
            val padTop = context.obtainStyledAttributes(
                    attrs, R.styleable.FlatGauge, 0, 0).apply {
                try {
                    fgPaddingLeft = getDimensionPixelSize(R.styleable.FlatGauge_flatg_paddingLeft, 0).toFloat()
                    fgPaddingTop = getDimensionPixelSize(R.styleable.FlatGauge_flatg_paddingTop, 0).toFloat()
                    fgPaddingRight = getDimensionPixelSize(R.styleable.FlatGauge_flatg_paddingRight, 0).toFloat()
                    fgPaddingBottom = getDimensionPixelSize(R.styleable.FlatGauge_flatg_paddingBottom, 0).toFloat()

                } finally {
                    recycle()
                }
            }
        }
    }

    private fun dpToPx(dp: Float): Float {
        return (dp * Resources.getSystem().displayMetrics.density)
    }

    private fun drawShadow() {
        if (isEnableBackGroundShadow) {
            backGroundPaint.setShadowLayer(15.0f, 0f, 5.0f, 0X50000000)
            setLayerType(View.LAYER_TYPE_SOFTWARE, backGroundPaint)
        }
        if (isEnableNeedleShadow) { //add Needle Shadow
            needlePaint.setShadowLayer(10f, 0f, 5.0f, 0X50000000)
            setLayerType(View.LAYER_TYPE_SOFTWARE, needlePaint)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (canvas == null) return

        //Add shadow
        drawShadow()


        // draw Ranges
        canvas.save()
        //canvas.translate(dx, dy)
        //canvas.scale(scaleRatio, scaleRatio)
        drawRange(canvas)
        canvas.restore()


        // Draw the needle
        canvas.save()

        //canvas.translate(dx, dy)
        //canvas.scale(scaleRatio, scaleRatio)
        //canvas.rotate(getNeedleAngle(), getRectRight() / 2f, getRectBottom() / 2f);
        val lineX = rectF.left + gaugeValue.toFloat() / valueRange * rectInnerWidth
        val ovalY = 230f
        canvas.drawLine(
                lineX,
                rectF.top,
                lineX,
                rectF.bottom,
                needlePaint)
        val ovalRadius = 10f
        canvas.drawOval(
                lineX - ovalRadius,
                ovalY - ovalRadius,
                lineX + ovalRadius,
                ovalY + ovalRadius,
                needlePaint)
        canvas.restore()


        //draw Text Value
        drawValueText(canvas)
        //drawMinValue
        drawMinValue(canvas)
        //drawMaxValue
        drawMaxValue(canvas)
    }

    private fun drawValueText(canvas: Canvas) {
        //TODO canvas.save()
        //TODO canvas.translate(width / 2f - rectRight / 2f * scaleRatio, height / 2f - 50f * scaleRatio)
        //TODO canvas.scale(scaleRatio, scaleRatio)
        //TODO canvas.drawText(value.toString() + "", 200f, 240f, textPaint)
        //TODO canvas.restore()
    }

    private fun drawMinValue(canvas: Canvas) {
        //TODO canvas.save()
        //TODO canvas.translate(width / 2f - rectRight / 2f * scaleRatio, height / 2f - 50f * scaleRatio)
        //TODO canvas.scale(scaleRatio, scaleRatio)
        //TODO canvas.rotate(26f, 10f, 130f)
        //TODO canvas.drawText(minValue.toString() + "", 10f + padding, 130f, rangeValue)
        //TODO canvas.restore()
    }

    private fun drawMaxValue(canvas: Canvas) {
        //TODO canvas.save()
        //TODO canvas.translate(width / 2f - rectRight / 2f * scaleRatio, height / 2f - 50f * scaleRatio)
        //TODO canvas.scale(scaleRatio, scaleRatio)
        //TODO canvas.rotate(-26f, 390f, 130f)
        //TODO canvas.drawText(maxValue.toString() + "", 390f - padding, 130f, rangeValue)
        //TODO canvas.restore()
    }

    private fun drawRange(canvas: Canvas) {
        Log.d(TAG, "rect: $rectF")
        for (range in ranges) {
            val startOffset = calculateStartOffset(range.from)
            val endOffset = calculateEndOffset(range.to)
            Log.d(TAG, "start off: $startOffset")
            Log.d(TAG, "end off: $endOffset")
            //canvas.drawArc(getRectF(), startAngle, sweepAngle, false, getRangePaint(range.getColor()));
//canvas.drawRect(getRectF(), getRangePaint(range.getColor()));
            canvas.drawRect(rectF.left, rectF.top, endOffset, rectF.bottom,
                    getRangePaint(range.color))
        }
    }

    /**
     * Padding is removed from this width
     * @return
     */
    private val rectInnerWidth: Float
        private get() = rectF.right - rectF.left

    private val rectInnerLeft: Float
        private get() = rectF.left

    private fun calculateStartOffset(from: Double): Float {
        return (rectInnerLeft + (from - startValue) / valueRange * rectInnerWidth).toFloat()
    }

    private fun calculateEndOffset(to: Double): Float {
        Log.d(TAG, "to: $to, inner width: $rectInnerWidth")
        return (rectInnerLeft + (to - startValue) / valueRange * rectInnerWidth).toFloat()
    }

    //TODO val needleAngle: Int
    //TODO     get() {
    //TODO         if (needleAngleNext != null) {
    //TODO             if (needleAngleNext!!.toFloat() != currentAngle) {
    //TODO                 if (needleAngleNext!! < currentAngle) currentAngle-- else currentAngle++
    //TODO                 handler.postDelayed(runnable, 5)
    //TODO             }
    //TODO         } else {
    //TODO             currentAngle = (needleEnd - needleStart) / 100 * calculateValuePercentage + needleStart
    //TODO         }
    //TODO         return currentAngle.toInt()
    //TODO     }

    fun setValue(value: Double) {
        //TODO super.setValue(value)
        //TODO needleAngleNext = ((needleEnd - needleStart) / 100 * calculateValuePercentage + needleStart).toInt()
    }

    protected fun getRangePaint(color: Int): Paint {
        val range = Paint()
        range.color = color
        range.isAntiAlias = true
        range.style = Paint.Style.FILL
        range.strokeWidth = backGroundPaint.strokeWidth
        return range
    }

    protected val rangeValue: Paint
        protected get() {
            val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
            textPaint.color = Color.GRAY
            textPaint.style = Paint.Style.FILL
            textPaint.textSize = 15f
            textPaint.textAlign = Paint.Align.CENTER
            return textPaint
        }


    fun addRange(range: Range?) {
        if (range == null) return
        ranges.add(range)
    }

}