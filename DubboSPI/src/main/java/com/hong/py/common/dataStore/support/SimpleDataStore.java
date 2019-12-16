package com.hong.py.common.dataStore.support;

import com.hong.py.annotation.Adaptive;
import com.hong.py.common.dataStore.DataStore;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 文件描述
 * 数据寄存器
 **/
public class SimpleDataStore implements DataStore {


    private ConcurrentMap<String, ConcurrentMap<String, Object>> dataMaps = new ConcurrentHashMap<>();

    @Override
    public Object get(String componentName, String key) {
        if (!dataMaps.containsKey(componentName)) {
            return null;
        }
        return dataMaps.get(componentName).get(key);
    }

    @Override
    public void put(String componentName, String key, Object value) {
        if (!dataMaps.containsKey(componentName)) {
            ConcurrentMap map = new ConcurrentHashMap();
            map.put(key, value);
            dataMaps.put(componentName, map);
        }
        else
        {
            ConcurrentMap map = dataMaps.get(componentName);
            map.putIfAbsent(key, value);
        }
    }

    @Override
    public void remove(String componentName, String key) {
        if (!dataMaps.containsKey(componentName)) {
            return;
        }
        dataMaps.get(componentName).remove(key);
    }

    @Override
    public Map<String, Object> get(String componetName) {

        return dataMaps.get(componetName)==null?new HashMap<String,Object>():dataMaps.get(componetName);
    }

}

