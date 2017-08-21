package me.nieyh.example.anim.clean;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.support.annotation.FloatRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;

import me.nieyh.example.anim.DrawUtils;
import me.nieyh.example.anim.FrameUiHandler;
import me.nieyh.example.anim.view.R;

/**
 * Created by nieyh on 17-8-9.
 * 简单的清理动画视图 <br/>
 * <note>
 * 该动画可以通过两种方式实现： <br/>
 * 1. 自定义View <br/>
 * 2. 自定义ViewGroup <br/>
 * 通过阅读google官方推荐的动画性能优化相关代码，官方更加建议自定义ViewGroup，因为不恰当的自定义View会造成GPU资源过于浪费（具体细节请自行上google官方网站查阅）<br/>
 * </note>
 * 布局排版: <br/>
 * <ol>
 * <li>各种纸带</li>
 * <li>垃圾桶</li>
 * </ol>
 * 实现思路: <br/>
 * <ol>
 * <li>纸带通过自定义贝塞尔曲线数据计算器取值来定位x、y坐标</li>
 * <li>垃圾桶分为三部分（垃圾桶盖、垃圾桶进度条、垃圾桶整体），垃圾桶盖通过指定盖的右下角为旋转轴心来进行旋转，垃圾桶进度条自定义View来进行绘制，垃圾桶整体通过指定底部中心为轴心来进行旋转</li>
 * </ol>
 */

public class CommonCleanAnimView extends FrameLayout {

    //纸带
    private View[] mPaperTapeViewArray = new View[9];
    private TrashCanLayout mTrashCanLayout;
    //动画是否自动开始
    private boolean isAnimAutoStart = true;

    public CommonCleanAnimView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        for (int i = 0; i < mPaperTapeViewArray.length; i++) {
            mPaperTapeViewArray[i] = new View(getContext());
        }
        mPaperTapeViewArray[0].setBackgroundResource(R.drawable.paper_tape_0);
        mPaperTapeViewArray[1].setBackgroundResource(R.drawable.paper_tape_1);
        mPaperTapeViewArray[2].setBackgroundResource(R.drawable.paper_tape_2);
        mPaperTapeViewArray[3].setBackgroundResource(R.drawable.paper_tape_3);
        mPaperTapeViewArray[4].setBackgroundResource(R.drawable.paper_tape_4);
        mPaperTapeViewArray[5].setBackgroundResource(R.drawable.paper_tape_5);
        mPaperTapeViewArray[6].setBackgroundResource(R.drawable.paper_tape_6);
        mPaperTapeViewArray[7].setBackgroundResource(R.drawable.paper_tape_7);
        mPaperTapeViewArray[8].setBackgroundResource(R.drawable.paper_tape_8);
        mTrashCanLayout = new TrashCanLayout(getContext());
        for (int i = 0; i < mPaperTapeViewArray.length; i++) {
            addView(mPaperTapeViewArray[i]);
        }
        addView(mTrashCanLayout);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        int height = bottom - top;
        int width = right - left;
        //通过垃圾桶的宽高计算结果来决定定位的位置
        int trashCanWidth = mTrashCanLayout.getMeasuredWidth();
        int trashCanHeight = mTrashCanLayout.getMeasuredHeight();

