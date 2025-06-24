package com.example.rpccommon.util;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.util.internal.shaded.org.jctools.queues.MpscUnboundedArrayQueue;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @Author Cbc
 * @DateTime 2025/6/13 17:32
 * @Description
 */
public class MyBatchQueue{
    private final AtomicBoolean executing;
    private final Queue<ByteBuf> queue;
    private final int chunkSize;
    private static final int defaultChunkSize = 128;
    private final Channel channel;
    private final List<ByteBuf> list;
    private  int sum = 0;
    public MyBatchQueue(Channel channel){
        this(channel,128);
    }
    public MyBatchQueue(Channel channel, int chunkSize){
        this.queue = new MpscUnboundedArrayQueue<>(10000);
        this.executing = new AtomicBoolean(false);
        this.chunkSize = chunkSize;
        this.channel = channel;
        this.list = new ArrayList<>(chunkSize);
    }

    public void enqueue(ByteBuf item){
        queue.add(item);
        execute();
    }

    private void execute(){
        if(!executing.compareAndSet(false, true)){
            return;
        }
        channel.eventLoop().execute(this::testBufferRun);
    }

    //固定大小数量批处理
    private void run(){

        try {
            long remainingWritable = channel.bytesBeforeUnwritable();
            int num = 0;
            ByteBuf item;
            while (num < chunkSize && (item = queue.poll()) != null){
                num++;
                push(item);
            }
            flush();
        } finally {
            executing.set(false);
            if(!queue.isEmpty()){
                run();
            }
        }
    }

    //根据缓冲区大小进行批处理
    private void testBufferRun(){
        try {
            long remainingWritable = channel.bytesBeforeUnwritable();
            ByteBuf item;
            while ((item = queue.poll()) != null && (sum += item.readableBytes()) <= remainingWritable ){
                push(item);
            }
            if(sum != 0 && channel.isWritable()){
                flush((int) remainingWritable);
                sum = 0;
                if(item != null){
                    push(item);
                    sum = item.readableBytes();
                }
            }


        } finally {
            if(!queue.isEmpty() || sum > 0){
                testBufferRun();
            }
            executing.set(false);
            if(!queue.isEmpty() || sum > 0){
               testBufferRun();
            }
        }
    }
    protected void push(ByteBuf item){
       list.add(item);
    }

    protected void flush(){
        ByteBuf buffer = channel.alloc().buffer();
        for (ByteBuf buf : list) {
            buffer.writeBytes(buf);
            buf.release();
        }
        list.clear();
        channel.writeAndFlush(buffer);
    }

    protected void flush(int len){
        ByteBuf buffer = channel.alloc().buffer(len);
        for (ByteBuf buf : list) {
            buffer.writeBytes(buf);
            buf.release();
        }
        list.clear();
        channel.writeAndFlush(buffer);
    }


}
