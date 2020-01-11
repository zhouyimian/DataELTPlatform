package com.km.writer;

import com.alibaba.fastjson.JSONObject;
import com.km.common.util.Configuration;
import com.km.core.transport.channel.Channel;
import com.km.reader.util.DBUtil;
import com.km.reader.util.DataBaseType;
import com.km.writer.util.WriterUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class MysqlWriter extends Writer {
    private static final DataBaseType DATABASE_TYPE = DataBaseType.MySql;

    public static class Job extends Writer.Job {

        public Job(Configuration configuration) {
            super(configuration);
        }

        @Override
        public List<Configuration> split(int mandatoryNumber) {
            List<Configuration> splitResultConfigs = new ArrayList<Configuration>();
            Configuration tempConfiguration = this.getConfiguration().clone();
            tempConfiguration.set("table",tempConfiguration.getString("connection[0].table[0]"));
            tempConfiguration.set("insertSql", WriterUtil.buildInsertSql(tempConfiguration));

            for (int i = 0; i < mandatoryNumber; i++) {
                splitResultConfigs.add(tempConfiguration.clone());
            }
            return splitResultConfigs;
        }
    }

    public static class Task extends Writer.Task {

        public Task(Configuration configuration) {
            super(configuration);
        }

        public void startWrite(Channel channel) throws SQLException {
            String jdbcUrl = this.getConfiguration().getString("writer.parameter.connection[0].jdbcUrl");
            String sql= this.getConfiguration().getString("writer.parameter.insertSql");
            String username = this.getConfiguration().getString("writer.parameter.username");
            String password = this.getConfiguration().getString("writer.parameter.password");

            Connection conn = DBUtil.getConnection(DataBaseType.MySql,jdbcUrl, username, password);

            PreparedStatement pstmt = conn.prepareStatement(sql);


            JSONObject object = null;
            while ((object=channel.remove())!=null){
                String[] paramters = sql.substring(sql.indexOf('(')+1,sql.indexOf(')')).split(",");

                for(int i = 0;i<paramters.length;i++){
                    pstmt.setObject(i+1,object.get(paramters[i]));
                }
                pstmt.execute();
            }
        }
    }


}
