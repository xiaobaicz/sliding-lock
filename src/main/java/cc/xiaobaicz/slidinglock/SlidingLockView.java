package cc.xiaobaicz.slidinglock;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by xiaobaicz on 2018/2/14.
 */

public class SlidingLockView extends View {

    private final String TAG = "SlidingLock";
    //默认行数
    private final int DEF_ROW = 3;
    //默认圈圈半径
    private final float DEF_R = 32f;
    //默认画笔大小
    private final float DEF_WIDTH = 10f;
    //默认线条颜色
    private final int DEF_LINE_COLOR = 0xffffffff;
    //默认圈圈颜色
    private final int DEF_POINT_COLOR = 0xffffffff;

    private float mWidth;

    private int mLineColor;

    private int mPointColor;

    //控件大小
    private int mSize;

    //画笔
    private Paint mPaint;

    //行数
    private int mRow;

    //点大小
    private final RectF mRectF = new RectF();

    //圈圈背景
    private Bitmap mBG;

    //所有选中的点
    private List<Item> mItems = new ArrayList<>();
    //点集合
    private Set<Item> mPoints = new HashSet<>();

    //点路径
    private Path mLinePath = new Path();

    //跟随手指的点
    private Item mDown = new Item();

    //是否触摸
    private boolean isDown = false;

    //圈圈原点坐标
    float mCoordinate;
    //圈圈半径
    float mR;
    //间隔
    float mInterval;

    public SlidingLockView(Context context) {
        this(context, null);
    }

