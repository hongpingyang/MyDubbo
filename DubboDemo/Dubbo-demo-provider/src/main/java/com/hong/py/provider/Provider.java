package com.hong.py.provider;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;

/**
 *
 *
 **/
public class Provider {

    public static void main(String[] args) throws IOException {

        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("demo-provider.xml");
        context.start();

        System.in.read(); // press any key to exit
    }
}
