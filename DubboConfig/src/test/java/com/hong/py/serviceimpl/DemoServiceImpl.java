package com.hong.py.serviceimpl;


import com.hong.py.service.Box;
import com.hong.py.service.DemoService;

/**
 * DemoServiceImpl
 */
public class DemoServiceImpl implements DemoService {

    private String prefix = "say:";

    public String sayName(String name) {
        return prefix + name;
    }

    public Box getBox() {
        return null;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

}