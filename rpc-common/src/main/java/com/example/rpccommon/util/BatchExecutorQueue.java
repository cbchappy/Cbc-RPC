package com.example.rpccommon.util;


import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.util.internal.shaded.org.jctools.queues.MpscUnboundedArrayQueue;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

public class BatchExecutorQueue<T> {

    static final int DEFAULT_QUEUE_SIZE = 128;
    private final Queue<T> queue;
    private final AtomicBoolean scheduled;
    private final int chunkSize;
    private final List<T> list;
    private final Channel channel;

    public Channel getChannel(){
        return channel;
    }


    public BatchExecutorQueue(Channel channel) {
        this(DEFAULT_QUEUE_SIZE, channel);
    }

    public BatchExecutorQueue(int chunkSize, Channel channel) {
        this.queue = new MpscUnboundedArrayQueue<>(10000);
        this.scheduled = new AtomicBoolean(false);
        this.chunkSize = chunkSize;
        this.list = new ArrayList<>(chunkSize + 2);
        this.channel = channel;
    }

    public void enqueue(T message, Executor executor) {
        queue.add(message);
        scheduleFlush(executor);
    }

    protected void scheduleFlush(Executor executor) {
        if (scheduled.compareAndSet(false, true)) {
            executor.execute(() -> this.run(executor));
        }
    }

    private void run(Executor executor) {
        try {
            Queue<T> snapshot = new LinkedList<>();
            T item;
            while ((item = queue.poll()) != null) {
                snapshot.add(item);
            }
            int i = 0;
            boolean flushedOnce = false;
            while ((item = snapshot.poll()) != null) {
                if (snapshot.size() == 0) {
                    flushedOnce = false;
                    break;
                }
                if (i == chunkSize) {
                    i = 0;
                    prepare(item);
                    flush(item);
                    flushedOnce = true;
                } else {
                    prepare(item);
                    i++;
                }
            }
            if (!flushedOnce && item != null) {
                prepare(item);
                flush(item);
            }
        } finally {
            scheduled.set(false);
            if (!queue.isEmpty()) {
                scheduleFlush(executor);
            }
        }
    }

    protected void prepare(T item) {
        list.add(item);
    }

    protected void flush(T item) {
        List<T> mid = new ArrayList<>(list);
        list.clear();
        channel.eventLoop().execute(() -> {
            ByteBuf t1 = (ByteBuf) mid.get(0);
            for(int i = 1; i < mid.size(); i++){
                ByteBuf t = (ByteBuf) mid.get(i);
                t1.writeBytes(t);
                t.release();
            }
            channel.writeAndFlush(t1);

        });
    }
}