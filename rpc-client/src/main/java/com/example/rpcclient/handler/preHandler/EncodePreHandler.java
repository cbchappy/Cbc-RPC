package com.example.rpcclient.handler.preHandler;

import com.example.rpcclient.config.ClientConfig;
import com.example.rpccommon.config.ProtocolConfig;
import com.example.rpccommon.constants.RpcMsgTypeCode;
import com.example.rpccommon.message.Request;
import com.example.rpccommon.message.RpcMsg;
import com.example.rpccommon.serializer.RpcSerializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

/**
 * @Author Cbc
 * @DateTime 2025/5/4 15:02
 * @Description
 *///1 版本  4 魔数 1 消息类型 1 额外信息码  1 序列化方式 4 消息id 4 消息长度
@Slf4j
public class EncodePreHandler implements PreHandler{
    @Override
    public Object handler(Channel channel, Object obj, PreHandlerChain chain, int index) {

        RpcMsg msg = (RpcMsg) obj;

        ByteBuf out = channel.alloc().buffer();
        //版本 1
        out.writeByte(ProtocolConfig.getVersion());
        //魔数 4
        out.writeInt(ProtocolConfig.getMagic());
        //消息类型 1
        int msgTypeCode = msg.getTypeCode();
        out.writeByte(msgTypeCode);
        //额外信息码 1
        out.writeByte(0);
        //序列化方式 1
        byte serializerTypeCode = ClientConfig.SERIALIZER_TYPE_CODE.byteValue();
        out.writeByte(serializerTypeCode);

        if(msgTypeCode != RpcMsgTypeCode.REQUEST){
            out.writeInt(0);
            out.writeInt(0);
            return out;
        }

        //消息id 4
        out.writeInt(((Request)msg).getMsgId());
        RpcSerializer serializer = RpcSerializer.getSerializerByCode(serializerTypeCode);

        byte[] bytes = serializer.serialize(msg);
        //内容长度 4
        out.writeInt(bytes.length);
        out.writeBytes(bytes);

        log.debug("ByteBuf-out的长度:{}", out.readableBytes());
        //i i
        return out;


    }
}
