package com.hong.py.extension;

import com.hong.py.annotation.Activate;
import com.hong.py.annotation.Adaptive;
import com.hong.py.annotation.SPI;
import com.hong.py.common.compiler.Compiler;
import com.hong.py.commonUtils.ConcurrentHashSet;
import com.hong.py.commonUtils.Holder;
import com.hong.py.commonUtils.URL;
import com.hong.py.logger.Logger;
import com.hong.py.logger.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;

/**
 * 文件描述
 *
 * @ProductName: HONGPY
 * @ProjectName: DubboDemoAll
 * @Package: com.hong.py.extension
 * @Description: note
 * @Author: hongpy21691
 * @CreateDate: 2019/11/11 11:35
 * @UpdateUser: hongpy21691
 * @UpdateDate: 2019/11/11 11:35
 * @UpdateRemark: The modified content
 * @Version: 1.0
 * <p>
 * Copyright © 2019 hongpy Technologies Inc. All Rights Reserved
 *
 * 加载器
 **/
public class ExtensionLoader<T> {

    private static final Logger logger = LoggerFactory.getLogger(ExtensionLoader.class);
    /**
     * 所有的加载器
     */
    private static final ConcurrentMap<Class<?>, ExtensionLoader<?>> EXTENSION_LOADERS = new ConcurrentHashMap<Class<?>, ExtensionLoader<?>>();

    private Class<?> type;

    private ExtensionFactory extensionFactory;

    private String cachedDefaultName;

    private final ConcurrentMap<String, Holder<Object>> cachedInstances = new ConcurrentHashMap<String, Holder<Object>>();
    private final Holder<Map<String, Class<?>>> cachedClasses = new Holder<Map<String, Class<?>>>();

    private final Holder<Object> cachedAdaptiveInstance = new Holder<Object>();

    private volatile Class<?> cachedAdaptiveClass = null;

    private Set<Class<?>> cachedWrapperClasses=null;

    private final Map<String, Activate> cachedActivates = new ConcurrentHashMap<String, Activate>();

    private final Map<Class<?>, String> cachedNames = new ConcurrentHashMap<>();


    private static final ConcurrentMap<Class<?>, Object> EXTENSION_INSTANCES = new ConcurrentHashMap<>();

    //,分隔符
    private static final Pattern NAME_SEPARATOR = Pattern.compile("\\s*[,]+\\s*");

    //路径
    private static final String DUBBO_DIRECTORY = "META-INF/dubbo/";

    /**
     * 构造函数
     * @param type
     */
    private  ExtensionLoader(Class<?> type) {
       this.type=type;
       this.extensionFactory=(this.type==ExtensionFactory.class?null:ExtensionLoader.getExtensionLoader(ExtensionFactory.class).getAdaptiveExtension());
    }


    private static <T> boolean withExtensionAnnotation(Class<T> type) {
        if (type.getAnnotation(SPI.class)!=null) {
           return true;
        }
        return false;
    }

    /**
     * 获取加载器 必须是有SPI注解的类，接口
     * @param type
     * @param <T>
     * @return
     */
    public static <T> ExtensionLoader<T> getExtensionLoader(Class<T> type){
        if (type == null)
            throw new IllegalArgumentException("Extension type == null");
        if (!type.isInterface()) {
            throw new IllegalArgumentException("Extension type(" + type + ") is not interface!");
        }
        //必须是有SPI注解
        if (!withExtensionAnnotation(type)) {
            throw new IllegalArgumentException("Extension type(" + type +
                    ") is not extension, because WITHOUT @" + SPI.class.getSimpleName() + " Annotation!");
        }

        ExtensionLoader<T> extensionLoader =(ExtensionLoader<T>) EXTENSION_LOADERS.get(type);
        if (extensionLoader == null) {
            EXTENSION_LOADERS.putIfAbsent(type,new ExtensionLoader<T>(type));
            extensionLoader = (ExtensionLoader<T>)EXTENSION_LOADERS.get(type);
        }
      return extensionLoader;
    }


