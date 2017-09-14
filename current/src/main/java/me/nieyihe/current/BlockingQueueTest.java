package me.nieyihe.current;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * nichool on 2017/9/13 13:20
 * 813825509@qq.com
 */

public class BlockingQueueTest {
    public void test() {
        ExecutorService executorService = Executors.newCachedThreadPool();
        final BlockingQueue<String> blockingQueue = new LinkedBlockingQueue(2);
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 10; i++) {
                    try {
                        blockingQueue.put("thread_1 >> " + String.valueOf(i));
                        System.out.println("thread_1 >> finish");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 10; i++) {
                    try {
                        blockingQueue.put("thread_2 >> " + String.valueOf(i));
                        System.out.println("thread_2 >> finish");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                while (!blockingQueue.isEmpty()) {
                    try {
                        System.out.println(blockingQueue.take());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        executorService.shutdown();
        try {
            Thread.sleep(30000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
