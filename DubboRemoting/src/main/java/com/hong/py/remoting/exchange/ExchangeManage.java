package com.hong.py.remoting.exchange;

import com.hong.py.commonUtils.Constants;
import com.hong.py.commonUtils.URL;
import com.hong.py.extension.ExtensionLoader;
import com.hong.py.remoting.RemotingException;

/**
 **/
public class ExchangeManage {


    //bind的默认为Header，获取的为HeaderExchangeServer
    public static ExchangeServer getExchangeServer(URL url,ExchangeHandler handler) throws RemotingException {
        if (url == null) {
            throw new IllegalArgumentException("url==null");
        }
        ExchangeServer exchangeServer = getExchange(url).bind(url, handler);
        return exchangeServer;
    }

    //默认为Header 默认为HeaderExchange
    public static ExchangeClient getExchangeClient(URL url,ExchangeHandler handler) throws RemotingException {
        if (url == null) {
            throw new IllegalArgumentException("url==null");
        }
        ExchangeClient exchangeClient = getExchange(url).connect(url, handler);
        return exchangeClient;
    }

    //默认为Header 默认为HeaderExchange
    public static Exchanger getExchange(URL url) {
        String parameter = url.getParameter(Constants.EXCHANGER_KEY, Constants.DEFAULT_EXCHANGER);
        return ExtensionLoader.getExtensionLoader(Exchanger.class).getExtension(parameter);
    }
}
