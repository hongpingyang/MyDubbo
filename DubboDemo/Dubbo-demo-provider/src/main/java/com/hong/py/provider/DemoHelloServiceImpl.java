package com.hong.py.provider;

import com.hong.py.demo.DemoHelloServiceforHpy;

/**
 **/
public class DemoHelloServiceImpl implements DemoHelloServiceforHpy {
    @Override
    public String sayHello(String name) {
        System.out.println("this is returned msg ["+name+"]");
        return "this is returned msg ["+name+"]";
    }
}
