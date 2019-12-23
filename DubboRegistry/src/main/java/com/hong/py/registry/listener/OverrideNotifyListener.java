package com.hong.py.registry.listener;

import com.hong.py.commonUtils.URL;
import com.hong.py.commonUtils.UrlUtils;
import com.hong.py.registry.NotifyListener;
import com.hong.py.registry.RegistryDirectory;
import com.hong.py.rpc.Invoker;
import org.apache.log4j.spi.Configurator;


import java.util.ArrayList;
import java.util.List;

public class OverrideNotifyListener implements NotifyListener {

    private final URL subscribeUrl;
    private final Invoker originInvoker;

    public OverrideNotifyListener(URL subscribeUrl, Invoker originalInvoker) {
        this.subscribeUrl = subscribeUrl;
        this.originInvoker = originalInvoker;
    }


    //重新暴露服务
    @Override
    public void notify(List<URL> urls) {

        List<URL> matchedUrls = getMatchedUrls(urls, subscribeUrl);
        if (matchedUrls.isEmpty()) {
            return;
        }

        //List<Configurator> configurators = RegistryDirectory.toConfigurators(matchedUrls);




    }

    private List<URL> getMatchedUrls(List<URL> configuratorUrls, URL currentSubscribe) {
        List<URL> result = new ArrayList<URL>();
        for (URL url : configuratorUrls) {
            URL overrideUrl = url;

            // Check whether url is to be applied to the current service
            if (UrlUtils.isMatch(currentSubscribe, overrideUrl)) {
                result.add(url);
            }
        }
        return result;
    }
}