        int trashCanLeft = (width - trashCanWidth) / 2;
        int trashCanTop = height - trashCanHeight;
        int trashCanRight = trashCanLeft + trashCanWidth;
        int trashCanBottom = trashCanTop + trashCanHeight;
        mTrashCanLayout.layout(trashCanLeft, trashCanTop, trashCanRight, trashCanBottom);
        mTrashCanLayout.setPivotX(trashCanWidth / 2f);
        mTrashCanLayout.setPivotY(trashCanHeight);
        safe2InitAnimator();
        for (int i = 0; i < mPaperTapeViewArray.length; i++) {
            mPaperTapeViewArray[i].layout((int) mStartPointF[i].x,
                    (int) mStartPointF[i].y,
                    (int) mStartPointF[i].x + mPaperTapeViewArray[i].getMeasuredWidth(),
                    (int) mStartPointF[i].y + mPaperTapeViewArray[i].getMeasuredHeight());
        }
        if (isAnimAutoStart) {
            startAnim();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int wMode = MeasureSpec.getMode(widthMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        int defaultWidth = getContext().getResources().getDisplayMetrics().widthPixels;

        for (int i = 0; i < getChildCount(); i++) {
            final View child = getChildAt(i);
            if (child.getBackground() != null) {
                //如果是彩纸的时候 直接限定宽高
                Drawable backgroundDrawable = child.getBackground();
                int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(backgroundDrawable.getIntrinsicWidth(), MeasureSpec.EXACTLY);
                int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(backgroundDrawable.getIntrinsicHeight(), MeasureSpec.EXACTLY);
                child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
            } else {
                //传入父布局传入的指定值 但是TrashCanLayout并不会理睬
                child.measure(widthMeasureSpec, heightMeasureSpec);
            }
        }

        //高度由LayoutParam决定
        if (wMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(defaultWidth, height);
        } else {
            setMeasuredDimension(width, height);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        if (mValueAnimator != null) {
            mValueAnimator.cancel();
            mValueAnimator.removeAllUpdateListeners();
            mValueAnimator.removeAllListeners();
        }
        super.onDetachedFromWindow();
    }


    /**
     * 垃圾桶整体
     * <ol>
     * <li>垃圾桶盖儿 - 直接使用View</li>
     * <li>垃圾桶底座 - 直接使用View</li>
     * <li>垃圾桶进度条 - 自定义View</li>
     * </ol>
     */
    private class TrashCanLayout extends FrameLayout {
        //进度条背景色
        private final int PROGRESS_BACKGROUND_COLOR = 0xff2d907a;
        //垃圾桶底部宽度89dp
        private final int TRASH_BOTTOM_WIDTH = DrawUtils.dip2px(89);
        //垃圾桶的高度84dp
        private final int TRASH_BOTTOM_HEIGHT = DrawUtils.dip2px(84);
        //垃圾桶进度条 边距(底部边距)
        private final int TRASH_PROGRESS_MARGIN_B = DrawUtils.dip2px(10);
        //垃圾桶进度条 边距(左右)
        private final int TRASH_PROGRESS_MARGIN_LR = DrawUtils.dip2px(21);
        //垃圾桶进度条的宽度
        private final int TRASH_PROGRESS_WIDTH = DrawUtils.dip2px(14);
        //垃圾桶进度条的高度
        private final int TRASH_PROGRESS_HEIGHT = DrawUtils.dip2px(61);
        //垃圾桶的盖子的宽度
        private final int TRASH_COVER_WIDTH = DrawUtils.dip2px(112);
        //垃圾桶的盖子的高度
        private final int TRASH_COVER_HEIGHT = DrawUtils.dip2px(31);
        //垃圾桶的盖子的底部边距
        private final int TRASH_COVER_MARGIN_B = DrawUtils.dip2px(7);
        //垃圾桶盖子
        private View mTrashCoverView;
        //垃圾桶底部
        private View mTrashBottomView;
        //左右进度条
        private TrashProgressBar mLeftTrashProgressBar, mRightTrashProgressBar;

        public TrashCanLayout(@NonNull Context context) {
            super(context);
            initView(context);
        }

        public TrashCanLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
            super(context, attrs);
            initView(context);
        }

        private void initView(Context context) {
            mTrashCoverView = new View(context);
            mTrashCoverView.setBackgroundResource(R.drawable.img_trash_cover);
            mTrashBottomView = new View(context);
            mTrashBottomView.setBackgroundResource(R.drawable.img_trash_bottom);
            mLeftTrashProgressBar = new TrashProgressBar(context);
            mLeftTrashProgressBar.setProgress(0);
            mRightTrashProgressBar = new TrashProgressBar(context);
            mRightTrashProgressBar.setProgress(0);
            addView(mTrashCoverView);
            addView(mTrashBottomView);
            addView(mLeftTrashProgressBar);
            addView(mRightTrashProgressBar);
        }


        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            //强制自己计算宽高 不理会父布局的传值与处理
            //宽度以垃圾桶盖的最大展示范围来计算 左边与右边对称
            //有些值不参与判断 比如盖子的宽度与底部的宽度谁宽 直接认为盖子宽
            //这些值会比实际占据的宽高大一点 并不妨碍
            int width = TRASH_COVER_WIDTH * 3;
            int height = TRASH_COVER_WIDTH + TRASH_COVER_MARGIN_B + TRASH_BOTTOM_HEIGHT;
            for (int i = 0; i < getChildCount(); i++) {
                final View child = getChildAt(i);
                if (child.getBackground() != null) {
                    //垃圾桶桶与盖儿要根据图片进行计算宽高
                    Drawable backgroundDrawable = child.getBackground();
                    int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(backgroundDrawable.getIntrinsicWidth(), MeasureSpec.EXACTLY);
                    int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(backgroundDrawable.getIntrinsicHeight(), MeasureSpec.EXACTLY);
                    child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
                } else {
                    //进度条的话就直接也强制计算 此处其实多此一举 onlayout中已经布局好了 getWidth也会有数值
                    int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(TRASH_PROGRESS_WIDTH, MeasureSpec.EXACTLY);
                    int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(TRASH_PROGRESS_HEIGHT, MeasureSpec.EXACTLY);
                    child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
                }
            }
            setMeasuredDimension(width, height);
        }

        @Override
        protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
            super.onLayout(changed, left, top, right, bottom);
            int width = right - left;
            int height = bottom - top;
            //定位
            int coverTop = height - TRASH_BOTTOM_HEIGHT - TRASH_COVER_HEIGHT - TRASH_COVER_MARGIN_B;
            int coverLeft = (width - TRASH_COVER_WIDTH) / 2;
            int coverRight = coverLeft + TRASH_COVER_WIDTH;
            int coverBottom = coverTop + TRASH_COVER_HEIGHT;
            mTrashCoverView.layout(coverLeft, coverTop, coverRight, coverBottom);
            //设置旋转点右下角
            mTrashCoverView.setPivotX(mTrashCoverView.getMeasuredWidth());
            mTrashCoverView.setPivotY(mTrashCoverView.getMeasuredHeight());
            int bottomTop = height - TRASH_BOTTOM_HEIGHT;
            int bottomLeft = (width - TRASH_BOTTOM_WIDTH) / 2;
            int bottomRight = bottomLeft + TRASH_BOTTOM_WIDTH;
            int bottomBottom = bottomTop + TRASH_BOTTOM_HEIGHT;
            mTrashBottomView.layout(bottomLeft, bottomTop, bottomRight, bottomBottom);

            int leftProgressTop = height - TRASH_PROGRESS_MARGIN_B - TRASH_PROGRESS_HEIGHT;
            int leftProgressLeft = bottomLeft + TRASH_PROGRESS_MARGIN_LR;
            int leftProgressRight = leftProgressLeft + TRASH_PROGRESS_WIDTH;
            int leftProgressBottom = leftProgressTop + TRASH_PROGRESS_HEIGHT;
            mLeftTrashProgressBar.layout(leftProgressLeft, leftProgressTop, leftProgressRight, leftProgressBottom);

            int rightProgressTop = leftProgressTop;
            int tempValue = bottomRight - TRASH_PROGRESS_MARGIN_LR;
            int rightProgressLeft = tempValue - TRASH_PROGRESS_WIDTH;
            int rightProgressRight = tempValue;
            int rightProgressBottom = leftProgressBottom;
            mRightTrashProgressBar.layout(rightProgressLeft, rightProgressTop, rightProgressRight, rightProgressBottom);
        }

