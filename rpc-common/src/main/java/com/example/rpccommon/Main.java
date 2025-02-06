package com.example.rpccommon;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.support.config.FastJsonConfig;
import com.example.rpccommon.message.PingMsg;
import com.example.rpccommon.message.Request;
import com.example.rpccommon.serializer.JsonSerializer;
import com.example.rpccommon.util.CommonUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @Author Cbc
 * @DateTime 2024/12/13 11:41
 * @Description
 */
@Slf4j
public class Main {
    public static void main(String[] args) throws Exception {

//        URL url = Main.class.getResource("");
//        System.out.println(url);
//        System.out.println(url.getPath());
//        System.out.println(url.getFile());
//        File file = new File(url.getFile());
//        for (int i = 0; i < 4; i++) {
//            System.out.println(file.getPath());
//            file = file.getParentFile();
//        }
        CommonUtil.printLogo();

    }


    @Data
    public static class Test{
        private Object[] args;
        private Class[] paramTypes;
        private String[] strings;


        public Test(Object[] args) {
            this.args = args;
            paramTypes = new Class[args.length];
            strings = new String[args.length];
            for (int i = 0; i < args.length; i++) {
                paramTypes[i] = args[i].getClass();
                strings[i] = args[i].getClass().getName();
            }
        }

        public void printCl(){
            for (Class paramType : paramTypes) {
                System.out.println(paramType);
            }
        }
    }
}
