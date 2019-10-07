package com.km.dataeltplatform.service;


import com.km.dataeltplatform.utils.HDFSUtil;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.util.List;
import java.util.Map;


@Service
public class HDFSService {
    //将内存中的数据写入HDFS
    public String insertDataToHDFS(List<Map<String,String>> mysqlData, HttpServletRequest req, List<String> tableStruct) throws Exception {
        //创建一个临时文件
        String tempFileName = "/tmp/"+System.currentTimeMillis();
        try {
            FileSystem fs=HDFSUtil.getFileSystem();
            OutputStream out = fs.create(new Path(tempFileName));
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(out));
            StringBuilder data = new StringBuilder();
            for(int i = 0;i<mysqlData.size();i++){
                Map<String,String> map = mysqlData.get(i);
                for(int j = 0;j<tableStruct.size();j++){
                    if(j==tableStruct.size()-1){
                        if(i==mysqlData.size()-1){
                            data.append(map.get(tableStruct.get(j)));
                        }else{
                            data.append(map.get(tableStruct.get(j))+"\n");
                        }
                    }else{
                        data.append(map.get(tableStruct.get(j))+"\t");
                    }
                }
                bw.write(data.toString());
                bw.flush();
            }
            bw.close();
            fs.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return tempFileName;
    }
    public static void open(String path) throws IOException {
        FileSystem fs = HDFSUtil.getFileSystem();
        InputStream is = fs.open(new Path(path));
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));
        String line = null;
        while ((line = bufferedReader.readLine()) != null) {
            System.out.println(line);
        }
        is.close();
        fs.close();
    }

    public static void deleteFile(String path) throws IOException {
        FileSystem fs = HDFSUtil.getFileSystem();
        //第二个参数true，直接删除，如果为false，先标记为删除，等链接关闭时再全部删除
        fs.delete(new Path(path), true);
    }
    /***
     * 创建文件
     * @throws IOException
     */
    private static boolean createFile(String path) throws IOException {
        FileSystem fs = HDFSUtil.getFileSystem();
        boolean created = fs.createNewFile(new Path(path));
        fs.close();
        return created;
    }
    /***
     * 创建文件并写入数据
     * @throws IOException
     */
    private static void createAndWriteFile() throws IOException {
        FileSystem fs = HDFSUtil.getFileSystem();
        FSDataOutputStream dos = fs.create(new Path("/kilometer/hdfs/createAndWriteFile2.txt"));
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(dos));
        bw.write("迟钝姐");
        bw.newLine();
        bw.write("siki");
        bw.close();
        fs.close();
    }

    /***
     * 创建目录
     * @throws IOException
     */
    private static void createDir() throws IOException {
        FileSystem fs = HDFSUtil.getFileSystem();
        boolean created = fs.mkdirs(new Path("/kilometer/hdfs/testMkdir"));
        System.out.println(created ? "创建成功" : "创建失败");
        fs.close();
    }

}
