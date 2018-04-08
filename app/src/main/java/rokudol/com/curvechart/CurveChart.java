package rokudol.com.curvechart;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by luolan on 2018/3/29.
 */

public class CurveChart extends View {
    //title背景颜色
    private int titleBgColor;
    //title文字颜色
    private int titleTextColor;
    //title文字大小
    private int titleTextSize;
    //title文字
    private String titleText;
    //图表背景颜色
    private int chartBgColor;
    //x轴坐标背景颜色
    private int xCoordinateBgColor;
    //y轴线的颜色
    private int lineColor;
    //x轴及y轴文字默认颜色
    private int textDefaultColor;
    //x轴及y轴被选中时文字颜色
    private int textSelectedColor;
    //x轴文字大小
    private int xCoordinateTextSize;
    //y轴文字大小
    private int yCoordinateTextSize;
    //曲线颜色
    private int curveColor;
    //图表中点的默认样式
    private int chartPointDefaultStyle;
    //图表中点被选中时的样式
    private int chartPointSelectedStyle;
    //x轴坐标间的间距
    private float xCoordinateSpacing;
    //图表中渐变色最浅的颜色
    private int chartMinimumColor;
    //浮窗背景颜色
    private int floatBoxBgColor;

    //title的画笔
    private Paint titlePaint;
    //背景颜色的画笔
    private Paint bgColorPaint;
    //x轴y轴文字画笔
    private Paint coordinateTextPaint;
    //曲线画笔
    private Paint curvePaint;
    //图表中点的画笔
    private Paint pointPaint;
    //渐变色画笔
    private Paint gradientColorPaint;
    //浮窗画笔
    private Paint floatBoxPaint;

    private Context mContext;
    private int height;

    //x，y轴数据
    private List<String> xCoordinateData;
    private List<Double> yCoordinateData;

    //第一个坐标点距离左边屏幕的间距
    private float firstSpacing;

    //第一个点x点坐标
    private float x;
    //第一个点对应的最小x坐标
    private float minX;
    //第一个点对应点最大x坐标
    private float maxX;

    //所有点的坐标
    private List<Point> mPoints;
    //所有点的中间点坐标，用于计算贝塞尔曲线的控制点
    private List<Point> mMidPoints;
    private List<Point> mMidMidPoints;
    private List<Point> mControlPoints;

    //速度检测器
    private VelocityTracker velocityTracker;
    //是否需要在ACTION_UP的时候，根据速度自行滑动
    private boolean isScroll = false;
    //触摸点
    private float startX;
    //y轴原点
    private int yOri;

    //被选中的点
    private int selectedIndex = -1;

    public CurveChart(Context context) {
        super(context);
        mContext = context;
    }

    public CurveChart(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init(attrs);
    }

