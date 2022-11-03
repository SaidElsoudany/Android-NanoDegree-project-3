package com.udacity

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.Button
import androidx.core.content.withStyledAttributes
import kotlin.properties.Delegates

private const val CIRCLE_DIAMETER = 60
private const val CIRCLE_MARGIN = 20

class LoadingButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var widthSize = 0
    private var heightSize = 0
    private var buttonText = resources.getText(R.string.button_name);
    private var animatedValueF = 0f
    private lateinit var rect: Rect
    private var bounds = Rect()
    private var downloadingTextBound = Rect()
    private var textOrigin = PointF(0f, 0f)
    private var circleOrigin = PointF(0f, 0f)
    private var circleColor = Color.YELLOW
    private var textColor = Color.WHITE
    private var primaryColor = 0
    private var primaryDarkColor = 0

    private val valueAnimator = ValueAnimator.ofFloat(0f, 1f).setDuration(1000).apply {
        addUpdateListener {
            animatedValueF = it.animatedValue as Float
            invalidate()
        }
        interpolator = LinearInterpolator()
        repeatCount = ValueAnimator.INFINITE
        repeatMode = ValueAnimator.RESTART
    }

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = 55.0f
        color = Color.WHITE
        typeface = Typeface.create("", Typeface.BOLD)
    }


    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        rect = Rect(0, 0, 0, h)
        paint.getTextBounds("a", 0, 1, bounds)
        paint.getTextBounds(
            resources.getString(R.string.button_loading),
            0,
            resources.getString(R.string.button_loading).length,
            downloadingTextBound
        )
        textOrigin.computeTextOrigin(bounds, width, height)
        circleOrigin.computeCircleOrigin(downloadingTextBound, width, height)
        primaryColor = resources.getColor(R.color.colorPrimary)
        primaryDarkColor = resources.getColor(R.color.colorPrimaryDark)

    }


    var buttonState: ButtonState by Delegates.observable<ButtonState>(ButtonState.IDLE) { _, _, new ->
        isEnabled = when (new) {
            ButtonState.IDLE -> {
                buttonText = resources.getString(R.string.button_name)
                valueAnimator.cancel()
                true
            }
            ButtonState.DOWNLOADING -> {
                buttonText = resources.getString(R.string.button_loading)
                valueAnimator.start()
                false
            }
        }
        invalidate()
    }


    init {
        context.withStyledAttributes(attrs, R.styleable.LoadingButton) {
            circleColor = getColor(R.styleable.LoadingButton_circleColor, 0)
            textColor = getColor(R.styleable.LoadingButton_textColor, 0)

        }
    }


    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.drawColor(primaryColor)
        if (buttonState == ButtonState.DOWNLOADING) {
            paint.color = primaryDarkColor
            rect.right = (animatedValueF * width).toInt()
            canvas?.drawRect(rect, paint)
            paint.color = circleColor
            canvas?.drawArc(
                circleOrigin.x,
                circleOrigin.y,
                circleOrigin.x + CIRCLE_DIAMETER,
                circleOrigin.y + CIRCLE_DIAMETER,
                -360f,
                360 * animatedValueF,
                false,
                paint
            )
        }
        paint.color = textColor
        canvas?.drawText(buttonText.toString(), textOrigin.x, textOrigin.y, paint)
    }

    private fun PointF.computeTextOrigin(bounds: Rect, viewWidth: Int, viewHeight: Int) {
        x = (viewWidth / 2).toFloat()
        y = (viewHeight + bounds.height()) / 2.toFloat()
    }

    private fun PointF.computeCircleOrigin(bounds: Rect, viewWidth: Int, viewHeight: Int) {
        x = (viewWidth / 2).toFloat() + bounds.width() / 2 + CIRCLE_MARGIN
        y = (viewHeight / 2).toFloat() - CIRCLE_DIAMETER / 2
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minw: Int = paddingLeft + paddingRight + suggestedMinimumWidth
        val w: Int = resolveSizeAndState(minw, widthMeasureSpec, 1)
        val h: Int = resolveSizeAndState(
            MeasureSpec.getSize(w),
            heightMeasureSpec,
            0
        )
        widthSize = w
        heightSize = h
        setMeasuredDimension(w, h)
    }

}