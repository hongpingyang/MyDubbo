package com.hong.py.common.bytecode;

import com.hong.py.commonUtils.ClassHelper;
import com.hong.py.commonUtils.ReflectUtils;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.LoaderClassPath;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 生成包装类
 **/
public abstract class Wrapper {

    private static AtomicLong CLASS_WRAPPER_ID = new AtomicLong(0);
    private static Map<Class<?>, Wrapper> wrapperMap = new HashMap<Class<?>, Wrapper>();

    //
    public static Wrapper getWrapper(Class<?> cl) {
        if(ClassGenerator.isWrapped(cl)) return null;
        else {
            Wrapper wrapper = wrapperMap.get(cl);
            if (wrapper == null) {
                wrapper = makeWrapper(cl);
                wrapperMap.put(cl, wrapper);
            }
            return wrapper;
        }
    }

    public static Wrapper makeWrapper(Class<?> cl) {
        String name = cl.getName();
        ClassLoader classLoader = ClassHelper.getClassLoader(cl);
        StringBuilder mt =new StringBuilder(" public Object invokeMethod(Object instance,String mn,Class[] types,Object[] args) throws "+ InvocationTargetException.class.getName()+"  { ");
        mt.append(name).append(" w; try { w= ((").append(name).append(")$1);} catch(Throwable e) { throw new IllegalArgumentException(e); }");

        List<String> mns = new ArrayList<>();
        Method[] methods = cl.getMethods();
        boolean hasMethod = hasMethods(methods);
        if (hasMethod) {
            mt.append(" try {");
        }
        for (Method method : methods) {
            if(method.getDeclaringClass()==Object.class)
                continue;
            String methodName = method.getName();
            mt.append(" if(\""+methodName+"\".equals( $2 ) ");
            if (method.getParameterTypes().length>0) {
                mt.append(" && $4.length == "+method.getParameterTypes().length);
            }


            mt.append(" ) { ");
            Class<?>[] parameterTypes = method.getParameterTypes();
            if (method.getReturnType() == Void.TYPE) {
                mt.append(" w."+methodName+"("+getArgsStr(parameterTypes,"$4")+"); return null;");
            } else {
                mt.append(" return ($w)w."+methodName+"("+getArgsStr(parameterTypes,"$4")+");");
            }
            mt.append(" }");
            mns.add(methodName);
        }
        if (hasMethod) {
            mt.append(" } catch(Throwable e) { throw new java.lang.reflect.InvocationTargetException(e); }");
        }

        mt.append(" throw new " + NoSuchMethodException.class.getName() + "(\"Not found method \\\"\"+$2+\"\\\" in class " + cl.getName() + ".\"); }");

        long id = CLASS_WRAPPER_ID.getAndIncrement();
        ClassGenerator cg = ClassGenerator.getInstance(classLoader);
        cg.setClassName(Wrapper.class.getName() + id);
        cg.setSuperClass(Wrapper.class);
        cg.addDefaultConstructor();

        cg.addField("public static String[] mns;");//所有的方法名

        cg.addMethod("public String[] getMethodNames(){ return mns; }");

        cg.addMethod(mt.toString());
        try {
            Class<?> wc = cg.toClass();
            wc.getField("mns").set(null,mns.toArray(new String[0]));
            //把生成的class文件写入文件 查看
            cg.toFile();

            return (Wrapper) wc.newInstance();
        }
        catch (RuntimeException e) {
            throw e;
        }
        catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean hasMethods(Method[] methods) {
        if (methods == null || methods.length == 0) {
            return false;
        }
        for (Method m : methods) {
            //object自带的除外
            if (m.getDeclaringClass() != Object.class) {
                return true;
            }
        }
        return false;
    }

    private static String getArgsStr(Class<?>[] parameterTypes, String args) {
        int len=parameterTypes.length;
        if(len==0) return "";
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < len; i++) {
            builder.append(arg(parameterTypes[i],args+"["+i+"]")+",");
        }
        String substring = builder.substring(0, builder.length() - 1);
        return substring;
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
        return "("+ReflectUtils.getName(cl)+")"+name;
    }

    public abstract String[] getMethodNames();

    public abstract Object invokeMethod(Object instance,String methodName,Class<?>[] types,Object[] args);

}
