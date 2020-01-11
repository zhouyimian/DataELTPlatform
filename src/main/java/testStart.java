import com.alibaba.fastjson.JSONObject;
import com.km.common.util.Configuration;
import com.km.core.Engine;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;

public class testStart {
    public static void main(String[] args) throws SQLException, ClassNotFoundException {
        String corePath = "src/main/resources/static/core.json";
        String jobPath = "src/main/resources/static/job.json";

        JSONObject corejson = JSONObject.parseObject(readFile(corePath));
        JSONObject jobjson = JSONObject.parseObject(readFile(jobPath));

        JSONObject mergeConfig = (JSONObject) corejson.clone();
        mergeConfig.putAll(jobjson);


        Engine engine = new Engine();
        engine.start(new Configuration(mergeConfig.toJSONString()));

    }

    private static String readFile(String filePath) {
        // 读取txt内容为字符串
        StringBuffer txtContent = new StringBuffer();
        // 每次读取的byte数
        byte[] b = new byte[8 * 1024];
        InputStream in = null;
        try
        {
            // 文件输入流
            in = new FileInputStream(filePath);

            while (in.read(b) != -1)
            {
                // 字符串拼接
                txtContent.append(new String(b));

            }
            // 关闭流
            in.close();
        }
        catch (FileNotFoundException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        finally
        {
            if (in != null)
            {
                try
                {
                    in.close();
                }
                catch (IOException e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        return txtContent.toString();
    }
}
