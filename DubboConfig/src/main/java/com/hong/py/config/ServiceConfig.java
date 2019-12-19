package com.hong.py.config;


import com.hong.py.common.bytecode.Wrapper;
import com.hong.py.commonUtils.*;
import com.hong.py.config.annotation.Parameter;
import com.hong.py.extension.ExtensionLoader;
import com.hong.py.rpc.Exporter;
import com.hong.py.rpc.Invoker;
import com.hong.py.rpc.Protocol;
import com.hong.py.rpc.ProxyFactory;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.*;

public class ServiceConfig<T> extends AbstractServiceConfig {


    private static final Protocol protocol = ExtensionLoader.getExtensionLoader(Protocol.class).getAdaptiveExtension();

    private static final ProxyFactory proxyFactory = ExtensionLoader.getExtensionLoader(ProxyFactory.class).getAdaptiveExtension();

    // interface type
    private String interfaceName;
    private Class<?> interfaceClass;

    private T ref;
    // service name
    private String path;
    private List<MethodConfig> methods;

    private volatile boolean exported;
    private volatile boolean unexported;
    private final List<URL> urls = new ArrayList<>();
    private final List<Exporter<?>> exporters = new ArrayList<Exporter<?>>();


    public ServiceConfig() {

    }

    public void setInterface(String interfaceName) {
        this.interfaceName = interfaceName;
        if (id == null || id.length() == 0) {
            id = interfaceName;
        }
    }

    public void setInterface(Class<?> interfaceClass) {
        if (interfaceClass != null && !interfaceClass.isInterface()) {
            throw new IllegalStateException("The interface class " + interfaceClass + " is not a interface!");
        }
        this.interfaceClass = interfaceClass;
        setInterface(interfaceClass == null ? null : interfaceClass.getName());
    }

    public T getRef() {
        return ref;
    }

    public void setRef(T ref) {
        this.ref = ref;
    }

    @Parameter(excluded = true)
    public boolean isExported() {
        return exported;
    }

    @Parameter(excluded = true)
    public boolean isUnexported() {
        return unexported;
    }

    //服务暴露
    public synchronized void export() {
       doExport();
    }

