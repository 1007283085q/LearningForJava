package juc.learning;

/**
 * @see Thread
 */
public class JoinTest {

    public static void main(String[] args) {
        Thread t1=new Thread(new MyThread1());
        Thread t2=new Thread(new MyThread2());
        t1.start();
        t2.start();
        try{
            t2.join();
        }catch (InterruptedException e){
            e.printStackTrace();
        }
        System.out.println("执行主方法完毕");
    }

}
class MyThread1 implements Runnable {
    private int count=7;
    @Override
    public void run() {
        while (count-- > 0) {
            System.out.println(" MyThread:count:"+count);
        }
    }
}
class MyThread2 implements Runnable {
    private int count2 = 5;
    @Override
    public void run() {
        while (count2-- > 0) {
            System.out.println(" MyThread2:count:"+count2);
        }
    }
}
