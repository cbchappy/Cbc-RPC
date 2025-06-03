package com.example.test;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author Cbc
 * @DateTime 2025/5/5 20:05
 * @Description
 */
public class Main {
    public static void main(String[] args) {
        AtomicInteger integer = new AtomicInteger(Integer.MIN_VALUE);
        int i = integer.addAndGet(-1);
        System.out.println(i);
        System.out.println();
    }
}
