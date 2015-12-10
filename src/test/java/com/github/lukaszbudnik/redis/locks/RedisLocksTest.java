package com.github.lukaszbudnik.redis.locks;

import org.junit.Test;
import org.redisson.Config;
import org.redisson.Redisson;
import org.redisson.RedissonClient;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/*
 * Modelled after: https://github.com/apache/curator/blob/master/curator-examples/src/main/java/locking/LockingExample.java
 */


public class RedisLocksTest {

    private static final int QTY = 50;
    private static final int REPETITIONS = 300;
    private static final String PATH = "/examples/locks";

    @Test
    public void test() throws InterruptedException {

        // simple Redis running on docker
        final Config config = new Config();
        config.useSingleServer().setAddress("192.168.99.100:6379");

        final FakeLimitedResource resource = new FakeLimitedResource();

        ExecutorService service = Executors.newFixedThreadPool(QTY);

        for (int i = 0; i < QTY; ++i) {
            final int index = i;
            service.submit(() -> {
                RedissonClient redisson = Redisson.create(config);
                try {
                    ExampleClientThatLocks example = new ExampleClientThatLocks(redisson, PATH, resource, "Client " + index);
                    for (int j = 0; j < REPETITIONS; ++j) {
                        example.doWork(10, TimeUnit.SECONDS, j);
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                } finally {
                    redisson.shutdown();
                }
            });
        }

        service.shutdown();
        service.awaitTermination(20, TimeUnit.MINUTES);
    }

}
