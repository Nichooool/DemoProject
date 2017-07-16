package me.nieyh.example.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.logging.Logger;

import static android.graphics.Paint.ANTI_ALIAS_FLAG;

/**
 * 作者：nieyh on 2017/7/9 10:02
 * 邮箱：813825509@qq.com
 * 描述：<br/>
 * <ol>
 *     <li>layout中布局好需要的试图</li>
 *     <li> 布局分层结构(从下到上)
 *         <ol>变色的矩形</ol>
 *         <ol>火箭</ol>
 *         <ol>流星雨</ol>
 *         <ol>火焰</ol>
 *         <ol>对勾</ol>
 *         <ol>中心空心圆的白色遮罩</ol>
 *         <ol>渐变色环</ol>
 *     </li>
 * </ol>
 */

public class CommonBoostAnimLayout extends FrameLayout {
    //前面250毫秒什么也不做,这是UI的设计的. 坐我右边, 请找他--->
    private final long START_DELAY = 250;
    //环的宽度
    private final float RING_WIDTH = DrawUtils.dip2px(4);
    //环的半径
    private final float RING_RADIUS = DrawUtils.dip2px(70);
    //火焰单个球半径
    private final float FIRE_RADIUS = DrawUtils.dip2px(3);
    //两个球的球心之间的距离(上下距离与左右距离)
    private final float FIRE_CENTER_DISTANCE = DrawUtils.dip2px(8);
    //火箭初始位置距离底部的边距
    private final float ROCKET_PADDING_BOTTOM = DrawUtils.dip2px(8);
    //圆的半径
    private final float CIRCLE_RADIUS = DrawUtils.dip2px(45);
    //颜色
    private final int RED = 0xFFE5394B;
    private final int YELLOW = 0xFFEBB158;
    private final int GREEN = 0xFF3CD776;
    //浅色
    private final int LIGHT_RED = 0x00E5394B;
    private final int LIGHT_GREEN = 0x003CD776;
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
    //震动距离
    private final float RANDOM_Y_START = DrawUtils.dip2px(2.5f);
    private final float RANDOM_X_START = DrawUtils.dip2px(1.5f);
    //颜色开始位置与结束位置
    private int START_COLOR, END_COLOR;
    //环与球的颜色
    private int mRingAndCircleColor = RED;
    //火箭
    private ImageView mRocket;
    //流星雨
    private ImageView mStarRain;
    //对勾
    private ImageView mMark;
    //火焰
    private FireCircle mFireSmallCircle;
    private FireCircle mFireBigCircle;
    //圆的遮罩
    private CircleMask mCircleMask;
    //变色的矩形
    private ImageView mSquareImg;
    //渐变环
    private GradualRing mGradualRing;
    //属性动画
    private ValueAnimator mValueAnimator;
    //属性动画中修改的属性
    private float mRingScale;
    private float mRingAlpha;
    private float mRingRotationAngle;
    private float mMarkAlpha;
    //Translate Y距离
    private float mRocketTranslateY;
    private float mRocketTranslateX;
    private float mStarRainTranslateX;
    private float mStarRainTranslateY;
    private float mFireSmallScale;
    private float mFireBigScale;
    private float mFireAlpha;
    private float mFireBigTranslateY;
    private float mFireSmallTranslateY;
    private ArgbEvaluator mArgbEvaluator;
    //震动次数
    private int mShakeTimes;
    private FrameUiHandler mUiFrameHandler;

