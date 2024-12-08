package com.example.rpccommon.config;

/**
 * @Author Cbc
 * @DateTime 2024/12/8 14:36
 * @Description
 */
public class ProtocolConfig {
    private static final Integer magic = 8888;

    private static final Byte version = 1;


    //todo 应该结合配置文件让用户进行自定义
    private static final Byte serializerTypeId = 0;

    public static Integer getMagic(){
        return magic;
    }

    public static Byte getVersion(){
        return version;
    }

    public static Byte getSerializerTypeCode(){
        return serializerTypeId;
    }

}
