package com.example.rpccommon.util;

import com.alibaba.fastjson.JSON;
import com.example.rpccommon.serializer.JsonSerializer;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import okio.BufferedSink;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @Author Cbc
 * @DateTime 2025/6/2 16:28
 * @Description
 */
@Slf4j
public class SpanReportClient {
    private static final List<Span> spanList = new ArrayList<>();
    private static final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);

    public static void report(Span span) {
        synchronized (spanList) {
            spanList.add(span);
        }
    }

    public static void startReport() {
        ScheduledFuture<?> future = executor.scheduleAtFixedRate(new ReportRunnable(), 5, 5, TimeUnit.SECONDS);
    }

    private static class ReportRunnable implements Runnable {

        @Override
        public void run() {
            OkHttpClient client = new OkHttpClient();
            String js;
            synchronized (spanList) {
                js = JSON.toJSONString(spanList);
                spanList.clear();
            }
            RequestBody body = RequestBody.create(js, MediaType.parse("application/json"));
            // 创建GET请求
            Request request = new Request.Builder()
                    .url("http://localhost:8083/report")
                    .post(body)
                    .build();
            try {
                Response response = client.newCall(request).execute();
                response.close();
            } catch (IOException ignored) {

            }
        }
    }


}
