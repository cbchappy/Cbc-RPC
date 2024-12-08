package com.example.rpcclient.protocol;

import com.example.rpccommon.config.ProtocolConfig;
import com.example.rpccommon.message.Request;
import com.example.rpccommon.message.RpcMsg;
import com.example.rpccommon.serializer.RpcSerializer;
import com.example.rpccommon.util.RpcUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;

import java.util.List;

/**
 * @Author Cbc
 * @DateTime 2024/12/8 16:40
 * @Description
 */
public class RpcClientMsgCodec extends ByteToMessageCodec<RpcMsg> {
    @Override
    protected void encode(ChannelHandlerContext ctx, RpcMsg msg, ByteBuf out) throws Exception {

        //魔数 4
        out.writeInt(ProtocolConfig.getMagic());
        //版本号 1
        out.writeByte(ProtocolConfig.getVersion());
        //todo 从用户配置文件获取 序列化方式 1
        Byte serializerTypeCode = ProtocolConfig.getSerializerTypeCode();
        out.writeByte(serializerTypeCode);
        //请求类型 1
        out.writeByte(RpcMsg.getTypeCode(msg));
        //对齐填充
        out.writeByte(0);


        RpcSerializer serializer = RpcSerializer.getSerializerByCode(serializerTypeCode);

        byte[] bytes = serializer.serialize(msg);
        //内容长度 4
        out.writeInt(bytes.length);
        out.writeBytes(bytes);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        RpcUtil.verifyMsg(in);

        byte serializerTypeCode = in.readByte();

        byte msgTypeCode = in.readByte();//根据消息类型进行相应处理

        in.readByte();//无意义
    }
}
