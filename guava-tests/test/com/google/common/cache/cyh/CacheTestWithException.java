package com.google.common.cache.cyh;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * Created by yanhuche on 6/21/2016.
 */
public class CacheTestWithException {


    private static final Logger LOGGER = LoggerFactory.getLogger(CacheTestWithException.class);
    private Cache<Integer, Double> cache = CacheBuilder.newBuilder().maximumSize(1000).build();


    public static void main(String[] args) throws InterruptedException {
        new CacheTestWithException().run();
    }

    private void run() throws InterruptedException {
        /**
         * 第一次由于第一根线程抛出异常，导致所有线程均无法获取值
         * 但是，这并不影响第二次的逻辑，即是说，第二次还是能正常获取值（前提是没有异常）
         */
        multiThreadGet("A");
        TimeUnit.SECONDS.sleep(10);
        /**
         * 第二次可以正常获取到值（前提是没有异常）
         */
        multiThreadGet("B");
    }

    private void multiThreadGet(String type) {
        int threadCount = 2;
        while (threadCount-- > 0) {
            new Thread(() -> {
                int key = 1;
                for (int i = 0; i < key; i++) {
                    LOGGER.info("retrieved value() {} --> {}", i, retrieveValue(i));
                }
            }, type + "_thread_" + threadCount).start();
        }
    }

    private static final AtomicInteger first = new AtomicInteger(0);

    private Double retrieveValue(final int key) {
        LOGGER.info("Trying_to_get_value_for " + key);
        try {
            return cache.get(key, new Callable<Double>() {
                @Override
                public Double call() throws InterruptedException {
                    LOGGER.info("Really_obtain_key: " + key);
                    TimeUnit.SECONDS.sleep(5);
                    if (first.get() == 0) {
                        // 模拟让执行此方法的第一根线程抛出异常，看后续线程是否还会进入此方法执行 ====> 不会！
                        first.set(1);
                        /**
                         * 当多线程同时进入此 get() 方法，并真正进入 LocalCache # Segment # lockedGetOrLoad 方法时，
                         * 若第一根线程抛出异常（通过 loadSync 方法），则其它线程不会再来执行此处的 cache.get() 方法，
                         * 而是也抛出异常（通过 waitForLoadingValue() --> valueReference.waitForValue() --> getUninterruptibly() 调用路径）
                         */
                        throw new RuntimeException("Test throw Exception for the first thread");
                    }
                    return Math.random();
                }
            });
        } catch (Exception e) {
            LOGGER.error("cache.get() throws Exception", e);
        }
        return null;
    }


}
