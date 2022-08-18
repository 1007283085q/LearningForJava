package juc.learning;

/**
 * 虚假唤醒问题
 */
public class Learning {
    public static void main(String[] args) {
        Product product = new Product();
        new Thread(() -> {
            for(int i=0;i<10;i++){
                try {
                    product.push();
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, "生产者A").start();
        new Thread(() -> {
            for(int i=0;i<10;i++){
                try {
                    product.push();
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, "生产者B").start();
        new Thread(() -> {
            for(int i=0;i<10;i++){
                try {
                    product.pop();
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, "消费者A").start();
        new Thread(() -> {
            for(int i=0;i<10;i++){
                try {
                    product.pop();
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, "消费者B").start();
    }
}

class Product {
    private int product = 0;
    public synchronized void push() throws InterruptedException {
        if(product > 0){
            this.wait();
        }
        product++;
        System.out.println(Thread.currentThread().getName() + "添加产品，剩余" + product + "件产品");
        this.notifyAll();
    }
    public synchronized void pop() throws InterruptedException {
        if(product == 0){
            this.wait();
        }
        product--;
        System.out.println(Thread.currentThread().getName() + "使用产品，剩余" + product + "件产品");
        this.notifyAll();
    }
}
