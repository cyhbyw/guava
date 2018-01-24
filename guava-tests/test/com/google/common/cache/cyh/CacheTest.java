package com.google.common.cache.cyh;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * Created by yanhuche on 6/21/2016.
 */
public class CacheTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(CacheTest.class);
    private Cache<Integer, Double> cache = CacheBuilder.newBuilder().maximumSize(1000).build();


    public static void main(String[] args) throws InterruptedException {
        new CacheTest().run();
    }

    private void run() throws InterruptedException {
        // 第一次模板多线程的并发操作：读 + 没有值就创建值并放入缓存
        multiThreadGet(1);
        TimeUnit.SECONDS.sleep(20);
        // 第二次，值已存在于缓存中，则直接读
        multiThreadGet(2);
    }

    private void multiThreadGet(final int x) {
        int threadCount = 3;
        while (threadCount-- > 0) {
            new Thread(() -> {
                int key = 2 * x;
                for (int i = 0; i < key; i++) {
                    LOGGER.info("retrieved value() {} --> {}", i, retrieveValue(i));
                }
            }, "thread_" + x + "_" + threadCount).start();
        }
    }

    private Double retrieveValue(final int key) {
        LOGGER.info("Trying_to_get_value_for " + key);
        try {
            return cache.get(key, new Callable<Double>() {
                @Override
                public Double call() throws InterruptedException {
                    LOGGER.info("Really_obtain_key: " + key);
                    TimeUnit.SECONDS.sleep(1);
                    return Math.random();
                }
            });
        } catch (ExecutionException e) {
            LOGGER.error("cache.get() throws Exception", e);
        }
        return null;
    }


}
