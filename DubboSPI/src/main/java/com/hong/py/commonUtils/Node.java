package com.hong.py.commonUtils;

/**
 * Node. (API/SPI, Prototype, ThreadSafe)
 */
public interface Node {

    /**
     * get url.
     *
     * @return url.
     */
    URL getUrl();

    /**
     * is available.
     *
     * @return available.
     */
    boolean isAvailable();

    /**
     * destroy.
     */
    void destroy();

}