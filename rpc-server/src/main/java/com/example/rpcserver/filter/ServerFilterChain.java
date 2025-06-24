package com.example.rpcserver.filter;

import com.example.rpccommon.message.Request;
import com.example.rpccommon.message.Response;
import com.example.rpcserver.config.ServerConfig;
import com.example.rpcserver.server.RpcServer;
import io.netty.channel.Channel;


import java.util.ArrayList;
import java.util.List;

/**
 * @Author Cbc
 * @DateTime 2025/6/2 15:32
 * @Description
 */
public class ServerFilterChain {
    private final List<ServerFilter> list = new ArrayList<>();
    public Response doFilter(Request request, Channel channel, int index){
        if(index < list.size()){
            return list.get(index).doFilter(request, channel, this, index + 1);
        }
        return RpcServer.handleRequest(request, channel);
    }

    public void addFilter(ServerFilter filter){
        list.add(filter);
    }

    public static ServerFilterChain createChain(){
        ServerFilterChain chain = new ServerFilterChain();
        chain.addFilter(new RpcContextFilter());
       if(ServerConfig.TRACE){
           chain.addFilter(new TraceFilter());
       }
        chain.addFilter(new LimiterFilter());
        return chain;
    }
}
