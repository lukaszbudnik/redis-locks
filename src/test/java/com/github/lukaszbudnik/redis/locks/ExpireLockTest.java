package com.github.lukaszbudnik.redis.locks;

import org.junit.Assert;
import org.junit.Test;
import org.redisson.Config;
import org.redisson.Redisson;
import org.redisson.RedissonClient;
import org.redisson.core.RLock;

import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ExpireLockTest {

    private static final String PATH = "/examples/locks";

    @Test
    public void test() throws InterruptedException {
        final Config config = new Config();
        config.useSingleServer().setAddress("192.168.99.100:6379");

        ExecutorService es = Executors.newFixedThreadPool(2);

        RedissonClient redisson1 = Redisson.create(config);
        RLock lock1 = redisson1.getLock(PATH);

        RedissonClient redisson2 = Redisson.create(config);
        RLock lock2 = redisson2.getLock(PATH);

        es.submit(() -> {
            System.out.println("About to acquire lock from 1 " + new Date());
            lock1.lock(10, TimeUnit.SECONDS);
            System.out.println("Released lock from 1 " + new Date());
        });

        // should be enough for executor service to pickup callable
        Thread.sleep(1000);

        // callable should have acquired the lock by now, lock2.tryLock returns false
        boolean shouldNotBeAcquired = lock2.tryLock(1, 10, TimeUnit.SECONDS);
        Assert.assertFalse(shouldNotBeAcquired);

        Thread.sleep(9000);

        // the lock1 should have expired by now, lock2.tryLock returns true
        boolean shouldBeAcquired = lock2.tryLock(1, 10, TimeUnit.SECONDS);
        Assert.assertTrue(shouldBeAcquired);

        es.shutdown();
    }

}
