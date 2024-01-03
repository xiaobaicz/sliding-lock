@file:Suppress("unused", "MemberVisibilityCanBePrivate")
package io.github.xiaobaicz.lib.widgets

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.Region
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import io.github.xiaobaicz.lib.R
import io.github.xiaobaicz.lib.utils.Utils
import kotlin.math.min

/**
 * Created by xiaobaicz on 2018/2/14.
 */
class SlidingLockView : View {

    companion object {
        // 默认行数
        private const val DEF_ROW = 3

        // 最大行数
        private const val MAX_ROW = 5

        // 默认圈圈半径
        private const val DEF_RADIUS = 32f

        // 默认画笔大小
        private const val DEF_LINE_WIDTH = 24f

        // 默认线条颜色
        private const val DEF_LINE_COLOR = 0xff000000.toInt()

        // 默认圈圈颜色
        private const val DEF_LOCK_COLOR = 0xff000000.toInt()

        // 区域
        private val regionRect = Rect()

        // 区域测试
        private val region = Region()
    }

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : this(context, attrs, defStyleAttr, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        val typedArray = context.theme.obtainStyledAttributes(attrs, R.styleable.SlidingLockView, defStyleAttr, defStyleRes)
        for (i in 0..< typedArray.indexCount) {
            when (val index = typedArray.getIndex(i)) {
                R.styleable.SlidingLockView_row -> row = typedArray.getInteger(index, DEF_ROW)
                R.styleable.SlidingLockView_line_width -> lineWidth = typedArray.getDimension(index, DEF_LINE_WIDTH)
                R.styleable.SlidingLockView_line_color -> lineColor = typedArray.getColor(index, DEF_LINE_COLOR)
                R.styleable.SlidingLockView_lock_color -> lockColor = typedArray.getColor(index, DEF_LOCK_COLOR)
                R.styleable.SlidingLockView_radius -> radius = typedArray.getDimension(index, DEF_RADIUS)
            }
        }
        typedArray.recycle()
    }

    private val lockPaint = Paint().apply {
        isAntiAlias = true
        color = DEF_LOCK_COLOR
        style = Paint.Style.FILL
    }

    private val linePaint = Paint().apply {
        isAntiAlias = true
        color = DEF_LINE_COLOR
        style = Paint.Style.STROKE
        strokeWidth = DEF_LINE_WIDTH
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    //控件大小
    private var size = 0
        set(value) {
            field = value
            updateLockNode()
        }

    //行数
    var row: Int = DEF_ROW
        set(value) {
            field = Utils.shrinkRange(DEF_ROW, MAX_ROW, value)
            updateLockNode()
        }

    //圈圈半径
    var radius: Float = DEF_RADIUS

    //所有选中的点
    private val nodeList = ArrayList<Node>()

    private val lockNodes = ArrayList<Node>()

    private fun updateLockNode() {
        val w = 1f * size / row
        val offset = w / 2
        lockNodes.clear()
        repeat(row) { y ->
            repeat(row) { x ->
                lockNodes.add(Node(y * row + x, x * w + offset, y * w + offset))
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val modeW = MeasureSpec.getMode(widthMeasureSpec)
        val modeH = MeasureSpec.getMode(heightMeasureSpec)
        val sizeW = MeasureSpec.getSize(widthMeasureSpec)
        val sizeH = MeasureSpec.getSize(heightMeasureSpec)
        size = when {
            //自适应 480px
            modeW == MeasureSpec.AT_MOST && modeH == MeasureSpec.AT_MOST -> 480
            //最大高度
            modeW == MeasureSpec.AT_MOST && modeH == MeasureSpec.EXACTLY -> sizeH
            //最大宽度
            modeW == MeasureSpec.EXACTLY && modeH == MeasureSpec.AT_MOST -> sizeW
            //取最小长度
            else -> min(sizeW, sizeH)
        }
        setMeasuredDimension(size, size)
    }

    override fun onDraw(canvas: Canvas) {
        canvas.save {
            drawLine(canvas)
            drawLock(canvas)
        }
    }

    private fun drawLock(canvas: Canvas) {
        canvas.save {
            val w = 1f * size / row
            val offset = w / 2
            canvas.translate(offset, offset)
            repeat(row) {
                canvas.save {
                    repeat(row) {
                        canvas.drawCircle(0f, 0f, radius, lockPaint)
                        canvas.translate(w, 0f)
                    }
                }
                canvas.translate(0f, w)
            }
        }
    }

    private val linePath = Path()

    private fun drawLine(canvas: Canvas) {
        canvas.save {
            linePath.also {
                it.reset()
                if (nodeList.isEmpty()) return@also
                nodeList.first().also { node ->
                    it.moveTo(node.x, node.y)
                }
                nodeList.forEach { node ->
                    it.lineTo(node.x, node.y)
                }
                if (!isComplete)
                    it.lineTo(current.x, current.y)
            }
            canvas.drawPath(linePath, linePaint)
        }
    }

    private fun Canvas.save(block: () -> Unit) {
        try {
            save()
            block()
        } finally {
            restore()
        }
    }

    private val current = PointF()

    private var isComplete = false

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.actionIndex != 0) return super.dispatchTouchEvent(event)
        current.set(event.x, event.y)
        isComplete = false
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                nodeList.clear()
                val node = testing(current, lockNodes, radius)
                node?.apply(nodeList::add)
            }
            MotionEvent.ACTION_MOVE -> {
                val node = testing(current, lockNodes, radius)
                if (node != null && !nodeList.contains(node))
                    nodeList.add(node)
            }
            MotionEvent.ACTION_UP -> {
                //结果处理事件,密码
                isComplete = true
                onSlidingComplete?.onComplete(nodeList)
            }
        }
        //通知刷新
        invalidate()
        return true
    }

    private fun testing(current: PointF, lockNodes: ArrayList<Node>, radius: Float): Node? {
        for (node in lockNodes) {
            regionRect.set(
                (node.x - radius).toInt(), (node.y - radius).toInt(),
                (node.x + radius).toInt(), (node.y + radius).toInt(),
            )
            region.set(regionRect)
            if (region.contains(current.x.toInt(), current.y.toInt()))
                return node
        }
        return null
    }

    var onSlidingComplete: OnSlidingComplete? = null

    var lineColor = DEF_LINE_COLOR
        set(value) {
            field = value
            linePaint.color = value
        }

    var lineWidth = DEF_LINE_WIDTH
        set(value) {
            field = value
            linePaint.strokeWidth = value
        }

    var lockColor = DEF_LOCK_COLOR
        set(value) {
            field = value
            lockPaint.color = value
        }

    fun interface OnSlidingComplete {
        /**
         * 滑动结果返回
         */
        fun onComplete(items: List<Node>)
    }

    data class Node(val id: Int, val x: Float, val y: Float)

}