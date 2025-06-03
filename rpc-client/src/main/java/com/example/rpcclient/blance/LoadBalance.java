package com.example.rpcclient.blance;


import com.example.rpcclient.server.InstanceWrapper;

import java.util.List;


/**
 * @Author Cbc
 * @DateTime 2024/12/9 15:56
 * @Description
 */
public interface LoadBalance {
    InstanceWrapper loadBalancingAndGet(List<InstanceWrapper> instances);
}
