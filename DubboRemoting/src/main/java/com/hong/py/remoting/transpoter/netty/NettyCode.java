package com.hong.py.remoting.transpoter.netty;

import com.hong.py.commonUtils.Bytes;
import com.hong.py.commonUtils.StringUtils;
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
import java.util.Optional;

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

    // header length. 16个字节 :MAGIC(2)+消息类型(1)+状态（1）+id(8)+内容长度（4）
    protected static final int HEADER_LENGTH = 16;
    // magic header. 2个字节
    protected static final short MAGIC = (short) 0xdabb;

    //消息类型 1个字节
    protected static final byte FLAG_REQUEST = (byte) 0x80;
    protected static final byte FLAG_TWOWAY=(byte)0x40;

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
            try {
                if (msg instanceof Request) {
                    encodeRequest(out, (Request) msg);
                } else if (msg instanceof Response) {
                    encodeResponse(out, (Response) msg);
                } else if (msg instanceof String) {
                        /*if (isClientSide(channel)) {
                            message = message + "\r\n";
                        }
                        byte[] msgData = ((String) msg).getBytes(getCharset(channel).name());
                        buffer.writeBytes(msgData);*/
                }
            } finally {
            }
        }
    }

    private void encodeRequest(ByteBuf out, Request req) {

        byte[] header = new byte[HEADER_LENGTH];
        // set magic number. 2个字节
        Bytes.short2bytes(MAGIC, header);

        header[2]=FLAG_REQUEST;
        if(req.ismTwoWay()) header[2]|=FLAG_TWOWAY;

        //set requset id  8个字节
        Bytes.long2bytes(req.getmId(), header, 4);

        Optional<byte[]> bytes = Bytes.objectToBytes(req.getData());
        if (bytes.isPresent()) {
            Bytes.int2bytes(bytes.get().length,header,12);
        } else {
            Bytes.int2bytes(0,header,12);
        }

        out.writeBytes(header);

        out.writeBytes(bytes.get());
    }

    private void encodeResponse( ByteBuf out, Response res) {

        byte[] header = new byte[HEADER_LENGTH];
        // set magic number. 2个字节
        Bytes.short2bytes(MAGIC, header);

        // set response status.
        byte status = res.getStatus();
        header[3] = status;

        //set requset id  8个字节
        Bytes.long2bytes(res.getmId(), header, 4);

        if (status == Response.OK) {
            Optional<byte[]> bytes  = Bytes.objectToBytes(res.getmResult());
            if (bytes.isPresent()) {
                Bytes.int2bytes(bytes.get().length, header, 12);
            } else {
                Bytes.int2bytes(0, header, 12);
            }

            out.writeBytes(header);

            out.writeBytes(bytes.get());

        } else {
            byte[] bytes  = res.getmErrorMsg().getBytes();
            Bytes.int2bytes(bytes.length, header, 11);

            out.writeBytes(header);

            out.writeBytes(bytes);
        }

    }

    private class InternalDecoder extends ByteToMessageDecoder {

        @Override
        protected void decode(ChannelHandlerContext ctx, ByteBuf input, List<Object> out) throws Exception {

            Object msg;

            int saveReaderIndex;

            try {
                // decode object.
                do {
                    saveReaderIndex = input.readerIndex();
                    try {

                        int readable = input.readableBytes();
                        byte[] header = new byte[Math.min(readable, HEADER_LENGTH)];
                        input.readBytes(header);

                        msg = NettyCode.this.decode(input, header, readable);

                    } catch (IOException e) {
                        throw e;
                    }
                    if (msg == Codec2.DecodeResult.NEED_MORE_INPUT) {
                        input.readerIndex(saveReaderIndex);
                        break;
                    } else {
                        //is it possible to go here ?
                        if (saveReaderIndex == input.readerIndex()) {
                            throw new IOException("Decode without read data.");
                        }

                        if (msg != null) {
                            out.add(msg);
                        }
                    }
                } while (input.isReadable());
            } finally {

            }
        }
    }


    private Object decode(ByteBuf input, byte[] header, int readable) throws IOException {
        // check length.
        if (readable < HEADER_LENGTH) {
            return Codec2.DecodeResult.NEED_MORE_INPUT;
        }
        // get data length.
        int len = Bytes.bytes2int(header, 12);

        int tt = len + HEADER_LENGTH;

        if (readable < tt) {
            return Codec2.DecodeResult.NEED_MORE_INPUT;
        }
        try {
            return decodeBody(input,  header,len);
        }finally {

        }
    }

    private Object decodeBody(ByteBuf input, byte[] header,int len) throws IOException {
        byte flag=header[2];
        long id = Bytes.bytes2long(header, 4);
        if ((flag & FLAG_REQUEST) == 0) {
            // decode response.
            Response res = new Response(id);
            // get status.
            byte status = header[3];
            res.setStatus(status);
            try {
                byte[] data = new byte[len];
                input.readBytes(data);

                if (status == Response.OK) {
                    res.setmResult(Bytes.bytesToObject(data));
                } else {
                    res.setmErrorMsg(data.toString());
                }
            } catch (Exception e) {
                res.setStatus(Response.CLIENT_ERROR);
                res.setmErrorMsg(e.getMessage());
            }
            return res;
        } else {
            // decode request
            Request req = new Request(id);
            req.setmTwoWay((flag & FLAG_TWOWAY) != 0);
            try {
                byte[] data = new byte[len];
                input.readBytes(data);
                req.setData(Bytes.bytesToObject(data));
            } catch (Throwable t) {
                req.setBroken(true);
                req.setData(t);
            }
            return req;
        }
    }
}
