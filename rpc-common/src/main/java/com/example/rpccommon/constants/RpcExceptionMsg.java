package com.example.rpccommon.constants;

/**
 * @Author Cbc
 * @DateTime 2024/12/9 12:27
 * @Description
 */
public class RpcExceptionMsg {

    public static final String SERIALIZER_NOTFOUND = "无法找到序列化器";
    public static final String VERIFY_ERROR = "消息解码校验不通过";
    public static final String NOT_FOUND_SERVER = "无可用的远程服务";
    public static final String LOAD_BALANCE_NOT_FOUND = "无法找到指定的负载均衡策略";
    public static final String MESSAGE_CONVERSION_ERROR = "消息类型转换出错";
    public static final String FAULT_TOLERANT_NOT_FOUND = "无法找到指定的容错处理策略";
    public static final String NACOS_CONNECT_FAIL = "nacos连接失败, 请检查输入信息";
    public static final String SERVER_REFUSE = "服务器拒绝连接";
    public static final String REQUEST_OVERTIME = "请求超时";
    public static final String IDLE_TIME_REFUSE = "通道因为到达服务器指定空闲时间而断开";
}
