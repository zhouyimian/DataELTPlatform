package com.km.data.writer.mysqlWriterUtil;


import com.alibaba.druid.sql.parser.ParserException;
import com.km.data.common.element.Column;
import com.km.data.common.element.Record;
import com.km.data.common.exception.DataETLException;
import com.km.data.common.util.Configuration;
import com.km.data.reader.mysqlReaderUtil.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class WriterUtil {
    private static final Logger LOG = LoggerFactory.getLogger(WriterUtil.class);

    public static List<String> renderPreOrPostSqls(List<String> preOrPostSqls, String tableName) {
        if (null == preOrPostSqls) {
            return Collections.emptyList();
        }

        List<String> renderedSqls = new ArrayList<String>();
        for (String sql : preOrPostSqls) {
            //preSql为空时，不加入执行队列
            if (StringUtils.isNotBlank(sql)) {
                renderedSqls.add(sql.replace(Constant.TABLE_NAME_PLACEHOLDER, tableName));
            }
        }

        return renderedSqls;
    }

    public static void executeSqls(Connection conn, List<String> sqls, String basicMessage, DataBaseType dataBaseType) {
        Statement stmt = null;
        String currentSql = null;
        try {
            stmt = conn.createStatement();
            for (String sql : sqls) {
                currentSql = sql;
                DBUtil.executeSqlWithoutResultSet(stmt, sql);
            }
        } catch (Exception e) {
        } finally {
            DBUtil.closeDBResources(null, stmt, null);
        }
    }

    public static String getWriteTemplate(List<String> columnHolders, List<String> valueHolders, String writeMode, DataBaseType dataBaseType, boolean forceUseUpdate) {
        boolean isWriteModeLegal = writeMode.trim().toLowerCase().startsWith("insert")
                || writeMode.trim().toLowerCase().startsWith("replace")
                || writeMode.trim().toLowerCase().startsWith("update");

        if (!isWriteModeLegal) {
            throw DataETLException.asDataETLException(DBUtilErrorCode.ILLEGAL_VALUE,
                    String.format("您所配置的 writeMode:%s 错误. 因为DataX 目前仅支持replace,update 或 insert 方式. 请检查您的配置并作出修改.", writeMode));
        }
        // && writeMode.trim().toLowerCase().startsWith("replace")
        String writeDataSqlTemplate;
        if (forceUseUpdate ||
                ((dataBaseType == DataBaseType.MySql || dataBaseType == DataBaseType.Tddl) && writeMode.trim().toLowerCase().startsWith("update"))
                ) {
            //update只在mysql下使用

            writeDataSqlTemplate = new StringBuilder()
                    .append("INSERT INTO %s (").append(StringUtils.join(columnHolders, ","))
                    .append(") VALUES(").append(StringUtils.join(valueHolders, ","))
                    .append(")")
                    .append(onDuplicateKeyUpdateString(columnHolders))
                    .toString();
        } else {

            //这里是保护,如果其他错误的使用了update,需要更换为replace
            if (writeMode.trim().toLowerCase().startsWith("update")) {
                writeMode = "replace";
            }
            writeDataSqlTemplate = new StringBuilder().append(writeMode)
                    .append(" INTO %s (").append(StringUtils.join(columnHolders, ","))
                    .append(") VALUES(").append(StringUtils.join(valueHolders, ","))
                    .append(")").toString();
        }

        return writeDataSqlTemplate;
    }

    public static String onDuplicateKeyUpdateString(List<String> columnHolders) {
        if (columnHolders == null || columnHolders.size() < 1) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(" ON DUPLICATE KEY UPDATE ");
        boolean first = true;
        for (String column : columnHolders) {
            if (!first) {
                sb.append(",");
            } else {
                first = false;
            }
            sb.append(column);
            sb.append("=VALUES(");
            sb.append(column);
            sb.append(")");
        }

        return sb.toString();
    }

    public static void preCheckPrePareSQL(Configuration originalConfig, DataBaseType type) {
        List<Object> conns = originalConfig.getList(Constant.CONN_MARK, Object.class);
        Configuration connConf = Configuration.from(conns.get(0).toString());
        String table = connConf.getList(Key.TABLE, String.class).get(0);

        List<String> preSqls = originalConfig.getList(Key.PRE_SQL,
                String.class);
        List<String> renderedPreSqls = WriterUtil.renderPreOrPostSqls(
                preSqls, table);

        if (null != renderedPreSqls && !renderedPreSqls.isEmpty()) {
            LOG.info("Begin to preCheck preSqls:[{}].",
                    StringUtils.join(renderedPreSqls, ";"));
            for (String sql : renderedPreSqls) {
                try {
                    DBUtil.sqlValid(sql, type);
                } catch (ParserException e) {
                }
            }
        }
    }

    public static void preCheckPostSQL(Configuration originalConfig, DataBaseType type) {
        List<Object> conns = originalConfig.getList(Constant.CONN_MARK, Object.class);
        Configuration connConf = Configuration.from(conns.get(0).toString());
        String table = connConf.getList(Key.TABLE, String.class).get(0);

        List<String> postSqls = originalConfig.getList(Key.POST_SQL,
                String.class);
        List<String> renderedPostSqls = WriterUtil.renderPreOrPostSqls(
                postSqls, table);
        if (null != renderedPostSqls && !renderedPostSqls.isEmpty()) {

            LOG.info("Begin to preCheck postSqls:[{}].",
                    StringUtils.join(renderedPostSqls, ";"));
            for (String sql : renderedPostSqls) {
                try {
                    DBUtil.sqlValid(sql, type);
                } catch (ParserException e) {
                }

            }
        }
    }

    public static Object buildInsertSql(Configuration configuration) {
        String columns = configuration.getString("column");
        int size = columns.contains(",") ? columns.split(",").length : 1;
        columns = columns.replace("[\"", "");
        columns = columns.replace("\"]", "");
        columns = columns.replace("\",\"", ",");

        String sql = "insert into " + configuration.getString("table") + " (" + columns + ") VALUES (";
        for (int i = 0; i < size; i++) {
            if (i != size - 1) {
                sql = sql + "?,";
            } else {
                sql = sql + "?)";
            }
        }
        return sql;
    }

    public static PreparedStatement
    fillPreparedStatement(PreparedStatement pstmt, Record record, int columnNumber,
                          Triple<List<String>, List<Integer>, List<String>> resultSetMetaData) throws SQLException {
        for (int i = 0; i < columnNumber; i++) {
            int columnSqltype = resultSetMetaData.getMiddle().get(i);
            pstmt = fillPreparedStatementColumnType(pstmt, i, columnSqltype, record.getColumn(i), resultSetMetaData);
        }

        return pstmt;
    }

    private static PreparedStatement fillPreparedStatementColumnType(
            PreparedStatement preparedStatement, int columnIndex,
            int columnSqltype, Column column,
            Triple<List<String>, List<Integer>, List<String>> resultSetMetaData) throws SQLException {
        java.util.Date utilDate;
        switch (columnSqltype) {
            case Types.CHAR:
            case Types.NCHAR:
            case Types.CLOB:
            case Types.NCLOB:
            case Types.VARCHAR:
            case Types.LONGVARCHAR:
            case Types.NVARCHAR:
            case Types.LONGNVARCHAR:
                preparedStatement.setString(columnIndex + 1, column
                        .asString());
                break;

            case Types.SMALLINT:
            case Types.INTEGER:
            case Types.BIGINT:
            case Types.NUMERIC:
            case Types.DECIMAL:
            case Types.FLOAT:
            case Types.REAL:
            case Types.DOUBLE:
                String strValue = column.asString();
                preparedStatement.setString(columnIndex + 1, strValue);
                break;

            //tinyint is a little special in some database like mysql {boolean->tinyint(1)}
            case Types.TINYINT:
                Long longValue = column.asLong();
                if (null == longValue) {
                    preparedStatement.setString(columnIndex + 1, null);
                } else {
                    preparedStatement.setString(columnIndex + 1, longValue.toString());
                }
                break;

            // for mysql bug, see http://bugs.mysql.com/bug.php?id=35115
            case Types.DATE:
                if (resultSetMetaData.getRight().get(columnIndex)
                        .equalsIgnoreCase("year")) {
                    if (column.asBigInteger() == null) {
                        preparedStatement.setString(columnIndex + 1, null);
                    } else {
                        preparedStatement.setInt(columnIndex + 1, column.asBigInteger().intValue());
                    }
                } else {
                    java.sql.Date sqlDate = null;
                    try {
                        utilDate = column.asDate();
                    } catch (DataETLException e) {
                        throw new SQLException(String.format(
                                "Date 类型转换错误：[%s]", column));
                    }

                    if (null != utilDate) {
                        sqlDate = new java.sql.Date(utilDate.getTime());
                    }
                    preparedStatement.setDate(columnIndex + 1, sqlDate);
                }
                break;

            case Types.TIME:
                java.sql.Time sqlTime = null;
                try {
                    utilDate = column.asDate();
                } catch (DataETLException e) {
                    throw new SQLException(String.format(
                            "TIME 类型转换错误：[%s]", column));
                }

                if (null != utilDate) {
                    sqlTime = new java.sql.Time(utilDate.getTime());
                }
                preparedStatement.setTime(columnIndex + 1, sqlTime);
                break;

            case Types.TIMESTAMP:
                java.sql.Timestamp sqlTimestamp = null;
                try {
                    utilDate = column.asDate();
                } catch (DataETLException e) {
                    throw new SQLException(String.format(
                            "TIMESTAMP 类型转换错误：[%s]", column));
                }

                if (null != utilDate) {
                    sqlTimestamp = new java.sql.Timestamp(
                            utilDate.getTime());
                }
                preparedStatement.setTimestamp(columnIndex + 1, sqlTimestamp);
                break;

            case Types.BINARY:
            case Types.VARBINARY:
            case Types.BLOB:
            case Types.LONGVARBINARY:
                preparedStatement.setBytes(columnIndex + 1, column
                        .asBytes());
                break;

            case Types.BOOLEAN:
                preparedStatement.setString(columnIndex + 1, column.asString());
                break;

            // warn: bit(1) -> Types.BIT 可使用setBoolean
            // warn: bit(>1) -> Types.VARBINARY 可使用setBytes
            case Types.BIT:
                preparedStatement.setBoolean(columnIndex + 1, column.asBoolean());
                break;
            default:
                throw DataETLException
                        .asDataETLException(
                                DBUtilErrorCode.UNSUPPORTED_TYPE,
                                String.format(
                                        "您的配置文件中的列配置信息有误. 不支持数据库写入这种字段类型. 字段名:[%s], 字段类型:[%d], 字段Java类型:[%s]. 请修改表中该字段的类型或者不同步该字段.",
                                        resultSetMetaData.getLeft()
                                                .get(columnIndex),
                                        resultSetMetaData.getMiddle()
                                                .get(columnIndex),
                                        resultSetMetaData.getRight()
                                                .get(columnIndex)));
        }
        return preparedStatement;
    }
}
