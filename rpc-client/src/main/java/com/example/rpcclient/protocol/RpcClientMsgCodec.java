package com.example.rpcclient.protocol;

import com.example.rpcclient.config.ClientConfig;
import com.example.rpccommon.config.ProtocolConfig;
import com.example.rpccommon.constants.RpcExceptionMsg;
import com.example.rpccommon.constants.RpcMsgTypeCode;
import com.example.rpccommon.exception.RpcResponseException;
import com.example.rpccommon.message.PingMsg;
import com.example.rpccommon.message.Request;
import com.example.rpccommon.message.Response;
import com.example.rpccommon.message.RpcMsg;
import com.example.rpccommon.serializer.RpcSerializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @Author Cbc
 * @DateTime 2024/12/8 16:40
 * @Description 信息解码和编码端   协议格式: 魔数(4) 版本号(1)请求类型(1) 序列化方式(1) 对齐填充(1) 长度(4)
 */
@Slf4j
public class RpcClientMsgCodec extends ByteToMessageCodec<RpcMsg> {
    @Override
    protected void encode(ChannelHandlerContext ctx, RpcMsg msg, ByteBuf out) throws Exception {
        //魔数 4
        out.writeInt(ProtocolConfig.getMagic());//
        out.writeByte(ProtocolConfig.getVersion());
        //请求类型 1
        int msgTypeCode = msg.getTypeCode();
        out.writeByte(msgTypeCode);
        //序列化方式 1
        byte serializerTypeCode = ClientConfig.SERIALIZER_TYPE_CODE.byteValue();
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

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {

        int magic = in.readInt();
        byte version = in.readByte();
        if(magic != ProtocolConfig.getMagic() || version != ProtocolConfig.getVersion()){
            log.debug("校验失败");
            throw new RpcResponseException(RpcExceptionMsg.VERIFY_ERROR);
        }

        byte msgTypeCode = in.readByte();//根据消息类型进行相应处理 1

        byte serializerTypeCode = in.readByte();// 1


        in.readByte();//无意义 1

        int len = in.readInt();//长度 4
        byte[] bytes = new byte[len];
        in.readBytes(bytes, 0, len);

        RpcSerializer serializer = RpcSerializer.getSerializerByCode(serializerTypeCode);
        Class<?> aClass = RpcMsg.getClassByTypeCode(msgTypeCode);
        Object o = serializer.deSerialize(bytes, aClass);

        out.add(o);

        log.debug("解码, magic:{}, version:{}, msgTypeCode:{}, serializerTypeCode:{}, len:{}",
                magic, version, msgTypeCode, serializerTypeCode, len);



    }
}
