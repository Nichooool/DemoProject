package me.nieyh.example.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.LinearInterpolator;

import static android.graphics.Color.WHITE;
import static android.graphics.Paint.ANTI_ALIAS_FLAG;

/**
 * Created by nieyh on 17-6-21. <br/>
 * 普通加速动画视图 <br/>
 * 实现思路： {<br/>
 * 1、通过一个Handler来进行60fps刷新界面执行动画. <br/>
 * 2、通过ValueAnimator 来修改数值 <br/>
 * 3、动画分为三层： { <br/>
 * 1、 环 <br/>
 * 2、 球 <br/>
 * 3、 其他的部件 <br/>
 * } (2与3层将会进行合并图形)<br/>
 * } <br/>
 * 整个View 144×144dp 宽高
 */

public class CommonBoostAnimView extends View {

    //前面250毫秒什么也不做,这是UI的设计的. 坐我右边, 请找他--->
    private final long START_DELAY = 250;
    //环的宽度
    private final float RING_WIDTH = DrawUtils.dip2px(4);
    //圆的半径
    private final float CIRCLE_RADIUS = DrawUtils.dip2px(45);
    //环的半径
    private final float RING_RADIUS = DrawUtils.dip2px(70);
    //颜色
    private final int RED = 0xFFE5394B;
    private final int YELLOW = 0xFFEBB158;
    private final int GREEN = 0xFF3CD776;
    //浅色
    private final int LIGHT_RED = 0x00E5394B;
    private final int LIGHT_GREEN = 0x003CD776;
    //环的第一段的默认开始角度
    private final int RING_START_ANGLE_1 = 90;
    //环的第二段的默认开始角度
    private final int RING_START_ANGLE_2 = 270;
    //扫过的角度
    private final int RING_SWEEP_ANGLE = 90;
    //颜色插值器
    private ArgbEvaluator mArgbEvaluator;
    //属性动画 此处只用于修改值
    private ValueAnimator mValueAnimator;
    private FrameUiHandler mUiFrameHandler;
    //视图的宽高 (每次通过getWidth获取会计算一次 存储变量可以提高性能)
    private int mWidth, mHeight;
    //环转动的角度 环的透明度 环的缩放比
    private float mRingRotationAngle, mRingAlpha, mRingScale;
    //环与球的颜色
    private int mRingAndCircleColor;
    /**
     * 当前位置信息
     */
    //火箭 X, Y坐标
    private float mRocketX, mRocketY;
    //流星雨1 X Y坐标
    private float mStarRainX, mStarRainY;
    //所谓的火焰(球心坐标) X Y 坐标
    private float mFireSmallCenterX, mFireSmallCenterY;
    //所谓的火焰(球心坐标) X Y 坐标
    private float mFireBigCenterX, mFireBigCenterY;
    //对勾的xy坐标
    private float mMarkX, mMarkY;
    //球的中心坐标 球的半径
    private float mCircleX, mCircleY;
    /**
     * 初始化时的位置
     */
    //火箭 X, Y坐标
    private float mRocketStartX, mRocketStartY;
    //流星雨1 X Y坐标
    private float mStarRainStartX, mStarRainStartY;
    //所谓的火焰(第一个球) 球心 X Y 坐标
    private float mFireSmallStartCenterX, mFireSmallStartCenterY;
    //所谓的火焰(第二个球) 球心 X Y 坐标
    private float mFireBigStartCenterX, mFireBigStartCenterY;
    //火焰的大小球的半径
    private float mFireBigRadius, mFireSmallRadius;
    //对勾的透明度
    private float mMarkAlpha;
    //火焰的透明度
    private float mFireAlpha;

    //环的画笔 中间球的画笔 火焰画笔 对勾画笔 其他物件的画笔 遮罩层画笔
    private Paint mRingPaint1, mRingPaint2, mCirclePaint, mFirePaint, mMarkPaint, mOtherTPaint, mMaskPaint;
    //环区域 球区域 火焰大小球的区域 对勾的区域
    private RectF mRingRectF;
    //其他部件的动画上边界 (球的上边)
    private float mOtherTUpBorder;
    //火箭的高度, 宽度
    private float mRocketImgHeight, mRocketImgWidth;
    //流星雨的宽高
    private float mStarRainImgHeight, mStarRainImgWidth;
    //对勾图片的宽高
    private float mMarkImgHeight, mMarkImgWidth;
    //火箭移动的需要的数据
    private final float Y80DP = DrawUtils.dip2px(80);
    private final float Y70DP = DrawUtils.dip2px(70);
    private final float Y3DP = DrawUtils.dip2px(3);
    //火焰移动的需要的数据
    private final float Y45DP = DrawUtils.dip2px(45);
    private final float Y40DP = DrawUtils.dip2px(40);
    //10dp的随机范围
    private final float RANDOM_Y_RANGE = DrawUtils.dip2px(5);
    private final float RANDOM_X_RANGE = DrawUtils.dip2px(3);

