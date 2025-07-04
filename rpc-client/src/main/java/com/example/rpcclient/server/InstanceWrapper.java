package com.example.rpcclient.server;

import com.alibaba.nacos.api.naming.pojo.Instance;
import com.example.rpcclient.tolerant.CircuitBreaker;
import com.example.rpccommon.util.BatchExecutorQueue;
import com.example.rpccommon.util.MyBatchQueue;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import lombok.Builder;
import lombok.Data;



/**
 * @Author Cbc
 * @DateTime 2025/5/18 15:16
 * @Description
 */
@Builder
@Data
public class InstanceWrapper {
    private  Instance instance;
    private  Channel channel;
    private  CircuitBreaker breaker;
    private  Object lock;
    private MyBatchQueue queue;
}