    /**
     *
     * @return
     */
    private static ClassLoader findClassLoader() {
        return ExtensionLoader.class.getClassLoader();
    }


    /**
     * 获取默认扩展类 SPI里指定的
     * @return
     */
    public T getDefaultExtension() {
        getExtensionClasses();
        if (null == cachedDefaultName || cachedDefaultName.length() == 0
                || "true".equals(cachedDefaultName)) {
            return null;
        }
        return getExtension(cachedDefaultName);
    }

    /**
     * 获取默认扩展类 SPI里指定的名称
     * @return
     */
    public String getDefaultExtensionName() {
        getExtensionClasses();
        return cachedDefaultName;
    }

    public boolean hasExtension(String name) {
        if (name == null || name.length() == 0)
            throw new IllegalArgumentException("Extension name == null");
        try {
            this.getExtensionClass(name);
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    /**
     * 获取所有的支持的扩展
     * @return
     */
    public Set<String>  getSupportedExtensions() {
        Map<String, Class<?>> classMap =getExtensionClasses();
        return Collections.unmodifiableSet(new TreeSet<>(classMap.keySet()));
    }

    /**
     * 通过名称 获取扩展类实例
     * @param name
     * @return
     */
    public T getExtension(String name) {

        if (name == null || name.length() == 0)
            throw new IllegalArgumentException("Extension name == null");
        if ("true".equals(name)) {
            return getDefaultExtension();
        }

        Holder<Object> objectHolder = cachedInstances.get(name);
        if (objectHolder == null) {
            cachedInstances.putIfAbsent(name, new Holder<Object>());
            objectHolder = cachedInstances.get(name);
        }

        Object instance = objectHolder.get();
        if (instance == null) {
            synchronized (objectHolder) {
                instance = objectHolder.get();
                if (instance == null) {
                    instance = createExtension(name);
                    objectHolder.set(instance);
                }
            }
        }
        return (T) instance;
    }


    private T createExtension(String name) {
        Class<?> classz = getExtensionClasses().get(name);
        if (classz == null) {
            //抛出异常 没有找到
        }
        try {

            T instance = (T) EXTENSION_INSTANCES.get(classz);
            if (instance == null) {
                EXTENSION_INSTANCES.putIfAbsent(classz, classz.newInstance());
                instance = (T) EXTENSION_INSTANCES.get(classz);
            }


            InjectInstance(instance);

            //包装类
            Set<Class<?>> wrapclasses=cachedWrapperClasses;

            if (wrapclasses != null && !wrapclasses.isEmpty()) {
                for (Class<?> clazz: wrapclasses) {
                    instance=InjectInstance((T)clazz.getConstructor(type).newInstance(instance));
                }
            }
            return instance;
        } catch (Throwable t) {
            throw new IllegalStateException("Extension instance(name: " + name + ", class: " +
                    type + ")  could not be instantiated: " + t.getMessage(), t);
        }
    }


    /**
     * 注入setter方法实例
     * @param instance
     */
    private T InjectInstance(T instance) {

        //通过extensionFactory去找extesion实例来注入到instance
        if (extensionFactory != null) {
            Method[] methods = instance.getClass().getMethods();
            for (Method method : methods) {
                if (method.getName().startsWith("set") &&
                        method.getParameterCount() == 1 &&
                        Modifier.isPublic(method.getModifiers())) {

                    Class<?> pt = method.getParameterTypes()[0]; //类型
                    try {
                        String property = method.getName().length() > 3 ? method.getName().substring(3, 4).toUpperCase() + method.getName().substring(4) : "";

                        Object extension = extensionFactory.getExtension(pt, property);

                        method.invoke(instance, extension);

                    } catch (Exception e) {
                        logger.error("fail to inject via method " + method.getName()
                               + " of interface " + type.getName() + ": " + e.getMessage(), e);
                    }
                }
            }
        }

        return instance;
    }

    /**
     * 按照名称获取单个扩展类
     * @param name
     * @return
     */
    private Class<?> getExtensionClass(String name) {

        if (type == null)
            throw new IllegalArgumentException("Extension type == null");
        if (name == null)
            throw new IllegalArgumentException("Extension name == null");
        Class<?> clazz = getExtensionClasses().get(name);
        if (clazz == null)
            throw new IllegalStateException("No such extension \"" + name + "\" for " + type.getName() + "!");
        return clazz;
    }

    /**
     * 是否是包装类
     * @param clazz
     * @return
     */
    private boolean isWrapperClass(Class<?> clazz) {
        try {
            clazz.getConstructor(type);
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    /**
     * 获取全部扩展类 除了Adaptive的
     * @return
     */
    private Map<String, Class<?>> getExtensionClasses() {
        Map<String, Class<?>> classMap = cachedClasses.get();
        if (classMap == null) {
            synchronized (cachedClasses) {
                classMap = cachedClasses.get();
                if (classMap == null) {
                    classMap = loadExtensionClasses();
                    cachedClasses.set(classMap);
                }
            }
        }
        return classMap;
    }

    /**
     * 加载全部扩展类 除了Adaptive的
     * 对于指定的扩展类的，记录下指定名称
     * @return
     */
    private Map<String, Class<?>> loadExtensionClasses() {
        final SPI defaultAnnotation = type.getAnnotation(SPI.class);
        if (defaultAnnotation != null) {
            String value = defaultAnnotation.value();
            //对于指定的扩展类的，记录下指定名称
            if ((value = value.trim()).length() > 0) {
                String[] names = NAME_SEPARATOR.split(value);
                if (names.length > 1) {
                    throw new IllegalStateException("more than 1 default extension name on extension " + type.getName()
                            + ": " + Arrays.toString(names));
                }
                if (names.length == 1) cachedDefaultName = names[0];
            }
        }

        Map<String, Class<?>> extensionClasses = new HashMap<>();
        loadDirectory(extensionClasses,DUBBO_DIRECTORY);
        return extensionClasses;
    }


    /**
     * 从指定路径的文件加载扩展类, 文件名必须是接口类型全限定名
     * @param extensionClasses
     * @param dir
     */
    private void loadDirectory(Map<String, Class<?>> extensionClasses, String dir) {

        String filename = dir + type.getName();
        ClassLoader classLoader = findClassLoader();
        try
        {
            Enumeration<java.net.URL> urls ;
            if (classLoader != null) {
                urls = classLoader.getResources(filename);
            } else {
                urls = ClassLoader.getSystemResources(filename);
            }

            if (urls != null) {
                while (urls.hasMoreElements()) {
                    java.net.URL resourceURL = urls.nextElement();
                    loadResource(extensionClasses, classLoader, resourceURL);
                }
            }
        } catch (Throwable t) {
            logger.error("Exception when load extension class(interface: " +
                    type + ", description file: " + filename + ").", t);
        }
    }

    /**
     * 文件内容加载扩展类
     * @param extensionClasses
     * @param classLoader
     * @param resourceURL
     */
    private void loadResource(Map<String, Class<?>> extensionClasses, ClassLoader classLoader, java.net.URL resourceURL){
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(resourceURL.openStream(), "utf-8"));
            String line;
            while ((line = reader.readLine()) != null) {

                String name = null;
                int i = line.indexOf('=');
                if (i > 0) {
                    name = line.substring(0, i).trim();
                    line = line.substring(i + 1).trim();
                }
                if (line.length() > 0) {
                    loadClass(extensionClasses, resourceURL, Class.forName(line, true, classLoader), name);
                }
            }
        } catch (Throwable t) {
            logger.error("Exception when load extension class(interface: " +
                    type + ", class file: " + resourceURL + ") in " + resourceURL, t);
        }
    }

    /**
     *加载扩展类
     * @param extensionClasses
     * @param resourceURL
     * @param clazz
     * @param name
     */
    private void loadClass(Map<String, Class<?>> extensionClasses, java.net.URL resourceURL, Class<?> clazz, String name) throws NoSuchMethodException {

        if (!type.isAssignableFrom(clazz)) {
            throw new IllegalStateException("Error when load extension class(interface: " +
                    type + ", class line: " + clazz.getName() + "), class "
                    + clazz.getName() + "is not subtype of interface.");
        }

        //对于在类上直接Adaptive的不需要生成字节码文件，这里直接指定。
        if (clazz.isAnnotationPresent(Adaptive.class)) {
            if (cachedAdaptiveClass == null) {
                cachedAdaptiveClass = clazz;
            } else if (!cachedAdaptiveClass.equals(clazz)) {
                throw new IllegalStateException("More than 1 adaptive class found: "
                        + cachedAdaptiveClass.getClass().getName()
                        + ", " + clazz.getClass().getName());
            }
        } else if (isWrapperClass(clazz)) {
            Set<Class<?>> wrappers = cachedWrapperClasses;
            if (wrappers == null) {
                cachedWrapperClasses = new ConcurrentHashSet<Class<?>>();
                wrappers = cachedWrapperClasses;
            }
            wrappers.add(clazz);
        } else {
            clazz.getConstructor();

            String[] names = NAME_SEPARATOR.split(name);
            if (names != null && names.length > 0) {
                Activate activate = clazz.getAnnotation(Activate.class);
                if (activate != null) {
                    cachedActivates.put(names[0], activate);
                }
                for (String n : names) {
                    if (!cachedNames.containsKey(clazz)) {
                        cachedNames.put(clazz, n);
                    }
                    Class<?> c = extensionClasses.get(n);
                    if (c == null) {
                        extensionClasses.put(n, clazz);
                    } else if (c != clazz) {
                        throw new IllegalStateException("Duplicate extension " + type.getName() + " name " + n + " on " + c.getName() + " and " + clazz.getName());
                    }
                }
            }
        }
    }


    /**
     * 获取适应类
     * @return
     */
    public T getAdaptiveExtension() {

        Object adaptiveExtension = cachedAdaptiveInstance.get();
        if (adaptiveExtension == null) {
            synchronized (cachedAdaptiveInstance) {
                adaptiveExtension = cachedAdaptiveInstance.get();
                if (adaptiveExtension == null) {
                    adaptiveExtension = createAdaptiveExtension();
                    cachedAdaptiveInstance.set(adaptiveExtension);
                }
            }
        }
        return (T)adaptiveExtension;
    }

    /**
     * 创建适应类实例
     * @return
     */
    private T createAdaptiveExtension() {
        try {
            return InjectInstance((T)getAdaptiveExtensionClass().newInstance());
        } catch (Exception e) {
            throw new IllegalStateException("Can not create adaptive extension " + type + ", cause: " + e.getMessage(), e);
        }
    }

    /**
     * 获取适应类 只能一个
     * @return
     */
    private Class<?> getAdaptiveExtensionClass() {
        getExtensionClasses();
        //对于在类上直接Adaptive的不需要生成字节码文件，这里直接回返回
        if (cachedAdaptiveClass != null) {
            return cachedAdaptiveClass;
        }
        //要生成 XXX&Adaptive 类
        return cachedAdaptiveClass = createAdaptiveExtensionClass();
    }

    /**
     * 编译创建适应类
     * @return
     */
    private Class<?> createAdaptiveExtensionClass() {
        String code = createAdaptiveExtensionClassCode();
        ClassLoader classLoader = findClassLoader();
        Compiler compiler=ExtensionLoader.getExtensionLoader(Compiler.class).getAdaptiveExtension();
        return compiler.compile(code, classLoader);
    }


    /**
     * 创建编码文本 生成适应类
     * @return
     */
    private String createAdaptiveExtensionClassCode() {
        StringBuilder codeBuilder = new StringBuilder();
        Method[] methods = type.getMethods();

        boolean hasAdaptiveAnnotation = false;
        for (Method m : methods) {
            if (m.isAnnotationPresent(Adaptive.class)) {
                hasAdaptiveAnnotation = true;
                break;
            }
        }
        // no need to generate adaptive class since there's no adaptive method found.
        if (!hasAdaptiveAnnotation)
            throw new IllegalStateException("No adaptive method on extension " + type.getName() + ", refuse to create the adaptive class!");

        codeBuilder.append("package ").append(type.getPackage().getName()).append(";");

        codeBuilder.append("\nimport ").append(ExtensionLoader.class.getName()).append(";");

        codeBuilder.append("\npublic class ").append(type.getSimpleName()).append("$Adaptive").append(" implements ").append(type.getCanonicalName()).append(" {");

        for (Method method : methods) {

            Class<?> rt = method.getReturnType();
            Class<?>[] pts = method.getParameterTypes();
            Class<?>[] ets = method.getExceptionTypes();

            StringBuilder code = new StringBuilder(512);

            Adaptive annotation = method.getAnnotation(Adaptive.class);
            //对于没有指定Adaptive的方法，不需要
            if (annotation == null) {
                code.append("throw new UnsupportedOperationException(\"method ")
                        .append(method.toString()).append(" of interface ")
                        .append(type.getName()).append(" is not adaptive method!\");");
            } else {

                //找到url
                int urlTypeIndex = -1;
                for (int i = 0; i < pts.length; ++i) {
                    if (pts[i].equals(URL.class)) {
                        urlTypeIndex = i;
                        break;
                    }
                }

                //存在url入参的话
                if (urlTypeIndex >= 0) {

                    //判断arg参数是否为null
                    code.append(String.format("\nif (arg%d == null) throw new IllegalArgumentException(\"url == null\");", urlTypeIndex));
                    //赋值给临时变量url
                    code.append(String.format("\n%s url=arg%d ;", URL.class.getName(), urlTypeIndex));

                } else {

                    //找到入参中有get方法返回值为url的
                    String attribMethod = null;

                    LBL_PTS:
                    for (int i = 0; i < pts.length; ++i) {
                        Method[] ms = pts[i].getMethods();
                        for (Method m : ms) {
                            String name = m.getName();
                            if ((name.startsWith("get") || name.length() > 3)
                                    && Modifier.isPublic(m.getModifiers())
                                    && !Modifier.isStatic(m.getModifiers())
                                    && m.getParameterTypes().length == 0
                                    && m.getReturnType() == URL.class) {
                                urlTypeIndex = i;
                                attribMethod = name;
                                break LBL_PTS;
                            }
                        }
                    }
                    if (attribMethod == null) {
                        throw new IllegalStateException("fail to create adaptive class for interface " + type.getName()
                                + ": not found url parameter or url attribute in parameters of method " + method.getName());
                    }

                    //判断arg参数是否为null
                    code.append(String.format("\nif (arg%d == null) throw new IllegalArgumentException(\"%s argument == null\");",
                            urlTypeIndex,pts[urlTypeIndex].getName()));
                    //判断get方法是否为null
                    code.append(String.format("\nif (arg%d.%s() == null) throw new IllegalArgumentException(\"%s %s == null\");",
                            urlTypeIndex,attribMethod,pts[urlTypeIndex].getName(),attribMethod));

                    //赋值给临时变量url
                    code.append(String.format("\n%s url=arg%d.%s() ;",
                            URL.class.getName(), urlTypeIndex,attribMethod));
                }


                String[] value = annotation.value();
                //如果方法上的Adaptive没有指定值，则用类型的名称作为url中的key
                if (value.length == 0) {
                    char[] charArray = type.getSimpleName().toCharArray();
                    StringBuilder sb = new StringBuilder(128);
                    for (int i = 0; i < charArray.length; i++) {
                        if (Character.isUpperCase(charArray[i])) {
                            if (i != 0) {
                                sb.append(".");
                            }
                            sb.append(Character.toLowerCase(charArray[i]));

                        } else {
                            sb.append(charArray[i]);
                        }
                    }
                    value = new String[]{sb.toString()};
                }

                boolean hasInvocation = false;
                for (int i = 0; i < pts.length; ++i) {
                    if (pts[i].getName().equals("com.alibaba.dubbo.rpc.Invocation")) {
                        // Null Point check
                        String s = String.format("\nif (arg%d == null) throw new IllegalArgumentException(\"invocation == null\");", i);
                        code.append(s);
                        s = String.format("\nString methodName = arg%d.getMethodName();", i);
                        code.append(s);
                        hasInvocation = true;
                        break;
                    }
                }

                String defaultExtName = cachedDefaultName;
                String getNameCode = null;
                for (int i = value.length - 1; i >= 0; --i) {
                    if (i == value.length - 1) {
                        if (null != defaultExtName) {
                            if (!"protocol".equals(value[i]))
                                if (hasInvocation)
                                    getNameCode = String.format("url.getMethodParameter(methodName, \"%s\", \"%s\")", value[i], defaultExtName);
                                else
                                    getNameCode = String.format("url.getParameter(\"%s\", \"%s\")", value[i], defaultExtName);
                            else
                                getNameCode = String.format("( url.getProtocol() == null ? \"%s\" : url.getProtocol() )", defaultExtName);
                        } else {
                            if (!"protocol".equals(value[i]))
                                if (hasInvocation)
                                    getNameCode = String.format("url.getMethodParameter(methodName, \"%s\", \"%s\")", value[i], defaultExtName);
                                else
                                    getNameCode = String.format("url.getParameter(\"%s\")", value[i]);
                            else
                                getNameCode = "url.getProtocol()";
                        }
                    } else {
                        if (!"protocol".equals(value[i]))
                            if (hasInvocation)
                                getNameCode = String.format("url.getMethodParameter(methodName, \"%s\", \"%s\")", value[i], defaultExtName);
                            else
                                getNameCode = String.format("url.getParameter(\"%s\", %s)", value[i], getNameCode);
                        else
                            getNameCode = String.format("url.getProtocol() == null ? (%s) : url.getProtocol()", getNameCode);
                    }
                }

                code.append(String.format("\nString extName=%s;",getNameCode));

                //判断extName是否为null
                code.append(String.format("\nif (extName == null) throw new IllegalArgumentException(\"Fail to get extension(%s) name from url(\" + url.toString() + \") use keys(%s)\");",
                       type.getName(),Arrays.toString(value)));

                //获取实例
                code.append(String.format("\n%s extension=(%s)ExtensionLoader.getExtensionLoader(%s.class).getExtension(extName);",
                        type.getName(),type.getName(),type.getName()));

                //返回
                if (!rt.equals(void.class)) {
                    code.append("\nreturn ");
                }

                code.append(String.format("extension.%s(", method.getName()));

                for (int i = 0; i < pts.length; i++) {
                    if (i != 0)
                        code.append(", ");
                    code.append("arg").append(i);
                }
                code.append(");");
            }

            codeBuilder.append(String.format("\npublic %s %s(",rt.getCanonicalName(),method.getName()));
            StringBuilder s=new StringBuilder();
            for (int i = 0; i < pts.length; i++) {
                if (i != 0)
                    s.append(", ");
                s.append(String.format("%s arg",pts[i].getCanonicalName())).append(i);
            }
            codeBuilder.append(s).append(")");
            //方法异常处理
            for (int i = 0; i < ets.length; i++) {
                codeBuilder.append(" throws ");
                if (i != 0)
                    s.append(", ");
                codeBuilder.append(ets[i].getCanonicalName());
            }

            codeBuilder.append("{").append(code).append("\n}");
        }

        codeBuilder.append("\n}");

        if (logger.isDebugEnabled()) {
            logger.debug(codeBuilder.toString());
        }

       return codeBuilder.toString();
    }


}
