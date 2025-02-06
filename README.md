# Cbc-RPC

## 简介

这是一款基于nacos和netty实现的rpc框架, 提供了多种负载均衡算法、序列化算法和容错策略, 并且提供了一些自定义注解和application文件下的属性参数, 方便用户在spring环境下使用

## 项目模块简介

- rpc-common  存储客户端和服务端通用的类和工具
- rpc-client  客户端的主要代码实现, 引入其jar包便可进行rpc调用
- rpc-server  服务端的主要代码实现, 引入其jar包便可提供rpc服务
- rpc-server-spring-boot-starter 和springboot相结合的服务端, 可在spring-boot环境下快速使用
- rpc-client-spring-boot-starter 和springboot相结合的客户端, 可在spring-boot环境下快速使用
- test-client 提供对客户端的测试
- test-server 提供对服务端的测试

## 框架图解

![架构图](./img/RPC框架思路.jpeg)

## 特点
- 三种序列化算法: java原生序列化算法、JSON算法、 Hessian算法
- 三种负载均衡策略: 轮询策略、随机算法、权重算法
- 两种容错策略: 重试策略(客户端)、熔断策略(服务端)
- 采用了ltc解码器, 防止粘包和粘包
- 提供自定义注解和配置文件参数、可以让用户在spring环境下方便快速使用
- 实现了自定义通信协议、简洁明了、节省空间
- 客户端使用了channel复用, 节省了资源和时间
- 客户端开启了服务监听, 确保能够按时获取健康的服务实例
- 使用了多种锁机制, 保证服务的并发安全
- 多种配置参数, 用户可以根据软件环境配置合适的参数

## 自定义rpc协议
```
+---------------+---------------+-----------------+
|  Magic Number | Version Number| Package Type    | 
|    4 byte     |    1 byte     |     1 bytes     | 
+---------------+---------------+-----------------+
|Serializer Type|    Align fill |   DataLength    |     
|     1 byte    |     1 byte    |       4 byte    |                     
+-------------------------------------------------+   
|               Data bytes                        |  
|           (length = DataLength)                 |  
+-------------------------------------------------+                                               
```
|       名字        |         解释          |
|:---------------:|:-------------------:|                         
|  Magic Number   |  魔数, 表明这是自定义的rpc协议  |
| Version Number  |    版本号, rpc协议的版本    |  
|  Package Type   |       消息包的类型        |
 | Serializer Type |      采用的序列化方式       |
  |   Align fill    |     对齐填充, 加快读取      | 
  |   DataLength    |       消息内容的长度       |
|   Data bytes    | 消息内容, 长度为DataLength |  

## 快速上手

### 软件使用环境
- jdk版本为17
- 部署了nacos, 版本最好为(1.4.1)
- 如果使用了springboot, 版本最好为(3.0.5)
- 确保客户端要远程调用的接口的包名同服务端开放的接口一模一样, 接口名和方法也一样
- 先在ClientConfig(客户端)和ServerConfig(服务端)配置好参数信息, 内有注释进行解释
- 如果是springboot环境下, 可以在application文件通过前缀"cbc.rpc"来配置参数信息(会自动有注解提示)

### 例子的前情提要
- 假设客户端要调用的接口名为TestRpc
- 服务端有接口TestRpc, 包名同客户端一样, 方法也和客户端一样, 另有一个实现类TestRpcImpl
- TestRpc有一个方法: ```public String get();```

### 原生使用(未结合spring使用)

#### 服务端

```java
public class TestServer {

    public static void main(String[] args) throws Exception {
        RpcServer.openServiceImpl(TestRpcImpl.class, TestRpc.class);//开放远程调用接口的实现类
        RpcServer.startServer();//开启远程调用服务
    }
    
}
```

#### 客户端

```java
public class TestClient {

    public static void main(String[] args) throws InterruptedException {
        //直接调用代理工厂生成要远程调用接口的代理实例即可
        TestRpc proxy = (TestRpc) ProxyFactory.createProxy(TestRpc.class);
        proxy.get();//直接调用方法便可进行远程调用
    }

}
```

### 结合spring使用

#### 服务端

```java
@SpringBootApplication
@StartRpcServer(values = {"com.example.test.service"})//启动类上添加@StartRpcServer注解, 指明远程调用接口所在的包
public class SpringTestServer {
    public static void main(String[] args) {
        SpringApplication.run(Server.class, args);
    }
}
```

```java
@OpenRpcService//在远程调用接口的实现类上添加@OpenRpcService注解, 表明这个接口可以被远程调用
public class TestRpcImpl implements TestRpc {
    @Override
    public String get() {
        return "test_res";
    }
}
```

#### 客户端

```java
@SpringBootApplication
@RpcClientScan(values = {"com.example.test.service"})//启动类上添加这个注解,并指明接口所在的包
public class SpringTestClient {
    public static void main(String[] args) {
        SpringApplication.run(Client.class, args);
    }
}
```

```java

@RpcInvoke//添加@RpcInvoke注解, 表明这个接口要被远程调用
public interface TestRpc {
    String get();
}

```

