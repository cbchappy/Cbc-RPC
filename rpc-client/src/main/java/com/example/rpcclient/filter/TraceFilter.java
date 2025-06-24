package com.example.rpcclient.filter;

import com.example.rpcclient.config.ClientConfig;
import com.example.rpcclient.server.InstanceWrapper;
import com.example.rpccommon.RpcContext;
import com.example.rpccommon.constants.ResponseStatus;
import com.example.rpccommon.message.Request;
import com.example.rpccommon.message.Response;
import com.example.rpccommon.util.RpcUtils;
import com.example.rpccommon.util.Span;
import com.example.rpccommon.util.SpanReportClient;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

/**
 * @Author Cbc
 * @DateTime 2025/6/2 16:51
 * @Description
 */
public class TraceFilter implements InvokeFilter{
    @Override
    public Response doFilter(InstanceWrapper wrapper, Request request, InvokeFilterChain chain, int index) throws Throwable {
        Map<String, Object> attachment = request.getAttachment();
        if(attachment == null){
            attachment = new HashMap<>();
            request.setAttachment(attachment);
        }
        RpcContext context = RpcContext.getContext();
        String traceId = (String) context.get("traceId");
        String parentSpanId = (String) context.get("spanId");
        if(traceId == null || parentSpanId == null){
            traceId = RpcUtils.createTraceId();
            parentSpanId = null;
            context.put("traceId", traceId);
        }else {
            context.put("use", true);
            request.setRetryNum(0);
        }


        String spanId = RpcUtils.generateSpanId(parentSpanId);
        context.put("spanId", spanId);
        attachment.put("traceId", traceId);
        attachment.put("spanId", spanId);


        Response response = chain.doFilter(wrapper, request, index);
        if(!response.isAsync() && !response.isSuccess() && !response.getStatus().equals(ResponseStatus.MOCK.code) && request.getRetryNum() > 0){
            return response;
        }

        if(response.isAsync()){
            CompletableFuture<?> future = (CompletableFuture<?>) response.getRes();
           future = future.whenComplete((BiConsumer<Object, Throwable>) (o, throwable) -> {
                new TraceRunnable((Response) o, request).run();
            });
            response.setRes(future);
        }else {
            new TraceRunnable(response, request).run();
        }

        return response;
    }

    private record TraceRunnable(Response response, Request request) implements Runnable {

        @Override
            public void run() {
                RpcContext context = RpcContext.getContext();
                String trId = (String) context.get("traceId");
                String spId = (String) context.get("spanId");
                String prSpanId = (String) context.get("parentSpanId");
                Span span = Span.builder()
                        .traceId(trId)
                        .parentSpanId(prSpanId)
                        .spanId(spId)
                        .interfaceName(request.getInterfaceName())
                        .methodName(request.getMethodName())
                        .argsClassNames(request.getArgsClassNames())
                        .isServer(false)
                        .type(prSpanId == null ? 0 : 2)
                        .startTime(System.nanoTime())
                        .build();



                span.setEndTime(System.nanoTime());
                span.setSuccess(true);
                if (!response.isSuccess()) {
                    span.setSuccess(false);
                    Throwable throwable = response.getThrowable();
                    if (throwable != null) {
                        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                        PrintStream printStream = new PrintStream(outputStream);
                        throwable.printStackTrace(printStream);
                        span.setExceptionMsg(outputStream.toString());
                    }
                }

                SpanReportClient.report(span);
            }
        }
}
