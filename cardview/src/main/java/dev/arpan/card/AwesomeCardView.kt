package dev.arpan.card

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.annotation.Px
import androidx.core.content.ContextCompat
import androidx.core.content.res.use
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.RoundedCornerTreatment
import com.google.android.material.shape.ShapeAppearanceModel
import com.google.android.material.shape.ShapeAppearancePathProvider

@SuppressLint("RestrictedApi")
class AwesomeCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    @AttrRes defStyleAttr: Int = R.attr.awesomeCardViewStyle
) : FrameLayout(context, attrs, defStyleAttr) {

    private val pathProvider = ShapeAppearancePathProvider.getInstance()

    private val maskRect = RectF()
    private val maskPath = Path()
    private val borderPath = Path()

    private var cardBackgroundColor = Color.WHITE
    private var cornerSizeTopLeft = 0f
    private var cornerSizeTopRight = 0f
    private var cornerSizeBottomLeft = 0f
    private var cornerSizeBottomRight = 0f
    private var cardElevation = 0f
    private var shadowColor = Color.DKGRAY
    private var shadowDy = 0
    private var shadowDx = 0
    private var strokeColor = Color.TRANSPARENT
    private var strokeWidth = 0f

    private val clipPaint = paint {
        isAntiAlias = true
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    }
    private val borderPaint = paint {
        isAntiAlias = true
        style = Paint.Style.STROKE
    }
    private lateinit var shapeAppearanceModel: ShapeAppearanceModel
    private lateinit var shadowDrawable: MaterialShapeDrawable

    // used for showing preview in edit mode
    private lateinit var tempBitmap: Bitmap
    private lateinit var tempCanvas: Canvas

    init {
        setWillNotDraw(true)
        context.obtainStyledAttributes(attrs, R.styleable.AwesomeCardView, defStyleAttr, 0)
            .use { a ->
                cardBackgroundColor =
                    if (a.hasValue(R.styleable.AwesomeCardView_acv_backgroundColor)) {
                        a.getColor(R.styleable.AwesomeCardView_acv_backgroundColor, Color.WHITE)
                    } else {
                        if (isLightBackground()) {
                            ContextCompat.getColor(
                                context,
                                R.color.awesome_cardview_light_background
                            )
                        } else {
                            ContextCompat.getColor(
                                context,
                                R.color.awesome_cardview_dark_background
                            )
                        }
                    }
                val cornerSize =
                    a.getDimension(R.styleable.AwesomeCardView_acv_cornerSize, 0f)
                cornerSizeTopLeft =
                    a.getDimension(R.styleable.AwesomeCardView_acv_cornerSizeTopLeft, 0f)
                cornerSizeTopRight =
                    a.getDimension(R.styleable.AwesomeCardView_acv_cornerSizeTopRight, 0f)
                cornerSizeBottomLeft =
                    a.getDimension(R.styleable.AwesomeCardView_acv_cornerSizeBottomLeft, 0f)
                cornerSizeBottomRight =
                    a.getDimension(R.styleable.AwesomeCardView_acv_cornerSizeBottomRight, 0f)
                cardElevation =
                    a.getDimension(R.styleable.AwesomeCardView_acv_elevation, 0f)
                shadowColor = if (a.hasValue(R.styleable.AwesomeCardView_acv_shadowColor)) {
                    a.getColor(R.styleable.AwesomeCardView_acv_shadowColor, Color.DKGRAY)
                } else {
                    if (isLightBackground()) {
                        ContextCompat.getColor(context, R.color.awesome_cardview_light_shadow)
                    } else {
                        ContextCompat.getColor(context, R.color.awesome_cardview_dark_shadow)
                    }
                }
                shadowDx = a.getDimensionPixelSize(R.styleable.AwesomeCardView_acv_shadowDx, 0)
                shadowDy = a.getDimensionPixelSize(R.styleable.AwesomeCardView_acv_shadowDy, 0)
                strokeWidth =
                    a.getDimension(R.styleable.AwesomeCardView_acv_strokeWidth, 0f)
                strokeColor =
                    a.getColor(R.styleable.AwesomeCardView_acv_strokeColor, Color.TRANSPARENT)

                if (cornerSize != 0f) {
                    cornerSizeTopLeft = cornerSize
                    cornerSizeTopRight = cornerSize
                    cornerSizeBottomLeft = cornerSize
                    cornerSizeBottomRight = cornerSize
                }

                updateShapeAppearanceModel()
                updateShadowDrawable()
            }
        if (isInEditMode) {
            clipPaint.isDither = false
        }
    }

    private fun isLightBackground(): Boolean {
        val hvs = floatArrayOf(0f, 0f, 0f)
        context.obtainStyledAttributes(intArrayOf(android.R.attr.colorBackground))
            .use { aa ->
                val themeColorBackground = aa.getColor(0, 0)
                Color.colorToHSV(themeColorBackground, hvs)
            }
        return hvs[2] > 0.5f
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        tellParentNotChip()
    }


    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (isInEditMode) {
            if (::tempBitmap.isInitialized) {
                tempBitmap.recycle()
            }
            tempBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            tempCanvas = Canvas(tempBitmap)
        }
        updateShapeMask(w, h)
    }


    private fun updateShapeAppearanceModel() {
        if (!::shapeAppearanceModel.isInitialized) {
            shapeAppearanceModel = ShapeAppearanceModel.builder()
                .setAllCorners(RoundedCornerTreatment())
                .build()
        }

        shapeAppearanceModel = shapeAppearanceModel.toBuilder()
            .setTopLeftCornerSize(cornerSizeTopLeft)
            .setTopRightCornerSize(cornerSizeTopRight)
            .setBottomLeftCornerSize(cornerSizeBottomLeft)
            .setBottomRightCornerSize(cornerSizeBottomRight)
            .build()

        updateShapeMask(width, height)
    }

    private fun updateShadowDrawable() {
        if (!::shadowDrawable.isInitialized) {
            shadowDrawable = MaterialShapeDrawable(shapeAppearanceModel).apply {
                initializeElevationOverlay(context)
                shadowCompatibilityMode = MaterialShapeDrawable.SHADOW_COMPAT_MODE_ALWAYS
            }
        }
        shadowDrawable.apply {
            shapeAppearanceModel = this@AwesomeCardView.shapeAppearanceModel
            setShadowColor(shadowColor)
            setTint(cardBackgroundColor)
            elevation = cardElevation
            shadowVerticalOffset = shadowDy
            shadowCompatRotation = shadowDx
        }
        super.setBackground(shadowDrawable)
    }

    private fun updateShapeMask(width: Int, height: Int) {
        maskRect.set(0f, 0f, width.toFloat(), height.toFloat())
        pathProvider.calculatePath(shapeAppearanceModel, 1f, maskRect, maskPath)
        borderPath.rewind()
        borderPath.addPath(maskPath)
        maskPath.addRect(maskRect, Path.Direction.CCW)
    }

    fun setCornerSizes(
        @Px cornerSizeTopLeft: Float,
        @Px cornerSizeTopRight: Float,
        @Px cornerSizeBottomLeft: Float,
        @Px cornerSizeBottomRight: Float
    ) {
        this.cornerSizeTopLeft = cornerSizeTopLeft
        this.cornerSizeTopRight = cornerSizeTopRight
        this.cornerSizeBottomLeft = cornerSizeBottomLeft
        this.cornerSizeBottomRight = cornerSizeBottomRight

        updateShapeAppearanceModel()
        updateShadowDrawable()
    }

    fun setCardElevation(@Px cardElevation: Float) {
        this.cardElevation = cardElevation
        updateShadowDrawable()
    }

    fun setCardBackgroundColor(@ColorInt cardBackgroundColor: Int) {
        this.cardBackgroundColor = cardBackgroundColor
        updateShadowDrawable()
    }

    fun setStrokeWidth(@Px strokeWidth: Float) {
        this.strokeWidth = strokeWidth
        postInvalidate()
    }

    fun setStrokeColor(@ColorInt strokeColor: Int) {
        this.strokeColor = strokeColor
        postInvalidate()
    }

    fun setShadowDy(@Dimension shadowDy:Int){
        this.shadowDy = shadowDy
        updateShadowDrawable()
    }

    fun setShadowDx(@Dimension shadowDx:Int){
        this.shadowDx = shadowDx
        updateShadowDrawable()
    }

    override fun setPadding(left: Int, top: Int, right: Int, bottom: Int) = Unit
    override fun setBackground(background: Drawable?) = Unit
    override fun setBackgroundColor(color: Int) = Unit
    override fun setBackgroundResource(resid: Int) = Unit

    override fun dispatchDraw(canvas: Canvas) {
        if (isInEditMode) {
            super.dispatchDraw(tempCanvas)
            tempCanvas.drawPath(maskPath, clipPaint)
            canvas.drawBitmap(tempBitmap, 0f, 0f, null)
            drawStroke(canvas)
            return
        }
        val saveCount = canvas.saveLayer(null, null)
        super.dispatchDraw(canvas)
        canvas.drawPath(maskPath, clipPaint)
        canvas.restoreToCount(saveCount)
        drawStroke(canvas)
    }

    private fun drawStroke(canvas: Canvas) {
        borderPaint.color = strokeColor
        borderPaint.strokeWidth = strokeWidth
        if (strokeWidth > 0 && strokeColor != Color.TRANSPARENT) {
            canvas.drawPath(borderPath, borderPaint)
        }
    }
}

private fun View.tellParentNotChip() {
    (parent as? ViewGroup)?.clipChildren = false
}

private fun paint(block: Paint.() -> Unit): Paint {
    return Paint().apply(block)
}