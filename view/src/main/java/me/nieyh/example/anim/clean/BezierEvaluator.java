package me.nieyh.example.anim.clean;

import android.animation.TypeEvaluator;
import android.graphics.PointF;

/**
 * Created by nieyh on 17-8-9.
 * 贝塞尔曲线数值计算器
 */

public class BezierEvaluator implements TypeEvaluator<PointF> {
    private PointF mControlPoint;

    public BezierEvaluator(PointF controlPoint) {
        this.mControlPoint = controlPoint;
    }

    @Override
    public PointF evaluate(float t, PointF startValue, PointF endValue) {
        return BezierUtil.CalculateBezierPointForQuadratic(t, startValue, mControlPoint, endValue);
    }
}