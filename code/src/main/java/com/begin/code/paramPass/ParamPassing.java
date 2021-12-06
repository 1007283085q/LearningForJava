package com.begin.code.paramPass;

import com.sun.org.glassfish.external.statistics.StringStatistic;

public class ParamPassing {
    private static int intStatic=222;
    private static String stringStatic ="old String";
    private static StringBuilder stringBuilderStatic=new StringBuilder("old stringBuilder");
    public static void main(String[] args){
        //1.对于基本类型的传参，两种情况
        method(intStatic);
        System.out.println("基本类型的传参"+intStatic+"\n");
        //结果是输出222
        method();
        System.out.println("基本类型的传参"+intStatic+"\n");
        //结果是输出777

        //对于没有提供改变自身方法的引用类型
        method(stringStatic);
        System.out.println("对于没有提供改变自身方法的引用类型"+ stringStatic +"\n");
        //对于提供自身方法的引用类型
        method(stringBuilderStatic,stringBuilderStatic);
        System.out.println("对于没有提供改变自身方法的引用类型"+ stringBuilderStatic +"\n");
    }
    public static void method(){
        intStatic=888;
    }

    public static void method(int Static){
        intStatic=777;
    }

    public static void method(String stringStatic){
        stringStatic="new String";
    }

    public static void method(StringBuilder stringBuilderStatic1,StringBuilder stringBuilderStatic2){
        stringBuilderStatic1.append(".method.first-");
        stringBuilderStatic2.append("method.second-");
        //创建了新的对象指向了新的引用地址
        stringBuilderStatic1=new StringBuilder("new stringBuilderStatic1");
        stringBuilderStatic1.append("new method's append");
    }
}
