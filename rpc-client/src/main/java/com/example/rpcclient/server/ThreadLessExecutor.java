package com.example.rpcclient.server;

import com.example.rpccommon.exception.RpcRequestException;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;


/**
 * @Author Cbc
 * @DateTime 2025/5/5 15:16
 * @Description
 */
@Slf4j
public class ThreadLessExecutor {
   private LinkedBlockingQueue<Callable<Object>> queue;
   public ThreadLessExecutor(){
       this.queue = new LinkedBlockingQueue<>(1);
   }

   public Object await(long waitTime, TimeUnit unit) throws Exception {
       Callable<Object> poll = queue.poll(waitTime, unit);
       if(poll == null){
           throw new RpcRequestException("请求超时!");
       }

       return poll.call();
   }

   public void aware(Callable<Object> callable){
           queue.add(callable);
   }
}