    public SlidingLockView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SlidingLockView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.SlidingLockView);

        int count = typedArray.getIndexCount();

        mRow = DEF_ROW;
        mR = DEF_R;
        mWidth = DEF_WIDTH;
        mLineColor = DEF_LINE_COLOR;
        mPointColor = DEF_POINT_COLOR;

        for(int i = 0; i < count; i++){
            int index = typedArray.getIndex(i);
            if (index == R.styleable.SlidingLockView_slv_row) {
                mRow = typedArray.getInteger(index, DEF_ROW);
                mRow = mRow <= DEF_ROW ? DEF_ROW : mRow >= 16 ? 16 : mRow;
            } else if (index == R.styleable.SlidingLockView_slv_radius) {
                mR = typedArray.getDimension(index, DEF_R);
            } else if (index == R.styleable.SlidingLockView_slv_line_width) {
                mWidth = typedArray.getDimension(index, DEF_WIDTH);
            } else if (index == R.styleable.SlidingLockView_slv_line_color) {
                mLineColor = typedArray.getColor(index, DEF_LINE_COLOR);
            } else if (index == R.styleable.SlidingLockView_slv_point_color) {
                mPointColor = typedArray.getColor(index, DEF_POINT_COLOR);
            }
        }

        typedArray.recycle();

        mPaint = new Paint();
        mPaint.setStrokeWidth(mWidth);
        mPaint.setAntiAlias(true);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int mode_w = MeasureSpec.getMode(widthMeasureSpec);
        int mode_h = MeasureSpec.getMode(heightMeasureSpec);
        int size_w = MeasureSpec.getSize(widthMeasureSpec);
        int size_h = MeasureSpec.getSize(heightMeasureSpec);

        if(mode_w == MeasureSpec.AT_MOST && mode_h == MeasureSpec.AT_MOST){
            //自适应 480px
            mSize = 480;
        }else if(mode_w == MeasureSpec.AT_MOST && mode_h == MeasureSpec.EXACTLY){
            //最大高度
            mSize = size_h;
        }else if(mode_w == MeasureSpec.EXACTLY && mode_h == MeasureSpec.AT_MOST){
            //最大宽度
            mSize = size_w;
        }else {
            //取最小长度
            mSize = size_w < size_h ? size_w : size_h;
        }

        setMeasuredDimension(mSize, mSize);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //平分控件间隔
        mInterval = 1.0f * mSize / mRow;
        //第一个点坐标
        mCoordinate = mInterval / 2f;

        if(isInvalidate)
            mItems.clear();
        drawLine(canvas);

        if(mBG == null || isInvalidate){
            //绘制背景
            isInvalidate = false;
            mBG = Bitmap.createBitmap(mSize, mSize, Bitmap.Config.ARGB_8888);
            drawBG(mBG);
        }

        canvas.drawBitmap(mBG, 0, 0, null);
    }

    boolean isInvalidate = false;

    /**
     * 绘制线条
     * @param canvas 画板
     */
    private void drawLine(Canvas canvas) {
        mPaint.setStyle(Paint.Style.STROKE);

        //重置点路径
        mLinePath.reset();
        //下标
        int index = 0;

        //路径
        for(Item item : mItems){
            if(index++ == 0){
                mLinePath.moveTo(item.x, item.y);
            }else{
                mLinePath.lineTo(item.x, item.y);
            }
        }

        if(isDown && mItems.size() > 0){
            //是否连接手指触摸点
            mLinePath.lineTo(mDown.x, mDown.y);
        }

        //绘制轨迹
        canvas.drawPath(mLinePath, mPaint);
    }

    /**
     * 绘制背景
     * @param bg 图片
     */
    private void drawBG(Bitmap bg) {
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(mPointColor);
        Canvas tmp = new Canvas(bg);
        mPoints.clear();
        //所有圈圈
        for (int i = 0; i < mRow; i++){
            for (int j = 0; j < mRow; j++){
                Item item = new Item(j * mInterval + mCoordinate, i * mInterval + mCoordinate);
                mRectF.set(item.x - mR, item.y - mR,
                        item.x + mR, item.y + mR);
                mPoints.add(item);
                tmp.drawOval(mRectF, mPaint);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        super.dispatchTouchEvent(event);
        if(event.getActionIndex() == 0){
            //处理第一个触摸点事件
            float x = event.getX();
            float y = event.getY();
            Item item = null;
            Item down = new Item(x, y);
            for(Item tmp : mPoints){
                if(tmp.exist(down, new RectF(0, 0, mR * 4, mR * 4))){
                    //点存在则获取该点
                    item = tmp;
                    break;
                }
            }
            switch (event.getActionMasked()){
                case MotionEvent.ACTION_DOWN:
                    //触摸
                    mPaint.setColor(mLineColor);
                    isDown = true;
                    mItems.clear();
                case MotionEvent.ACTION_MOVE:
                    if(item != null & !mItems.contains(item))
                        //点存在且没有选中则选中该点
                        mItems.add(item);
                    break;
                case MotionEvent.ACTION_UP:
                    //离开
                    isDown = false;
                    if(mOnSlidingListener != null){
                        //结果处理事件,密码
                        StringBuilder sb = new StringBuilder();
                        for (Item tmp : mItems){
                            sb.append((int)((tmp.x - mCoordinate) / mInterval) << 16 | (int)((tmp.y - mCoordinate) / mInterval));
//                            sb.append((int)((tmp.x - mCoordinate) / mInterval) + (int)((tmp.y - mCoordinate) / mInterval) * mRow);
                        }
                        mOnSlidingListener.onSliding(sb.toString(), mItems.size());
                    }
                    break;
            }
            //记录当前触摸位置
            mDown.x = x;
            mDown.y = y;
            //通知刷新
            invalidate();
        }
        return true;
    }

    private OnSlidingListener mOnSlidingListener;

    public void setOnSlidingListener(OnSlidingListener listener){
        mOnSlidingListener = listener;
    }

    public void setLineColor(int color){
        mPaint.setColor(color);
    }

    public void setDefLineColor(int color){
        mLineColor = color;
        isInvalidate = true;
    }

    public void setDefPointColor(int color){
        mBG.recycle();
        mBG = null;
        mPointColor = color;
        isInvalidate = true;
    }

    public void setDefLineWidth(int width){
        mWidth = width;
        mPaint.setStrokeWidth(mWidth);
        isInvalidate = true;
    }

    public void setRow(int row){
        mRow = row <= DEF_ROW ? DEF_ROW : row >= 16 ? 16 : row;
        isInvalidate = true;
    }

    public void setR(int r){
        mR = r;
        isInvalidate = true;
    }

    public interface OnSlidingListener{
        /**
         * 滑动结果返回
         * @param password 密码
         */
        void onSliding(String password, int len);
    }

    static class Item {

        int id;

        float x;

        float y;

        Item(){
            this(0f, 0f);
        }

        Item(float x, float y) {
            this(0, x, y);
        }

        Item(int id, float x, float y) {
            this.x = x;
            this.y = y;
            this.id = id;
        }

        /**
         * 判断点item是否存在当前点的rect范围内
         * @param item 点
         * @param rect 范围
         * @return 是否存在
         */
        boolean exist(Item item, RectF rect){
            float l = x - rect.width() / 2;
            float r = x + rect.width() / 2;
            float t = y - rect.height() / 2;
            float b = y + rect.height() / 2;

            if(item.x > l && item.x < r && item.y > t && item.y < b){
                return true;
            }else{
                return false;
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Item item = (Item) o;

            if (Float.compare(item.x, x) != 0) return false;
            return Float.compare(item.y, y) == 0;
        }

        @Override
        public int hashCode() {
            int result = (x != +0.0f ? Float.floatToIntBits(x) : 0);
            result = 31 * result + (y != +0.0f ? Float.floatToIntBits(y) : 0);
            return result;
        }

        @Override
        public String toString() {
            return "Item{" +
                    "x=" + x +
                    ", y=" + y +
                    '}';
        }
    }

}