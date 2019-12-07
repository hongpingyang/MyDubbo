package com.hong.py.extension;

import com.hong.py.commonUtils.URL;
import com.hong.py.extension.loader.ext.SimpleExt;

import org.hamcrest.CoreMatchers;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * 文件描述
 *
 * @ProductName: HONGPY
 * @ProjectName: DubboDemoAll
 * @Package: com.hong.py.extension
 * @Description: note
 * @Author: hongpy21691
 * @CreateDate: 2019/11/14 13:51
 * @UpdateUser: hongpy21691
 * @UpdateDate: 2019/11/14 13:51
 * @UpdateRemark: The modified content
 * @Version: 1.0
 * <p>
 * Copyright © 2019 hongpy Technologies Inc. All Rights Reserved
 **/
public class ExtensionLoader_Test {



    @Test
    public void test_adaptiveExtension() throws Exception {
        //SimpleExt ext = ExtensionLoader.getExtensionLoader(SimpleExt.class).getDefaultExtension();
        //assertThat(ext, instanceOf(SimpleExtImpl1.class));

        //String name = ExtensionLoader.getExtensionLoader(SimpleExt.class).getDefaultExtensionName();
        //assertEquals("impl1", name);
        /*{
            SimpleExt adaptiveExtension = ExtensionLoader.getExtensionLoader(SimpleExt.class).getAdaptiveExtension();

            Map<String, String> map = new HashMap<>();
            //有设置了默认值为impl1
            //map.put("simple.ext", "impl1");

            URL url = new URL("p1", "1.2.3.4", 1010, "path1", map);


            String haha = adaptiveExtension.echo(url, "haha");

            assertEquals("Ext1Impl1-echo", haha);
        }

        {
            SimpleExt adaptiveExtension = ExtensionLoader.getExtensionLoader(SimpleExt.class).getAdaptiveExtension();

            Map<String, String> map = new HashMap<>();
            map.put("simple.ext", "impl2");

            URL url = new URL("p1", "1.2.3.4", 1010, "path1", map);


            String haha = adaptiveExtension.echo(url, "haha");

            assertEquals("Ext1Impl2-echo", haha);
        }

        {
            SimpleExt adaptiveExtension = ExtensionLoader.getExtensionLoader(SimpleExt.class).getAdaptiveExtension();

            Map<String, String> map = new HashMap<>();
            map.put("simple.ext", "impl3");

            URL url = new URL("p1", "1.2.3.4", 1010, "path1", map);


            String haha = adaptiveExtension.echo(url, "haha");

            assertEquals("Ext1Impl3-echo", haha);
        }

        {
            SimpleExt adaptiveExtension = ExtensionLoader.getExtensionLoader(SimpleExt.class).getAdaptiveExtension();

            Map<String, String> map = new HashMap<>();
            //有设置了默认值为impl1
            //map.put("simple.ext", "impl1");

            URL url = new URL("p1", "1.2.3.4", 1010, "path1", map);


            String haha = adaptiveExtension.yell(url, "haha");

            assertEquals("Ext1Impl1-yell", haha);
        }

        {
            SimpleExt adaptiveExtension = ExtensionLoader.getExtensionLoader(SimpleExt.class).getAdaptiveExtension();

            Map<String, String> map = new HashMap<>();
            map.put("key2", "impl2");

            URL url = new URL("p1", "1.2.3.4", 1010, "path1", map);

            String haha = adaptiveExtension.echo(url, "haha");

            assertEquals("Ext1Impl2-yell", haha);
        }

        {
            SimpleExt adaptiveExtension = ExtensionLoader.getExtensionLoader(SimpleExt.class).getAdaptiveExtension();

            Map<String, String> map = new HashMap<>();
            map.put("key1", "impl3");

            URL url = new URL("p1", "1.2.3.4", 1010, "path1", map);


            String haha = adaptiveExtension.echo(url, "haha");

            assertEquals("Ext1Impl3-yell", haha);
        }*/

    }

    @Test
    public void test_noadaptiveMethod() {

        /*{
            SimpleExt adaptiveExtension = ExtensionLoader.getExtensionLoader(SimpleExt.class).getAdaptiveExtension();

            Map<String, String> map = new HashMap<>();
            //有设置了默认值为impl1
            //map.put("simple.ext", "impl1");
            URL url = new URL("p1", "1.2.3.4", 1010, "path1", map);

            try {
                adaptiveExtension.bang(url, 123);
                fail();
            } catch (UnsupportedOperationException expected) {
                assertThat(expected.getMessage(), CoreMatchers.containsString("method "));
                assertThat(
                        expected.getMessage(),
                        CoreMatchers.containsString(" of interface com.hong.py.extension.loader.ext.SimpleExt is not adaptive method!"));
            }
        }*/
    }
}
