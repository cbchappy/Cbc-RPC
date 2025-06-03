package com.example.rpcclient.server;

import com.alibaba.nacos.api.naming.pojo.Instance;
import com.example.rpcclient.tolerant.CircuitBreaker;
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

}
