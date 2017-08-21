package me.nieyh.example.anim;

import android.os.Handler;
import android.os.Looper;

/**
 * Created by nieyh on 17-6-23.
 */

public class FrameUiHandler {

    //人眼与大脑之间的协作无法感知超过60fps的画面更新。 所以也就是最低一帧是16ms
    private final long COMMON_FRAME_TIME = 16;
    //UI线程刷新的处理器
    private Handler mUiFrameHandler = new Handler(Looper.getMainLooper());

    /**
     * 开始刷新帧
     * */
    public void startRefreshFrame(final Runnable onFramenAction, long delayTime) {
        mUiFrameHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                onFramenAction.run();
                mUiFrameHandler.postDelayed(this, COMMON_FRAME_TIME);
            }
        }, delayTime);
    }

    public void startRefreshFrame(final Runnable onFramenAction) {
        mUiFrameHandler.post(new Runnable() {
            @Override
            public void run() {
                onFramenAction.run();
                mUiFrameHandler.postDelayed(this, COMMON_FRAME_TIME);
            }
        });
    }

    /**
     * 停止刷新
     * */
    public void stopRefresh() {
        mUiFrameHandler.removeCallbacksAndMessages(null);
    }
}
