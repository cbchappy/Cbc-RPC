package com.example.collector.controller;

import com.alibaba.fastjson.JSON;
import com.example.collector.entity.Span;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @Author Cbc
 * @DateTime 2025/6/2 20:34
 * @Description
 */
@RestController
@RequestMapping("/report")
@Slf4j
public class ReportController {
    private final Map<String, List<Span>> map = new HashMap<>();
    private final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);

    @PostMapping
    public void report(@RequestBody List<Span> spanList) {
        if (spanList == null || spanList.size() == 0) {
            return;
        }
        synchronized (map) {
            for (Span span : spanList) {
                List<Span> list = map.computeIfAbsent(span.getTraceId(), k -> new ArrayList<>());
                list.add(span);
                if (span.getType() == 0) {
                    executor.schedule(new CollectRunnable(list), 5, TimeUnit.SECONDS);
                }
            }
        }
    }

    private record CollectRunnable(List<Span> list) implements Runnable {
        @Override
        public void run() {
            //1 开始
            Map<Integer, List<Span>> listMap = new HashMap<>();
            for (Span span : list) {
                int l = span.getSpanId().split("\\.").length;
                List<Span> spanList = listMap.computeIfAbsent(l, k -> new ArrayList<>());
                spanList.add(span);
            }
            int len = listMap.size();
            StringBuilder builder = new StringBuilder(list.get(0).getTraceId());
            builder.append("--->");
            for(int i = 1; i <= len; i++){
                List<Span> spans = listMap.get(i);
                Span span = spans.get(0);
                String name = span.isServer() ? "服务端" : "客户端";
                long time = span.getEndTime() - span.getStartTime();
                builder.append(name);
                builder.append("耗时：");
                builder.append(time);
                builder.append("纳秒");
                if(!span.isSuccess()){
                    builder.append('\n');
                    builder.append(span.getExceptionMsg());
                }
                if(i != len){
                    builder.append("--->");
                }
            }
            System.out.println(builder);
        }
    }


}
