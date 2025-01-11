package com.example.rpcclient.server;

import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.listener.AbstractEventListener;
import com.alibaba.nacos.api.naming.listener.Event;
import com.alibaba.nacos.api.naming.listener.EventListener;
import com.alibaba.nacos.api.naming.listener.NamingEvent;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.example.rpcclient.blance.LoadBalance;
import com.example.rpcclient.blance.RandomLoadBalance;
import com.example.rpcclient.blance.RoundRobinLoadBalance;
import com.example.rpcclient.blance.WeightLoadBalance;
import com.example.rpcclient.config.ClientConfig;
import com.example.rpcclient.constants.FaultTolerantCode;
import com.example.rpcclient.handler.*;
import com.example.rpcclient.monitor.MonitorHandler;
import com.example.rpcclient.protocol.RpcClientMsgCodec;
import com.example.rpcclient.tolerant.FaultTolerant;
import com.example.rpcclient.tolerant.RetryFaultTolerant;
import com.example.rpccommon.constants.RpcExceptionMsg;
import com.example.rpccommon.exception.RpcException;
import com.example.rpccommon.message.CloseMsg;
import com.example.rpccommon.message.Request;
import com.example.rpccommon.protocol.ProtocolFrameDecoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.DefaultPromise;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;


import static com.example.rpcclient.config.ClientConfig.*;
import static com.example.rpcclient.constants.LoadBalanceTypeCode.*;
import static com.example.rpcclient.server.InstanceService.getAvailableServer;

//提供拓展点
//监控:单个实例->地址 名字 速度 出错次数 访问次数 是否可用 刷新速度
/**
 * @Author Cbc
 * @DateTime 2024/12/9 14:15
 * @Description 服务管理中心   管理所有rpc网络连接 //todo 注意考虑线程安全问题  先进行服务注册
 */
@Slf4j
public class ServerCenter {
    //todo 容错处理
    //todo 更新instance也要更新channelMap 处理掉弃用的连接
    private static final Map<Instance, Channel> channelMap = new ConcurrentHashMap<>();//根据实例存储channel 进行channel复用


    private final static FaultTolerant FAULT_TOLERANT;//根据配置文件获取容错处理实例


    static {
       if (FAULT_TOLERANT_CODE == FaultTolerantCode.RETRY) {
            FAULT_TOLERANT = new RetryFaultTolerant();
        }else {
            FAULT_TOLERANT = new RetryFaultTolerant();
            throw new RpcException(RpcExceptionMsg.FAULT_TOLERANT_NOT_FOUND);
        }
    }//初始化容错处理类

    static {
        InstanceService.addUpdateInstancesConsumer((o, n) -> {
            channelMap.keySet().removeIf(next -> !n.contains(next));
        });
    }//在instances更新时弃用掉不健康的连接

    static {
        if(MONITOR_LOG){
            log.debug("开启服务实例信息详细监控");
            MonitorHandler monitorHandler = new MonitorHandler();
            AfterResponseDoHandler.addAfterResponseHandler(monitorHandler);
            BeforeEncodeDoHandler.addBeforeEncodeHandler(monitorHandler);
        }
    }//添加监控处理器


