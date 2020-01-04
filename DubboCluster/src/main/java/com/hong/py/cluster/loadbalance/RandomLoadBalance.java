package com.hong.py.cluster.loadbalance;

import com.hong.py.commonUtils.URL;
import com.hong.py.rpc.Invocation;
import com.hong.py.rpc.Invoker;

import java.util.List;
import java.util.Random;

/**
 * 文件描述
 *
 * @ProductName: HONGPY
 * @ProjectName: MyDubbo
 * @Package: com.hong.py.cluster.loadbalance
 * @Description: note
 * @Author: hongpy21691
 * @CreateDate: 2020/1/3 11:04
 * @UpdateUser: hongpy21691
 * @UpdateDate: 2020/1/3 11:04
 * @UpdateRemark: The modified content
 * @Version: 1.0
 * <p>
 * Copyright © 2020 hongpy Technologies Inc. All Rights Reserved
 **/
public class RandomLoadBalance extends AbstractLoadBalance {

    private final Random random = new Random();

    @Override
    protected <T> Invoker<T> doSelect(List<Invoker<T>> invokers, URL url, Invocation invocation) {
        int length = invokers.size();
        int totalWeight=0;
        boolean sameWeight=true;

        for (int i = 0; i < length; i++) {
            int weight = getWeight(invokers.get(i), invocation);
            totalWeight+=weight;
            if (sameWeight&&length > ++i) {
                int weight2 = getWeight(invokers.get(i), invocation);
                totalWeight+=weight2;
                if (weight != weight2) {
                    sameWeight=false;
                }
            }
        }

        if (totalWeight > 0 && !sameWeight) {

            int offest=random.nextInt(totalWeight);

            for (int i = 0; i < length; i++) {
                offest-=getWeight(invokers.get(i),invocation);
                if (offest<0){
                    return invokers.get(i);
                }
            }
        }

        return invokers.get(random.nextInt(length));
    }


}
