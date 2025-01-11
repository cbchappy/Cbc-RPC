package com.example.rpcclient.handler;

import com.alibaba.nacos.api.naming.pojo.Instance;
import com.example.rpcclient.extend.AfterResponseHandler;
import com.example.rpccommon.message.Response;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author Cbc
 * @DateTime 2025/1/7 23:06
 * @Description 在获取response后执行过滤器 扩展点之一
 */
@Slf4j
public class AfterResponseDoHandler extends SimpleChannelInboundHandler<Response> {
    private final static List<AfterResponseHandler> list = new ArrayList<>();
    private final Instance instance;
    public AfterResponseDoHandler(Instance instance){
        this.instance = instance;
    }
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Response msg) throws Exception {
        log.debug("在获取并处理完response后执行处理器链");
        for (AfterResponseHandler handler : list) {
            handler.doHandlerAfterResponse(ctx, msg, instance);
        }
        ctx.fireChannelRead(msg);
    }


    public static void addAfterResponseHandler(AfterResponseHandler handler){
        list.add(handler);
    }
}
