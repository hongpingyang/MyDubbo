package com.hong.py.common.bytecode;


import com.hong.py.commonUtils.ClassHelper;
import com.hong.py.commonUtils.ReflectUtils;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public abstract class Proxy {

    //WeakHashMap
    private static final Map<ClassLoader, Map<String, Object>> ProxyCacheMap = new WeakHashMap<ClassLoader, Map<String, Object>>();
    private static AtomicLong PROXY_ID = new AtomicLong();

    protected Proxy() {

    }

    public static Proxy getProxy(Class<?>... ics) {
        return getProxy(ClassHelper.getClassLoader(Proxy.class), ics);
    }

    public static Proxy getProxy(ClassLoader cl,Class<?>... ics) {
        //组合成 InterfaceName;InterfaceName
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ics.length; i++) {
            String itf = ics[i].getName();
            if (!ics[i].isInterface())
                throw new RuntimeException(itf + " is not a interface.");

            Class<?> tmp = null;
            try {
                tmp = Class.forName(itf, false, cl);
            } catch (ClassNotFoundException e) {
            }

            if (tmp != ics[i])
                throw new IllegalArgumentException(ics[i] + " is not visible from class loader");
            sb.append(itf).append(';');
        }
        // use interface class name list as key.
        String key = sb.toString();

        Map<String, Object> cache = ProxyCacheMap.get(cl);
        if (cache == null) {
            cache = new HashMap<>();
            ProxyCacheMap.put(cl, cache);
        }

        Proxy proxy = null;
        long id = PROXY_ID.getAndIncrement();
        ClassGenerator cg=null;
        try {
            cg = ClassGenerator.getInstance(cl);

        List<Method> methods = new ArrayList<Method>();
        for (int i = 0; i < ics.length; i++) {
            cg.addInterface(ics[i]);
            for (Method method : ics[i].getMethods()) {

                int ix = methods.size();

                Class<?> rt = method.getReturnType();
                Class<?>[] pts = method.getParameterTypes();

                StringBuilder code = new StringBuilder("Object[] args = new Object[").append(pts.length).append("];");
                for (int j = 0; j < pts.length; j++)
                    code.append(" args[").append(j).append("] = ($w)$").append(j + 1).append(";");
                code.append(" Object ret = handler.invoke(this, methods[" + ix + "], args);");
                if (!Void.TYPE.equals(rt))
                    code.append(" return ").append(arg(rt, "ret")).append(";");

                methods.add(method);
                cg.addMethod(method.getName(), method.getModifiers(), rt, pts, method.getExceptionTypes(), code.toString());
            }
        }

        String pcn = Proxy.class.getPackage().getName()+ ".proxy" + id;
        cg.setClassName(pcn);
        cg.addField("public static java.lang.reflect.Method[] methods;");
        cg.addField("private " + InvocationHandler.class.getName() + " handler;");
        cg.addDefaultConstructor();
        //cg.addConstructor(Modifier.PUBLIC, new Class<?>[]{InvocationHandler.class}, new Class<?>[0], "handler=$1;");
        cg.setSuperClass(Proxy.class);
        cg.addMethod("public Object newInstance(" + InvocationHandler.class.getName() + " h){ handler=$1; return this; }");
        Class<?> clazz = cg.toClass();
        cg.toFile();
        clazz.getField("methods").set(null, methods.toArray(new Method[0]));
        proxy = (Proxy) clazz.newInstance();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        finally {
            // release ClassGenerator
            if (cg != null)
                cg.release();
            synchronized (cache) {
                if (proxy == null)
                    cache.remove(key);
                else
                    cache.put(key, new WeakReference<Proxy>(proxy));
                cache.notifyAll();
            }
        }
        return proxy;
    }

    public static String arg(Class<?> cl, String name) {
        if (cl.isPrimitive()) {
            if (cl == Boolean.TYPE)
                return "((Boolean)" + name + ").booleanValue()";
            if (cl == Byte.TYPE)
                return "((Byte)" + name + ").byteValue()";
            if (cl == Character.TYPE)
                return "((Character)" + name + ").charValue()";
            if (cl == Double.TYPE)
                return "((Number)" + name + ").doubleValue()";
            if (cl == Float.TYPE)
                return "((Number)" + name + ").floatValue()";
            if (cl == Integer.TYPE)
                return "((Number)" + name + ").intValue()";
            if (cl == Long.TYPE)
                return "((Number)" + name + ").longValue()";
            if (cl == Short.TYPE)
                return "((Number)" + name + ").shortValue()";
            throw new RuntimeException("Unknown primitive type: " + cl.getName());
        }
        return "("+ ReflectUtils.getName(cl)+")"+name;
    }

    public abstract Object newInstance(InvocationHandler handler);
}
