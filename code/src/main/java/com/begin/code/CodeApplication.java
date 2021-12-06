package com.begin.code;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CodeApplication {

    public static void main(String[] args) {
        try {
            SpringApplication.run(CodeApplication.class, args);

        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
    private void test(String s){

    }
}
