package com.km.reader;

import com.km.common.element.*;
import com.km.common.exception.DataETLException;
import com.km.common.record.DefaultRecord;
import com.km.common.util.Configuration;
import com.km.core.transport.channel.Channel;
import com.km.reader.mysqlReaderUtil.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MysqlReader extends Reader {

    public static class Job extends Reader.Job {
        Configuration configuration;

        public Job(Configuration configuration) {
            super(configuration);
            this.configuration = this.getConfiguration();
        }

        @Override
        public List<Configuration> split(int adviceNumber) {
            //改变量用于设置是否为自定义sql
            boolean isTableMode = true;

            String column = this.configuration.getString(Key.COLUMN);
            column = column.replace("[", "");
            column = column.replace("]", "");
            column = column.replace("\"", "");
            String where = this.configuration.getString(Key.WHERE, null);
            Configuration connection = this.configuration.getConfiguration(Constant.CONN_MARK);

            List<Configuration> splittedConfigs = new ArrayList<Configuration>();


            Configuration sliceConfig = this.getConfiguration().clone();


            String jdbcUrl = connection.getString(Key.JDBC_URL);
            sliceConfig.set(Key.JDBC_URL, jdbcUrl);

            sliceConfig.remove(Constant.CONN_MARK);

            Configuration tempSlice;

            // 说明是配置的 table 方式
            if (isTableMode) {

                String table = connection.getString(Key.TABLE);

                Validate.isTrue(null != table, "您读取数据库表配置错误.");

                String splitPk = this.getConfiguration().getString(Key.SPLIT_PK, null);

                //最终切分份数不一定等于 eachTableShouldSplittedNumber
                boolean needSplitTable = adviceNumber > 1
                        && StringUtils.isNotBlank(splitPk);
                if (needSplitTable) {
                    // 尝试对表，切分为adviceNumber份
                    tempSlice = sliceConfig.clone();
                    tempSlice.set(Key.TABLE, table);

                    List<Configuration> splittedSlices = SingleTableSplitUtil
                            .splitSingleTable(DataBaseType.MySql, tempSlice, adviceNumber);

                    splittedConfigs.addAll(splittedSlices);
                } else {
                    tempSlice = sliceConfig.clone();
                    tempSlice.set(Key.TABLE, table);
                    String queryColumn = HintUtil.buildQueryColumn(jdbcUrl, table, column);
                    tempSlice.set(Key.QUERY_SQL, SingleTableSplitUtil.buildQuerySql(queryColumn, table, where));
                    splittedConfigs.add(tempSlice);
                }
            } else {
                // 说明是配置的 querySql 方式
                String querySql = connection.getString(Key.QUERY_SQL);

                    tempSlice = sliceConfig.clone();
                    tempSlice.set(Key.QUERY_SQL, querySql);
                    splittedConfigs.add(tempSlice);
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
            String jdbcUrl = this.getConfiguration().getString(Key.JDBC_URL);
            String querySql = this.getConfiguration().getString(Key.QUERY_SQL);
            String username = this.getConfiguration().getString(Key.USERNAME);
            String password = this.getConfiguration().getString(Key.PASSWORD);

            Connection conn = DBUtil.getConnection(DataBaseType.MySql, jdbcUrl, username, password);

            ResultSet rs = null;
            int columnNumber = 0;
            try {
                rs = DBUtil.query(conn, querySql);

                ResultSetMetaData metaData = rs.getMetaData();
                columnNumber = metaData.getColumnCount();

                while (rs.next()) {
                    Record record = buildOneRecord(rs, metaData, columnNumber);
                    channel.add(record);
                }
            } catch (SQLException e) {
                throw e;
            } finally {
                DBUtil.closeDBResources(null, conn);
            }

        }

        private Record buildOneRecord(ResultSet rs, ResultSetMetaData metaData, int columnNumber) throws SQLException {
            Record record = new DefaultRecord();

            try {
                for (int i = 1; i <= columnNumber; i++) {
                    switch (metaData.getColumnType(i)) {

                        case Types.CHAR:
                        case Types.NCHAR:
                        case Types.VARCHAR:
                        case Types.LONGVARCHAR:
                        case Types.NVARCHAR:
                        case Types.LONGNVARCHAR:
                            String rawData = rs.getString(i);
                            record.addColumn(new StringColumn(rawData));
                            break;

                        case Types.CLOB:
                        case Types.NCLOB:
                            record.addColumn(new StringColumn(rs.getString(i)));
                            break;

                        case Types.SMALLINT:
                        case Types.TINYINT:
                        case Types.INTEGER:
                        case Types.BIGINT:
                            record.addColumn(new LongColumn(rs.getString(i)));
                            break;

                        case Types.NUMERIC:
                        case Types.DECIMAL:
                            record.addColumn(new DoubleColumn(rs.getString(i)));
                            break;

                        case Types.FLOAT:
                        case Types.REAL:
                        case Types.DOUBLE:
                            record.addColumn(new DoubleColumn(rs.getString(i)));
                            break;

                        case Types.TIME:
                            record.addColumn(new DateColumn(rs.getTime(i)));
                            break;

                        // for mysql bug, see http://bugs.mysql.com/bug.php?id=35115
                        case Types.DATE:
                            if (metaData.getColumnTypeName(i).equalsIgnoreCase("year")) {
                                record.addColumn(new LongColumn(rs.getInt(i)));
                            } else {
                                record.addColumn(new DateColumn(rs.getDate(i)));
                            }
                            break;

                        case Types.TIMESTAMP:
                            record.addColumn(new DateColumn(rs.getTimestamp(i)));
                            break;

                        case Types.BINARY:
                        case Types.VARBINARY:
                        case Types.BLOB:
                        case Types.LONGVARBINARY:
                            record.addColumn(new BytesColumn(rs.getBytes(i)));
                            break;

                        // warn: bit(1) -> Types.BIT 可使用BoolColumn
                        // warn: bit(>1) -> Types.VARBINARY 可使用BytesColumn
                        case Types.BOOLEAN:
                        case Types.BIT:
                            record.addColumn(new BoolColumn(rs.getBoolean(i)));
                            break;

                        case Types.NULL:
                            String stringData = null;
                            if (rs.getObject(i) != null) {
                                stringData = rs.getObject(i).toString();
                            }
                            record.addColumn(new StringColumn(stringData));
                            break;

                        default:
                            throw DataETLException
                                    .asDataETLException(
                                            DBUtilErrorCode.UNSUPPORTED_TYPE,
                                            String.format(
                                                    "您的配置文件中的列配置信息有误. 因为不支持数据库读取这种字段类型. 字段名:[%s], 字段名称:[%s], 字段Java类型:[%s]. 请尝试使用数据库函数将其转换为支持的类型 或者不同步该字段 .",
                                                    metaData.getColumnName(i),
                                                    metaData.getColumnType(i),
                                                    metaData.getColumnClassName(i)));
                    }
                }
            } catch (Exception e) {
                throw e;
            }
            return record;
        }
    }
}
