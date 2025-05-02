package com.example.rpcclient.handler.postHandler;

import com.alibaba.nacos.api.naming.pojo.Instance;
import com.example.rpcclient.server.InstanceService;
import com.example.rpccommon.message.Response;
import io.netty.util.concurrent.Promise;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;

/**
 * @Author Cbc
 * @DateTime 2025/4/20 16:47
 * @Description spi创建
 */
public class PostHandlerChain {
    private Instance instance;

    private final List<PostHandler> list = new ArrayList<>();

    public PostHandlerChain(Instance instance){
        this.instance = instance;
    }


    public PostHandlerChain addPostHandler(PostHandler hd){
        list.add(hd);
        return this;
    }

    public void doHandle(Response response, int index){
        if(index < list.size()){
            list.get(index).handler(response, this, index + 1);
        }
    }

    public static PostHandlerChain createPostHandlerChain(Instance instance){
        PostHandlerChain chain = new PostHandlerChain(instance);
        chain.addPostHandler(DefaultPostHandler.getInstance());
        for (PostHandler next : ServiceLoader.load(PostHandler.class)) {
            chain.addPostHandler(next);
        }
        return chain;
    }

    public Instance getInstance(){
        return instance;
    }

}
