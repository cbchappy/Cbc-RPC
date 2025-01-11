package com.example.rpcclient.extend;

import com.alibaba.nacos.api.naming.pojo.Instance;
import com.example.rpccommon.message.Response;
import io.netty.channel.ChannelHandlerContext;

/**
 * @Author Cbc
 * @DateTime 2025/1/7 23:48
 * @Description
 */
public interface AfterResponseHandler {
    void doHandlerAfterResponse(ChannelHandlerContext ctx, Response msg, Instance instance);
}
