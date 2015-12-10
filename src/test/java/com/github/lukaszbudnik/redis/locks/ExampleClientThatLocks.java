package com.github.lukaszbudnik.redis.locks;

import org.redisson.RedissonClient;
import org.redisson.core.RLock;

import java.util.concurrent.TimeUnit;

public class ExampleClientThatLocks {
    private final RLock lock;
    private final FakeLimitedResource resource;
    private final String clientName;

    public ExampleClientThatLocks(RedissonClient client, String lockPath, FakeLimitedResource resource, String clientName) {
        this.resource = resource;
        this.clientName = clientName;
        lock = client.getLock(lockPath);
    }

    public void doWork(long time, TimeUnit unit, int repetition) throws Exception {
        boolean acquired = lock.tryLock(time, time, unit);

        if (!acquired) {
            System.err.println(clientName + " didn't acquire the lock repetition " + repetition);
            return;
        }

        try {
            System.out.println(clientName + " has the lock repetition " + repetition);
            resource.use();
        } finally {
            System.out.println(clientName + " releasing the lock " + repetition);
            lock.unlock();
        }
    }
}
