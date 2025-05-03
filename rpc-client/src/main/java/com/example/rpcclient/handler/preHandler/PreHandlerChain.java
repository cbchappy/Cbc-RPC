package com.example.rpcclient.handler.preHandler;


import com.alibaba.nacos.api.naming.pojo.Instance;
import com.example.rpccommon.message.Request;
import io.netty.channel.Channel;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

/**
 * @Author Cbc
 * @DateTime 2025/4/22 16:07
 * @Description
 */
public class PreHandlerChain {
    private final Instance instance;
    private final List<PreHandler> list = new ArrayList<>();

    public PreHandlerChain(Instance instance){
        this.instance = instance;
    }

    public PreHandlerChain addPreHandler(PreHandler hd){
        list.add(hd);
        return this;
    }

    public void doHandle(Request rq, int index){
        if(index < list.size()){
            list.get(index).handler(rq, this, index + 1);
        }
    }

    public static PreHandlerChain createPreHandlerChain(Instance instance){
        PreHandlerChain chain = new PreHandlerChain(instance);
        for (PreHandler next : ServiceLoader.load(PreHandler.class)) {
            chain.addPreHandler(next);
        }
        return chain;
    }
    public Instance getInstance(){
        return instance;
    }
}
