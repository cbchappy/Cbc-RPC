package com.example.rpccommon.constants;

import java.util.HashMap;
import java.util.Map;

public enum ResponseStatus {
    //1表示正常
    //3开头鉴权错误
    //4开头是request错误
    //5是response错误
    SUCCESS(100, "成功"),
    NO_PERMISSION(300, "鉴权失败"),
    INTERFACE_NOT_FOUND(401, "请求接口不存在"),
    ARGS_METHOD(402, "方法参数解析错误"),
    METHOD_NOT_FOUND(403, "请求调用的方法不存在"),
    SERVER_EXCEPTION(500, "服务端出现未知异常"),
    SERVER_IMPL_NOT_FOUND(501, "服务端未存在接口的实现类"),
    SERIALIZATION(502, "服务端序列化器异常"),
    ;
    public final Integer code;

    public final String msg;

    public static final Map<Integer, ResponseStatus> map = new HashMap<>();

    static {
        for (ResponseStatus value : ResponseStatus.values()) {
            map.put(value.code, value);
        }
    }

    public static ResponseStatus getEnumByCode(Integer code){
       return map.get(code);
    }


    ResponseStatus(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }


}