    public CommonBoostAnimLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mRocket = new ImageView(getContext());
        mRocket.setImageResource(R.drawable.rocket);
        mStarRain = new ImageView(getContext());
        mSquareImg = new ImageView(getContext());
        mMark = new ImageView(getContext());
        mMark.setImageResource(R.drawable.mark);
        mStarRain.setImageResource(R.drawable.star_rain);
        mFireSmallCircle = new FireCircle(getContext());
        mFireBigCircle = new FireCircle(getContext());
        mCircleMask = new CircleMask(getContext());
        mGradualRing = new GradualRing(getContext());
        // <ol>变色的矩形</ol>
        // <ol>火箭</ol>
        // <ol>流星雨</ol>
        // <ol>火焰</ol>
        // <ol>对勾</ol>
        // <ol>中心空心圆的白色遮罩</ol>
        // <ol>渐变色环</ol>
        addView(mSquareImg);
        addView(mRocket);
        addView(mStarRain);
        addView(mFireBigCircle);
        addView(mFireSmallCircle);
        addView(mMark);
        addView(mCircleMask);
        addView(mGradualRing);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        Log.w("nieyh", "CommonBoostAnimLayout >> onMeasure()");
        //必须注释掉原先的父类中默认的measure配置
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        int size = (int) (RING_RADIUS * 2 + RING_WIDTH);
        if (widthMode == MeasureSpec.AT_MOST && heightMode == MeasureSpec.AT_MOST) {
            //火焰球
            mFireBigCircle.measure(MeasureSpec.makeMeasureSpec((int) (FIRE_RADIUS * 2), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec((int) (FIRE_RADIUS * 2), MeasureSpec.EXACTLY));
            mFireSmallCircle.measure(MeasureSpec.makeMeasureSpec((int) (FIRE_RADIUS * 2), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec((int) (FIRE_RADIUS * 2), MeasureSpec.EXACTLY));
            mGradualRing.measure(MeasureSpec.makeMeasureSpec(size, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(size, MeasureSpec.EXACTLY));
            mCircleMask.measure(MeasureSpec.makeMeasureSpec(size, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(size, MeasureSpec.EXACTLY));
            mSquareImg.measure(MeasureSpec.makeMeasureSpec(size, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(size, MeasureSpec.EXACTLY));
            //此处三张图片 让它自己计算自己视图的大小 传入AT_MOST
            mMark.measure(MeasureSpec.makeMeasureSpec(size, MeasureSpec.AT_MOST), MeasureSpec.makeMeasureSpec(size, MeasureSpec.AT_MOST));
            mRocket.measure(MeasureSpec.makeMeasureSpec(size, MeasureSpec.AT_MOST), MeasureSpec.makeMeasureSpec(size, MeasureSpec.AT_MOST));
            mStarRain.measure(MeasureSpec.makeMeasureSpec(size, MeasureSpec.AT_MOST), MeasureSpec.makeMeasureSpec(size, MeasureSpec.AT_MOST));
            setMeasuredDimension(size, size);
        } else if (widthMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(size, height);
        } else if (heightMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(width, size);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        Log.w("nieyh", "CommonBoostAnimLayout >> onLayout()");
        //此处的left top right bottom值指的是相对于父布局的位置信息
        //同样调用子布局的layout 也是传入相对于当前布局的值
        //布局到指定位置
        mSquareImg.layout(0, 0, right - left, bottom - top);
        int rocketLeft = (getMeasuredWidth() - mRocket.getMeasuredWidth()) / 2;
        int rocketRight = rocketLeft + mRocket.getMeasuredWidth();
        int rocketTop = (int) ((getMeasuredHeight() / 2) + CIRCLE_RADIUS + ROCKET_PADDING_BOTTOM);
        int rocketBottom = rocketTop + mRocket.getMeasuredHeight();
        mRocket.layout(rocketLeft, rocketTop, rocketRight, rocketBottom);
        int starRainLeft = ((getMeasuredWidth() - mStarRain.getMeasuredWidth()) / 2);
        int starRainTop = (int) ((getMeasuredWidth() / 2) - CIRCLE_RADIUS - mStarRain.getMeasuredHeight());
        int starRainRight = starRainLeft + mStarRain.getMeasuredWidth();
        int starRainBottom = starRainTop + mStarRain.getMeasuredHeight();
        mStarRain.layout(starRainLeft, starRainTop, starRainRight, starRainBottom);
        float centerX = getMeasuredWidth() / 2;
        float centerY = centerX;
        float marginRight = FIRE_CENTER_DISTANCE / 2;
        int fireSmallCircleLeft = (int) (centerX - marginRight - FIRE_RADIUS);
        int fireSmallCircleTop = (int) centerY;
        int fireBigCircleLeft = (int) (fireSmallCircleLeft + FIRE_CENTER_DISTANCE);
        int fireBigCircleTop = (int) (fireSmallCircleTop + FIRE_CENTER_DISTANCE);
        mFireBigCircle.layout(fireBigCircleLeft, fireBigCircleTop, (int)(fireBigCircleLeft + 2 * FIRE_RADIUS), (int)(fireBigCircleTop + 2 * FIRE_RADIUS));
        mFireSmallCircle.layout(fireSmallCircleLeft, fireSmallCircleTop, (int)(fireSmallCircleLeft + 2 * FIRE_RADIUS), (int) (fireSmallCircleTop + 2 * FIRE_RADIUS));
        int markLeft = ((getMeasuredWidth() - mMark.getMeasuredWidth()) / 2);
        int markTop = ((getMeasuredHeight() - mMark.getMeasuredHeight()) / 2);
        mMark.layout(markLeft, markTop, markLeft + mMark.getMeasuredWidth(), markTop + mMark.getMeasuredHeight());
        mCircleMask.layout(0, 0, right - left, bottom - top);
        mGradualRing.layout(0, 0, right - left, bottom - top);
    }
    /**
     * 初始化动画
     * */
    private void initAnim() {
        if (mValueAnimator == null) {
            mValueAnimator = ValueAnimator.ofInt(0, 4500);
            mValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    int value = (int) animation.getAnimatedValue();
                    if (value < 250) {
                        return;
                    }
                    /**
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
                        START_COLOR = LIGHT_RED;
                        END_COLOR = RED;
                    } else if (value > 1500 && value <= 2500) {
                        float rate = (float) (value - 1500) / 1000;
                        mRingAndCircleColor = (int) mArgbEvaluator.evaluate(rate, RED, YELLOW);
                        //设置圆环的渐变色
                        int startColor = mRingAndCircleColor & 0x00FFFFFF;
                        START_COLOR = startColor;
                        END_COLOR = mRingAndCircleColor;
                    } else if (value > 2500 && value <= 3500) {
                        float rate = (float) (value - 2500) / 1000;
                        mRingAndCircleColor = (int) mArgbEvaluator.evaluate(rate, YELLOW, GREEN);
                        //设置圆环的渐变色
                        int startColor = mRingAndCircleColor & 0x00FFFFFF;
                        START_COLOR = startColor;
                        END_COLOR = mRingAndCircleColor;
                    } else if (value > 3500) {
                        mRingAndCircleColor = GREEN;
                        START_COLOR = LIGHT_GREEN;
                        END_COLOR = GREEN;
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
                        mRocketTranslateY = - rate * Y70DP;
                    } else if (value >= 1000 && value < 1500) {
                        //下移3dp
                        float rate = (float) (value - 1000) / 500;
                        mRocketTranslateY =  - Y70DP + rate * Y3DP;
                    } else if (value >= 1500 && value < 2500) {
                        //上移3dp
                        float rate = (float) (value - 1500) / 1000;
                        mRocketTranslateY =  - Y70DP + Y3DP - rate * Y3DP;
                    } else if (value >= 2500 && value < 3500) {
                        //抖动
                        //100毫秒1次
                        int time = (value - 2500) / 100;
                        if (mShakeTimes != time) {
                            float offsetX = -RANDOM_X_START + (float) (Math.random() * RANDOM_X_RANGE);
                            float offsetY = -RANDOM_Y_START + (float) (Math.random() * RANDOM_Y_RANGE);
                            mRocketTranslateX =  offsetX;
                            mRocketTranslateY = - Y70DP + offsetY;
                            mShakeTimes = time;
                        }
                    } else if (value >= 3500 && value < 4000) {
                        //用于摆正火箭
                        mRocketTranslateY = - Y70DP;
                        mRocketTranslateX = 0;
                    } else if (value >= 4000 && value < 4100) {
                        //下移3dp
                        float rate = (float) (value - 4000) / 100;
                        mRocketTranslateY = - Y70DP + rate * Y3DP;
                    } else if (value >= 4100 && value < 4250) {
                        //上移80dp
                        float rate = (float) (value - 4100) / 150;
                        mRocketTranslateY =  - Y70DP + Y3DP - rate * Y80DP;
                    }

                    /**
                     * 雨的动画 {
                     *     1. 下降自己的高度 + 球的高度 -- (1000-2500)
                     *     2. 再次降临 距离与上面一样 -- (4000-4250)
                     * }
                     * */
                    if (value >= 1000 && value < 2500) {
                        float rate = (float) (value - 1000) / 1500;
                        mStarRainTranslateY = rate * (mStarRain.getMeasuredHeight() + CIRCLE_RADIUS * 2);
                    } else if (value >= 4000 && value < 4250) {
                        float rate = (float) (value - 4000) / 250;
                        mStarRainTranslateY = rate * (mStarRain.getMeasuredHeight() + CIRCLE_RADIUS * 2);
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
                        mFireSmallScale = (0.4f + rate * 0.6f);
                        mFireBigScale = 0.6f + rate * 0.9f;
                        mFireAlpha = rate;

                        mFireSmallTranslateY = rate * Y45DP;
                        mFireBigTranslateY = rate * Y40DP;
                    }
                }
            });
            mValueAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    if (mUiFrameHandler != null) {
                        mUiFrameHandler.stopRefresh();
                    }
                    mSquareImg.setLayerType(View.LAYER_TYPE_NONE, null);
                    mRocket.setLayerType(View.LAYER_TYPE_NONE, null);
                    mStarRain.setLayerType(View.LAYER_TYPE_NONE, null);
                    mFireBigCircle.setLayerType(View.LAYER_TYPE_NONE, null);
                    mFireSmallCircle.setLayerType(View.LAYER_TYPE_NONE, null);
                    mCircleMask.setLayerType(View.LAYER_TYPE_NONE, null);
                    mMark.setLayerType(View.LAYER_TYPE_NONE, null);
                }

                @Override
                public void onAnimationStart(Animator animation) {
                    super.onAnimationStart(animation);
                    mSquareImg.setLayerType(View.LAYER_TYPE_HARDWARE, null);
                    mRocket.setLayerType(View.LAYER_TYPE_HARDWARE, null);
                    mStarRain.setLayerType(View.LAYER_TYPE_HARDWARE, null);
                    mFireBigCircle.setLayerType(View.LAYER_TYPE_HARDWARE, null);
                    mFireSmallCircle.setLayerType(View.LAYER_TYPE_HARDWARE, null);
                    mCircleMask.setLayerType(View.LAYER_TYPE_HARDWARE, null);
                    mMark.setLayerType(View.LAYER_TYPE_HARDWARE, null);
                }
            });

            mValueAnimator.setInterpolator(new LinearInterpolator());
            mValueAnimator.setDuration(4500);
            //前250毫秒 啥也没有发生
            mValueAnimator.setStartDelay(START_DELAY);
            mUiFrameHandler = new FrameUiHandler();
            mArgbEvaluator = new ArgbEvaluator();
        }
    }

    /**
     * 开始加速动画
     */
    public void startCommonBoostAnim() {
        initAnim();
        if (mUiFrameHandler != null) {
            //逐帧动画 开始
//            setLayerType(View.LAYER_TYPE_HARDWARE, null);
            mUiFrameHandler.startRefreshFrame(new Runnable() {
                @Override
                public void run() {
                   refresh();
                }
            }, START_DELAY);
            //属性动画开始执行
            if (mValueAnimator != null) {
                mValueAnimator.start();
            }
        }
    }

    /**
     * 刷新
     * */
    private void refresh() {
        //颜色开始位置与结束位置
        mSquareImg.setBackgroundColor(mRingAndCircleColor);
//        mGradualRing.setRotateColor((int) mRingRotationAngle, START_COLOR, END_COLOR);
//        mGradualRing.setScaleX(mRingScale);
//        mGradualRing.setScaleY(mRingScale);
//        mGradualRing.setAlpha(mRingAlpha);
        mMark.setAlpha(mMarkAlpha);
        mRocket.setTranslationY(mRocketTranslateY);
        mRocket.setTranslationX(mRocketTranslateX);
        mStarRain.setTranslationX(mStarRainTranslateX);
        mStarRain.setTranslationY(mStarRainTranslateY);
        mFireBigCircle.setScaleX(mFireBigScale);
        mFireBigCircle.setScaleY(mFireBigScale);
        mFireBigCircle.setAlpha(mFireAlpha);
        mFireBigCircle.setTranslationY(mFireBigTranslateY);
        mFireSmallCircle.setScaleX(mFireSmallScale);
        mFireSmallCircle.setScaleY(mFireSmallScale);
        mFireSmallCircle.setAlpha(mFireAlpha);
        mFireSmallCircle.setTranslationY(mFireSmallTranslateY);
    }

    /**
     * 渐变的两段圆环
     */
    private class GradualRing extends View {
        private int mWidth, mHeight;
        //环的画笔 中间球的画笔 火焰画笔 对勾画笔 其他物件的画笔 遮罩层画笔
        private Paint mRingPaint1, mRingPaint2;
        //环区域 球区域 火焰大小球的区域 对勾的区域
        private RectF mRingRectF;
        //对于Shader进行旋转
        private Matrix mMatrix, mMatrix1;
        //环的第一段的默认开始角度
        private final int RING_START_ANGLE_1 = 90;
        //环的第二段的默认开始角度
        private final int RING_START_ANGLE_2 = 270;
        //扫过的角度
        private final int RING_SWEEP_ANGLE = 90;
        //环旋转的角度
        private float mRingRotationAngle;

        public GradualRing(Context context) {
            super(context);
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);
            if (oldh != h || w != oldw) {
                mWidth = w;
                mHeight = h;
                mRingPaint1 = new Paint(ANTI_ALIAS_FLAG);
                mRingPaint2 = new Paint(ANTI_ALIAS_FLAG);
                mRingPaint1.setStyle(Paint.Style.FILL);
                mRingPaint2.setStyle(Paint.Style.FILL);
                float halfRingWidth = RING_WIDTH / 2;
                //四边要间隔出四个halfRingWidth 距离
                mRingRectF = new RectF(halfRingWidth, halfRingWidth, mWidth - halfRingWidth, mHeight - halfRingWidth);
            }
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            Log.w("nieyh", "GradualRing >> onDraw");
            //绘制环
            canvas.drawArc(mRingRectF, RING_START_ANGLE_1 + mRingRotationAngle, RING_SWEEP_ANGLE, false, mRingPaint1);
            canvas.drawArc(mRingRectF, RING_START_ANGLE_2 + mRingRotationAngle, RING_SWEEP_ANGLE, false, mRingPaint2);
        }

        /**
         * 设置环的颜色
         *
         * @param startColor 开始颜色
         * @param endColor   结束颜色
         */
        public void setRotateColor(int rotate, int startColor, int endColor) {
            mRingRotationAngle = rotate;
            if (mRingPaint1 != null) {
                if (mMatrix == null) {
                    mMatrix = new Matrix();
                }
                mMatrix.setRotate(mRingRotationAngle + 85, mWidth / 2, mHeight / 2);
                SweepGradient sweepGradient = new SweepGradient(mWidth / 2, mHeight / 2, new int[]{startColor, endColor}, new float[]{0, 0.25f});
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
                SweepGradient sweepGradient = new SweepGradient(mWidth / 2, mHeight / 2, new int[]{startColor, endColor}, new float[]{0, 0.25f});
                sweepGradient.setLocalMatrix(mMatrix1);
                mRingPaint2.setStrokeCap(Paint.Cap.ROUND);
                mRingPaint2.setShader(sweepGradient);
                mRingPaint2.setStrokeWidth(RING_WIDTH);
                mRingPaint2.setStyle(Paint.Style.STROKE);
            }
            //无法避免的重绘
            GradualRing.this.invalidate();
        }
    }

    /**
     * 火箭后面的火焰球
     */
    private class FireCircle extends View {
        private int mWidth, mHeight;
        private Paint mCirclePaint;
        public FireCircle(Context context) {
            super(context);
            mCirclePaint = new Paint();
            mCirclePaint.setAntiAlias(true);
            mCirclePaint.setColor(Color.WHITE);
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);
            if (w != oldw || h != oldh) {
                mWidth = w;
                mHeight = h;
            }
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            Log.w("nieyh", "FireCircle >> onDraw");
            //画一个圆
            canvas.drawCircle(mWidth / 2, mHeight / 2, mWidth / 2, mCirclePaint);
        }
    }

    /**
     * 中心圆遮罩
     */
    private class CircleMask extends View {
        //遮罩画笔
        private Paint mMaskPaint;
        //背景画笔
        private Paint mBgPaint;
        private int mWidth, mHeight;

        public CircleMask(Context context) {
            super(context);
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);

            if (w != oldw || h != oldh) {
                mWidth = w;
                mHeight = h;
                mMaskPaint = new Paint();
                mMaskPaint.setAntiAlias(true);
                mMaskPaint.setColor(Color.WHITE);
                mMaskPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));

                mBgPaint = new Paint();
                mBgPaint.setStyle(Paint.Style.FILL);
                mBgPaint.setAntiAlias(true);
                mBgPaint.setColor(Color.WHITE);
            }
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            Log.w("nieyh", "CircleMask >> onDraw");
            //必须使用saveLayer 不然会有黑色背景会被合并到图层中
            int layerId = canvas.saveLayer(0, 0, getWidth(), getHeight(), null, Canvas.ALL_SAVE_FLAG);
            //生成一个空心圆周围白色的矩形遮罩
            canvas.drawRect(0, 0, mWidth, mHeight, mBgPaint);
            canvas.drawCircle(mWidth / 2, mHeight / 2, CIRCLE_RADIUS, mMaskPaint);
            //将这个层合并到整个视图中
            canvas.restoreToCount(layerId);
        }
    }
}
