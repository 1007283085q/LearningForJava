package juc.learning;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 虚假唤醒问题
 */
public class Learning {
    public static void main(String[] args) {
        Product product = new Product();
        new Thread(() -> {
            for (int i = 0; i < 10; i++) {
                try {
                    product.push();
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, "生产者A").start();
        new Thread(() -> {
            for (int i = 0; i < 10; i++) {
                try {
                    product.push();
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, "生产者B").start();
        new Thread(() -> {
            for (int i = 0; i < 10; i++) {
                try {
                    product.pop();
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, "--消费者A").start();
        new Thread(() -> {
            for (int i = 0; i < 10; i++) {
                try {
                    product.pop();
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, "--消费者B").start();
    }
}

class Product {
    private int product = 0;
    private Lock lock = new ReentrantLock();
    private Condition condition1 = lock.newCondition();
    private Condition condition2 = lock.newCondition();

    private ReadWriteLock readWriteLock=new ReentrantReadWriteLock();
    private Condition condition3 =readWriteLock.readLock().newCondition();
    public void push() throws InterruptedException {
        lock.lock();
        try {
            while (product > 0) {
                condition1.await();
            }
            product++;
            System.out.println(Thread.currentThread().getName() + "添加产品，剩余" + product + "件产品");
            condition2.signalAll();
        }
        finally {
            lock.unlock();
        }

    }

    public void pop() throws InterruptedException {
        lock.lock();
        try {
            while (product == 0) {
                condition2.await();
            }
            product--;
            System.out.println(Thread.currentThread().getName() + "使用产品，剩余" + product + "件产品");
            condition1.signalAll();
        }
        finally {
            lock.unlock();
        }

    }
}
