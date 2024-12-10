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
import com.example.rpcclient.handler.IdleStateEventHandler;
import com.example.rpcclient.handler.ResponseHandler;
import com.example.rpcclient.protocol.RpcClientMsgCodec;
import com.example.rpccommon.constants.RpcExceptionMsg;
import com.example.rpccommon.exception.RpcException;
import com.example.rpccommon.message.Request;
import com.example.rpccommon.protocol.ProtocolFrameDecoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultPromise;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;


import static com.example.rpcclient.config.ClientConfig.*;
import static com.example.rpcclient.constants.LoadBalanceTypeCode.*;


/**
 * @Author Cbc
 * @DateTime 2024/12/9 14:15
 * @Description 服务管理中心 //todo 注意考虑线程安全问题  先进行服务注册
 */
@Slf4j
public class ServerCenter {
    //todo 容错处理
    //todo 更新instance也要更新channelMap 处理掉一些连接问题
    private static final Map<Instance, Channel> channelMap = new ConcurrentHashMap<>();

    private static volatile NamingService namingService;//nacos总服务端

    private static volatile List<Instance> instances;//服务实例

    //todo delete
    public final static AtomicInteger all = new AtomicInteger(0);

    //todo countMap
    public final static Map<String, AtomicInteger> countMap = new ConcurrentHashMap<>(7);

    private final static ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();//读写锁

    private final static LoadBalance LOAD_BALANCE;//根据配置文件获取负载均衡实例

    static {
        if(RANDOM_LOAD_BALANCE == LOAD_BALANCE_CODE){
            LOAD_BALANCE = new RandomLoadBalance();
            log.debug("开启随机策略");
        } else if (WEIGHT_LOAD_BALANCE == LOAD_BALANCE_CODE) {
            LOAD_BALANCE = new WeightLoadBalance();
            log.debug("开启权重策略");
        } else if (ROUND_ROBIN_LOAD_BALANCE == LOAD_BALANCE_CODE) {
            log.debug("开启轮询策略");
            LOAD_BALANCE = new RoundRobinLoadBalance();
        }else {
            log.debug("开启轮询策略");
            LOAD_BALANCE = new RoundRobinLoadBalance();
            throw new RpcException(RpcExceptionMsg.LOAD_BALANCE_NOT_FOUND);
        }
    }//初始化负载均衡类

    //服务发现 只启动一次
    private static void findServer() throws NacosException {

        if(namingService == null){

            synchronized (ServerCenter.class){
                if(namingService != null){
                    return;
                }
                Properties properties = new Properties();
                properties.setProperty(PropertyKeyConst.SERVER_ADDR, ClientConfig.SERVER_ADDR);
                properties.setProperty(PropertyKeyConst.PASSWORD, ClientConfig.PASSWORD);
                properties.setProperty(PropertyKeyConst.USERNAME, ClientConfig.USERNAME);

                namingService = NamingFactory.createNamingService(properties);
                String[] strs = {"0", "1", "2", "3", "4"};
                List<String> list = new ArrayList<>();
                list.addAll(Arrays.asList(strs));
                //instances = namingService.selectInstances(SERVER_NAME, SERVER_GROUP_NAME, SERVER_CLUSTER_NAME,true);
                instances = namingService.selectInstances(SERVER_NAME, SERVER_GROUP_NAME, list,true);
                listenServer();
                log.debug("count:{}", instances.size());
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                if(instances == null || instances.size() == 0){
                    throw new RpcException(RpcExceptionMsg.NOT_FOUND_SERVER);
                }


            }

        }



    }

    //监听服务实例变化
    private static void listenServer() throws NacosException {

        final ExecutorService executorService = Executors.newFixedThreadPool(1);

        EventListener serviceListener = new AbstractEventListener() {
            @Override
            public void onEvent(Event event) {
                if (event instanceof NamingEvent ) {
                    NamingEvent namingEvent = (NamingEvent) event;
                    List<Instance> list = namingEvent.getInstances();
                    List<Instance> collect = list.stream().filter(Instance::isHealthy).toList();
                    if(collect.size() == 0){
                        throw new RpcException(RpcExceptionMsg.NOT_FOUND_SERVER);
                    }
                    log.debug("服务实例发生变化, 服务个数为:{}", collect.size() );
                    updateInstances(collect);
                }
            }
            @Override
            public Executor getExecutor() {
                return executorService;
            }
        };
        String[] strs = {"0", "1", "2", "3", "4"};
        List<String> list = new ArrayList<>();
        list.addAll(Arrays.asList(strs));
        //namingService.subscribe(SERVER_NAME, SERVER_GROUP_NAME, SERVER_CLUSTER_NAME, serviceListener);
        namingService.subscribe(SERVER_NAME, SERVER_GROUP_NAME, list, serviceListener);
    }

    //更新实例 线程安全
    private static void updateInstances(List<Instance> list){
        rwl.writeLock().lock();
        HashSet<Instance> set = new HashSet<>(channelMap.keySet());
        set.forEach(ServerCenter::stopChannel);
        instances = list;
        //todo test
        for (Instance instance : list) {
           if(countMap.get(instance.getInstanceId()) == null){
               countMap.put(instance.getInstanceId(), new AtomicInteger());
           }
        }
        rwl.writeLock().unlock();
    }

    //获取负载均衡后的可用服务
    private static Instance getAvailableServer(){
        rwl.readLock().lock();
        try {
            //todo 测试
            Instance instance = LOAD_BALANCE.loadBalancingAndGet(instances);
            AtomicInteger atomic = countMap.get(instance.getInstanceId());
            atomic.addAndGet(1);
            return instance;
        } finally {
            rwl.readLock().unlock();
        }
    }


    //进行远程调用
    public static Object remoteInvoke(Request request) throws Throwable {

        if(namingService == null){
            findServer();
        }
        //todo 待获取结果
        Channel channel = getChannel(getAvailableServer());

        DefaultPromise<Object> promise = new DefaultPromise<>(channel.eventLoop());

        ResponseHandler.getMap().put(request.getMsgId(), promise);

        channel.writeAndFlush(request);

        promise.await();

        if(promise.isSuccess()){
            all.addAndGet(1);
            return promise.get();
        } else{
            throw promise.cause();
        }

    }
    //channel复用
    private static Channel getChannel(Instance instance) throws InterruptedException {

        if (channelMap.containsKey(instance)){
            return channelMap.get(instance);
        }

        synchronized (instance){
            if (channelMap.containsKey(instance)){
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
                            ch.pipeline().addLast(new ResponseHandler());//响应处理器
                            //到达指定空闲时间触发事件
                            ch.pipeline().addLast(new IdleStateHandler(0, 5, 0, TimeUnit.SECONDS));
                            ch.pipeline().addLast(new IdleStateEventHandler(instance));//处理空闲事件

                        }
                    })
                    .connect(instance.getIp(), instance.getPort())
                    .sync()
                    .channel();

            channelMap.put(instance, channel);

            channel.closeFuture().addListener((ChannelFutureListener) future -> {
                log.debug("关闭eventLoop");
                channelMap.remove(instance);
                future.channel().eventLoop().shutdownGracefully();
            });
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


}
