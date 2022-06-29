package io.github.lanicc.protocol;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.internal.PlatformDependent;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Created on 2022/6/28.
 *
 * @author lan
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class ClientHandler extends SimpleChannelInboundHandler<Protocol> {


    private final Map<String, CompletableFuture> completableFutureMap;

    private final Semaphore throttle;

    private final int maxWaitSize;

    public ClientHandler(int maxWaitSize) {
        this.maxWaitSize = maxWaitSize;
        this.completableFutureMap = PlatformDependent.newConcurrentHashMap(maxWaitSize);
        this.throttle = new Semaphore(maxWaitSize);
    }

    public <T> CompletableFuture<T> future(String id) throws InterruptedException {
        boolean b = throttle.tryAcquire(1, TimeUnit.SECONDS);
        if (!b) {
            throw new RuntimeException("too many waiting requests");
        }
        CompletableFuture<T> completableFuture = new CompletableFuture<>();
        completableFuture.whenComplete((t, throwable) -> remove(id));
        CompletableFuture<T> future = completableFutureMap.putIfAbsent(id, completableFuture);
        return Objects.isNull(future) ? completableFuture : future;
    }

    public void remove(String id) {
        throttle.release();
        completableFutureMap.remove(id);
    }

    public int getProcessingRequestCount() {
        return maxWaitSize - throttle.availablePermits();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Protocol protocol) throws Exception {
        String id = protocol.getId();
        CompletableFuture future = completableFutureMap.remove(id);
        future.complete(protocol);
        throttle.release();
    }
}
