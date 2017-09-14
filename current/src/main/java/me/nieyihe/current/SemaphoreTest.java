package me.nieyihe.current;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

/**
 * nichool on 2017/9/13 12:51
 * 813825509@qq.com
 */

public class SemaphoreTest {

    public void test() {
        ExecutorService executor = Executors.newCachedThreadPool();
        Semaphore semaphore = new Semaphore(2);
        for (int i = 0; i < 10; i++) {
            executor.submit(new Work(i, semaphore));
        }
        executor.shutdown();
        semaphore.acquireUninterruptibly(2);
        System.out.println("使用完毕，需要清扫了");
        semaphore.release(2);
        try {
            Thread.sleep(12000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private class Work implements Runnable {
        private int id;
        private Semaphore mWorkSemaphore;

        public Work(int id, Semaphore mWorkSemaphore) {
            this.id = id;
            this.mWorkSemaphore = mWorkSemaphore;
        }

        @Override
        public void run() {
            if(mWorkSemaphore.availablePermits() <= 0) {
                System.out.println("顾客" + id + "进入饭店，没有空座 排队.");
            } else {
                System.out.println("顾客" + id + "进入饭店，有空座！");
            }
            try {
                mWorkSemaphore.acquire();
                System.out.println("顾客" + id + "等到了空座!");
                Thread.sleep(1000);
                System.out.println("顾客" + id + "吃完饭了 离开了");
                mWorkSemaphore.release();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
