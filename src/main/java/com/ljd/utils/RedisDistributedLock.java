package com.ljd.utils;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.params.SetParams;

public class RedisDistributedLock {

    private static final String LOCK_SUCCESS = "OK";
    private static final Long RELEASE_SUCCESS = 1L;

    public static boolean tryGetDistributedLock(Jedis jedis, String lockKey, String requestId, int expireTime) {
        SetParams params = new SetParams();
        params.nx().px(expireTime);

        String result = jedis.set(lockKey, requestId, params);
        return LOCK_SUCCESS.equals(result);
    }

    public static boolean releaseDistributedLock(Jedis jedis, String lockKey, String requestId) {
        String script =
                "if redis.call('get', KEYS[1]) == ARGV[1] " +
                "then " +
                "    return redis.call('del', KEYS[1]) " +
                "else " +
                "    return 0 " +
                "end";
        Object result = jedis.eval(script, 1, lockKey, requestId);

        return RELEASE_SUCCESS.equals(result);
    }
}
