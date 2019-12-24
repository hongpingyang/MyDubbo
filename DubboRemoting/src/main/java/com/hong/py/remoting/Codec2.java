package com.hong.py.remoting;

import com.hong.py.annotation.Adaptive;
import com.hong.py.annotation.SPI;
import com.hong.py.commonUtils.Constants;

import java.io.IOException;

/**
 * 文件描述
 *
 **/
@SPI
public interface Codec2 {

    /*@Adaptive({Constants.CODEC_KEY})
    void encode(Channel channel, ChannelBuffer buffer, Object message) throws IOException;

    @Adaptive({Constants.CODEC_KEY})
    Object decode(Channel channel, ChannelBuffer buffer) throws IOException;
   */
    enum DecodeResult {
        NEED_MORE_INPUT,
        SKIP_SOME_INPUT
    }
}
