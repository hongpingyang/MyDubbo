package com.hong.py.remoting.exchange.support.header;

import com.hong.py.commonUtils.URL;
import com.hong.py.remoting.RemotingException;
import com.hong.py.remoting.Transporters;
import com.hong.py.remoting.exchange.ExchangeClient;
import com.hong.py.remoting.exchange.ExchangeHandler;
import com.hong.py.remoting.exchange.ExchangeServer;
import com.hong.py.remoting.exchange.Exchanger;

/**
 *
 **/
public class HeaderExchange implements Exchanger {

    @Override
    public ExchangeServer bind(URL url, ExchangeHandler handler) throws RemotingException {
        return new HeaderExchangeServer(Transporters.bind(url, handler));
    }

    @Override
    public ExchangeClient connect(URL url, ExchangeHandler handler) throws RemotingException {
        return null;
    }
}
