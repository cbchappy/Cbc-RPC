package com.example.test;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class FileSHA1Calculator {
    public static void main(String[] args) {
        String filename = "C:/Users/陈宝聪/Pictures/Saved Pictures/cbc.txt"; // 指定要计算 SHA-1 值的文件

        try {
            String sha1Hash = calculateSHA1(filename);
            System.out.println("文件 " + filename + " 的 SHA-1 值为: " + sha1Hash);
        } catch (IOException e) {
            System.out.println("读取文件时发生错误: " + e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            System.out.println("不支持 SHA-1 算法: " + e.getMessage());
        }
    }

    public static String calculateSHA1(String filename) throws IOException, NoSuchAlgorithmException {
        // 创建 SHA-1 消息摘要对象
        MessageDigest digest = MessageDigest.getInstance("SHA-1");
        
        // 使用 FileInputStream 读取文件
        try (FileInputStream fis = new FileInputStream(filename)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            
            // 逐块读取文件内容并更新摘要
            while ((bytesRead = fis.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }
        }
        
        // 计算最终的哈希值
        byte[] hashBytes = digest.digest();
        
        // 将字节数组转换为十六进制字符串
        StringBuilder hexString = new StringBuilder();
        for (byte b : hashBytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        
        return hexString.toString();
    }
}