    //进行远程调用
    public static Object remoteInvoke(Request request) throws Throwable {
        Instance instance = null;
        try {
           instance  = getAvailableServer();

            Channel channel = getChannel(instance);

            DefaultPromise<Object> promise = new DefaultPromise<>(channel.eventLoop());

            ResponseHandler.getMap().put(request.getMsgId(), promise);

            MyCloseFutureListener listener = new MyCloseFutureListener(promise);

            channel.closeFuture().addListener(listener);

            //服务器应该发送关闭连接信息
            //拒绝策略 0.协议错误导致关闭  1.熔断状态导致关闭  2.到达指定空闲时间导致主动关闭
            //进行容错处理 三种状态应该更换服务器进行重试
            //CloseMsg: status对应关闭理由

            channel.writeAndFlush(request);

            promise.await(OVERTIME, TimeUnit.SECONDS);

            channel.closeFuture().removeListener(listener);

            if(!promise.isDone()){
                log.debug("请求连接超时, msgId:{}", request.getMsgId());
                promise.setFailure(new RuntimeException(RpcExceptionMsg.REQUEST_OVERTIME));
            }

            if(promise.isSuccess()){
                log.debug("MsgId{}:成功获取结果!!", request.getMsgId());
                return promise.get();
            } else {
                log.debug("MsgId-{}:进行容错处理", request.getMsgId());
                return FAULT_TOLERANT.faultHandler(instance, promise.cause(), request);
            }
        } catch (Exception e) {
            log.debug("MsgId-{}:重大连接错误: 进行容错处理", request.getMsgId());
            return FAULT_TOLERANT.faultHandler(instance, e, request);
        }
    }
    //channel复用
    private static Channel getChannel(Instance instance) throws InterruptedException {

        if (channelMap.containsKey(instance)){
            return channelMap.get(instance);
        }

        synchronized (instance){
            if (channelMap.containsKey(instance)){
                log.debug("进行channel复用");
                return channelMap.get(instance);
            }
            NioEventLoopGroup work = new NioEventLoopGroup(1);
            Channel channel = new Bootstrap()
                    .group(work)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new ProtocolFrameDecoder());//解码
                            ch.pipeline().addLast(new RpcClientMsgCodec());//codec
                            ch.pipeline().addLast(new ServerCloseHandler());//服务器主动关闭处理器
                            ch.pipeline().addLast(new ResponseHandler());//响应处理器
                            ch.pipeline().addLast(new AfterResponseDoHandler(instance));//响应后处理器
                            ch.pipeline().addLast(new BeforeEncodeDoHandler(instance));//编码前处理器


                            //到达指定空闲时间触发事件
                            if(LONG_CONNECTION){
                                ch.pipeline().addLast(new IdleStateHandler( CONNECT_IDLE_TIME, 0, 0, TimeUnit.SECONDS));
                                ch.pipeline().addLast(new IdleStateHandler(0, PING_INTERVAL,0, TimeUnit.SECONDS));
                                ch.pipeline().addLast(new ReadIdleStateEventHandler(instance));//处理写空闲
                                ch.pipeline().addLast(new WriteIdleEventHandler());//处理读空闲
                            }

                        }
                    })
                    .option(ChannelOption.SO_KEEPALIVE, LONG_CONNECTION)
                    .connect(instance.getIp(), instance.getPort())
                    .sync()
                    .channel();

            channelMap.put(instance, channel);

            channel.closeFuture().addListener((ChannelFutureListener) future -> {
                log.debug("关闭eventLoop");
                channelMap.remove(instance);
                future.channel().eventLoop().shutdownGracefully();
            });
            log.debug("连接到新channel");
            return channel;
        }

    }

    //停止channel
    public static void stopChannel(Instance instance){
        Channel channel = channelMap.get(instance);
        if(channel == null){
            return;
        }
        channel.close();
    }


    public static class MyCloseFutureListener implements ChannelFutureListener{
        private DefaultPromise<Object> promise;

        private MyCloseFutureListener(DefaultPromise<Object> promise){
            this.promise = promise;
        }

        @Override
        public void operationComplete(ChannelFuture future) throws Exception {
            log.debug("operationComplete");
            Attribute<Object> attr = future.channel().attr(AttributeKey.valueOf("close"));
            Object o = attr.get();
            if(o == null || promise.isDone()){
                log.debug("return");
                return;
            }
            CloseMsg closeMsg = (CloseMsg) o;
            Integer status = closeMsg.getStatus();
            if(status.intValue() == CloseMsg.CloseStatus.normal.code){
                promise.setFailure(new RuntimeException(RpcExceptionMsg.IDLE_TIME_REFUSE));
                return;
            }
            if(status.intValue() == CloseMsg.CloseStatus.protocolError.code){
                promise.setFailure(new RuntimeException(RpcExceptionMsg.VERIFY_ERROR));
                return;
            }
            if(status.intValue() == CloseMsg.CloseStatus.refuse.code){
                promise.setFailure(new RuntimeException(RpcExceptionMsg.SERVER_REFUSE));
            }

        }
    }


}