        public void setLeftProgress(@FloatRange(from = 0f, to = 1f) float progress) {
            mLeftTrashProgressBar.setProgress(progress);
        }

        public void setRightProgress(@FloatRange(from = 0f, to = 1f) float progress) {
            mRightTrashProgressBar.setProgress(progress);
        }

        public void setTrashCoverRotate(int rotate) {
            mTrashCoverView.setRotation(rotate);
        }

        /**
         * 垃圾加载进度条
         */
        private class TrashProgressBar extends View {

            private float mProgress = 0;
            private RectF mProgressForegroundRectF = new RectF();
            private RectF mProgressBackgroundRectF = new RectF();
            private Paint mProgressForegroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            private Paint mProgressBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

            public TrashProgressBar(Context context) {
                super(context);
                //颜色
                mProgressForegroundPaint.setColor(Color.WHITE);
                mProgressBackgroundPaint.setColor(PROGRESS_BACKGROUND_COLOR);
                //实心
                mProgressForegroundPaint.setStyle(Paint.Style.FILL);
                mProgressBackgroundPaint.setStyle(Paint.Style.FILL);
            }

            @Override
            protected void onSizeChanged(int w, int h, int oldw, int oldh) {
                super.onSizeChanged(w, h, oldw, oldh);
                if (w != oldw || h != oldh) {
                    setProgress(mProgress);
                }
            }

            /**
             * 设置进度条进度
             *
             * @param progress 0 ~ 1 之间
             */
            public void setProgress(float progress) {
                if (mProgress == progress) {
                    return;
                }
                mProgress = progress;
                //剩余高度的比率
                float remainRate = 1 - progress;
                int height = getHeight();
                int width = getWidth();
                //定位线段的位置信息
                mProgressForegroundRectF.left = 0 ;
                mProgressForegroundRectF.top = remainRate * height ;
                mProgressForegroundRectF.right = width;
                mProgressForegroundRectF.bottom = height;
                mProgressBackgroundRectF.left = 0 ;
                mProgressBackgroundRectF.top = 0 ;
                mProgressBackgroundRectF.right = width;
                mProgressBackgroundRectF.bottom = height;
                //设置画笔的粗度
                mProgressForegroundPaint.setStrokeWidth(1);
                mProgressBackgroundPaint.setStrokeWidth(1);
                this.invalidate();
            }

