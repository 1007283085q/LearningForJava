package com.begin.code;

import org.json.JSONObject;

import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @ClassName AboutBlockingQueueTest
 * @Description TODO:
 * @Author quanjiaxing
 * @Date 2023/4/25 11:05
 * @Version 1.0
 **/
public class AboutBlockingQueueTest {


    static class Producer implements Runnable {
        private int taskId = 0;
        private final BlockingQueue queue;

        Producer(BlockingQueue q) {
            queue = q;
        }

        public void run() {
            try {
                while (true) {
                    queue.put(produce());
                    taskId++;
                    Random random = new Random();
                    int randomNumber = random.nextInt(1501) + 500;
                    Thread.sleep(randomNumber);
                }
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        Object produce() throws Exception {
            return new JSONObject().put("1", "当前是第" + taskId + "任务");
        }
    }

    static class Consumer implements Runnable {
        private final BlockingQueue queue;

        Consumer(BlockingQueue q) {
            queue = q;
        }

        public void run() {
            try {
                while (true) {
                    consume(queue.take());
                }
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        void consume(Object x) throws Exception {
            JSONObject temp = (JSONObject) x;
            Thread thread = Thread.currentThread();
            System.out.println(thread.getName() + "|开始消费|" + temp.getString("1"));
            Thread.sleep(2000);
            System.out.println(thread.getName() + "|消费结束|" + temp.getString("1"));
        }
    }

    public static void main(String[] args) {
        BlockingQueue q = new LinkedBlockingQueue();
        Producer p = new Producer(q);
        Consumer c1 = new Consumer(q);
        Consumer c2 = new Consumer(q);
//        Consumer c3 = new Consumer(q);
//        new Thread(p).start();
//        new Thread(c1, "当前是threadC1执行消费").start();
//        new Thread(c2, "当前是threadC2执行消费").start();
//        new Thread(c3, "当前是threadC3执行消费").start();
        //为什么会是两个线程交替执行呢，唤醒问题
        //如果消费速度比生产速度慢，新的线程会自动争夺后续的新插入任务，猜测有唤醒所有线程的方法
        ExecutorService executor =
                new ThreadPoolExecutor(3, 3, 0L, TimeUnit.MILLISECONDS, q,
                       new NamedThreadFactory());


        while (true){
            executor.execute(() -> {
                Thread thread = Thread.currentThread();
                System.out.println(thread.getName() + "|开始消费|");
                try {
                    Thread.sleep(5000);
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    private static class NamedThreadFactory implements ThreadFactory {
        private final AtomicInteger poolNumber = new AtomicInteger(1);
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        public NamedThreadFactory() {
            namePrefix = "mypool-" + poolNumber.getAndIncrement() + "-thread-";
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, namePrefix + threadNumber.getAndIncrement());
            if (t.isDaemon()) {
                t.setDaemon(false);
            }
            if (t.getPriority() != Thread.NORM_PRIORITY) {
                t.setPriority(Thread.NORM_PRIORITY);
            }
            return t;
        }

        public void threadDestroyed() {
            threadNumber.decrementAndGet();
        }
    }

}
