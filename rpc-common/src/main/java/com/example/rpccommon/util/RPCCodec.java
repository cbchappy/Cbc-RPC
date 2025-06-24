package com.example.rpccommon.util;

import com.example.rpccommon.exception.RpcException;
import com.example.rpccommon.message.Request;
import com.example.rpccommon.message.Response;
import com.example.rpccommon.serializer.RpcSerializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import lombok.Data;

/**
 * @Author Cbc
 * @DateTime 2025/6/19 14:47
 * @Description  //魔数 2字节 请求唯一id 8字节  序列化号 1字节 消息类型 1字节  消息长度 4字节
 */
public class RPCCodec {

    private static final Short magicCode = 1111;

    public static final Byte requestCode = 0;

    public static final Byte responseCode = 1;

    public static final Byte pingCode = 2;

    public static final Byte pingAckCode = 3;

    public static ByteBuf encodeRequest(Request request, Channel channel){
        ByteBuf buffer = channel.alloc().buffer();
        buffer.writeShort(magicCode);//魔数
        buffer.writeLong(request.getRqId());//请求id
        buffer.writeByte(request.getSerializeCode());//序列化号
        buffer.writeByte(requestCode);//消息类型

        RpcSerializer serializer = RpcSerializer.getSerializerByCode(request.getSerializeCode());
        byte[] bytes = serializer.serialize(request);

        buffer.writeInt(bytes.length);//长度
        buffer.writeBytes(bytes);//数据


        return buffer;
    }

    public static ByteBuf encodeResponse(Response response, Channel channel){
        ByteBuf buffer = channel.alloc().buffer();

        buffer.writeShort(magicCode);//魔数
        buffer.writeLong(response.getRqId());//请求id
        buffer.writeByte(response.getSerializeCode());//序列化号
        buffer.writeByte(responseCode);//消息类型

        RpcSerializer serializer = RpcSerializer.getSerializerByCode(response.getSerializeCode());
        byte[] bytes = serializer.serialize(response);

        buffer.writeInt(bytes.length);//长度
        buffer.writeBytes(bytes);//数据

        return buffer;
    }

    public static ByteBuf encodePingMsg(Channel channel){
        ByteBuf buffer = channel.alloc().buffer();
        buffer.writeShort(magicCode);//魔数
        buffer.writeLong(0);//请求id
        buffer.writeByte(0);//序列化号
        buffer.writeByte(pingCode);
        buffer.writeInt(0);//长度

        return buffer;
    }

    public static ByteBuf encodePingAckMsg(Channel channel){
        ByteBuf buffer = channel.alloc().buffer();
        buffer.writeShort(magicCode);//魔数
        buffer.writeLong(0);//请求id
        buffer.writeByte(0);//序列化号
        buffer.writeByte(pingAckCode);
        buffer.writeInt(0);//长度

        return buffer;
    }

    public static Pack decodePack(Channel channel, ByteBuf buf){
        Pack pack;
        try {
            short mc = buf.readShort();
            if(mc != magicCode){
                channel.close();
                throw new RpcException("消息校验错误，关闭连接!");
            }
            //魔数 2字节 请求唯一id 8字节  序列化号 1字节 消息类型 1字节  消息长度 4字节
            pack = new Pack();
            pack.setMagicCode(mc);
            pack.setRqId(buf.readLong());
            pack.setSerializeCode(buf.readByte());
            pack.setMsgType(buf.readByte());
            pack.setDataLen(buf.readInt());
            byte[] bytes = new byte[pack.getDataLen()];
            buf.readBytes(bytes);
            pack.setData(bytes);
        } finally {
            buf.release();
        }

        return pack;
    }

    @Data
    public static class Pack{
        private Short magicCode;

        private Long rqId;

        private Byte serializeCode;

        private Byte msgType;

        private Integer dataLen;

        private byte[] data;
    }

}
