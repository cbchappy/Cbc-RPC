package com.example.rpcserver.handler;

import com.example.rpccommon.message.CloseMsg;
import com.example.rpccommon.message.RpcMsg;
import com.example.rpcserver.server.RpcServer;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

/**
 * @Author Cbc
 * @DateTime 2024/12/15 17:48
 * @Description 熔断处理器 根据熔断状态进行请求拦截
 */
@Slf4j
public class FusingHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        log.debug("RpcMsg?{}", msg instanceof RpcMsg);
        if(RpcServer.isFusing()){
            ctx.channel().writeAndFlush(new CloseMsg(CloseMsg.CloseStatus.refuse));
            ctx.channel().close();
            log.debug("熔断已开启, 拒绝连接");

            return;
        }
        RpcServer.countAdd();
        super.channelRead(ctx, msg);
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {

        log.debug("Registered isFusing?{}", RpcServer.isFusing());
        if(RpcServer.isFusing()){
            ctx.channel().writeAndFlush(new CloseMsg(CloseMsg.CloseStatus.refuse));
            ctx.channel().close();
            log.debug("熔断已开启, 拒绝连接");
            return;
        }
        super.channelRegistered(ctx);
    }


}
