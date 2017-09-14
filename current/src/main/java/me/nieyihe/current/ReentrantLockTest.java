package me.nieyihe.current;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;

/**
 * nichool on 2017/9/13 12:58
 * 813825509@qq.com
 */

public class ReentrantLockTest {
    public void test() {
        ExecutorService executor = Executors.newCachedThreadPool();
        final ReentrantLock reentrantLock = new ReentrantLock();
        for (int i = 0; i < 10; i++) {
            final int finalI = i;
            executor.submit(new Runnable() {
                @Override
                public void run() {
                    reentrantLock.lock();
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println(finalI);
                    reentrantLock.unlock();
                }
            });
        }
        executor.shutdown();
        try {
            Thread.sleep(12000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
