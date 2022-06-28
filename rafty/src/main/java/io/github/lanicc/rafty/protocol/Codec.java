package io.github.lanicc.rafty.protocol;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.serializer.SerializerFeature;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;

import java.util.List;

/**
 * Created on 2022/6/28.
 *
 * @author lan
 */
public class Codec extends ByteToMessageCodec<Protocol> {

    @Override
    protected void encode(ChannelHandlerContext ctx, Protocol msg, ByteBuf out) throws Exception {
        byte[] bytes = JSON.toJSONBytes(msg, SerializerFeature.WriteClassName);
        out.writeBytes(bytes);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        byte[] bytes = new byte[in.readableBytes()];
        in.readBytes(bytes);
        out.add(JSON.parse(bytes, Feature.SupportAutoType));
    }
}
