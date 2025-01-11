package com.example.rpcclient.extend;

import com.alibaba.nacos.api.naming.pojo.Instance;
import io.netty.channel.ChannelHandlerContext;

/**
 * @Author Cbc
 * @DateTime 2025/1/7 23:48
 * @Description
 */
public interface BeforeEncodeHandler {

    void doHandleBeforeEncode(ChannelHandlerContext ctx, Object msg, Instance instance);
}
