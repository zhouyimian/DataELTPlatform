package com.km.dataeltplatform;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class test {
    public static List<String> getAllFileName(String path) {
        List<String> res = new ArrayList<>();
        File file = new File(path);
        File[] files = file.listFiles();
        String[] names = file.list();
        if (names != null) {
            String[] completNames = new String[names.length];
            for (int i = 0; i < names.length; i++) {
                completNames[i] = path + names[i];
            }
            res.addAll(Arrays.asList(completNames));
        }
        return res;
    }

    public static void main(String[] args) {
        String sourcePath = "C:\\Users\\Administrator\\Desktop\\赶紧看完电子书\\input";
        String targetPath = "C:\\Users\\Administrator\\Desktop\\赶紧看完电子书\\output";
        File[] sourceFiles = new File(sourcePath).listFiles();
        File[] targetFiles = new File(targetPath).listFiles();
        int count = 0;
        for(int i = 0;i<targetFiles.length;i++){
            for(int j = 0;j<sourceFiles.length;j++){
                if(targetFiles[i].getName().split("\\.")[0].equals(sourceFiles[j].getName().split("\\.")[0])){
                    sourceFiles[j].delete();
                    count++;
                    //System.out.println(targetFiles[i].getName());
                }
            }
        }
        System.out.println(count);
    }
}
