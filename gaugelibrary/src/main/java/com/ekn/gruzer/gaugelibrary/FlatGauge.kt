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

    // paint
    private lateinit var needlePaint: Paint
    private lateinit var backGroundPaint: Paint
    private lateinit var valueTextPaint: Paint
    private lateinit var minValTextPaint: Paint
    private lateinit var maxValTextPaint: Paint
    private lateinit var msgTextPaint: Paint
    private lateinit var rectF: RectF

    private val TAG = "FlatGauge"

    private val startValue = 0f
    private val valueRange = 100f
    private var needleAngleNext: Int? = null

    // XML attrs
    private var fgPaddingLeft = 0f
    private var fgPaddingTop = 0f
    private var fgPaddingRight = 0f
    private var fgPaddingBottom = 0f
    private var fgValueTextSize = 0f
    private var fgMinValTextSize = 0f
    private var fgMaxValTextSize = 0f
    private var fgValueTextColor: Int = Color.BLACK
    private var fgMinValTextColor: Int = Color.BLACK
    private var fgMaxValTextColor: Int = Color.BLACK
    private var fgMsgTextColor: Int = Color.BLACK
    private var fgMsgTextSize = 0f
    private var fgMsgText = ""
    private var fgMsgTextRegionHeight = 0f
    private var fgValueTextRegionHeight = 0f
    private var fgNeedleBottomOffset = 0f
    private var fgNeedleOvalRadius = 5f // dp

    // drawing parameters
    private var rangeDrawBottom = 0f
    private var rangeDrawTop = 0f
    private var needleDrawBottomY = 0f
    private var needleDrawTopY = 0f
    //private var needleBottomOffset = 10f // dp, target.Y.px - rectF.bottom
    private var valueTextYPx = 10f // px, value text Y pos
    private var msgTextYPx = 10f // px, value text Y pos
    private var gaugeValueTextXPx = 10f
    private var minValueTextXPx = 10f
    private var maxValueTextXPx = 10f

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

    private fun parseAttr(attrs: AttributeSet?) {
        attrs?.let {
            val padTop = context.obtainStyledAttributes(
                    attrs, R.styleable.FlatGauge, 0, 0).apply {
                try {
                    fgPaddingLeft = getDimensionPixelSize(R.styleable.FlatGauge_flatg_paddingLeft, 0).toFloat()
                    fgPaddingTop = getDimensionPixelSize(R.styleable.FlatGauge_flatg_paddingTop, 0).toFloat()
                    fgPaddingRight = getDimensionPixelSize(R.styleable.FlatGauge_flatg_paddingRight, 0).toFloat()
                    fgPaddingBottom = getDimensionPixelSize(R.styleable.FlatGauge_flatg_paddingBottom, 0).toFloat()
                    fgValueTextSize = getDimensionPixelSize(R.styleable.FlatGauge_flatg_valueTextSize, 30).toFloat()
                    fgMinValTextSize = getDimensionPixelSize(R.styleable.FlatGauge_flatg_minValueTextSize, 30).toFloat()
                    fgMaxValTextSize = getDimensionPixelSize(R.styleable.FlatGauge_flatg_maxValueTextSize, 30).toFloat()
                    fgMsgTextSize = getDimensionPixelSize(R.styleable.FlatGauge_flatg_msgTextSize, 30).toFloat()

                    fgNeedleBottomOffset = getDimensionPixelSize(
                            R.styleable.FlatGauge_flatg_needleBottomOffset, 0).toFloat()

                    fgValueTextColor = getColor(R.styleable.FlatGauge_flatg_valueTextColor, Color.BLACK)
                    fgMinValTextColor = getColor(R.styleable.FlatGauge_flatg_minValueTextColor, Color.BLACK)
                    fgMaxValTextColor = getColor(R.styleable.FlatGauge_flatg_maxValueTextColor, Color.BLACK)
                    fgMsgTextColor = getColor(R.styleable.FlatGauge_flatg_msgTextColor, Color.BLACK)

                    fgMsgTextRegionHeight = getDimensionPixelSize(
                            R.styleable.FlatGauge_flatg_msgTextRegionHeight, fgMsgTextSize.toInt()).toFloat()
                    fgValueTextRegionHeight = getDimensionPixelSize(
                            R.styleable.FlatGauge_flatg_valueTextRegionHeight, fgValueTextSize.toInt()).toFloat()

                    getString(R.styleable.FlatGauge_flatg_valueMsg)?.let {
                        fgMsgText = it
                    }
                } finally {
                    recycle()
                }
            }
        }
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

        valueTextPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        valueTextPaint.color = fgValueTextColor
        valueTextPaint.style = Paint.Style.FILL
        valueTextPaint.textSize = fgValueTextSize
        valueTextPaint.textAlign = Paint.Align.CENTER

        minValTextPaint = Paint()
        minValTextPaint.color = fgMinValTextColor
        minValTextPaint.style = Paint.Style.FILL
        minValTextPaint.textSize = fgMinValTextSize
        minValTextPaint.textAlign = Paint.Align.CENTER

        maxValTextPaint = Paint()
        maxValTextPaint.color = fgMaxValTextColor
        maxValTextPaint.style = Paint.Style.FILL
        maxValTextPaint.textSize = fgMaxValTextSize
        maxValTextPaint.textAlign = Paint.Align.CENTER

        msgTextPaint = Paint()
        msgTextPaint.color = fgMsgTextColor
        msgTextPaint.style = Paint.Style.FILL
        msgTextPaint.textSize = fgMsgTextSize
        msgTextPaint.textAlign = Paint.Align.CENTER

        //add BG Shadow (high CPU usage)
        //drawShadow();

    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        rectF.right = measuredWidth - fgPaddingRight
        rectF.bottom = measuredHeight - fgPaddingBottom

        gaugeValueTextXPx = (rectF.left + rectF.right)*0.5f
        minValueTextXPx = rectF.left    // TODO: consider text length
        maxValueTextXPx = rectF.right // TODO: consider text length
        valueTextYPx = rectF.bottom - fgMsgTextRegionHeight // TODO: consider text length
        msgTextYPx = rectF.bottom
        rangeDrawBottom = rectF.bottom - fgMsgTextRegionHeight - fgValueTextRegionHeight
        rangeDrawTop = rectF.top
        needleDrawBottomY = rangeDrawBottom - fgNeedleBottomOffset
        needleDrawTopY = rangeDrawTop
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
        val lineX = calculateStartOffset(gaugeValue)
        val ovalY = needleDrawBottomY
        canvas.drawLine(
                lineX,
                needleDrawTopY,
                lineX,
                needleDrawBottomY,
                needlePaint)
        val ovalRadius = fgNeedleOvalRadius
        canvas.drawOval(
                lineX - ovalRadius,
                needleDrawBottomY - ovalRadius,
                lineX + ovalRadius,
                needleDrawBottomY + ovalRadius,
                needlePaint)
        canvas.restore()


        //draw Text Value
        drawValueText(canvas)
        //drawMinValue
        drawMinValue(canvas)
        //drawMaxValue
        drawMaxValue(canvas)
        //draw msg
        drawMsg(canvas)
    }

    private fun drawValueText(canvas: Canvas) {
        canvas.save()
        //canvas.translate(width / 2f - rectRight / 2f * scaleRatio, height / 2f - 50f * scaleRatio)
        //canvas.scale(scaleRatio, scaleRatio)
        canvas.drawText(gaugeValue.toString(), gaugeValueTextXPx, valueTextYPx, valueTextPaint)
        canvas.restore()
    }

    private fun drawMsg(canvas: Canvas) {
        if (fgMsgText.isNotBlank()) {
            canvas.save()
            //canvas.translate(width / 2f - rectRight / 2f * scaleRatio, height / 2f - 50f * scaleRatio)
            //canvas.scale(scaleRatio, scaleRatio)
            //canvas.drawText(fgMsgText, gaugeValueTextXPx, valueTextYPx+100, msgTextPaint)
            canvas.drawText(fgMsgText, gaugeValueTextXPx, msgTextYPx, msgTextPaint)
            canvas.restore()
        }
    }

    private fun drawMinValue(canvas: Canvas) {
        canvas.save()
        //canvas.translate(width / 2f - rectRight / 2f * scaleRatio, height / 2f - 50f * scaleRatio)
        //canvas.scale(scaleRatio, scaleRatio)
        //canvas.rotate(26f, 10f, 130f)
        canvas.drawText(minValue.toString(), minValueTextXPx, valueTextYPx, minValTextPaint)
        canvas.restore()
    }

    private fun drawMaxValue(canvas: Canvas) {
        canvas.save()
        //canvas.translate(width / 2f - rectRight / 2f * scaleRatio, height / 2f - 50f * scaleRatio)
        //canvas.scale(scaleRatio, scaleRatio)
        //canvas.rotate(-26f, 390f, 130f)
        canvas.drawText(maxValue.toString() + "", maxValueTextXPx, valueTextYPx, maxValTextPaint)
        canvas.restore()
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
            canvas.drawRect(startOffset, rangeDrawTop, endOffset, rangeDrawBottom,
                    getRangePaint(range.color))
        }
    }

    /**
     * Padding is removed from this width
     * @return
     */
    private val rectInnerWidth: Float
        private get() = rectF.right - rectF.left


    private fun calculateStartOffset(from: Double): Float {
        return (rectF.left + (from - startValue) / valueRange * rectInnerWidth).toFloat()
    }

    private fun calculateEndOffset(to: Double): Float {
        return (rectF.left + (to - startValue) / valueRange * rectInnerWidth).toFloat()
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

    //TMP protected val rangeValuePaint: Paint
    //TMP     protected get() {
    //TMP         val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    //TMP         textPaint.color = Color.GRAY
    //TMP         textPaint.style = Paint.Style.FILL
    //TMP         textPaint.textSize = 15f
    //TMP         textPaint.textAlign = Paint.Align.CENTER
    //TMP         return textPaint
    //TMP     }


    fun addRange(range: Range?) {
        if (range == null) return
        ranges.add(range)
    }

}