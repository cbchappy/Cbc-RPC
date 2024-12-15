package com.example.rpcserver.protocol;

import com.example.rpccommon.config.ProtocolConfig;
import com.example.rpccommon.constants.ResponseStatus;
import com.example.rpccommon.constants.RpcMsgTypeCode;
import com.example.rpccommon.constants.SerializerCode;
import com.example.rpccommon.message.CloseMsg;
import com.example.rpccommon.message.Request;
import com.example.rpccommon.message.Response;
import com.example.rpccommon.message.RpcMsg;
import com.example.rpccommon.serializer.RpcSerializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.DefaultChannelPromise;
import io.netty.handler.codec.ByteToMessageCodec;
import io.netty.util.AttributeKey;
import io.netty.util.AttributeMap;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @Author Cbc
 * @DateTime 2024/12/8 18:26  ping(不用配置id) response(配置id)
 * @Description 魔数(4) 版本号(1) 请求类型(1)  序列化方式(1)  对齐填充(1) 长度(4)
 */
@Slf4j
public class RpcServerMsgCodec extends ByteToMessageCodec<RpcMsg> {
    @Override
    protected void encode(ChannelHandlerContext ctx, RpcMsg msg, ByteBuf out) throws Exception {
        //从上下文获取 序列化方式 1
        Object o = ctx.attr(AttributeKey.valueOf("serializerTypeCode")).get();
        //获取序列化方式
        byte serializerTypeCode = o == null ? SerializerCode.JDK.byteValue() : (byte) o;

        //魔数 4
        out.writeInt(ProtocolConfig.getMagic());
        //版本号 1
        out.writeByte(ProtocolConfig.getVersion());
        //请求类型 1
        Integer msgTypeCode = RpcMsg.getTypeCode(msg);
        out.writeByte(msgTypeCode);
        //序列化方式 1
        out.writeByte(serializerTypeCode);
        //对齐填充 1
        out.writeByte(0);

        RpcSerializer serializer = RpcSerializer.getSerializerByCode(serializerTypeCode);

        byte[] bytes = serializer.serialize(msg);
        //内容长度 4
        out.writeInt(bytes.length);

        out.writeBytes(bytes);

        log.debug("编码, magic:{}, version:{}, msgTypeCode:{}, serializerTypeCode:{}, len:{}",
                ProtocolConfig.getMagic(), ProtocolConfig.getVersion(), msgTypeCode, serializerTypeCode, bytes.length);
    }

    //魔数(4) 版本号(1) 请求类型(1) 序列化方式(1)  对齐填充(1) 长度(4)
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {

        int magic = in.readInt();//魔数

        byte version = in.readByte();//版本

        //校验失败
        if(magic != ProtocolConfig.getMagic() || version != ProtocolConfig.getVersion()){
            log.error("协议校验失败, 关闭channel");
            ctx.channel().writeAndFlush(new CloseMsg(CloseMsg.CloseStatus.protocolError));
            ctx.channel().close();
            return;
        }

        byte msgTypeCode = in.readByte();//消息类型


        byte serializerTypeCode = in.readByte();//序列化方式

        in.readByte();//无意义

        //将序列化方式添加进参数
        ctx.attr(AttributeKey.valueOf("serializerTypeCode")).set(serializerTypeCode);

        int len = in.readInt();//长度
        byte[] bytes = new byte[len];
        in.readBytes(bytes, 0, len);

        log.debug("解码, magic:{}, version:{}, msgTypeCode:{}, serializerTypeCode:{}, len:{}",
                magic, version, msgTypeCode, serializerTypeCode, len);

        RpcSerializer serializer = RpcSerializer.getSerializerByCode(serializerTypeCode);
        Object o = serializer.deSerialize(bytes);

        //todo delete
        //in.release(); 释放ByteBuf导致对象无法发送到后面的处理器 估计是netty自己会释放ByteBuf

        out.add(RpcMsg.typeConversion(o, msgTypeCode));

    }
}
