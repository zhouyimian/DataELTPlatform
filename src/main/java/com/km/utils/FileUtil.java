package com.km.utils;

import org.springframework.core.io.ClassPathResource;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileUtil {
    public static String readFile(String filePath){
        // 读取txt内容为字符串
        StringBuffer txtContent = new StringBuffer();
        // 每次读取的byte数
        byte[] b = new byte[8 * 1024];
        InputStream in = null;
        try {
            // 文件输入流
            ClassPathResource classPathResource = new ClassPathResource(filePath);
            in = classPathResource.getInputStream();
            while (in.read(b) != -1) {
                // 字符串拼接
                txtContent.append(new String(b));
            }
            // 关闭流
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            if (in != null) {
                try {
                    in.close();
                }
                catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        return txtContent.toString();
    }
}
