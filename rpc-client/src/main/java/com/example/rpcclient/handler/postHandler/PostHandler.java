package com.example.rpcclient.handler.postHandler;

import com.example.rpccommon.message.Response;
import io.netty.util.concurrent.Promise;

/**
 * @Author Cbc
 * @DateTime 2025/4/20 16:47
 * @Description
 */
public interface PostHandler {
    void handler(Response response, PostHandlerChain chain, int index);
}
