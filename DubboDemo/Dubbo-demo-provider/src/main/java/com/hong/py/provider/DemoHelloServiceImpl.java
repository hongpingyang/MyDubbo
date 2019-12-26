package com.hong.py.provider;

import com.hong.py.demo.DemoHelloService;

/**
 **/
public class DemoHelloServiceImpl implements DemoHelloService {
    @Override
    public String sayHello(String name) {
        return "this is returned msg ["+name+"]";
    }
}
