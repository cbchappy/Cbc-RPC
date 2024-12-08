package com.example.rpcserver.protocol;

import com.example.rpccommon.config.ProtocolConfig;
import com.example.rpccommon.message.RpcMsg;
import com.example.rpccommon.serializer.RpcSerializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;
import io.netty.util.AttributeKey;
import io.netty.util.AttributeMap;

import java.util.List;

/**
 * @Author Cbc
 * @DateTime 2024/12/8 18:26
 * @Description
 */
public class RpcServerMsgCodec extends ByteToMessageCodec<RpcMsg> {
    @Override
    protected void encode(ChannelHandlerContext ctx, RpcMsg msg, ByteBuf out) throws Exception {


        //魔数 4
        out.writeInt(ProtocolConfig.getMagic());
        //版本号 1
        out.writeByte(ProtocolConfig.getVersion());
        //从上下文获取 序列化方式 1
        byte serializerTypeCode = (byte) ctx.attr(AttributeKey.valueOf("serializerTypeCode")).get();

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
        int magic = in.readInt();

        byte version = in.readByte();

        if(magic != ProtocolConfig.getMagic() || version != ProtocolConfig.getVersion()){
            //
        }

        byte serializerTypeCode = in.readByte();
    }
}
