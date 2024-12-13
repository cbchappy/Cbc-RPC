package com.example.rpccommon.config;

/**
 * @Author Cbc
 * @DateTime 2024/12/8 14:36
 * @Description rpc协议默认配置
 */
public class ProtocolConfig {
    private static final Integer magic = 8888;

    private static final Byte version = 1;

    public static Integer getMagic(){
        return magic;
    }

    public static Byte getVersion(){
        return version;
    }


}
