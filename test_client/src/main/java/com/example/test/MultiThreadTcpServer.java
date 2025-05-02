package com.example.test;

import java.io.*;
import java.net.*;

public class MultiThreadTcpServer {
    public static void main(String[] args) {
        int port = 8888; // 服务端监听的端口号

        try (
            // 创建ServerSocket对象，监听指定端口
            ServerSocket serverSocket = new ServerSocket(port)
        ) {
            System.out.println("Server is listening on port " + port);

            while (true) {
                try {
                    // 接受客户端连接（阻塞，直到有客户端连接）
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("New client connected: " + clientSocket.getInetAddress().getHostAddress());

                    // 为每个客户端创建一个新线程处理
                    Thread clientThread = new Thread(new ClientHandler(clientSocket));
                    clientThread.start();
                } catch (IOException e) {
                    System.err.println("Error accepting client connection: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Could not listen on port " + port);
            System.exit(1);
        }
    }
}

// 客户端处理器类，负责处理单个客户端的通信
class ClientHandler implements Runnable {
    private final Socket clientSocket;

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
    }

    @Override
    public void run() {
        try (
            // 创建字符输入流，读取客户端发送的数据
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            // 创建字符输出流，向客户端发送数据
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)
        ) {
            String inputLine;
            // 循环读取客户端消息并响应
            while ((inputLine = in.readLine()) != null) {
                System.out.println("Client [" + clientSocket.getInetAddress().getHostAddress() + "]: " + inputLine);

                // 如果客户端发送"exit"，则关闭连接
                if (inputLine.equalsIgnoreCase("exit")) {
                    out.println("Bye");
                    break;
                }

                // 向客户端发送响应
                out.println("Server received: " + inputLine);
            }
        } catch (IOException e) {
            System.err.println("Error handling client " + clientSocket.getInetAddress().getHostAddress() + ": " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
                System.out.println("Client disconnected: " + clientSocket.getInetAddress().getHostAddress());
            } catch (IOException e) {
                System.err.println("Error closing client socket: " + e.getMessage());
            }
        }
    }
}
