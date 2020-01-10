package com.hong.py.commonUtils;

import java.io.*;
import java.util.Optional;

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

    //2个字节
    public static void short2bytes(short v, byte[] b, int off) {
        b[off+1]=(byte)v; //低8位
        b[off + 0] = (byte) (v >>> 8); //高8位
    }

    public static void int2bytes(int v, byte[] b) {
        int2bytes(v, b, 0);
    }

    //4个字节
    public static void int2bytes(int v, byte[] b, int off) {

        b[off+3]=(byte)v; //低8位
        b[off+2]=(byte)(v >>> 8); //
        b[off+1]=(byte)(v >>> 16); //
        b[off + 0] = (byte) (v >>> 24); //高8位
    }

    public static int bytes2int(byte[] b, int off) {
        return ((b[off + 3] & 0xFF) << 0) +
                ((b[off + 2] & 0xFF) << 8) +
                ((b[off + 1] & 0xFF) << 16) +
                ((b[off + 0]) << 24);
    }

    //8个字节
    public static void long2bytes(long v, byte[] b, int off) {
        b[off + 7] = (byte) v;
        b[off + 6] = (byte) (v >>> 8);
        b[off + 5] = (byte) (v >>> 16);
        b[off + 4] = (byte) (v >>> 24);
        b[off + 3] = (byte) (v >>> 32);
        b[off + 2] = (byte) (v >>> 40);
        b[off + 1] = (byte) (v >>> 48);
        b[off + 0] = (byte) (v >>> 56);
    }

    public static long bytes2long(byte[] b, int off) {
        return  ((b[off + 7] & 0xFF) << 0) +
                ((b[off + 6] & 0xFF) << 8) +
                ((b[off + 5] & 0xFF) << 16) +
                ((b[off + 4] & 0xFF) << 24) +
                ((b[off + 3] & 0xFF) << 32) +
                ((b[off + 2] & 0xFF) << 40) +
                ((b[off + 1] & 0xFF) << 48) +
                ((b[off + 0]) << 56);
    }

    public static Optional<byte[]> objectToBytes(Object obj){
        byte[] bytes = null;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream sOut;
        try {
            sOut = new ObjectOutputStream(out);
            sOut.writeObject(obj);
            sOut.flush();
            bytes= out.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Optional.ofNullable(bytes);
    }

    public static Optional<Object> bytesToObject(byte[] bytes) {
        Object t = null;
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        ObjectInputStream sIn;
        try {
            sIn = new ObjectInputStream(in);
            t = (Object)sIn.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Optional.ofNullable(t);
    }


}
