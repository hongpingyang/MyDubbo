package com.hong.py.common.dataStore;

import com.hong.py.annotation.SPI;

import java.util.Map;

/**
 * 文件描述
 *
 **/
@SPI("simple")
public interface DataStore {

    Object get(String componentName, String key);

    void put(String componentName, String key, Object value);

    void remove(String componentName, String key);

    Map<String, Object> get(String componetName);

}
