package com.google.common.cache.cyh;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * Created by yanhuche on 6/21/2016.
 */
public class CacheTest {


    private Cache<Integer, Double> cache = CacheBuilder.newBuilder().maximumSize(1000).build();


    public static void main(String[] args) {
        new CacheTest().run();
    }

    private void run() {
        int threadCount = 5;
        while (threadCount-- > 0) {
            new Thread(() -> {
                int key = 2;
                while (key-- > 0) {
                    System.out.println(Thread.currentThread().getName() + " --> " + retrieveValue(key));
                }
            }, "thread_" + threadCount).start();
        }
    }

    private Double retrieveValue(final int key) {
        System.out.println(Thread.currentThread().getName() + " is trying to get value for " + key);
        try {
            return cache.get(key, new Callable<Double>() {
                @Override
                public Double call() throws InterruptedException {
                    System.out.println(Thread.currentThread().getName() + " really obtain key: " + key);
                    TimeUnit.SECONDS.sleep(2);
                    return Math.random();
                }
            });
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }


}
