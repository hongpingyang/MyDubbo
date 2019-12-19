package com.hong.py.common.bytecode;

import com.hong.py.commonUtils.ClassHelper;
import com.sun.xml.internal.bind.v2.model.core.ID;
import javassist.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.ProtectionDomain;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 类生成器
 * mClassName不能为null
 **/
public class ClassGenerator {

    private ClassPool mPool;
    private CtClass mCtClass;
    private String mClassName,mSuperClass;
    private Set<String> mInterfaces;
    private List<String> mFields,mConstructors,mMethods;
    private boolean mDefaultConstructor = false;
    private static  Map<ClassLoader, ClassPool> poolMap = new HashMap<>();

    private ClassGenerator(ClassPool pool) {
        mPool=pool;
    }

    public static ClassGenerator getInstance() {
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        return getInstance(contextClassLoader);
    }

    public static ClassGenerator getInstance(ClassLoader loader) {
        ClassPool classPool = getmPool(loader);
        return new ClassGenerator(classPool);
    }

    //获取池
    private static ClassPool getmPool(ClassLoader loader) {
        ClassPool classPool = poolMap.get(loader);
        if (classPool == null) {
            classPool = new ClassPool(true);
            classPool.appendClassPath(new LoaderClassPath(loader));
            poolMap.put(loader, classPool);
        }
        return classPool;
    }

    public ClassGenerator setClassName(String name) {
        this.mClassName = name;
        return this;
    }

    public ClassGenerator setSuperClass(String name) {
        this.mSuperClass = name;
        return this;
    }

    public ClassGenerator setSuperClass(Class<?> cl) {
        this.mSuperClass = cl.getName();
        return this;
    }

    public ClassGenerator addInterface(String name) {
        if (mInterfaces == null) {
            mInterfaces = new HashSet<>();
        }
        this.mInterfaces.add(name);
        return this;
    }

    public ClassGenerator addInterface(Class<?> cl) {
        if (mInterfaces == null) {
            mInterfaces = new HashSet<>();
        }
        this.mInterfaces.add(cl.getName());
        return this;
    }

    public ClassGenerator addField(String code) {
        if (mFields == null) {
            mFields = new ArrayList<>();
        }
        this.mFields.add(code);
        return this;
    }


    public ClassGenerator addMethod(String code) {
        if (mMethods == null) {
            mMethods = new ArrayList<>();
        }
        this.mMethods.add(code);
        return this;
    }

    public ClassGenerator addConstructor(String code) {
        if (mConstructors == null) {
            mConstructors = new ArrayList<>();
        }
        this.mConstructors.add(code);
        return this;
    }

    public ClassGenerator addDefaultConstructor() {
        mDefaultConstructor=true;
        return this;
    }

    public Class<?> toClass() {
        return toClass(ClassHelper.getClassLoader(ClassGenerator.class), getClass().getProtectionDomain());
    }

    public Class<?> toClass(ClassLoader loader, ProtectionDomain protectionDomain) {
        if (mCtClass != null) mCtClass.detach();
        try {
            //从池中获取父类
            CtClass ctcs = mSuperClass == null ? null : mPool.get(mSuperClass);
            mCtClass = mPool.makeClass(mClassName);
            if (mSuperClass != null)
                mCtClass.setSuperclass(ctcs);
            //添加
            mCtClass.addInterface(mPool.get(Wappered.class.getName()));
            if (mInterfaces != null) {
                for (String minterface : mInterfaces) {
                    mCtClass.addInterface(mPool.get(minterface));
                }
            }
            if (mFields != null) {
                for (String field : mFields) {
                    mCtClass.addField(CtField.make(field,mCtClass));
                }
            }
            if (mMethods != null) {
                for (String code : mMethods) {
                    mCtClass.addMethod(CtNewMethod.make(code, mCtClass));
                }
            }
            if(mDefaultConstructor)
                mCtClass.addConstructor(CtNewConstructor.defaultConstructor(mCtClass));

            if (mConstructors != null) {
                for (String code : mConstructors) {
                    mCtClass.addConstructor(CtNewConstructor.make(code, mCtClass));
                }
            }

            return mCtClass.toClass(loader, protectionDomain);

        }  catch (NotFoundException e) {
            throw new RuntimeException(e.getMessage(), e);
        } catch (CannotCompileException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }


    public void release() {
        if (mCtClass != null) mCtClass.detach();
        if (mInterfaces != null) mInterfaces.clear();
        if (mFields != null) mFields.clear();
        if (mMethods != null) mMethods.clear();
        if (mConstructors != null) mConstructors.clear();
    }

    //判断是否已经被包装过了
    public static boolean isWrapped(Class<?> cl) {
        return ClassGenerator.Wappered.class.isAssignableFrom(cl);
    }

    //把生成的class文件写入文件
    public void toFile() {
        byte[] byteArr = new byte[0];
        try {
            byteArr = mCtClass.toBytecode();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CannotCompileException e) {
            e.printStackTrace();
        }
        FileOutputStream fos = null;
        try {
            String fileName = "F://Wrapper//" + mClassName + ".class";
            File file = new File(fileName);
            if (!file.exists()) {
                file.getParentFile().mkdir();
                file.createNewFile();
            }
            fos = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            fos.write(byteArr);
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    //用来标志是否已经被包装了
    public static interface Wappered {

    }
}
