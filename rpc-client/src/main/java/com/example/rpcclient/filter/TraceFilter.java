package com.example.rpcclient.filter;

import com.example.rpcclient.server.InstanceWrapper;
import com.example.rpccommon.RpcContext;
import com.example.rpccommon.message.Request;
import com.example.rpccommon.message.Response;
import com.example.rpccommon.util.RpcUtils;
import com.example.rpccommon.util.Span;
import com.example.rpccommon.util.SpanReportClient;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

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
        String traceId = (String) RpcContext.getServerAttachment("traceId");
        String parentSpanId = (String) RpcContext.getServerAttachment("spanId");
        if(traceId == null || parentSpanId == null){
            traceId = RpcUtils.createTraceId();
            parentSpanId = null;
        }

        String spanId = RpcUtils.generateSpanId(parentSpanId);
        attachment.put("traceId", traceId);
        attachment.put("spanId", spanId);
        Span span = Span.builder()
                .traceId(traceId)
                .parentSpanId(parentSpanId)
                .spanId(spanId)
                .interfaceName(request.getInterfaceName())
                .methodName(request.getMethodName())
                .argsClassNames(request.getArgsClassNames())
                .isServer(false)
                .type(parentSpanId == null ? 0 : 2)
                .startTime(System.nanoTime())
                .build();

        Response response = chain.doFilter(wrapper, request, index);
        span.setStatus(request.getTypeCode());
        span.setEndTime(System.nanoTime());
        span.setSuccess(true);
        if(!response.isSuccess()){
            span.setSuccess(false);
            Throwable throwable = response.getThrowable();
            if(throwable != null){
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                PrintStream printStream = new PrintStream(outputStream);
                throwable.printStackTrace(printStream);
                span.setExceptionMsg(outputStream.toString());
            }
        }
        RpcContext.removeServerAttachment("traceId");
        RpcContext.removeServerAttachment("spanId");
        SpanReportClient.report(span);
        return response;
    }
}