    private final float RANDOM_Y_START = DrawUtils.dip2px(2.5f);
    private final float RANDOM_X_START = DrawUtils.dip2px(1.5f);
    //火焰单个球半径
    private final float FIRE_RADIUS = DrawUtils.dip2px(3);
    //两个球的球心之间的距离(上下距离与左右距离)
    private final float FIRE_CENTER_DISTANCE = DrawUtils.dip2px(8);
    //火箭初始位置距离底部的边距
    private final float ROCKET_PADDING_BOTTOM = DrawUtils.dip2px(8);
    //火箭抖动次数
    private int mShakeTimes = 0;
    //火箭位图 流星雨位图 对勾位图 遮罩位图
    private Bitmap mRocketBitmap, mStarRainBitmap, mMarkBitmap, mMaskBitmap;
    //是否已经开始
    private boolean isStart;
    //是否自动开始
    private boolean isAutoStart;
    //对于Shader进行旋转
    private Matrix mMatrix, mMatrix1;

    public CommonBoostAnimView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (oldw != w || oldh != h) {
            mWidth = w;
            mHeight = h;
            initView();
            if (!isStart && isAutoStart) {
                startCommonBoostAnim();
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        int size = (int) (RING_RADIUS * 2 + RING_WIDTH);
        if (widthMode == MeasureSpec.AT_MOST && heightMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(size, size);
        } else if (widthMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(size, height);
        } else if (heightMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(width, size);
        }
    }

    /**
     * 初始化环
     *
     * @param startColor 开始颜色
     * @param endColor   结束颜色
     */
    void reInitRingColor(int startColor, int endColor) {
        if (mRingPaint1 != null) {
            if (mMatrix == null) {
                mMatrix = new Matrix();
            }
            mMatrix.setRotate(mRingRotationAngle + 85, mWidth / 2, mHeight / 2);
            SweepGradient sweepGradient = new SweepGradient(mWidth / 2, mHeight / 2, new int[]{startColor, endColor}, new float[] {0, 0.25f});
            sweepGradient.setLocalMatrix(mMatrix);
            mRingPaint1.setStrokeCap(Paint.Cap.ROUND);
            mRingPaint1.setShader(sweepGradient);
            mRingPaint1.setStrokeWidth(RING_WIDTH);
            mRingPaint1.setStyle(Paint.Style.STROKE);
        }
        if (mRingPaint2 != null) {
            if (mMatrix1 == null) {
                mMatrix1 = new Matrix();
            }
            mMatrix1.setRotate(mRingRotationAngle + 265, mWidth / 2, mHeight / 2);
            SweepGradient sweepGradient = new SweepGradient(mWidth / 2, mHeight / 2, new int[]{startColor, endColor}, new float[] {0, 0.25f});
            sweepGradient.setLocalMatrix(mMatrix1);
            mRingPaint2.setStrokeCap(Paint.Cap.ROUND);
            mRingPaint2.setShader(sweepGradient);
            mRingPaint2.setStrokeWidth(RING_WIDTH);
            mRingPaint2.setStyle(Paint.Style.STROKE);
        }
    }

    /**
     * 初始化遮罩层
     */
    void initMaskPaint() {
        mMaskPaint = new Paint();
        mMaskPaint.setColor(Color.WHITE);
        mMaskPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));

