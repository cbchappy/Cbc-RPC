package com.example.rpcclient.blance;


import com.alibaba.nacos.api.naming.pojo.Instance;

import java.net.InetSocketAddress;
import java.util.List;


/**
 * @Author Cbc
 * @DateTime 2024/12/9 15:56
 * @Description
 */
public interface LoadBalance {
    Instance loadBalancingAndGet(List<Instance> instances);
}
