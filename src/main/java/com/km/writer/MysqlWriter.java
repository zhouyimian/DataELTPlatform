package com.km.writer;

import com.alibaba.fastjson.JSONObject;
import com.km.common.element.Record;
import com.km.common.util.Configuration;
import com.km.core.transport.channel.Channel;
import com.km.reader.mysqlReaderUtil.DBUtil;
import com.km.reader.mysqlReaderUtil.DataBaseType;
import com.km.writer.mysqlWriterUtil.Key;
import com.km.writer.mysqlWriterUtil.WriterUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MysqlWriter extends Writer {
    private static final DataBaseType DATABASE_TYPE = DataBaseType.MySql;

    public static class Job extends Writer.Job {
        Configuration configuration;


        public Job(Configuration configuration) {
            super(configuration);
            this.configuration = this.getConfiguration();
        }

        @Override
        public List<Configuration> split(int adviceNumber) {
            List<Configuration> splitResultConfigs = new ArrayList<Configuration>();
            Configuration tempConfiguration = this.getConfiguration().clone();
            tempConfiguration.set("table",tempConfiguration.getString("connection.table"));
            tempConfiguration.set("insertSql", WriterUtil.buildInsertSql(tempConfiguration));

            for (int i = 0; i < adviceNumber; i++) {
                splitResultConfigs.add(tempConfiguration.clone());
            }
            return splitResultConfigs;
        }
    }

    public static class Task extends Writer.Task {

        Configuration configuration;
        private String jdbcUrl;
        private String insertSql;
        private String username;
        private String password;
        private Triple<List<String>, List<Integer>, List<String>> resultSetMetaData;
        private String table;
        private List<String> columns;


        public Task(Configuration configuration) {
            super(configuration);
            this.configuration = this.getConfiguration();
            this.jdbcUrl = this.configuration.getString("connection.jdbcUrl");
            this.insertSql= this.configuration.getString(Key.INSERTSQL);
            this.username = this.configuration.getString(Key.USERNAME);
            this.password = this.configuration.getString(Key.PASSWORD);
            this.table = this.configuration.getString(Key.TABLE);
            this.columns = this.configuration.getList(Key.COLUMN,String.class);
        }

        public void startWrite(Channel channel) throws SQLException {


            Connection connection = DBUtil.getConnection(DataBaseType.MySql,jdbcUrl, username, password);
            PreparedStatement pstmt = connection.prepareStatement(insertSql);
            this.resultSetMetaData = DBUtil.getColumnMetaData(connection,
                    this.table, StringUtils.join(this.columns, ","));

            Record record = null;
            while ((record=channel.remove())!=null){
                pstmt = WriterUtil.fillPreparedStatement(
                        pstmt, record,this.columns.size(),resultSetMetaData);
                pstmt.execute();
            }
        }
    }


}
