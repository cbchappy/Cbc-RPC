package com.example.rpcclient.handler;

import com.alibaba.nacos.api.naming.pojo.Instance;
import com.example.rpcclient.extend.BeforeEncodeHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author Cbc
 * @DateTime 2025/1/7 23:07
 * @Description
 */
@Slf4j
public class BeforeEncodeDoHandler extends ChannelOutboundHandlerAdapter {
    private final Instance instance;
    private final static List<BeforeEncodeHandler> LIST = new ArrayList<>();

    public BeforeEncodeDoHandler(Instance instance) {
        this.instance = instance;
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        //获取filter并执行
        log.debug("在消息被编码前执行过滤器链");
        for (BeforeEncodeHandler handler : LIST) {
            handler.doHandleBeforeEncode(ctx, msg, instance);
        }
        super.write(ctx, msg, promise);
    }

    public static void addBeforeEncodeHandler(BeforeEncodeHandler handler){
        LIST.add(handler);
    }
}
