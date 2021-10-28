package cc.xiaobaicz.slidinglock

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.jvm.JvmOverloads
import cc.xiaobaicz.slidinglock.SlidingLockView.OnSlidingListener
import cc.xiaobaicz.slidinglock.SlidingLockView.OnSlidingComplete
import java.lang.StringBuilder
import java.util.ArrayList
import java.util.HashSet

/**
 * Created by xiaobaicz on 2018/2/14.
 */
class SlidingLockView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : View(context, attrs, defStyleAttr) {
    private val TAG = "SlidingLock"

    //默认行数
    private val DEF_ROW = 3

    //默认圈圈半径
    private val DEF_R = 32f

    //默认画笔大小
    private val DEF_WIDTH = 10f

    //默认线条颜色
    private val DEF_LINE_COLOR = -0x1

    //默认圈圈颜色
    private val DEF_POINT_COLOR = -0x1
    private var mWidth: Float
    private var mLineColor: Int
    private var mPointColor: Int

    //控件大小
    private var mSize = 0

    //画笔
    private val mPaint: Paint

    //行数
    private var mRow: Int

    //点大小
    private val mRectF = RectF()

    //圈圈背景
    private var mBG: Bitmap? = null

    //所有选中的点
    private val mItems: MutableList<Item?> = ArrayList()

    //点集合
    private val mPoints: MutableSet<Item> = HashSet()

    //点路径
    private val mLinePath = Path()

    //跟随手指的点
    private val mDown = Item()

    //是否触摸
    private var isDown = false

    //圈圈原点坐标
    var mCoordinate = 0f

    //圈圈半径
    var mR: Float

