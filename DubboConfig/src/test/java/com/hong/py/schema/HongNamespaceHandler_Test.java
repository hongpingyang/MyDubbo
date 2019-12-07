package com.hong.py.schema;


import com.hong.py.config.ApplicationConfig;
import com.hong.py.config.ProtocolConfig;
import com.hong.py.service.DemoService;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;

public class HongNamespaceHandler_Test {

    @Test
    public void testProviderXml() {
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(HongNamespaceHandler_Test.class.getPackage().getName().replace('.', '/') + "/demo-provider.xml");
        ctx.start();

        ProtocolConfig protocolConfig = ctx.getBean(ProtocolConfig.class);
        assertThat(protocolConfig, not(nullValue()));
        assertThat(protocolConfig.getName(), is("dubbo"));
        assertThat(protocolConfig.getPort(), is(20813));

        ApplicationConfig applicationConfig = ctx.getBean(ApplicationConfig.class);
        assertThat(applicationConfig, not(nullValue()));
        assertThat(applicationConfig.getName(), is("demo-provider"));

        DemoService service = ctx.getBean(DemoService.class);
        assertThat(service, not(nullValue()));
    }

}
