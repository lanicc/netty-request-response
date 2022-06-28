package io.github.lanicc.rafty.protocol;

import io.github.lanicc.rafty.protocol.command.HelloResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * Created on 2022/6/28.
 *
 * @author lan
 */
public class ServerHandler extends SimpleChannelInboundHandler<Protocol> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Protocol protocol) throws Exception {
        System.out.println(protocol);
        HelloResponse helloResponse = new HelloResponse();
        helloResponse.setId(protocol.getId());
        helloResponse.setResponse("this is response: " + protocol);
        ctx.writeAndFlush(helloResponse);
    }
}