    //间隔
    var mInterval = 0f
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val mode_w = MeasureSpec.getMode(widthMeasureSpec)
        val mode_h = MeasureSpec.getMode(heightMeasureSpec)
        val size_w = MeasureSpec.getSize(widthMeasureSpec)
        val size_h = MeasureSpec.getSize(heightMeasureSpec)
        mSize = if (mode_w == MeasureSpec.AT_MOST && mode_h == MeasureSpec.AT_MOST) {
            //自适应 480px
            480
        } else if (mode_w == MeasureSpec.AT_MOST && mode_h == MeasureSpec.EXACTLY) {
            //最大高度
            size_h
        } else if (mode_w == MeasureSpec.EXACTLY && mode_h == MeasureSpec.AT_MOST) {
            //最大宽度
            size_w
        } else {
            //取最小长度
            if (size_w < size_h) size_w else size_h
        }
        setMeasuredDimension(mSize, mSize)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        //平分控件间隔
        mInterval = 1.0f * mSize / mRow
        //第一个点坐标
        mCoordinate = mInterval / 2f
        if (isInvalidate) mItems.clear()
        drawLine(canvas)
        if (mBG == null || isInvalidate) {
            //绘制背景
            isInvalidate = false
            mBG = Bitmap.createBitmap(mSize, mSize, Bitmap.Config.ARGB_8888)
            drawBG(mBG)
        }
        canvas.drawBitmap(mBG!!, 0f, 0f, null)
    }

    var isInvalidate = false

    /**
     * 绘制线条
     * @param canvas 画板
     */
    private fun drawLine(canvas: Canvas) {
        mPaint.style = Paint.Style.STROKE

        //重置点路径
        mLinePath.reset()
        //下标
        var index = 0

        //路径
        for (item in mItems) {
            if (index++ == 0) {
                mLinePath.moveTo(item!!.x, item.y)
            } else {
                mLinePath.lineTo(item!!.x, item.y)
            }
        }
        if (isDown && mItems.size > 0) {
            //是否连接手指触摸点
            mLinePath.lineTo(mDown.x, mDown.y)
        }

        //绘制轨迹
        canvas.drawPath(mLinePath, mPaint)
    }

    /**
     * 绘制背景
     * @param bg 图片
     */
    private fun drawBG(bg: Bitmap?) {
        mPaint.style = Paint.Style.FILL
        mPaint.color = mPointColor
        val tmp = Canvas(bg!!)
        mPoints.clear()
        //所有圈圈
        for (i in 0 until mRow) {
            for (j in 0 until mRow) {
                val item = Item(j * mInterval + mCoordinate, i * mInterval + mCoordinate)
                mRectF[item.x - mR, item.y - mR, item.x + mR] = item.y + mR
                mPoints.add(item)
                tmp.drawOval(mRectF, mPaint)
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return super.onTouchEvent(event)
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        super.dispatchTouchEvent(event)
        if (event.actionIndex == 0) {
            //处理第一个触摸点事件
            val x = event.x
            val y = event.y
            var item: Item? = null
            val down = Item(x, y)
            for (tmp in mPoints) {
                if (tmp.exist(down, RectF(0f, 0f, mR * 4, mR * 4))) {
                    //点存在则获取该点
                    item = tmp
                    break
                }
            }
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    //触摸
                    mPaint.color = mLineColor
                    isDown = true
                    mItems.clear()
                    if (item != null && !mItems.contains(item)) //点存在且没有选中则选中该点
                        mItems.add(item)
                }
                MotionEvent.ACTION_MOVE -> if (item != null && !mItems.contains(item)) mItems.add(item)
                MotionEvent.ACTION_UP -> {
                    //离开
                    isDown = false
                    //结果处理事件,密码
                    val sb = StringBuilder()
                    val items = IntArray(mItems.size)
                    var index = 0
                    for (tmp in mItems) {
                        val i = ((tmp!!.x - mCoordinate) / mInterval).toInt() shl 16 or ((tmp.y - mCoordinate) / mInterval).toInt()
                        sb.append(i)
                        items[index++] = i
                    }
                    if (mOnSlidingListener != null) {
                        mOnSlidingListener!!.onSliding(sb.toString(), mItems.size)
                    }
                    if (mOnSlidingComplete != null) {
                        mOnSlidingComplete!!.onComplete(items)
                    }
                }
            }
            //记录当前触摸位置
            mDown.x = x
            mDown.y = y
            //通知刷新
            invalidate()
        }
        return true
    }

    private var mOnSlidingListener: OnSlidingListener? = null
    fun setOnSlidingListener(listener: OnSlidingListener?) {
        mOnSlidingListener = listener
    }

    private var mOnSlidingComplete: OnSlidingComplete? = null
    fun setOnSlidingListener(listener: OnSlidingComplete?) {
        mOnSlidingComplete = listener
    }

    fun setLineColor(color: Int) {
        mPaint.color = color
    }

    fun setDefLineColor(color: Int) {
        mLineColor = color
        isInvalidate = true
    }

    fun setDefPointColor(color: Int) {
        mBG!!.recycle()
        mBG = null
        mPointColor = color
        isInvalidate = true
    }

    fun setDefLineWidth(width: Int) {
        mWidth = width.toFloat()
        mPaint.strokeWidth = mWidth
        isInvalidate = true
    }

    fun setRow(row: Int) {
        mRow = if (row <= DEF_ROW) DEF_ROW else if (row >= 16) 16 else row
        isInvalidate = true
    }

    fun setR(r: Int) {
        mR = r.toFloat()
        isInvalidate = true
    }

    @Deprecated("")
    interface OnSlidingListener {
        /**
         * 滑动结果返回
         * @param password 密码
         */
        @Deprecated("")
        fun onSliding(password: String?, len: Int)
    }

    interface OnSlidingComplete {
        /**
         * 滑动结果返回
         * @param items 密码坐标，高16位X，低16位Y
         */
        fun onComplete(items: IntArray?)
    }

    internal class Item(var id: Int, var x: Float, var y: Float) {
        @JvmOverloads
        constructor(x: Float = 0f, y: Float = 0f) : this(0, x, y) {
        }

        /**
         * 判断点item是否存在当前点的rect范围内
         * @param item 点
         * @param rect 范围
         * @return 是否存在
         */
        fun exist(item: Item, rect: RectF): Boolean {
            val l = x - rect.width() / 2
            val r = x + rect.width() / 2
            val t = y - rect.height() / 2
            val b = y + rect.height() / 2
            return if (item.x > l && item.x < r && item.y > t && item.y < b) {
                true
            } else {
                false
            }
        }

        override fun equals(o: Any?): Boolean {
            if (this === o) return true
            if (o == null || javaClass != o.javaClass) return false
            val item = o as Item
            return if (java.lang.Float.compare(item.x, x) != 0) false else java.lang.Float.compare(item.y, y) == 0
        }

        override fun hashCode(): Int {
            var result = if (x != +0.0f) java.lang.Float.floatToIntBits(x) else 0
            result = 31 * result + if (y != +0.0f) java.lang.Float.floatToIntBits(y) else 0
            return result
        }

        override fun toString(): String {
            return "Item{" +
                    "x=" + x +
                    ", y=" + y +
                    '}'
        }
    }

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.SlidingLockView)
        val count = typedArray.indexCount
        mRow = DEF_ROW
        mR = DEF_R
        mWidth = DEF_WIDTH
        mLineColor = DEF_LINE_COLOR
        mPointColor = DEF_POINT_COLOR
        for (i in 0 until count) {
            val index = typedArray.getIndex(i)
            if (index == R.styleable.SlidingLockView_slv_row) {
                mRow = typedArray.getInteger(index, DEF_ROW)
                mRow = if (mRow <= DEF_ROW) DEF_ROW else if (mRow >= 16) 16 else mRow
            } else if (index == R.styleable.SlidingLockView_slv_radius) {
                mR = typedArray.getDimension(index, DEF_R)
            } else if (index == R.styleable.SlidingLockView_slv_line_width) {
                mWidth = typedArray.getDimension(index, DEF_WIDTH)
            } else if (index == R.styleable.SlidingLockView_slv_line_color) {
                mLineColor = typedArray.getColor(index, DEF_LINE_COLOR)
            } else if (index == R.styleable.SlidingLockView_slv_point_color) {
                mPointColor = typedArray.getColor(index, DEF_POINT_COLOR)
            }
        }
        typedArray.recycle()
        mPaint = Paint()
        mPaint.strokeWidth = mWidth
        mPaint.isAntiAlias = true
    }
}