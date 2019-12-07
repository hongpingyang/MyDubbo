package com.hong.py.common.compiler;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * 文件描述
 *
 * @ProductName: HONGPY
 * @ProjectName: DubboDemoAll
 * @Package: com.hong.py.common.compiler
 * @Description: note
 * @Author: hongpy21691
 * @CreateDate: 2019/11/13 19:50
 * @UpdateUser: hongpy21691
 * @UpdateDate: 2019/11/13 19:50
 * @UpdateRemark: The modified content
 * @Version: 1.0
 * <p>
 * Copyright © 2019 hongpy Technologies Inc. All Rights Reserved
 **/
public class ClassUtils {





    public static String toString(Throwable e) {
        StringWriter w = new StringWriter();
        PrintWriter p = new PrintWriter(w);
        p.print(e.getClass().getName() + ": ");
        p.print(e.getClass().getName() + ": ");
        if (e.getMessage() != null) {
            p.print(e.getMessage() + "\n");
        }
        p.println();
        try {
            e.printStackTrace(p);
            return w.toString();
        } finally {
            p.close();
        }
    }
}
