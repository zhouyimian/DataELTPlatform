package com.km.reader;

import com.alibaba.fastjson.JSONObject;
import com.km.common.util.Configuration;
import com.km.core.transport.channel.Channel;
import com.km.core.util.container.CoreConstant;
import com.km.reader.util.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MysqlReader extends Reader {
    public static class Job extends Reader.Job {
        private static final Logger LOG = LoggerFactory
                .getLogger(Job.class);

        public Job(Configuration configuration) {
            super(configuration);
        }

        @Override
        public List<Configuration> split(int adviceNumber) {
            //改变量用于设置是否为自定义sql
            boolean isTableMode = true;

            String column = this.getConfiguration().getString(Key.COLUMN);
            String where = this.getConfiguration().getString(Key.WHERE, null);

            List<Object> conns = this.getConfiguration().getList(Constant.CONN_MARK, Object.class);

            List<Configuration> splittedConfigs = new ArrayList<Configuration>();

            for (int i = 0, len = conns.size(); i < len; i++) {
                Configuration sliceConfig = this.getConfiguration().clone();

                Configuration connConf = Configuration.from(conns.get(i).toString());
                String jdbcUrl = connConf.getString(Key.JDBC_URL);
                sliceConfig.set(Key.JDBC_URL, jdbcUrl);

                sliceConfig.remove(Constant.CONN_MARK);

                Configuration tempSlice;

                // 说明是配置的 table 方式
                if (isTableMode) {

                    List<String> tables = connConf.getList(Key.TABLE, String.class);

                    Validate.isTrue(null != tables && !tables.isEmpty(), "您读取数据库表配置错误.");

                    String splitPk = this.getConfiguration().getString(Key.SPLIT_PK, null);

                    //最终切分份数不一定等于 eachTableShouldSplittedNumber
                    boolean needSplitTable = adviceNumber > 1
                            && StringUtils.isNotBlank(splitPk);
                    if (needSplitTable) {
                        // 尝试对表，切分为adviceNumber份
                        for (String table : tables) {
                            tempSlice = sliceConfig.clone();
                            tempSlice.set(Key.TABLE, table);

                            List<Configuration> splittedSlices = SingleTableSplitUtil
                                    .splitSingleTable(DataBaseType.MySql,tempSlice, adviceNumber);

                            splittedConfigs.addAll(splittedSlices);
                        }
                    } else {
                        for (String table : tables) {
                            tempSlice = sliceConfig.clone();
                            tempSlice.set(Key.TABLE, table);
                            String queryColumn = HintUtil.buildQueryColumn(jdbcUrl, table, column);
                            tempSlice.set(Key.QUERY_SQL, SingleTableSplitUtil.buildQuerySql(queryColumn, table, where));
                            splittedConfigs.add(tempSlice);
                        }
                    }
                } else {
                    // 说明是配置的 querySql 方式
                    List<String> sqls = connConf.getList(Key.QUERY_SQL, String.class);

                    for (String querySql : sqls) {
                        tempSlice = sliceConfig.clone();
                        tempSlice.set(Key.QUERY_SQL, querySql);
                        splittedConfigs.add(tempSlice);
                    }
                }

            }
            return splittedConfigs;
        }

    }

    public static class Task extends Reader.Task {
        public Task(Configuration configuration) {
            super(configuration);
        }

        @Override
        public void startRead(Channel channel) throws SQLException {
            String jdbcUrl = this.getConfiguration().getString("reader.parameter.jdbcUrl");
            String querySql = this.getConfiguration().getString("reader.parameter.querySql");
            String username = this.getConfiguration().getString("reader.parameter.username");
            String password = this.getConfiguration().getString("reader.parameter.password");

            Connection conn = DBUtil.getConnection(DataBaseType.MySql,jdbcUrl, username, password);

            ResultSet rs = null;
            int columnNumber = 0;
            try {
                rs = DBUtil.query(conn,querySql);

                ResultSetMetaData metaData = rs.getMetaData();
                columnNumber = metaData.getColumnCount();

                while (rs.next()) {
                    JSONObject obj = new JSONObject();//将每一个结果集放入到jsonObject对象中
                    for(int i=1;i<=columnNumber;i++) {
                        obj.put(metaData.getColumnName(i), rs.getObject(i));//列值一一对应
                    }
                    channel.add(obj);
                }
            }catch (SQLException e){
                throw e;
            }finally {
                DBUtil.closeDBResources(null, conn);
            }

        }
    }
}
