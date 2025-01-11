package com.example.rpcclient.monitor;

import com.alibaba.nacos.api.naming.pojo.Instance;
import com.example.rpcclient.extend.AfterResponseHandler;
import com.example.rpcclient.extend.BeforeEncodeHandler;
import com.example.rpcclient.server.InstanceService;
import com.example.rpccommon.constants.ResponseStatus;
import com.example.rpccommon.message.Request;
import com.example.rpccommon.message.Response;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @Author Cbc
 * @DateTime 2025/1/10 21:45
 * @Description 监控信息处理器
 */
@Slf4j
public class MonitorHandler implements AfterResponseHandler, BeforeEncodeHandler {
    private final Map<Instance, MonitorModel> infoMap = new ConcurrentHashMap<>();
    private final Map<Integer, Long> timeMap = new ConcurrentHashMap<>();

    public MonitorHandler() {
        InstanceService.addUpdateInstancesConsumer((o, n) -> {
            for (Instance instance : infoMap.keySet()) {
                if (!n.contains(instance)) {
                    infoMap.get(instance).setUseful(false);
                }
            }
            for (Instance instance : n) {
                if (!infoMap.containsKey(instance)) {
                    infoMap.put(instance, getOriginalMonitorModel(instance));
                }
            }
        });
        //开启打印
        printInfo();

    }

    @Override
    public void doHandlerAfterResponse(ChannelHandlerContext ctx, Response msg, Instance instance) {
        MonitorModel model = infoMap.get(instance);

       synchronized (model){
           model.setCount(model.getCount() + 1);//总次数
           if (msg.getStatus().intValue() != ResponseStatus.SUCCESS.code) {
               model.setExceptionCount(model.getExceptionCount() + 1);//错误次数
           }
           long end = System.currentTimeMillis();
           Long start = timeMap.remove(msg.getMsgId());
           model.setAllTime(model.getAllTime() + end - start);
       }



    }

    @Override
    public void doHandleBeforeEncode(ChannelHandlerContext ctx, Object msg, Instance instance) {
        if (msg instanceof Request r) {
            timeMap.put(r.getMsgId(), System.currentTimeMillis());
        }
    }

    private static MonitorModel getOriginalMonitorModel(Instance instance) {
        return MonitorModel.builder()
                .hashcode(instance.hashCode())
                .name(instance.getInstanceId())
                .address(instance.getIp() + ":" + instance.getPort())
                .count(0)
                .exceptionCount(0)
                .useful(instance.isHealthy())
                .allTime(0)
                .build();
    }


    private void printInfo(){

        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1, r -> {
            Thread thread = new Thread(r);
            thread.setName("monitor-thread");
            return thread;
        });
        executor.scheduleAtFixedRate(this::doPrintInfo, 5, 5, TimeUnit.SECONDS);

    }

    private void doPrintInfo(){
        String line = "*************************************************************************";
        StringBuilder all = new StringBuilder("\n");
        all.append(line).append("\n");
        Set<Instance> keySet = infoMap.keySet();
        for (Instance instance : keySet) {
            MonitorModel model = infoMap.get(instance);
            StringBuilder mid = new StringBuilder();
            mid.append("hashcode: ").append(model.getHashcode()).append("\n");
            mid.append("name: ").append(model.getName()).append("\n");
            mid.append("address: ").append(model.getAddress()).append("\n");
            mid.append("count: ").append(model.getCount()).append("\n");
            mid.append("exceptionCount: ").append(model.getExceptionCount()).append("\n");
            mid.append("useful: ").append(model.isUseful()).append("\n");
            float speed = 0;
            if(model.getCount() != 0){
                speed = (float) (model.getAllTime() / model.getCount());
            }
            mid.append("speed: ").append(String.format("%.2f", speed)).append("\n");

            all.append("{").append("\n");
            all.append(mid);
            all.append("}").append("\n");
        }
        all.append(line);
        log.debug("rpcClient监控日志信息:{}", all);
    }
}
