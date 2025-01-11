package com.example.rpcclient.monitor;


import lombok.Builder;
import lombok.Data;

/**
 * @Author Cbc
 * @DateTime 2025/1/10 21:44
 * @Description 监控的实体模型
 */
@Data
@Builder
public class MonitorModel {

        private int hashcode;
        private String name;//instanceId
        private String address;//ip:port
        private Integer count;
        private Integer exceptionCount;
        private boolean useful;
        private long allTime;//

}
