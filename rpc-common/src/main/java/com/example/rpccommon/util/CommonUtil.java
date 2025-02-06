package com.example.rpccommon.util;

import com.example.rpccommon.Main;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.URL;

/**
 * @Author Cbc
 * @DateTime 2025/1/12 16:19
 * @Description
 */
public class CommonUtil {

    public static void printLogo(){

        URL url = CommonUtil.class.getResource("");

        File file = new File(url.getPath());
        for (int i = 0; i < 4; i++) {
            file = file.getParentFile();
        }

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file.getPath() + "/logo.txt"));
            String s = null;
            while ((s = reader.readLine()) != null){
                System.out.println(s);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }




}