        try {
            mMaskBitmap = Bitmap.createBitmap(mWidth, mHeight,
                    Bitmap.Config.ARGB_8888);
            Canvas cv = new Canvas(mMaskBitmap);
            Paint paint = new Paint();
            paint.setStyle(Paint.Style.FILL);
            paint.setAntiAlias(true);
            paint.setColor(Color.WHITE);
            //坐标系与canvas的坐标系不同
            cv.drawCircle(CIRCLE_RADIUS, CIRCLE_RADIUS, CIRCLE_RADIUS, paint);
        } catch (OutOfMemoryError error) {

        }
    }

    void initView() {
        /**
         * 初始化所有部件显示需要的数据
         * */
        mRingPaint1 = new Paint(ANTI_ALIAS_FLAG);
        mRingPaint2 = new Paint(ANTI_ALIAS_FLAG);
        mCirclePaint = new Paint(ANTI_ALIAS_FLAG);
        mFirePaint = new Paint(ANTI_ALIAS_FLAG);
        mMarkPaint = new Paint(ANTI_ALIAS_FLAG);
        mOtherTPaint = new Paint(ANTI_ALIAS_FLAG);
        mRingPaint1.setStyle(Paint.Style.FILL);
        mRingPaint2.setStyle(Paint.Style.FILL);
        mCirclePaint.setStyle(Paint.Style.FILL);
        mFirePaint.setStyle(Paint.Style.FILL);
        mMarkPaint.setStyle(Paint.Style.FILL);
        mOtherTPaint.setStyle(Paint.Style.FILL);
        mRingPaint1.setAlpha(0);
        mRingPaint2.setAlpha(0);
        mCirclePaint.setColor(RED);
        mFirePaint.setColor(WHITE);
        mMarkPaint.setColor(WHITE);
        mMarkPaint.setAlpha(0);
        mOtherTPaint.setColor(WHITE);
        float halfRingWidth = RING_WIDTH / 2;
        //四边要间隔出四个halfRingWidth 距离
        mRingRectF = new RectF(halfRingWidth, halfRingWidth, halfRingWidth + RING_RADIUS * 2, halfRingWidth + RING_RADIUS * 2);
        float size = mWidth > mHeight ? mHeight : mWidth;
        float padding = (size / 2) - CIRCLE_RADIUS;
        mCircleX = padding + CIRCLE_RADIUS;
        mCircleY = padding + CIRCLE_RADIUS;
        mOtherTUpBorder = padding;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        mRocketBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.rocket, options);
        mMarkBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.mark, options);
        mStarRainBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.star_rain, options);
        mRocketImgWidth = mRocketBitmap.getWidth();
        mRocketImgHeight = mRocketBitmap.getHeight();
        mMarkImgWidth = mMarkBitmap.getWidth();
        mMarkImgHeight = mMarkBitmap.getHeight();
        mStarRainImgWidth = mStarRainBitmap.getWidth();
        mStarRainImgHeight = mStarRainBitmap.getHeight();
        //火箭
        float rocketPaddingLeft = ((size - mRocketImgWidth) / 2);
        float rocketPaddingTop = padding + CIRCLE_RADIUS * 2 + ROCKET_PADDING_BOTTOM;
        mRocketStartX = mRocketX = rocketPaddingLeft;
        mRocketStartY = mRocketY = rocketPaddingTop;
        //流星雨
        float rainPaddingLeft = ((size - mStarRainImgWidth) / 2);
        float rainPaddingTop = mOtherTUpBorder - mStarRainImgHeight;
        mStarRainStartX = mStarRainX = rainPaddingLeft;
        mStarRainStartY = mStarRainY = rainPaddingTop;
        //对勾
        float markPaddingLeft = ((size - mMarkImgWidth) / 2);
        float markPaddingTop = ((size - mMarkImgHeight) / 2);
        mMarkX = markPaddingLeft;
        mMarkY = markPaddingTop;
        //火焰
        float centerX = size / 2;
        float centerY = centerX;
        float marginRight = FIRE_CENTER_DISTANCE / 2;
        mFireSmallStartCenterX = mFireSmallCenterX = centerX - marginRight;
        mFireSmallStartCenterY = mFireSmallCenterY = centerY + FIRE_RADIUS;
        mFireBigStartCenterX = mFireBigCenterX = mFireSmallStartCenterX + FIRE_CENTER_DISTANCE;
        mFireBigStartCenterY = mFireBigCenterY = mFireSmallStartCenterY + FIRE_CENTER_DISTANCE;
        reInitRingColor(LIGHT_RED, RED);
        initMaskPaint();
        /**
         * 初始化动画
         * */
        if (mValueAnimator == null) {
            mValueAnimator = ValueAnimator.ofInt(0, 4500);
            mValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    int value = (int) animation.getAnimatedValue();
                    if (value < 250) {
                        return;
                    }

                    /***
                     * 环动画 {
                     *     1. 出现 -- 透明度从0-1、缩放从0.6-1. (250 - 500)
                     *     2. 转 360度 -- (500 - 2500)
                     *     3. 转 270+360度 -- (2500 - 3600)
                     *     4. (颜色变化 单拿出来)
                     *     5. 消失 -- 透明度从1-0、缩放从1-0. (3500 - 3600)
                     * }
                     * */
                    if (value >= 250 && value <= 500) {
                        float rate = (float) (value - 250) / 250;
                        mRingScale = 0.6f + rate * 0.4f;
                        mRingAlpha = rate;
                    } else if (value > 500 && value <= 2500) {
                        mRingScale = 1;
                        mRingAlpha = 1;
                        float rate = (float) (value - 500) / 2000;
                        mRingRotationAngle = rate * 360;
                    } else if (value > 2500 && value <= 3600) {
                        float rate = (float) (value - 2500) / 1100;
                        mRingRotationAngle = rate * 720;
                    }
                    //环消失
                    if (value >= 3500 && value <= 3600) {
                        float rate = (float) (value - 3500) / 100;
                        mRingAlpha = mRingScale = (1 - rate);
                    }

                    /***
                     * 颜色动画 {
                     *     1. 环的颜色 {
                     *          1. 红色 #E5394B-- (0 - 1500)
                     *          2. 红色变成黄色 #EBB158 -- (1500 - 2500)
                     *          3. 黄色变成绿色 #3CD776 -- (2500 - 3500)
                     *     }
                     *     2. 球的颜色 ～～ 同上
                     * }
                     * */

                    if (value < 1500) {
                        mRingAndCircleColor = RED;
                        reInitRingColor(LIGHT_RED, RED);
                        mCirclePaint.setColor(mRingAndCircleColor);
                    } else if (value > 1500 && value <= 2500) {
                        float rate = (float) (value - 1500) / 1000;
                        mRingAndCircleColor = (int) mArgbEvaluator.evaluate(rate, RED, YELLOW);
                        //设置圆环的渐变色
                        int startColor = mRingAndCircleColor & 0x00FFFFFF;
                        reInitRingColor(startColor, mRingAndCircleColor);
                        mCirclePaint.setColor(mRingAndCircleColor);
                    } else if (value > 2500 && value <= 3500) {
                        float rate = (float) (value - 2500) / 1000;
                        mRingAndCircleColor = (int) mArgbEvaluator.evaluate(rate, YELLOW, GREEN);
                        //设置圆环的渐变色
                        int startColor = mRingAndCircleColor & 0x00FFFFFF;
                        reInitRingColor(startColor, mRingAndCircleColor);
                        mCirclePaint.setColor(mRingAndCircleColor);
                    } else if (value > 3500) {
                        mRingAndCircleColor = GREEN;
                        reInitRingColor(LIGHT_GREEN, GREEN);
                        mCirclePaint.setColor(mRingAndCircleColor);
                    }

                    /*****
                     * 火箭动画 {
                     *     1. 上移70dp -- (500 - 1000)
                     *     2. 下移3dp -- (1000 - 1500)
                     *     3. 上移3dp -- (1500 - 2500)
                     *     4. 抖动 可以理解为抽搐 -- (2500 - 3500)
                     *     5. 无 -- (3500 - 4000)
                     *     6. 下移3dp -- (4000 - 4100)
                     *     6. 上移70dp -- (4100 - 4250)
                     * }
                     * ***/
                    if (value >= 500 && value < 1000) {
                        //上移70dp
                        float rate = (float) (value - 500) / 500;
                        mRocketY = mRocketStartY - rate * Y70DP;
                    } else if (value >= 1000 && value < 1500) {
                        //下移3dp
                        float rate = (float) (value - 1000) / 500;
                        mRocketY = (mRocketStartY - Y70DP) + rate * Y3DP;
                    } else if (value >= 1500 && value < 2500) {
                        //上移3dp
                        float rate = (float) (value - 1500) / 1000;
                        mRocketY = (mRocketStartY - Y70DP + Y3DP) - rate * Y3DP;
                    } else if (value >= 2500 && value < 3500) {
                        //抖动
                        //100毫秒1次
                        int time = (value - 2500) / 100;
                        if (mShakeTimes != time) {
                            float offsetX = -RANDOM_X_START + (float) (Math.random() * RANDOM_X_RANGE);
                            float offsetY = -RANDOM_Y_START + (float) (Math.random() * RANDOM_Y_RANGE);
                            mRocketX = mRocketStartX + offsetX;
                            mRocketY = mRocketStartY - Y70DP + offsetY;
                            mShakeTimes = time;
                        }
                    } else if (value >= 3500 && value < 4000) {
                        //用于摆正火箭
                        mRocketY = mRocketStartY - Y70DP;
                        mRocketX = mRocketStartX;
                    } else if (value >= 4000 && value < 4100) {
                        //下移3dp
                        float rate = (float) (value - 4000) / 100;
                        mRocketY = (mRocketStartY - Y70DP) + rate * Y3DP;
                    } else if (value >= 4100 && value < 4250) {
                        //上移80dp
                        float rate = (float) (value - 4100) / 150;
                        mRocketY = (mRocketStartY - Y70DP + Y3DP) - rate * Y80DP;
                    }

                    /**
                     * 雨的动画 {
                     *     1. 下降自己的高度 + 球的高度 -- (1000-2500)
                     *     2. 再次降临 距离与上面一样 -- (4000-4250)
                     * }
                     * */

                    if (value >= 1000 && value < 2500) {
                        float rate = (float) (value - 1000) / 1500;
                        mStarRainY = mStarRainStartY + rate * (mStarRainImgHeight + CIRCLE_RADIUS * 2);
                    } else if (value >= 4000 && value < 4250) {
                        float rate = (float) (value - 4000) / 250;
                        mStarRainY = mStarRainStartY + rate * (mStarRainImgHeight + CIRCLE_RADIUS * 2);
                    }

                    /**
                     * 对勾动画 {
                     *     1. 透明度从0-1 -- (4250 - 4500)
                     * }
                     * */
                    if (value >= 4250 && value < 4500) {
                        float rate = (float) (value - 4250) / 250;
                        mMarkAlpha = rate;
                    }

                    /**
                     * 火焰动画 {
                     *     1. 小球缩放从0.4 - 1 -- (1000-2500)
                     *     2. 大球缩放从0.6 - 1.5 -- (1000-2500)
                     * }
                     * */
                    if (value >= 1000 && value < 2500) {
                        float rate = (float) (value - 1000) / 1500;
                        mFireSmallRadius = FIRE_RADIUS * (0.4f + rate * 0.6f);
                        mFireBigRadius = FIRE_RADIUS * (0.6f + rate * 0.9f);
                        mFireAlpha = rate;

                        mFireSmallCenterY = mFireSmallStartCenterY + rate * Y45DP;
                        mFireBigCenterY = mFireBigStartCenterY + rate * Y40DP;
                    }
                    //更新所有的绘画需要的数据 尽量在onDraw中只存在绘制
                    prepareForDraw();
                }
            });
            mValueAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    //        setLayerType(View.LAYER_TYPE_NONE, null);
                    if (mUiFrameHandler != null) {
                        mUiFrameHandler.stopRefresh();
                    }
                    if (mMarkBitmap != null) {
                        mMarkBitmap.recycle();
                        mMarkBitmap = null;
                    }
                    if (mMaskBitmap != null) {
                        mMaskBitmap.recycle();
                        mMaskBitmap = null;
                    }
                    if (mStarRainBitmap != null) {
                        mStarRainBitmap.recycle();
                        mStarRainBitmap = null;
                    }
                    if (mRocketBitmap != null) {
                        mRocketBitmap.recycle();
                        mRocketBitmap = null;
                    }
                    if (mOnAnimatorAction != null) {
                        mOnAnimatorAction.onEnd();
                    }
                }

                @Override
                public void onAnimationStart(Animator animation) {
                    super.onAnimationStart(animation);
                    if (mOnAnimatorAction != null) {
                        mOnAnimatorAction.onStart();
                    }
                }
            });

            mValueAnimator.setInterpolator(new LinearInterpolator());
            mValueAnimator.setDuration(4500);
            //前250毫秒 啥也没有发生
            mValueAnimator.setStartDelay(START_DELAY);
            mUiFrameHandler = new FrameUiHandler();
            mArgbEvaluator = new ArgbEvaluator();
        }
        //重绘一次
        invalidate();
    }

    /**
     * 更新所有的画笔以及区域等数据 用于绘制
     */
    private void prepareForDraw() {
        //对勾透明度
        if (mMarkPaint != null) {
            mMarkPaint.setAlpha((int) (mMarkAlpha * 255));
        }
        //火焰的透明度
        if (mFirePaint != null) {
            mFirePaint.setAlpha((int) (mFireAlpha * 255));
        }
        //环的透明度
        if (mRingPaint1 != null) {
            mRingPaint1.setAlpha((int) (mRingAlpha * 255));
        } //环的透明度
        if (mRingPaint2 != null) {
            mRingPaint2.setAlpha((int) (mRingAlpha * 255));
        }
        //计算机当前缩放比的整个环的宽度
        float size = RING_RADIUS * 2 * mRingScale;
        float viewSize = mWidth > mHeight ? mHeight : mWidth;
        float paddingLeft = (viewSize - size) / 2;
        mRingRectF.top = paddingLeft;
        mRingRectF.left = paddingLeft;
        mRingRectF.right = paddingLeft + size;
        mRingRectF.bottom = paddingLeft + size;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        /**
         * 绘制环
         * */
        canvas.drawArc(mRingRectF, RING_START_ANGLE_1 + mRingRotationAngle, RING_SWEEP_ANGLE, false, mRingPaint1);
        canvas.drawArc(mRingRectF, RING_START_ANGLE_2 + mRingRotationAngle, RING_SWEEP_ANGLE, false, mRingPaint2);

        /**
         * 绘制球
         * */
        canvas.drawCircle(mCircleX, mCircleY, CIRCLE_RADIUS, mCirclePaint);

        /**
         * 绘制其他部件 （火箭 雨 火焰 对勾）
         * */
        //创建一个新层 专门用于绘制部件
        float x = mCircleX - CIRCLE_RADIUS;
        float y = mCircleY - CIRCLE_RADIUS;
        int layerId = canvas.saveLayer(x, y, mCircleX + CIRCLE_RADIUS, mCircleY + CIRCLE_RADIUS, null, Canvas.ALL_SAVE_FLAG);
        //火箭
        if (mRocketBitmap != null && !mRocketBitmap.isRecycled()) {
            canvas.drawBitmap(mRocketBitmap, mRocketX, mRocketY, mOtherTPaint);
        }
        //雨
        if (mStarRainBitmap != null && !mStarRainBitmap.isRecycled()) {
            canvas.drawBitmap(mStarRainBitmap, mStarRainX, mStarRainY, mOtherTPaint);
        }
        //火焰 小
        canvas.drawCircle(mFireSmallCenterX, mFireSmallCenterY, mFireSmallRadius, mFirePaint);
        //火焰 大
        canvas.drawCircle(mFireBigCenterX, mFireBigCenterY, mFireBigRadius, mFirePaint);
        if (mMarkBitmap != null && !mMarkBitmap.isRecycled()) {
            //对勾
            canvas.drawBitmap(mMarkBitmap, mMarkX, mMarkY, mMarkPaint);
        }
        if (mMaskBitmap != null && !mMaskBitmap.isRecycled()) {
            canvas.drawBitmap(mMaskBitmap, x, y, mMaskPaint);
        }
        //将这个层合并到整个视图中
        canvas.restoreToCount(layerId);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mUiFrameHandler != null) {
            mUiFrameHandler.stopRefresh();
        }
        if (mValueAnimator != null) {
            mValueAnimator.cancel();
        }
//        setLayerType(View.LAYER_TYPE_NONE, null);
        mOnAnimatorAction = null;
        isStart = false;
        isAutoStart = false;
    }

    /***
     * 以下暴露给外部调用
     * */

    public void setAutoStart() {
        isAutoStart = true;
    }

    /**
     * 开始加速动画
     */
    public void startCommonBoostAnim() {
        if (mUiFrameHandler != null) {
            isStart = true;
            //逐帧动画 开始
//            setLayerType(View.LAYER_TYPE_HARDWARE, null);
            mUiFrameHandler.startRefreshFrame(new Runnable() {
                @Override
                public void run() {
                    //重绘视图
                    //哎 这样子 写 性能不高
                    invalidate();
                }
            }, START_DELAY);
            //属性动画开始执行
            if (mValueAnimator != null) {
                mValueAnimator.start();
            }
        }
    }

    /*****
     * 动画监听器
     * */

    public void setOnAnimatorAction(OnAnimatorAction onAnimatorAction) {
        this.mOnAnimatorAction = onAnimatorAction;
    }

    OnAnimatorAction mOnAnimatorAction;

    public interface OnAnimatorAction {
        void onStart();

        void onEnd();
    }

}
