package com.hong.py.rpc;

import com.hong.py.annotation.SPI;

/**
 * ExporterListener. (SPI, Singleton, ThreadSafe)
 */
@SPI
public interface ExporterListener {

    /**
     * The exporter exported.
     *
     * @param exporter
     * @throws RpcException
     * @see com.hong.py.rpc.Protocol#export(Invoker)
     */
    void exported(Exporter<?> exporter) throws RpcException;

    /**
     * The exporter unexported.
     *
     * @param exporter
     * @throws RpcException
     * @see com.hong.py.rpc.Exporter#unexport()
     */
    void unexported(Exporter<?> exporter);

}