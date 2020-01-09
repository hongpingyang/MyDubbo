package com.hong.py.remoting.transpoter.netty;

import com.hong.py.commonUtils.URL;
import com.hong.py.remoting.ChannelBuffer;
import com.hong.py.remoting.Codec2;
import com.hong.py.remoting.exchange.Request;
import com.hong.py.remoting.exchange.Response;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToByteEncoder;

import java.io.IOException;
import java.util.List;

/**
 * 文件描述
 *
 * @ProductName: HONGPY
 * @ProjectName: MyDubbo
 * @Package: com.hong.py.remoting.transpoter.netty
 * @Description: note
 * @Author: hongpy21691
 * @CreateDate: 2020/1/9 18:37
 * @UpdateUser: hongpy21691
 * @UpdateDate: 2020/1/9 18:37
 * @UpdateRemark: The modified content
 * @Version: 1.0
 * <p>
 * Copyright © 2020 hongpy Technologies Inc. All Rights Reserved
 **/
public class NettyCode {

    private final ChannelHandler encoder = new InternalEncoder();

    private final ChannelHandler decoder = new InternalDecoder();

    private final URL url;
    private final com.hong.py.remoting.ChannelHandler handler;

    public NettyCode(URL url, com.hong.py.remoting.ChannelHandler handler) {
       this.url=url;
       this.handler=handler;
    }

    public ChannelHandler getEncoder() {
        return encoder;
    }

    public ChannelHandler getDecoder() {
        return decoder;
    }
    private class InternalEncoder extends MessageToByteEncoder {

        @Override
        protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
            ChannelBuffer buffer = new NettyBackedChannelBuffer(out);
            Channel ch = ctx.channel();
            NettyChannel channel = NettyChannel.getOrAddChannel(ch, url, handler);
            try {
                if (msg instanceof Request) {
                    encodeRequest(channel, buffer, (Request) msg);
                } else if (msg instanceof Response) {
                    encodeResponse(channel, buffer, (Response) msg);
                } else if (msg instanceof String) {
                        /*if (isClientSide(channel)) {
                            message = message + "\r\n";
                        }
                        byte[] msgData = ((String) msg).getBytes(getCharset(channel).name());
                        buffer.writeBytes(msgData);*/
                }
            } finally {
                NettyChannel.removeChannelIfDisconnected(ch);
            }
        }
    }

    private void encodeRequest(NettyChannel channel, ChannelBuffer buffer, Request msg) {

    }

    private void encodeResponse(NettyChannel channel, ChannelBuffer buffer, Response msg) {

    }

    private class InternalDecoder extends ByteToMessageDecoder {

        @Override
        protected void decode(ChannelHandlerContext ctx, ByteBuf input, List<Object> out) throws Exception {

            ChannelBuffer message = new NettyBackedChannelBuffer(input);

            NettyChannel channel = NettyChannel.getOrAddChannel(ctx.channel(), url, handler);

            Object msg;

            int saveReaderIndex;

            try {
                // decode object.
                do {
                    saveReaderIndex = message.readerIndex();
                    try {
                        msg = codec.decode(channel, message);
                    } catch (IOException e) {
                        throw e;
                    }
                    if (msg == Codec2.DecodeResult.NEED_MORE_INPUT) {
                        message.readerIndex(saveReaderIndex);
                        break;
                    } else {
                        //is it possible to go here ?
                        if (saveReaderIndex == message.readerIndex()) {
                            throw new IOException("Decode without read data.");
                        }
                        if (msg != null) {
                            out.add(msg);
                        }
                    }
                } while (message.readable());
            } finally {
                NettyChannel.removeChannelIfDisconnected(ctx.channel());
            }
        }
    }
}
