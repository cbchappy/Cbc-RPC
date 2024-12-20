package com.example.rpccommon;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.support.config.FastJsonConfig;
import com.example.rpccommon.message.PingMsg;
import com.example.rpccommon.message.Request;
import com.example.rpccommon.serializer.JsonSerializer;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

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
    public static void main(String[] args) {
        Object[] ar = {new ArrayList<>(), new int[]{0, 8, 9}};
        Test t = new Test(ar);
        String json = JSON.toJSONString(t);
        System.out.println(json);
        Test test = JSON.parseObject(json, Test.class);
        Class[] paramTypes = test.paramTypes;
        String[] strings = test.strings;
        for (String string : strings) {
            System.out.println(string);
        }

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
