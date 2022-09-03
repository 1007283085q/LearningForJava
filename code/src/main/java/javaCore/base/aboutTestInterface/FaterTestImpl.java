package javaCore.base.aboutTestInterface;

public class FaterTestImpl implements FaterTest{
    @Override
    public void TestA() {
        System.out.println("Father->TestA");
    }

    @Override
    public void TestB() {
        System.out.println("Father->TestB");
    }
}
