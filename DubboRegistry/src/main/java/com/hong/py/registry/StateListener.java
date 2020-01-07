package com.hong.py.registry;

/**
 * 文件描述
 *
 * @ProductName: HONGPY
 * @ProjectName: MyDubbo
 * @Package: com.hong.py.registry
 * @Description: note
 * @Author: hongpy21691
 * @CreateDate: 2020/1/7 19:05
 * @UpdateUser: hongpy21691
 * @UpdateDate: 2020/1/7 19:05
 * @UpdateRemark: The modified content
 * @Version: 1.0
 * <p>
 * Copyright © 2020 hongpy Technologies Inc. All Rights Reserved
 **/
public interface StateListener {

    int DISCONNECTED = 0;

    int CONNECTED = 1;

    int RECONNECTED = 2;

    void stateChanged(int connected);

}
