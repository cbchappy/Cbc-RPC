package com.example.rpcclient;

import lombok.extern.slf4j.Slf4j;

/**
 * @Author Cbc
 * @DateTime 2025/1/11 19:30
 * @Description
 */
@Slf4j
public class Main {
    int i = 0;

    public static void main(String[] args) {
        Main main = new Main();
        main.i++;
        System.out.println(main.i);
    }
}
