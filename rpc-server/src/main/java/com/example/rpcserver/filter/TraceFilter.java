package com.example.rpcserver.filter;

import com.example.rpccommon.RpcContext;
import com.example.rpccommon.message.Request;
import com.example.rpccommon.message.Response;
import com.example.rpccommon.util.RpcUtils;
import com.example.rpccommon.util.Span;
import com.example.rpccommon.util.SpanReportClient;
import io.netty.channel.Channel;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Map;

/**
 * @Author Cbc
 * @DateTime 2025/6/2 16:03
 * @Description
 */
public class TraceFilter implements ServerFilter{
    @Override
    public Response doFilter(Request request, Channel channel, ServerFilterChain chain, int index) {

        Map<String, Object> attachment = request.getAttachment();
        String traceId = (String) attachment.get("traceId");
        String parentSpanId = (String) attachment.get("spanId");
        Long over = (Long) attachment.get("term");
        if(traceId == null || parentSpanId == null){
            return chain.doFilter(request, channel, index);
        }
        RpcContext context = RpcContext.getContext();
        String spanId = RpcUtils.generateSpanId(parentSpanId);
        context.put("traceId", traceId);
        context.put("spanId", spanId);
        context.put("use", false);
        context.put("term", over);
        Span span = Span.builder()
                .traceId(traceId)
                .parentSpanId(parentSpanId)
                .spanId(spanId)
                .interfaceName(request.getInterfaceName())
                .methodName(request.getMethodName())
                .argsClassNames(request.getArgsClassNames())
                .isServer(true)
                .startTime(System.nanoTime())
                .build();

        Response response = chain.doFilter(request, channel, index);
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
                throwable.printStackTrace();
            }
        }
        context.remove("traceId");
        context.remove("spanId");
        context.remove("term");
        Object use = context.remove("use");

        int code = (boolean) use ? 2 : 1;
       span.setType(code);
        SpanReportClient.report(span);
        return response;
    }
}
