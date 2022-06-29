package io.github.lanicc.rpc;

import io.github.lanicc.protocol.ClientHandler;
import io.github.lanicc.protocol.Codec;
import io.github.lanicc.protocol.Protocol;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;

import java.util.StringJoiner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created on 2022/6/28.
 *
 * @author lan
 */
public class Client {

    private final String host;

    private final int port;

    private final Channel channel;

    private final ClientHandler responseHandler;

    public Client(String host, int port) {
        this.host = host;
        this.port = port;

        responseHandler = new ClientHandler(4);
        EventLoopGroup group = new NioEventLoopGroup();

        ChannelFuture channelFuture =
                new Bootstrap()
                        .channel(NioSocketChannel.class)
                        .handler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel channel) throws Exception {
                                channel.pipeline()
                                        .addLast(new LengthFieldBasedFrameDecoder(1024, 0, 4, 0, 4))
                                        .addLast(new LengthFieldPrepender(4))
                                        .addLast(new Codec())
                                        .addLast(responseHandler);
                            }

                        })
                        .group(group)
                        .connect(host, port);
        try {
            channel = channelFuture.sync().channel();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public <T extends Protocol> T requestSync(Protocol protocol) throws ExecutionException, InterruptedException, TimeoutException {
        CompletableFuture<T> future = requestAsync(protocol);
        T t = future.get(5, TimeUnit.SECONDS);
        if (future.isDone()) {
            return t;
        }
        future.cancel(false);
        throw new TimeoutException();
    }

    public <T extends Protocol> CompletableFuture<T> requestAsync(Protocol protocol) throws InterruptedException {
        CompletableFuture<T> future = responseHandler.future(protocol.getId());
        channel.writeAndFlush(protocol);
        return future;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Client.class.getSimpleName() + "[", "]")
                .add("host='" + host + "'")
                .add("port=" + port)
                .add("request-processing=" + responseHandler.getProcessingRequestCount())
                .toString();
    }
}
