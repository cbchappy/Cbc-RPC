package com.example.rpccommon;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * @Author Cbc
 * @DateTime 2025/6/2 14:32
 * @Description
 */
public class RpcContext {
    private static ThreadLocal<Map<String, Object>> clientAttachment = ThreadLocal.withInitial(HashMap::new);
    private static ThreadLocal<Map<String, Object>> serverAttachment = ThreadLocal.withInitial(HashMap::new);

    public static Object getClientAttachment(String s){
        return clientAttachment.get().get(s);
    }

    public static Object getServerAttachment(String s){
        return serverAttachment.get().get(s);
    }
    public static void putServerAttachment(String s, Object v){
        serverAttachment.get().put(s, v);
    }
    public static void putClientAttachment(String s, Object v){
        clientAttachment.get().put(s, v);
    }
    public static Object removeServerAttachment(String s){
        return serverAttachment.get().remove(s);
    }
    public static void removeClientAttachment(String s){
        clientAttachment.get().remove(s);
    }
}
