package com.km.utils;

import org.springframework.core.io.ClassPathResource;

import java.io.*;

public class FileUtil {
//    public static String readFile(String filePath){
//        // 读取txt内容为字符串
//        StringBuffer txtContent = new StringBuffer();
//        // 每次读取的byte数
//        byte[] b = new byte[8 * 1024];
//        InputStream in = null;
//        try {
//            // 文件输入流
//            ClassPathResource classPathResource = new ClassPathResource(filePath);
//            in = classPathResource.getInputStream();
//            while (in.read(b) != -1) {
//                // 字符串拼接
//                txtContent.append(new String(b));
//            }
//            // 关闭流
//            in.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        finally {
//            if (in != null) {
//                try {
//                    in.close();
//                }
//                catch (IOException e) {
//                    // TODO Auto-generated catch block
//                    e.printStackTrace();
//                }
//            }
//        }
//        return txtContent.toString();
//    }

    public static String readFile(String fileName) {
        String encoding = "UTF-8";
        File file = new File(fileName);
        Long filelength = file.length();
        byte[] filecontent = new byte[filelength.intValue()];
        try {
            FileInputStream in = new FileInputStream(file);
            in.read(filecontent);
            in.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            return new String(filecontent, encoding);
        } catch (UnsupportedEncodingException e) {
            System.err.println("The OS does not support " + encoding);
            e.printStackTrace();
            return "";
        }
    }
}
