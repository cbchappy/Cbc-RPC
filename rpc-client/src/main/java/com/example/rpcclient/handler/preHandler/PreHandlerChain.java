package com.example.rpcclient.handler.preHandler;


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

    private final List<PreHandler> list = new ArrayList<>();


    public PreHandlerChain addPreHandler(PreHandler hd){
        list.add(hd);
        return this;
    }

    public Object doHandle(Channel channel, Object rq, int index){
        if(index < list.size()){
            return list.get(index).handler(channel, rq, this, index + 1);
        }
        return rq;
    }

    public static PreHandlerChain createPreHandlerChain(){
        PreHandlerChain chain = new PreHandlerChain();
        chain.addPreHandler(new EncodePreHandler());
        for (PreHandler next : ServiceLoader.load(PreHandler.class)) {
            chain.addPreHandler(next);
        }
        return chain;
    }

}
