package com.example.test;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.CompleteFuture;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.FailedFuture;

import java.io.*;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @Author Cbc
 * @DateTime 2025/3/13 14:15
 * @Description
 */
public class TestC1 {

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        Socket socket = null;
        PrintWriter out = null;
        BufferedReader in = null;
        BufferedReader stdIn = null;


        try {
            // 1. 创建Soctke连接服务器//10.199.12.47
            socket = new Socket("10.199.12.47", 12345);

            FileSHA1Calculator calculator = new FileSHA1Calculator();

            // 2. 获取输入输出流
            OutputStream outputStream1 = socket.getOutputStream();
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            //0bb25455b477cd57e65e3011ecf893ab99f2ee04

            //"C:\Users\陈宝聪\Pictures\Saved Pictures\avatar1.jpg"
            FileInputStream fIn = new FileInputStream("C:/Users/陈宝聪/Pictures/Saved Pictures/cbc.txt");
            byte[] bytes = fIn.readAllBytes();
            System.out.println(bytes.length);
            outputStream1.write(bytes);



//            // 3. 通信循环
//            String userInput;
//            while ((userInput = stdIn.readLine()) != null) {
//                // 发送消息到服务器
//                out.println(userInput);
//
//                // 接收服务器回复
//                System.out.println("服务器回复: " + in.readLine());
//
//                // 如果输入"bye"，则结束通信
//                if ("bye".equalsIgnoreCase(userInput)) {
//                    break;
//                }
//            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 4. 关闭资源
            try {
                if (out != null) out.close();
                if (in != null) in.close();
                if (stdIn != null) stdIn.close();
                if (socket != null) socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
