package io.github.lanicc.rpc;

import io.github.lanicc.protocol.Protocol;
import io.github.lanicc.protocol.command.HelloRequest;
import org.junit.jupiter.api.Test;

import java.util.Scanner;
import java.util.concurrent.*;

/**
 * Created on 2022/6/28.
 *
 * @author lan
 */
class ServerTest {

    @Test
    void start() {

        final String host = "127.0.0.1";
        final int port = 8090;

        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch stopWatch = new CountDownLatch(1);
        pool.execute(() -> {
            Server server = new Server(port);
            server.start();
            try {
                stopWatch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                server.stop();
            }
        });
        pool.execute(() -> {
            Client client = new Client(host, port);

            while (true) {
                HelloRequest helloRequest =
                        new HelloRequest()
                                .setMessage("hello world");
                try {

                    try {
                        Protocol protocol = client.requestSync(helloRequest);
                        System.out.println(protocol);
                    } catch (TimeoutException e) {
                        throw new RuntimeException(e);
                    }
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    TimeUnit.SECONDS.sleep(2);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }
            }
        });

        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNext()) {
            String s = scanner.nextLine();
            if (s.contains("exit")) {
                pool.shutdownNow();
                return;
            }
        }


    }
}
