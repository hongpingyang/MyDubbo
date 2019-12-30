package com.hong.py.common.bytecode;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;

/**
 *
 */
public class ProxyTest {

    public static void main(String[] args) {

        Proxy proxy = Proxy.getProxy(ITest.class);

        ITest instance = (ITest)proxy.newInstance(new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                if ("getName".equals(method.getName())) {
                    System.out.println("this is getName");
                } else if ("setName".equals(method.getName())) {
                    assertEquals(args.length, 2);
                    assertEquals(args[0], "haha");
                    assertEquals(args[1], "adcd");
                }
                return null;
            }
        });

        instance.setName("haha","adcd");
    }




    public static interface ITest {
        String getName();

        void setName(String name, String name2);
    }
}