    public CurveChart(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        TypedArray array = mContext.obtainStyledAttributes(attrs, R.styleable.CurveChart);
        if (array != null) {
            titleBgColor = array.getColor(R.styleable.CurveChart_titleBgColor, Color.parseColor("#ffffff"));
            titleTextColor = array.getColor(R.styleable.CurveChart_titleTextColor, Color.parseColor("#000000"));
            titleTextSize = array.getDimensionPixelSize(R.styleable.CurveChart_titleTextSize,
                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 16,
                            getResources().getDisplayMetrics()));
            titleText = array.getString(R.styleable.CurveChart_titleText);
            chartBgColor = array.getColor(R.styleable.CurveChart_chartBgColor, Color.parseColor("#eeeeee"));
            xCoordinateBgColor = array.getColor(R.styleable.CurveChart_xCoordinateBgColor, Color.parseColor("#ffffff"));
            lineColor = array.getColor(R.styleable.CurveChart_lineColor, Color.parseColor("#30515F71"));
            textDefaultColor = array.getColor(R.styleable.CurveChart_textDefaultColor, Color.parseColor("#666666"));
            textSelectedColor = array.getColor(R.styleable.CurveChart_textSelectedColor, Color.parseColor("#2A5DC0"));
            xCoordinateTextSize = array.getDimensionPixelSize(R.styleable.CurveChart_xCoordinateTextSize,
                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12,
                            getResources().getDisplayMetrics()));
            yCoordinateTextSize = array.getDimensionPixelSize(R.styleable.CurveChart_yCoordinateTextSize,
                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12,
                            getResources().getDisplayMetrics()));
            curveColor = array.getColor(R.styleable.CurveChart_curveColor, Color.parseColor("#2A5DC0"));
            chartPointDefaultStyle =
                    array.getResourceId(R.styleable.CurveChart_chartPointDefaultStyle, R.drawable.default_point_style);
            chartPointSelectedStyle = array.getResourceId(R.styleable.CurveChart_chartPointSelectedStyle,
                    R.drawable.selected_point_style);
            xCoordinateSpacing = array.getDimension(R.styleable.CurveChart_xCoordinateSpacing,
                    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 70, getResources().getDisplayMetrics()));
            chartMinimumColor = array.getColor(R.styleable.CurveChart_chartMinimumColor, Color.parseColor("#15eeeeee"));
            floatBoxBgColor = array.getColor(R.styleable.CurveChart_floatBoxBgColor, Color.WHITE);
            array.recycle();
        }
        firstSpacing = xCoordinateSpacing / 3;
        initPaint();
    }

    private void initPaint() {
        titlePaint = new Paint();
        titlePaint.setAntiAlias(true);
        titlePaint.setStyle(Paint.Style.FILL);
        titlePaint.setTextSize(titleTextSize);
        titlePaint.setColor(titleTextColor);
        titlePaint.setFakeBoldText(true);

        bgColorPaint = new Paint();
        bgColorPaint.setAntiAlias(true);
        bgColorPaint.setStyle(Paint.Style.FILL);

        coordinateTextPaint = new Paint();
        coordinateTextPaint.setAntiAlias(true);
        coordinateTextPaint.setStyle(Paint.Style.FILL);


        curvePaint = new Paint();
        curvePaint.setAntiAlias(true);
        curvePaint.setStrokeWidth(10);
        curvePaint.setStyle(Paint.Style.STROKE);
        curvePaint.setColor(curveColor);
        curvePaint.setDither(true);


        pointPaint = new Paint();
        pointPaint.setAntiAlias(true);
        pointPaint.setStyle(Paint.Style.FILL);

        gradientColorPaint = new Paint();
        gradientColorPaint.setAntiAlias(true);
        gradientColorPaint.setColor(curveColor);

        floatBoxPaint = new Paint();
        floatBoxPaint.setAntiAlias(true);
        floatBoxPaint.setColor(floatBoxBgColor);
        setLayerType(LAYER_TYPE_SOFTWARE, floatBoxPaint);
        floatBoxPaint.setMaskFilter(new BlurMaskFilter(30, BlurMaskFilter.Blur.SOLID));

    }


    private void initPoints() {
        yOri = height - (height / 8);
        mPoints = new ArrayList<>();
        for (int i = 0; i < yCoordinateData.size(); i++) {
            int x = (int) (this.x + this.xCoordinateSpacing * i + (getTextWidth(i) / 2));
            int y = (int) (yOri - (((yOri * (1 - 0.5f)) * yCoordinateData.get(i)) / Collections.max(yCoordinateData)));
            mPoints.add(new Point(x, y));
        }
        //该点用于绘制从最后一个点到屏幕最右侧到二阶贝塞尔曲线
        mPoints.add(new Point(getWidth(), (int) (height - (height / 2.5))));
    }

    private void initMidPoints() {
        mMidPoints = new ArrayList<>();
        for (int i = 0; i < mPoints.size(); i++) {
            Point midPoint = null;
            if (i == mPoints.size() - 1) {
                return;
            } else {
                midPoint = new Point((mPoints.get(i).x + mPoints.get(i + 1).x) / 2,
                        (mPoints.get(i).y + mPoints.get(i + 1).y) / 2);
            }
            mMidPoints.add(midPoint);
        }
    }

    private void initMidMidPoints() {
        mMidMidPoints = new ArrayList<>();
        for (int i = 0; i < mMidPoints.size(); i++) {
            Point midMidPoint = null;
            if (i == mMidPoints.size() - 1) {
                return;
            } else {
                midMidPoint = new Point((mMidPoints.get(i).x + mMidPoints.get(i + 1).x) / 2,
                        (mMidPoints.get(i).y + mMidPoints.get(i + 1).y) / 2);
            }
            mMidMidPoints.add(midMidPoint);
        }
    }

    private void initControlPoints() {
        mControlPoints = new ArrayList<>();
        for (int i = 0; i < mPoints.size(); i++) {
            if (i == 0 || i == mPoints.size() - 1) {
                continue;
            } else {
                Point before = new Point();
                Point after = new Point();
                before.x = mPoints.get(i).x - mMidMidPoints.get(i - 1).x + mMidPoints.get(i - 1).x;
                before.y = mPoints.get(i).y - mMidMidPoints.get(i - 1).y + mMidPoints.get(i - 1).y;
                after.x = mPoints.get(i).x - mMidMidPoints.get(i - 1).x + mMidPoints.get(i).x;
                after.y = mPoints.get(i).y - mMidMidPoints.get(i - 1).y + mMidPoints.get(i).y;
                mControlPoints.add(before);
                mControlPoints.add(after);
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);

        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        //宽度始终为match，只考虑高为自适应的情况
        if (heightMode == MeasureSpec.AT_MOST) {
            heightSize = dip2px(350);
        }
        height = heightSize;
        setMeasuredDimension(widthSize, height);

        x = firstSpacing;

        //减去0.1f是因为最后一个X周刻度距离右边的长度为X轴可见长度的10%
        minX = getWidth() - (getWidth() * 0.1f) - xCoordinateSpacing * (xCoordinateData.size() - 1);
        maxX = x;

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (xCoordinateData == null || yCoordinateData == null || xCoordinateData.size() != yCoordinateData.size()) {
            return;
        }
        drawTitle(canvas);
        drawXCoordinate(canvas);

        initPoints();
        initMidPoints();
        initMidMidPoints();
        initControlPoints();

        drawChart(canvas);
        drawFloatBox(canvas);
        drawCurve(canvas);
        drawPoint(canvas);
        drawYCoordinate(canvas);

    }

    //绘制title
    private void drawTitle(Canvas canvas) {
        bgColorPaint.setColor(titleBgColor);
        int titleBottom = height / 8;
        canvas.drawRect(0, 0, getWidth(), titleBottom, bgColorPaint);

        Rect mTextBound = new Rect();
        titlePaint.getTextBounds(titleText, 0, titleText.length(), mTextBound);
        Paint.FontMetrics fontMetrics = titlePaint.getFontMetrics();
        float textX = getWidth() / 2 - mTextBound.width() / 2;
        float textY = (titleBottom - fontMetrics.bottom + fontMetrics.top) / 2 - fontMetrics.top;
        canvas.drawText(titleText, textX, textY, titlePaint);

    }

    //绘制图表
    private void drawChart(Canvas canvas) {
        bgColorPaint.setColor(chartBgColor);
        int chartTop = height / 8;

        pointPaint.setColor(Color.BLUE);

        bgColorPaint.setColor(chartBgColor);

        //先绘制图表背景颜色
        canvas.drawRect(0, chartTop, getWidth(), height - chartTop, bgColorPaint);

        bgColorPaint.setColor(lineColor);
        for (int i = 0; i < yCoordinateData.size(); i++) {
            //绘制坐标线
            float lineX = mPoints.get(i).x;
            canvas.drawLine(lineX, height / 8, lineX, height - chartTop, bgColorPaint);
        }
    }

    //绘制曲线
    private void drawCurve(Canvas canvas) {
        // 重置路径
        Path curvePath = new Path();
        curvePath.reset();

        Path gradientPath = new Path();
        gradientPath.reset();

        //从屏幕左边绘制到第一个点
        curvePath.moveTo(0, (float) (height - (height / 2.5)));// 起点
        curvePath.quadTo(0, (float) (height - (height / 2.5)),// 控制点
                mPoints.get(0).x, mPoints.get(0).y);

        gradientPath.moveTo(0, height - (height / 8));
        gradientPath.lineTo(0, (float) (height - (height / 2.5)));
        gradientPath.quadTo(0, (float) (height - (height / 2.5)),// 控制点
                mPoints.get(0).x, mPoints.get(0).y);

        for (int i = 0; i < mPoints.size(); i++) {
            if (i == 0) {// 第一条为二阶贝塞尔
                curvePath.moveTo(mPoints.get(i).x, mPoints.get(i).y);// 起点
                curvePath.quadTo(mControlPoints.get(i).x, mControlPoints.get(i).y,// 控制点
                        mPoints.get(i + 1).x, mPoints.get(i + 1).y);


//                gradientPath.moveTo(mPoints.get(i).x, mPoints.get(i).y);// 起点
                gradientPath.quadTo(mControlPoints.get(i).x, mControlPoints.get(i).y,// 控制点
                        mPoints.get(i + 1).x, mPoints.get(i + 1).y);

            } else if (i < mPoints.size() - 1) {// 三阶贝塞尔
                //三阶
//                curvePath.cubicTo(mControlPoints.get(2 * i - 1).x, mControlPoints.get(2 * i - 1).y,// 控制点
//                        mControlPoints.get(2 * i).x, mControlPoints.get(2 * i).y,// 控制点
//                        mPoints.get(i + 1).x, mPoints.get(i + 1).y);// 终点
                //二阶
                curvePath.quadTo(mControlPoints.get(2 * i - 1).x, mControlPoints.get(2 * i - 1).y,
                        mPoints.get(i + 1).x, mPoints.get(i + 1).y);

//                gradientPath.cubicTo(mControlPoints.get(2 * i - 1).x, mControlPoints.get(2 * i - 1).y,// 控制点
//                        mControlPoints.get(2 * i).x, mControlPoints.get(2 * i).y,// 控制点
//                        mPoints.get(i + 1).x, mPoints.get(i + 1).y);// 终点

                gradientPath.quadTo(mControlPoints.get(2 * i - 1).x, mControlPoints.get(2 * i - 1).y,
                        mPoints.get(i + 1).x, mPoints.get(i + 1).y);
            } else if (i == mPoints.size() - 2) {// 最后一条为二阶贝塞尔
                curvePath.moveTo(mPoints.get(i).x, mPoints.get(i).y);// 起点
                curvePath.quadTo(mControlPoints.get(mControlPoints.size() - 1).x,
                        mControlPoints.get(mControlPoints.size() - 1).y,
                        mPoints.get(i + 1).x, mPoints.get(i + 1).y);// 终点

//                gradientPath.moveTo(mPoints.get(i).x, mPoints.get(i).y);// 起点
                gradientPath.quadTo(mControlPoints.get(mControlPoints.size() - 1).x,
                        mControlPoints.get(mControlPoints.size() - 1).y,
                        mPoints.get(i + 1).x, mPoints.get(i + 1).y);//终点
            }
        }

        //绘制渐变色
        gradientPath.lineTo(getWidth(), height - (height / 8));
        gradientPath.lineTo(0, height - (height / 8));
        float left = 0;
        float top = getPaddingTop();
        float bottom = height - (height / 8);
        //渐变色中最深的颜色用绘制曲线的颜色
        LinearGradient lg = new LinearGradient(left, top, left, bottom, curveColor,
                chartMinimumColor, Shader.TileMode.CLAMP);// CLAMP重复最后一个颜色至最后
        gradientColorPaint.setShader(lg);
        gradientColorPaint.setXfermode(new PorterDuffXfermode(
                android.graphics.PorterDuff.Mode.SRC_ATOP));
        canvas.drawPath(gradientPath, gradientColorPaint);

        canvas.drawPath(curvePath, curvePaint);
    }

    //获取文字的宽度
    private int getTextWidth(int position) {
        String text = xCoordinateData.get(position);
        Rect rect = new Rect();
        coordinateTextPaint.getTextBounds(text, 0, text.length(), rect);
        //文字的宽度
        return rect.width();
    }

    //绘制图表上点数据点
    // FIXME: 2018/3/30 在评审时需要确定是否使用图片还是直接绘制原点
    private void drawPoint(Canvas canvas) {

        for (int i = 0; i < xCoordinateData.size(); i++) {

            //绘制数据点
            canvas.drawCircle(mPoints.get(i).x, mPoints.get(i).y, 25, pointPaint);
        }

    }


    //绘制x轴
    private void drawXCoordinate(Canvas canvas) {
        int xCoordinateHeight = height - (height / 8);
        bgColorPaint.setColor(xCoordinateBgColor);
        coordinateTextPaint.setTextSize(xCoordinateTextSize);
        //先绘制背景颜色
        canvas.drawRect(0, xCoordinateHeight, getWidth(), height, bgColorPaint);


        for (int i = 0; i < xCoordinateData.size(); i++) {
            float textX = this.x + xCoordinateSpacing * i;
            float textY = (height - (height / 8)) + ((height - xCoordinateHeight) / 2);

            if (i == selectedIndex) {
                coordinateTextPaint.setColor(textSelectedColor);
            } else {
                coordinateTextPaint.setColor(textDefaultColor);
            }
            canvas.drawText(xCoordinateData.get(i), textX, textY, coordinateTextPaint);
        }

    }

    //绘制y轴
    private void drawYCoordinate(Canvas canvas) {
        coordinateTextPaint.setTextSize(yCoordinateTextSize);

        for (int i = 0; i < yCoordinateData.size(); i++) {
            String text = yCoordinateData.get(i) + "";
            float textX = this.x + xCoordinateSpacing * i;
            float textY = (height / 9) + (height / 8);

            if (i == selectedIndex) {
                coordinateTextPaint.setColor(textSelectedColor);
            } else {
                coordinateTextPaint.setColor(textDefaultColor);
            }
            canvas.drawText(text, textX, textY, coordinateTextPaint);
        }
    }

    private void drawFloatBox(Canvas canvas) {
        RectF rectF = new RectF();

        for (int i = 0; i < xCoordinateData.size(); i++) {
            if (i == selectedIndex) {
                rectF.left = mPoints.get(i).x - dip2px(47);
                rectF.right = mPoints.get(i).x + dip2px(47);
                rectF.top = mPoints.get(i).y - dip2px(70);
                rectF.bottom = mPoints.get(i).y + dip2px(90);

                canvas.drawRoundRect(rectF, 5, 5, floatBoxPaint);
                coordinateTextPaint.setTextSize(60);
                coordinateTextPaint.setColor(textSelectedColor);
                canvas.drawText(yCoordinateData.get(i) + "", this.x + xCoordinateSpacing * i,
                        mPoints.get(i).y - dip2px(35), coordinateTextPaint);
            }
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        obtainVelocityTracker(event);
        this.getParent().requestDisallowInterceptTouchEvent(true);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startX = event.getX();
                break;
            case MotionEvent.ACTION_MOVE:
                //如果屏幕不足以展示所有数据时
                if (xCoordinateSpacing * xCoordinateData.size() > getWidth()) {
                    float dis = event.getX() - startX;
                    startX = event.getX();
                    if (x + dis < minX) {
                        x = minX;
                    } else if (x + dis > maxX) {
                        x = maxX;
                    } else {
                        x = x + dis;
                    }
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_UP:
                clickAction(event);
                scrollAfterActionUp();
                this.getParent().requestDisallowInterceptTouchEvent(false);
                recycleVelocityTracker();

                break;
            case MotionEvent.ACTION_CANCEL:
                this.getParent().requestDisallowInterceptTouchEvent(false);
                recycleVelocityTracker();
                break;
        }
        return true;
    }

    /**
     * 点击X轴坐标或者折线节点
     *
     * @param event
     */
    private void clickAction(MotionEvent event) {
        int dp10 = dip2px(10);
        float eventX = event.getX();
        float eventY = event.getY();
        for (int i = 0; i < xCoordinateData.size(); i++) {
            //节点
            float x = mPoints.get(i).x;
            float y = mPoints.get(i).y;
            if (eventX >= x - dp10 && eventX <= x + dp10 &&
                    eventY >= y - dp10 && eventY <= y + dp10 && selectedIndex != i) {//每个节点周围10dp都是可点击区域
                selectedIndex = i;
                invalidate();
                return;
            }
            //X轴刻度
            x = mPoints.get(i).x;
            y = (height - (height / 8)) + ((height - (height - (height / 8))) / 2);
            if (eventX >= x - getTextWidth(i) / 2 - dp10 && eventX <= x + getTextWidth(i) + dp10 / 2 &&
                    eventY >= y - dp10 && eventY <= y + dp10 && selectedIndex != i) {
                selectedIndex = i;
                invalidate();
                return;
            }
        }
    }

    /**
     * 获取速度跟踪器
     *
     * @param event
     */
    private void obtainVelocityTracker(MotionEvent event) {
        if (!isScroll)
            return;
        if (velocityTracker == null) {
            velocityTracker = VelocityTracker.obtain();
        }
        velocityTracker.addMovement(event);
    }

    /**
     * 获取速度
     *
     * @return
     */
    private float getVelocity() {
        if (velocityTracker != null) {
            velocityTracker.computeCurrentVelocity(1000);
            return velocityTracker.getXVelocity();
        }
        return 0;
    }

    /**
     * 手指抬起后的滑动处理
     */
    private void scrollAfterActionUp() {
        if (!isScroll)
            return;
        final float velocity = getVelocity();
        float scrollLength = maxX - minX;
        if (Math.abs(velocity) < 10000)//10000是一个速度临界值，如果速度达到10000，最大可以滑动(maxX - minX)
            scrollLength = (maxX - minX) * Math.abs(velocity) / 10000;
        ValueAnimator animator = ValueAnimator.ofFloat(0, scrollLength);
        animator.setDuration((long) (scrollLength / (maxX - minX) * 1000));//时间最大为1000毫秒，此处使用比例进行换算
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float value = (float) valueAnimator.getAnimatedValue();
                if (velocity < 0 && x > minX) {//向左滑动
                    if (x - value <= minX)
                        x = minX;
                    else
                        x = x - value;
                } else if (velocity > 0 && x < maxX) {//向右滑动
                    if (x + value >= maxX)
                        x = maxX;
                    else
                        x = x + value;
                }
                invalidate();
            }
        });
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
            }

            @Override
            public void onAnimationEnd(Animator animator) {
            }

            @Override
            public void onAnimationCancel(Animator animator) {
            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        animator.start();

    }


    /**
     * 回收速度跟踪器
     */
    private void recycleVelocityTracker() {
        if (velocityTracker != null) {
            velocityTracker.recycle();
            velocityTracker = null;
        }
    }

    public void setData(@NonNull List<String> xCoordinateData, @NonNull List<Double> yCoordinateData) {
        if (xCoordinateData.size() != yCoordinateData.size()) {
            try {
                throw new Exception("x，y轴数据数量必须相等！");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        this.xCoordinateData = xCoordinateData;
        this.yCoordinateData = yCoordinateData;
        invalidate();
    }

    public List<String> getxCoordinateData() {
        return xCoordinateData;
    }


    public List<Double> getyCoordinateData() {
        return yCoordinateData;
    }

    public int dip2px(float dpValue) {
        final float scale = mContext.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

}