    public synchronized void doExport() {
        if (exported) {
            return;
        }
        exported=true;

        //interfaceName必选要有
        if (interfaceName == null || interfaceName.length() <= 0) {
            throw new IllegalArgumentException("interface==null");
        }

        //获取注册中心
        if (application != null) {
            registries = application.getRegistries();
        }

        try {
            interfaceClass = Class.forName(interfaceName, true, Thread.currentThread().getContextClassLoader());
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
        checkInterfaceAndMethods(interfaceClass, methods);
        checkRef();


        checkApplication();
        checkRegistry();
        checkProtocol();
        appendProperties(this);
        if (path == null || path.length() == 0) {
            path=interfaceName;
        }

        doExportURL();
    }

    private void doExportURL() {
        //有可能存在多个注册中心
        List<URL> urls = loadRegistries(true);

        for (ProtocolConfig protocolConfig : protocols) {
            doExportUrlsFor1Protocol(protocolConfig, urls);
        }
    }


    private void doExportUrlsFor1Protocol(ProtocolConfig config,List<URL> registryURLs) {

        String protocolName = config.getName();
        if (protocolName == null || protocolName.length() == 0) {
            protocolName = Constants.DEFAULT_PROTOCOL;
        }
        Map<String, String> map = new HashMap<String, String>();
        //服务提供方
        map.put(Constants.SIDE_KEY, Constants.PROVIDER_SIDE);
        map.put(Constants.TIMESTAMP_KEY, String.valueOf(System.currentTimeMillis()));
        if (ConfigUtils.getPid() > 0) {//获取当前进程id
            map.put(Constants.PID_KEY, String.valueOf(ConfigUtils.getPid()));
        }

        appendParameters(map,application);
        appendParameters(map,config);
        appendParameters(map, this);

        String[] methods = Wrapper.getWrapper(interfaceClass).getMethodNames();

        if (methods.length == 0) {
            logger.warn("NO method found in service interface " + interfaceClass.getName());
            map.put(Constants.METHODS_KEY, Constants.ANY_VALUE);
        } else {
            map.put(Constants.METHODS_KEY, StringUtils.join(new HashSet<String>(Arrays.asList(methods)), ","));
        }


        //使用外部容器发布服务，例如tomcat，项目路径
        String contextPath = config.getContextpath();

        String host = this.findConfigHosts(config, registryURLs, map);
        Integer port = this.findConfigPort(config, map);
        URL url = new URL(protocolName, host, port, (contextPath == null || contextPath.length() == 0 ? "" : contextPath + "/") + path, map);

        if (logger.isInfoEnabled()) {
            logger.info("Export dubbo service " + interfaceClass.getName() + " to url " + url);
        }
        if (registryURLs != null && !registryURLs.isEmpty()) {
            for (URL registeryurl : registryURLs) {
                if (logger.isInfoEnabled()) {
                    logger.info("Register dubbo service " + interfaceClass.getName() + " url " + url + " to registry " + registryURL);
                }

                String proxy = url.getParameter(Constants.PROXY_KEY);
                if (StringUtils.isNotEmpty(proxy)) {
                    registeryurl.addParameter(Constants.PROXY_KEY, proxy);
                }

                //编码
                URL decodeurl = registeryurl.addParameterAndEncoded(Constants.EXPORT_KEY, url.toFullString());

                Invoker invoker = proxyFactory.getInvoker(ref, (Class) interfaceClass, decodeurl);

                Exporter export = protocol.export(invoker);
                exporters.add(export);
            }
        }
        this.urls.add(url);
    }

    //获取ip 先从系统获取后再向配置里查找
    private String findConfigHosts(ProtocolConfig config,List<URL> registryURLs,Map<String, String> map) {
        String iptoBind = getValueFromSystemProperty(config, Constants.DUBBO_IP_TO_BIND);
        if (iptoBind == null || iptoBind.length() == 0) {
            iptoBind=config.getHost();

            if (NetUtils.isInvalidLocalHost(iptoBind)) {
                //本地地址
                try {
                    iptoBind = InetAddress.getLocalHost().getHostAddress();
                } catch (UnknownHostException e) {
                    logger.warn(e.getMessage(), e);
                }

                if (NetUtils.isInvalidLocalHost(iptoBind)) {
                    if (registryURLs != null && !registryURLs.isEmpty()) {
                        for (URL url : registryURLs) {
                            iptoBind = url.getHost();
                        }
                    }
                    if (NetUtils.isInvalidLocalHost(iptoBind)) {
                        iptoBind = NetUtils.getLocalHost();
                    }
                }
            }
        }
        map.put(Constants.BIND_IP_KEY, iptoBind);

        String iptoRegistry = getValueFromSystemProperty(config, Constants.DUBBO_IP_TO_REGISTRY);
        if (iptoRegistry == null || iptoRegistry.length() == 0 || NetUtils.isInvalidLocalHost(iptoRegistry)) {
            iptoRegistry=iptoBind;
        }

       return iptoRegistry;
    }

    //获取端口
    private Integer findConfigPort(ProtocolConfig config,  Map<String, String> map) {
        Integer initport=null;
        String port = getValueFromSystemProperty(config, Constants.DUBBO_PORT_TO_BIND);
        initport = parsePort(port);

        if (initport == null) {
            initport = config.getPort();
            if (initport == null || initport == 0) {
                initport=20880; //dubbo默認的端口
            }
        }

        map.put(Constants.BIND_PORT_KEY, String.valueOf(initport));

        String portToRegistryStr = getValueFromSystemProperty(config, Constants.DUBBO_PORT_TO_REGISTRY);
        Integer portToRegistry = parsePort(portToRegistryStr);
        if (portToRegistry == null) {
            portToRegistry = initport;
        }
        return portToRegistry;
    }

    //从系统属性中获取
    private String getValueFromSystemProperty(ProtocolConfig config,String key) {
        String protocolName = config.getName().toUpperCase() + "_";
        String value = ConfigUtils.getSystemProperty(protocolName + key);
        if (value == null || value.length() == 0) {
            value = ConfigUtils.getSystemProperty(key);
        }
        return value;
    }

    private Integer parsePort(String portStr) {
         Integer port=null;
        if (portStr != null && portStr.length() != 0) {

            Integer intport = null;
            try {
                intport = Integer.parseInt(portStr);
                if (NetUtils.isInvalidPort(intport)) {
                    throw new IllegalArgumentException("port");
                }
                port=intport;
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("port");
            }
        }
         return port;
    }


    private void checkRef() {
        // ref不能为null
        if (ref == null) {
            throw new IllegalStateException("ref not allow null!");
        }
        if (!interfaceClass.isInstance(ref)) {
            throw new IllegalStateException("The class "
                    + ref.getClass().getName() + " unimplemented interface "
                    + interfaceClass + "!");
        }
    }

    private void checkProtocol() {

        // backward compatibility
        if (protocols == null || protocols.isEmpty()) {
            setProtocol(new ProtocolConfig());
        }
        for (ProtocolConfig protocolConfig : protocols) {

            if (StringUtils.isEmpty(protocolConfig.getName())) {
                protocolConfig.setName(Constants.DUBBO_VERSION_KEY);
            }

            appendProperties(protocolConfig);
        }
    }


    public synchronized void unExport() {

    }

}
