package com.km.dataeltplatform.Test;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import java.io.IOException;

public class test {
    public static void main(String[] args) throws IOException {
        Configuration conf = new Configuration();
        conf.set("fs.defaultFS", "hdfs://192.168.43.51:9000");
        FileSystem fs= FileSystem.get(conf);

        System.out.println();
        System.out.println(fs.exists(new Path("/data")));
        fs.close();
    }
}
