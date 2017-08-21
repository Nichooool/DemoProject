package me.nieyh.example.anim;

/**
 * 绘制工具类
 * 功能：相对屏幕百分比转化为实际像素值，罩子层的实际显示区域， 不能分辨率下的等比转换
 *
 */
public class DrawUtils {
    public static float sDensity = 1.0f;

    /**
     * dip/dp转像素
     *
     * @param dipValue dip或 dp大小
     * @return 像素值
     */
    public static int dip2px(float dipValue) {
        sDensity = TestApplication.getAppContext().getResources()
                .getDisplayMetrics().density;
        return (int) (dipValue * sDensity + 0.5f);
    }

}