            @Override
            protected void onDraw(Canvas canvas) {
                super.onDraw(canvas);
                //先画背景
                canvas.drawRoundRect(mProgressBackgroundRectF, getWidth() / 2f, getWidth() / 2f, mProgressBackgroundPaint);
                //再画前景
                canvas.drawRoundRect(mProgressForegroundRectF, getWidth() / 2f, getWidth() / 2f, mProgressForegroundPaint);
            }
        }
    }

    /****
     * 动画部分 <br/>
     * <note>
     *     【善意的提醒：ValueAnimator 作用已经不再是作为动画触发器了，不要再用这个来直接进行动画处理（会造成性能严重浪费，太多冗余的绘制），ValueAnimator只是一个动画数值计算器。】
     * </note>
     * **/
    //属性动画
    private ValueAnimator mValueAnimator;
    //贝塞尔数值计算器数组
    private BezierEvaluator[] mBezierEvaluators = new BezierEvaluator[9];
    //垃圾桶的尺寸
    private float mTrashCanSize;
    //垃圾桶透明度
    private float mTrashCanAlpha = 1;
    //垃圾桶盖子的旋转角度
    private int mTrashCanCoverRotate;
    //垃圾桶整体的旋转角度
    private int mTrashCanRotate;
    //垃圾箱进度条(左右)
    private float mTrashCanLeftProgress, mTrashCanRightProgress;
    //纸带结束位置
    private PointF mEndPointF;
    //纸带开始位置
    private PointF[] mStartPointF = new PointF[9];
    //纸带当前位置
    private PointF[] mCurrentPointF = new PointF[9];
    //纸带的旋转角度
    private int[] mPaperTapeRotateArray = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0};
    //动画帧刷新器
    private FrameUiHandler mFrameUiHandler;
    //纸张出现的次数
    private int mPaperTapeTimes = 0;

    /**
     * 开始执行动画 <br/>
     * {@link #setAnimAutoStart(boolean)} 请设置不自动开始 才可以使用此方法
     */
    public void startAnim() {
        if (mValueAnimator != null && mValueAnimator.isStarted()) {
            return;
        }
        mFrameUiHandler = new FrameUiHandler();
        mValueAnimator.start();
        mFrameUiHandler.startRefreshFrame(new Runnable() {
            @Override
            public void run() {
                refreshView();
            }
        });
    }

    public void setAnimAutoStart(boolean animAutoStart) {
        isAnimAutoStart = animAutoStart;
    }

    /**
     * 刷新页面
     */
    private void refreshView() {
        mTrashCanLayout.setScaleX(mTrashCanSize);
        mTrashCanLayout.setScaleY(mTrashCanSize);
        mTrashCanLayout.setRotation(mTrashCanRotate);
        mTrashCanLayout.setAlpha(mTrashCanAlpha);
        mTrashCanLayout.setTrashCoverRotate(mTrashCanCoverRotate);

        mTrashCanLayout.setLeftProgress(mTrashCanLeftProgress);
        mTrashCanLayout.setRightProgress(mTrashCanRightProgress);

        for (int i = 0; i < mPaperTapeViewArray.length; i++) {
            mPaperTapeViewArray[i].setX(mCurrentPointF[i].x);
            mPaperTapeViewArray[i].setY(mCurrentPointF[i].y);
            mPaperTapeViewArray[i].setRotation(mPaperTapeRotateArray[i]);
        }
    }

    /**
     * 初始化动画
     */
    private void safe2InitAnimator() {
        if (mValueAnimator != null) {
            return;
        }
        //初始化控制点
        initBezierControlPoint();
        //总共4720毫秒
        mValueAnimator = ValueAnimator.ofInt(0, 4720);
        mValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = (int) animation.getAnimatedValue();
                /**
                 * 垃圾桶整体大小变化流程 <br/>
                 * <ol>
                 *     <li>从0 - 1.2 [0 - 500]</li>
                 *     <li>从1.2 - 0.9 [500 - 620]</li>
                 *     <li>从0.9 - 1 [620 - 740]</li>
                 *     <li>从1 - 0 [4240 - 4560]</li>
                 * </ol>
                 * */
                if (value > 0 && value < 500) {
                    float rate = value / 500f;
                    mTrashCanSize = 1.2f * rate;
                } else if (value > 500 && value <= 620) {
                    float rate = (value - 500) / 120f;
                    mTrashCanSize = 0.9f + 0.3f * (1 - rate);
                } else if (value > 620 && value <= 740) {
                    float rate = (value - 620) / 120f;
                    mTrashCanSize = 0.9f + 0.1f * rate;
                } else if (value > 740 && value <= 4240) {
                    mTrashCanSize = 1f;
                } else if (value > 4240 && value <= 4560) {
                    float rate = (value - 4240) / 320;
                    mTrashCanSize = 1 - rate;
                } else {
                    //设置默认值 防止数值没有归零显示
                    mTrashCanSize = 0f;
                }

                /**
                 * 垃圾桶的透明度变化 <br/>
                 * <ol>
                 *     <li>从1 - 0 [4240 - 4560]</li>
                 * </ol>
                 * */
                if (value <= 4240 ) {
                    mTrashCanAlpha = 1;
                } else if (value > 4240 && value <= 4560) {
                    float rate = (value - 4240) / 320;
                    mTrashCanAlpha = 1 - rate;
                } else {
                    mTrashCanAlpha = 0f;
                }

                /**
                 * 垃圾桶盖子旋转变化流程 <br/>
                 * <ol>
                 *     <li>0 - 218度【740 - 1000】</li>
                 *     <li>218 - 203度【1000 - 1200】</li>
                 *     <li>203 - 210度【1200 - 1320】</li>
                 *     <li>210 - 0度【3360 - 3600】</li>
                 * </ol>
                 * */
                if (value <= 740) {
                    mTrashCanCoverRotate = 0;
                } else if (value > 740 && value <= 1000) {
                    float rate = (value - 740) / 260f;
                    mTrashCanCoverRotate = (int) (rate * 218);
                } else if (value > 1000 && value <= 1200) {
                    float rate = (value - 1000) / 2000f;
                    mTrashCanCoverRotate = (int) (203 + (1 - rate) * 15);
                } else if (value > 1200 && value <= 1320) {
                    float rate = (value - 1200) / 120f;
                    mTrashCanCoverRotate = (int) (203 + (1 - rate) * 15);
                } else if (value > 1320 && value <= 3360) {
                    mTrashCanCoverRotate = 210;
                } else if (value > 3360 && value <= 3600) {
                    float rate = (value - 3360) / 240f;
                    mTrashCanCoverRotate = (int) ((1 - rate) * 210);
                } else {
                    mTrashCanCoverRotate = 0;
                }

                /**
                 * 垃圾桶震动 - 转动 变化流程 <br/>
                 * <ol>
                 *     <li>0 - 6度【3560 - 3640】</li>
                 *     <li>6 - 0度【3640 - 3720】</li>
                 *     <li>0 - (-6)度【3720 - 3800】</li>
                 *     <li>(-6) - 0度【3800 - 3880】</li>
                 *     <li>0 - 6度【3880 - 3960】</li>
                 *     <li>6 - 0度【3960 - 4040】</li>
                 * </ol>
                 * */
                if (value <= 3560) {
                    mTrashCanRotate = 0;
                } else if (value > 3560 && value <= 3640) {
                    float rate = (value - 3560) / 80f;
                    mTrashCanRotate = (int) (rate * 6);
                } else if (value > 3640 && value <= 3720) {
                    float rate = (value - 3640) / 80f;
                    mTrashCanRotate = (int) ((1 - rate) * 6);
                } else if (value > 3720 && value <= 3800) {
                    float rate = (value - 3720) / 80f;
                    mTrashCanRotate = (int) (-6 * rate);
                } else if (value > 3800 && value <= 3880) {
                    float rate = (value - 3800) / 80f;
                    mTrashCanRotate = -6 + (int) (rate * 6);
                } else if (value > 3880 && value <= 3960) {
                    float rate = (value - 3880) / 80f;
                    mTrashCanRotate = (int) (rate * 6);
                } else if (value > 3960 && value <= 4040) {
                    float rate = (value - 3960) / 80f;
                    mTrashCanRotate = (int) ((1 - rate) * 6);
                } else {
                    mTrashCanRotate = 0;
                }

                /**
                 * 垃圾桶左边进度条
                 * <ol>
                 *     <li>1000 - 2120 : 左边进度条加满</li>
                 * </ol>
                 * */
                if (value <= 1000) {
                    mTrashCanLeftProgress = 0f;
                } else if (value > 1000 && value <= 2120) {
                    float rate = (value - 1000) / 1120f;
                    mTrashCanLeftProgress = rate;
                } else {
                    mTrashCanLeftProgress = 1f;
                }


                /**
                 * 垃圾桶右边进度条 <br/>
                 * <ol>
                 *     <li>2240 - 3360 : 右边进度条加满</li>
                 * </ol>
                 * */
                if (value <= 2240) {
                    mTrashCanRightProgress = 0f;
                } else if (value > 2240 && value <= 3360) {
                    float rate = (value - 2240) / 1120f;
                    mTrashCanRightProgress = rate;
                } else {
                    mTrashCanRightProgress = 1f;
                }

                /**
                 * 纸带下落与转圈<br/>
                 * <ol>
                 *     <li>按照0 - 8 的顺序</li>
                 *     <li>以1s开始</li>
                 *     <li>每个用时0.48s</li>
                 *     <li>间隔0.08s</li>
                 *     <li>自转3周</li>
                 *     <li>每个循环用时1.12s</li>
                 *     <li>间隔0.12s 再次循环一次 共两次</li>
                 * </ol>
                 * <ol>
                 *     <li>1000-1480</li>
                 *     <li>1080-1560</li>
                 *     <li>1160-1640</li>
                 *     <li>1240-1720</li>
                 *     <li>1320-1800</li>
                 *     <li>1400-1880</li>
                 *     <li>1480-1960</li>
                 *     <li>1560-2040</li>
                 *     <li>1640-2120</li>
                 * </ol>
                 * */
                if (value <= 1000 + (1240 * mPaperTapeTimes)) {
                    mPaperTapeRotateArray[0] = 0;
                    mCurrentPointF[0] = mStartPointF[0];
                } else if (value > 1000 + (1240 * mPaperTapeTimes) && value <= 1480 + (1240 * mPaperTapeTimes)) {
                    float rate = (value - (1000 + (1240 * mPaperTapeTimes))) / 480f;
                    mPaperTapeRotateArray[0] = (int) (rate * 1080);
                    mCurrentPointF[0] = mBezierEvaluators[0].evaluate(rate, mStartPointF[0], mEndPointF);
                } else {
                    mPaperTapeRotateArray[0] = 1080;
                    mCurrentPointF[0] = mEndPointF;
                }

                if (value <= 1080 + (1240 * mPaperTapeTimes)) {
                    mPaperTapeRotateArray[1] = 0;
                    mCurrentPointF[1] = mStartPointF[1];
                } else if (value > 1080 + (1240 * mPaperTapeTimes) && value <= 1560 + (1240 * mPaperTapeTimes)) {
                    float rate = (value - (1080 + (1240 * mPaperTapeTimes))) / 480f;
                    mPaperTapeRotateArray[1] = (int) (rate * 1080);
                    mCurrentPointF[1] = mBezierEvaluators[1].evaluate(rate, mStartPointF[1], mEndPointF);
                } else {
                    mPaperTapeRotateArray[1] = 1080;
                    mCurrentPointF[1] = mEndPointF;
                }

                if (value <= 1160 + (1240 * mPaperTapeTimes)) {
                    mPaperTapeRotateArray[2] = 0;
                    mCurrentPointF[2] = mStartPointF[2];
                } else if (value > 1160 + (1240 * mPaperTapeTimes) && value <= 1640 + (1240 * mPaperTapeTimes)) {
                    float rate = (value - (1160 + (1240 * mPaperTapeTimes))) / 480f;
                    mPaperTapeRotateArray[2] = (int) (rate * 1080);
                    mCurrentPointF[2] = mBezierEvaluators[2].evaluate(rate, mStartPointF[2], mEndPointF);
                } else {
                    mPaperTapeRotateArray[2] = 1080;
                    mCurrentPointF[2] = mEndPointF;
                }

                if (value <= 1240 + (1240 * mPaperTapeTimes)) {
                    mPaperTapeRotateArray[3] = 0;
                    mCurrentPointF[3] = mStartPointF[3];
                } else if (value > 1240 + (1240 * mPaperTapeTimes) && value <= 1720 + (1240 * mPaperTapeTimes)) {
                    float rate = (value - (1240 + (1240 * mPaperTapeTimes))) / 480f;
                    mPaperTapeRotateArray[3] = (int) (rate * 1080);
                    mCurrentPointF[3] = mBezierEvaluators[3].evaluate(rate, mStartPointF[3], mEndPointF);
                } else {
                    mPaperTapeRotateArray[3] = 1080;
                    mCurrentPointF[3] = mEndPointF;
                }

                if (value <= 1320 + (1240 * mPaperTapeTimes)) {
                    mPaperTapeRotateArray[4] = 0;
                    mCurrentPointF[4] = mStartPointF[4];
                } else if (value > 1320 + (1240 * mPaperTapeTimes) && value <= 1800 + (1240 * mPaperTapeTimes)) {
                    float rate = (value - (1320 + (1240 * mPaperTapeTimes))) / 480f;
                    mPaperTapeRotateArray[4] = (int) (rate * 1080);
                    mCurrentPointF[4] = mBezierEvaluators[4].evaluate(rate, mStartPointF[4], mEndPointF);
                } else {
                    mPaperTapeRotateArray[4] = 1080;
                    mCurrentPointF[4] = mEndPointF;
                }

                if (value <= 1400 + (1240 * mPaperTapeTimes)) {
                    mPaperTapeRotateArray[5] = 0;
                    mCurrentPointF[5] = mStartPointF[5];
                } else if (value > 1400 + (1240 * mPaperTapeTimes) && value <= 1880 + (1240 * mPaperTapeTimes)) {
                    float rate = (value - (1400 + (1240 * mPaperTapeTimes))) / 480f;
                    mPaperTapeRotateArray[5] = (int) (rate * 1080);
                    mCurrentPointF[5] = mBezierEvaluators[5].evaluate(rate, mStartPointF[5], mEndPointF);
                } else {
                    mPaperTapeRotateArray[5] = 1080;
                    mCurrentPointF[5] = mEndPointF;
                }

                if (value <= 1480 + (1240 * mPaperTapeTimes)) {
                    mPaperTapeRotateArray[6] = 0;
                    mCurrentPointF[6] = mStartPointF[6];
                } else if (value > 1480 + (1240 * mPaperTapeTimes) && value <= 1960 + (1240 * mPaperTapeTimes)) {
                    float rate = (value - (1480 + (1240 * mPaperTapeTimes))) / 480f;
                    mPaperTapeRotateArray[6] = (int) (rate * 1080);
                    mCurrentPointF[6] = mBezierEvaluators[6].evaluate(rate, mStartPointF[6], mEndPointF);
                } else {
                    mPaperTapeRotateArray[6] = 1080;
                    mCurrentPointF[6] = mEndPointF;
                }

                if (value <= 1560 + (1240 * mPaperTapeTimes)) {
                    mPaperTapeRotateArray[7] = 0;
                    mCurrentPointF[7] = mStartPointF[7];
                } else if (value > 1560 + (1240 * mPaperTapeTimes) && value <= 2040 + (1240 * mPaperTapeTimes)) {
                    float rate = (value - (1560 + (1240 * mPaperTapeTimes))) / 480f;
                    mPaperTapeRotateArray[7] = (int) (rate * 1080);
                    mCurrentPointF[7] = mBezierEvaluators[7].evaluate(rate, mStartPointF[7], mEndPointF);
                } else {
                    mPaperTapeRotateArray[7] = 1080;
                    mCurrentPointF[7] = mEndPointF;
                }

                if (value <= 1640 + (1240 * mPaperTapeTimes)) {
                    mPaperTapeRotateArray[8] = 0;
                    mCurrentPointF[8] = mStartPointF[8];
                } else if (value > 1640 + (1240 * mPaperTapeTimes) && value <= 2120 + (1240 * mPaperTapeTimes)) {
                    float rate = (value - (1560 + (1240 * mPaperTapeTimes))) / 480f;
                    mPaperTapeRotateArray[8] = (int) (rate * 1080);
                    mCurrentPointF[8] = mBezierEvaluators[8].evaluate(rate, mStartPointF[8], mEndPointF);
                } else {
                    mPaperTapeRotateArray[8] = 1080;
                    mCurrentPointF[8] = mEndPointF;
                    mPaperTapeTimes = 1;
                }
            }
        });
        mValueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                refreshView();
                if (mFrameUiHandler != null) {
                    mFrameUiHandler.stopRefresh();
                }
                if (mOnAnimatorAction != null) {
                    mOnAnimatorAction.onEnd();
                }
            }

            @Override
            public void onAnimationStart(Animator animation) {
                mPaperTapeTimes = 0;
                if (mOnAnimatorAction != null) {
                    mOnAnimatorAction.onStart();
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                if (mFrameUiHandler != null) {
                    mFrameUiHandler.stopRefresh();
                }
            }
        });
        mValueAnimator.setInterpolator(new LinearInterpolator());
        mValueAnimator.setDuration(4720);
    }

    /**
     * 初始化贝塞尔曲线控制点以及计算器 <br/>
     * 起始点实现思路<br/>
     * <note>
     * 为了到达逼真的效果，此处起始点采用随机得出，从顶部进入进入5个，从左侧进入2个，从右侧进入2个.<br/>
     * 区域分别是：<br/>
     * 左侧为整体布局的高度的1/4区域 <br/>
     * 右侧同样为整体布局的高度的1/4区域 <br/>
     * 顶部为整体布局的宽度的中心1/3区域 <br/>
     * </note>
     * 控制点实现思路<br/>
     * <note>
     * 由于需要实现抛落的效果，贝塞尔曲线应该是先曲率大然后曲率小，控制点受到这几点限制.
     * </note>
     * <ol>
     * <li>限制控制点X坐标在离起始点X坐标2/5水平距离（起始与终点之间的水平距离）的位置上</li>
     * <li>左边的控制点X在右边， 右边的控制点X在左边</li>
     * <li>顶部控制点左右随机</li>
     * </ol>
     * 结束点为整体布局的中心底部
     * <b>{@link #initBezierControlPoint()} 方法必须在布局初始化完成{@link #onLayout(boolean, int, int, int, int)} 之后调用 不然无法获取准确的值</b>
     */
    private void initBezierControlPoint() {
        int width = getWidth();
        int height = getHeight();

        float rangeStartY = 0;
        float lrRange = height / 4f;

        float topRange = width / 3f;
        float topStartX = topRange;
        //初始化顺序： 右 左 左 上 右 上 上 上 上 （别想了 没有BABA）
        mStartPointF[0] = randomPointFLR(rangeStartY, lrRange, width, false);
        mStartPointF[1] = randomPointFLR(rangeStartY, lrRange, width, true);
        mStartPointF[1].x -= mPaperTapeViewArray[1].getWidth();
        mStartPointF[2] = randomPointFLR(rangeStartY, lrRange, width, true);
        mStartPointF[2].x -= mPaperTapeViewArray[2].getWidth();
        mStartPointF[3] = randomPointFTop(topStartX, topRange);
        mStartPointF[3].y -= mPaperTapeViewArray[3].getHeight();
        mStartPointF[4] = randomPointFLR(rangeStartY, lrRange, width, false);
        mStartPointF[5] = randomPointFTop(topStartX, topRange);
        mStartPointF[5].y -= mPaperTapeViewArray[5].getHeight();
        mStartPointF[6] = randomPointFTop(topStartX, topRange);
        mStartPointF[6].y -= mPaperTapeViewArray[6].getHeight();
        mStartPointF[7] = randomPointFTop(topStartX, topRange);
        mStartPointF[7].y -= mPaperTapeViewArray[7].getHeight();
        mStartPointF[8] = randomPointFTop(topStartX, topRange);
        mStartPointF[8].y -= mPaperTapeViewArray[8].getHeight();
        //结束位置
        mEndPointF = new PointF(width / 2f, height);
        //开始位置
        for (int i = 0; i < mCurrentPointF.length; i++) {
            mCurrentPointF[i] = mStartPointF[i];
        }
        //初始化贝塞尔曲线数值计算器
        for (int i = 0; i < mBezierEvaluators.length; i++) {
            mBezierEvaluators[i] = new BezierEvaluator(randomControlPointF(mStartPointF[i], mEndPointF));
        }
    }

    /**
     * 随机点左右位置
     */
    private PointF randomPointFLR(float startRange, float range, int width, boolean isLeft) {
        PointF pointF = new PointF();
        float value = (float) (startRange + Math.random() * range);
        pointF.y = value;
        if (isLeft) {
            pointF.x = 0;
        } else {
            pointF.x = width;
        }
        return pointF;
    }

    /**
     * 随机点左右位置
     */
    private PointF randomPointFTop(float startRange, float range) {
        PointF pointF = new PointF();
        float value = (float) (startRange + Math.random() * range);
        pointF.y = 0;
        pointF.x = value;
        return pointF;
    }

    /**
     * 随机控制点
     */
    private PointF randomControlPointF(PointF start, PointF end) {
        //废弃方案 ： 太多的随机值 无法把控 还是直接定死最好
        //float height = end.y - start.y;
        //float rangeStartY = start.y;
        //float rangeEndY = rangeStartY + height / 3f;
        //计算两点一线的一元一次方程 y = kx + h
        //float k = (end.y - start.y) / (end.x - start.x);
        //float h = (end.x * start.y - start.x * end.y) / (end.x - start.x);
        PointF controlPointF = new PointF();
        controlPointF.y = start.y;
        //结束点可能在左边也可能在右边 结果值带正负
        float rangeX = (end.x - start.x) * 2 / 5;
        controlPointF.x = start.x + rangeX;
        return controlPointF;
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
