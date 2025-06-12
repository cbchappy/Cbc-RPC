package com.example.rpccommon.util;

import lombok.Builder;
import lombok.Data;

/**
 * @Author Cbc
 * @DateTime 2025/6/2 16:08
 * @Description
 */
@Data
@Builder
public class Span {
    private String traceId;
    private String spanId;
    private String parentSpanId;
    private long startTime;
    private long endTime;
    private String interfaceName;
    private String methodName;
    private String[] argsClassNames;//json序列化必需
    private boolean isServer;
    private boolean isSuccess;
    private String exceptionMsg;
    private int status;
    private int type;// 0开始 //1结束 2//中间
}
