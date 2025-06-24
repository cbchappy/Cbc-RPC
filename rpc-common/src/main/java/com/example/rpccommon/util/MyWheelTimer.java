package com.example.rpccommon.util;

import com.example.rpccommon.exception.RpcException;
import io.netty.util.HashedWheelTimer;
import io.netty.util.internal.shaded.org.jctools.queues.atomic.MpscUnboundedAtomicArrayQueue;

import java.util.Queue;
import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @Author Cbc
 * @DateTime 2025/6/21 15:52
 * @Description 单线程处理任务转移，单线程执行任务，应该避免任务阻塞过长
 */
public class MyWheelTimer {
    private final long tickDuration; //单位为毫秒
    private long p;
    private final Queue<TimeOutTask> queue;//存储任务
    private final TimeOutTask[] wheel;
    private final Executor executor;
    private final AtomicBoolean start;
    private long startTime;
    private final int mask;

    public MyWheelTimer(){
       this(200, 512);
    }

    public MyWheelTimer(long tickDuration, int wheeLen){
        // 格子数应该满足2的幂次方
        this.tickDuration = tickDuration;
        //时间轮格子数
        wheeLen = normalizeTicksPerWheel(wheeLen);
        this.mask = wheeLen - 1;
        this.p = 0;
        this.queue = new MpscUnboundedAtomicArrayQueue<>(10000);
        this.wheel = new TimeOutTask[wheeLen];
        this.executor = Executors.newSingleThreadExecutor();
        this.start = new AtomicBoolean(false);
        this.startTime = 0;
    }

    public void addTask(Runnable runnable, long timeout, TimeUnit timeUnit){
        start();
       long deadline = System.nanoTime() + timeUnit.toNanos(timeout);
        TimeOutTask task = new TimeOutTask();
        task.deadlineTime = deadline;
        task.runnable = runnable;
        queue.add(task);
    }

    private int normalizeTicksPerWheel(int wheelLen){
        int v = 1;
        while (v < wheelLen){
            v = v << 1;
        }
        return v;
    }


    private static class TimeOutTask{
        private long deadlineTime;
        private Runnable runnable;
        private TimeOutTask next;
        private TimeOutTask pre;
        private long round;
    }

    //开启工作线程
    public void start(){
        if(!start.compareAndSet(false, true)){
            return;
        }
        this.startTime = System.nanoTime();
        Runnable runnable = () -> {
            while (true){
                long deadline = startTime + tickDuration * (p + 1) * 1000000;
               //p表示当前格子 余数即可获得
                transferTaskToBuckets();
                int idx = (int) (p & mask);
                executeBucket(idx);
                long currentTime = System.nanoTime();
                long sleepTimeMs = (deadline - currentTime) / 1000000;
                //双重检查
                if(sleepTimeMs > 0){
                        try {
                            Thread.sleep(sleepTimeMs);
                        } catch (InterruptedException ignored) {
                            System.err.println("时间轮报错！！！");
                        }
                }
                p++;
            }
        };
        executor.execute(runnable);
    }

    private void transferTaskToBuckets(){
        int v = 0;
        while (v < 100000 && !queue.isEmpty()){
            v++;
           addTaskToBucket(queue.poll());
        }

    }

    private void executeBucket(int idx){
        TimeOutTask task = wheel[idx];
        while (task != null){
            if(task.round > 0){
                task.round--;
                task = task.next;
                continue;
            }
            if(task.pre == null){
                wheel[idx] = task.next;
               if(wheel[idx] != null){
                   wheel[idx].pre = null;
               }
            }else {
                task.pre.next = task.next;
                if(task.next != null){
                    task.next.pre = task.pre;
                }
            }
            task.runnable.run();
            task.pre = null;
            TimeOutTask nt = task.next;
            task.next = null;
            task = nt;
        }
    }
    private void addTaskToBucket(TimeOutTask task){
        long t = ((task.deadlineTime - startTime) / 1000000) / tickDuration + 1;
        long round = (t - p) / wheel.length;
        int idx = (int) (Math.max(t, p) & mask);
        task.round = round;
        task.next = wheel[idx];
        if(wheel[idx] != null){
            wheel[idx].pre = task;
        }
        wheel[idx] = task;
    }
}
