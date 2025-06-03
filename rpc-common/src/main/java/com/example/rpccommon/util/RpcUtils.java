package com.example.rpccommon.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.InetAddress;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author Cbc
 * @DateTime 2025/1/12 16:19
 * @Description
 */
public class RpcUtils {
    // 当前服务的Span层级计数器（线程安全）
    private static final ThreadLocal<AtomicInteger> spanCounter = ThreadLocal.withInitial(() -> new AtomicInteger(0));


    public static void printLogo(){

        URL url = RpcUtils.class.getResource("");

        File file = new File(url.getPath());
        for (int i = 0; i < 4; i++) {
            file = file.getParentFile();
        }

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file.getPath() + "/logo.txt"));
            String s = null;
            while ((s = reader.readLine()) != null){
                System.out.println(s);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // 生成TraceId的规则：IP(16进制) + 时间戳 + UUID前20位 + 进程号
    public static String createTraceId(){
        StringBuilder traceId = new StringBuilder();

        // 1. 添加IP地址（转为16进制）
        try {
            InetAddress address = InetAddress.getLocalHost();
            String ip = address.getHostAddress();
            String[] ipSegments = ip.split("\\.");
            for (int i = 0; i < 4; i++) {
                int segment = Integer.parseInt(ipSegments[i]);
                traceId.append(String.format("%02x", segment)); // 每段转为2位16进制
            }
        } catch (Exception e) {
            traceId.append("00000000"); // IP获取失败时使用默认值
        }

        // 2. 添加时间戳（精确到毫秒）
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        traceId.append(sdf.format(new Date()));

        // 3. 添加UUID前20位（去除横杠）
        traceId.append(UUID.randomUUID().toString().replace("-", "").substring(0, 20));

        // 4. 添加进程号（5位）
        RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
        String pid = runtime.getName().split("@")[0];
        traceId.append(String.format("%05d", Integer.parseInt(pid)));

        return traceId.toString();
    }


    /**
     * 生成子SpanId（基于父SpanId拼接）
     * @param parentSpanId 父SpanId（如首次请求为null或"0"）
     * @return 格式：父SpanId.当前序号（如"0.1"）
     */
    public static String generateSpanId(String parentSpanId) {
        if (parentSpanId == null || parentSpanId.isEmpty()) {
            return "0"; // 根节点
        }
        int currentSeq = spanCounter.get().incrementAndGet();
        return parentSpanId + "." + currentSeq;
    }

    // 清除当前线程的Span计数器（请求完成后调用）
    public static void clear() {
        spanCounter.remove();
    }


}
