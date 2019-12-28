package com.hong.py.config.spring;

import com.hong.py.config.ReferenceConfig;
import com.hong.py.extension.SpringExtensionFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 *
 * @param <T>
 */
public class ReferenceBean<T>  extends ReferenceConfig<T> implements FactoryBean, ApplicationContextAware,
        InitializingBean, DisposableBean {

    private transient ApplicationContext applicationContext;


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
       this.applicationContext=applicationContext;
        SpringExtensionFactory.addApplicationContext(applicationContext);
    }



    //获取真正的实例
    @Override
    public Object getObject() throws Exception {
        return get();
    }

    //获取接口类型
    @Override
    public Class<?> getObjectType() {
        return getInterfaceClass();
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public void afterPropertiesSet() throws Exception {

    }

    @Override
    public void destroy() throws Exception {

    }
}
