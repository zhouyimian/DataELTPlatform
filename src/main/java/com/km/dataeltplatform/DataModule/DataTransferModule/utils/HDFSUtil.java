package com.km.dataeltplatform.DataModule.DataTransferModule.utils;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;

import java.io.IOException;

public class HDFSUtil {
    public static Configuration getConfiguration(){
        Configuration conf = new Configuration();http:
        conf.set("fs.defaultFS","hdfs://192.168.43.51:9000");
        return conf;
    }
    public static FileSystem getFileSystem() throws IOException {
        return getFileSystem(getConfiguration());
    }
    public static FileSystem getFileSystem(Configuration configuration) throws IOException {
        return FileSystem.get(configuration);
    }
}
