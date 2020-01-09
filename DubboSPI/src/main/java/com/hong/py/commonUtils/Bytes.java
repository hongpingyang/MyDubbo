package com.hong.py.commonUtils;

/**
 * 文件描述
 *
 * @ProductName: HONGPY
 * @ProjectName: MyDubbo
 * @Package: com.hong.py.commonUtils
 * @Description: note
 * @Author: hongpy21691
 * @CreateDate: 2020/1/9 20:01
 * @UpdateUser: hongpy21691
 * @UpdateDate: 2020/1/9 20:01
 * @UpdateRemark: The modified content
 * @Version: 1.0
 * <p>
 * Copyright © 2020 hongpy Technologies Inc. All Rights Reserved
 **/
public class Bytes {

    public static void short2bytes(short v, byte[] b) {
        short2bytes(v, b, 0);
    }

    public static void short2bytes(short v, byte[] b, int off) {
        b[off+1]=(byte)v; //低8位
        b[off + 0] = (byte) (v >>> 8); //高8位
    }
}
