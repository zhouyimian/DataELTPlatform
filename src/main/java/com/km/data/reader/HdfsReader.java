package com.km.data.reader;

import com.km.data.common.exception.DataETLException;
import com.km.data.common.util.Configuration;
import com.km.data.core.transport.channel.Channel;
import com.km.data.reader.hdfsReaderUtil.*;
import org.apache.commons.io.Charsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class HdfsReader extends Reader {
    public static class Job extends Reader.Job {
        private Configuration readerOriginConfig = null;
        private String encoding = null;
        private HashSet<String> sourceFiles;
        private String specifiedFileType = null;
        private DFSUtil dfsUtil = null;
        private List<String> path = null;

        public Job(Configuration configuration) {
            super(configuration);
            this.readerOriginConfig = super.getConfiguration();
            this.validate();
            dfsUtil = new DFSUtil(this.readerOriginConfig);
            this.sourceFiles = dfsUtil.getAllFiles(path, specifiedFileType);
        }

        @Override
        public void init() {

        }

        @Override
        public void prepare() {
        }

        @Override
        public void post() {

        }

        @Override
        public void destroy() {

        }


        public void validate() {
            this.readerOriginConfig.getNecessaryValue(Key.DEFAULT_FS,
                    HdfsReaderErrorCode.DEFAULT_FS_NOT_FIND_ERROR);

            // path check
            String pathInString = this.readerOriginConfig.getNecessaryValue(Key.PATH, HdfsReaderErrorCode.REQUIRED_VALUE);
            if (!pathInString.startsWith("[") && !pathInString.endsWith("]")) {
                path = new ArrayList<String>();
                path.add(pathInString);
            } else {
                path = this.readerOriginConfig.getList(Key.PATH, String.class);
                if (null == path || path.size() == 0) {
                    throw DataETLException.asDataETLException(HdfsReaderErrorCode.REQUIRED_VALUE, "您需要指定待读取的源目录或文件");
                }
                for (String eachPath : path) {
                    if (!eachPath.startsWith("/")) {
                        String message = String.format("请检查参数path:[%s],需要配置为绝对路径", eachPath);
                        throw DataETLException.asDataETLException(HdfsReaderErrorCode.ILLEGAL_VALUE, message);
                    }
                }
            }

            specifiedFileType = this.readerOriginConfig.getNecessaryValue(Key.FILETYPE, HdfsReaderErrorCode.REQUIRED_VALUE);
            if (!specifiedFileType.equalsIgnoreCase(Constant.ORC) &&
                    !specifiedFileType.equalsIgnoreCase(Constant.TEXT) &&
                    !specifiedFileType.equalsIgnoreCase(Constant.CSV) &&
                    !specifiedFileType.equalsIgnoreCase(Constant.SEQ) &&
                    !specifiedFileType.equalsIgnoreCase(Constant.RC)) {
                String message = "HdfsReader插件目前支持ORC, TEXT, CSV, SEQUENCE, RC五种格式的文件," +
                        "请将fileType选项的值配置为ORC, TEXT, CSV, SEQUENCE 或者 RC";
                throw DataETLException.asDataETLException(HdfsReaderErrorCode.FILE_TYPE_ERROR, message);
            }

            encoding = this.readerOriginConfig.getString(Key.ENCODING, "UTF-8");

            try {
                Charsets.toCharset(encoding);
            } catch (UnsupportedCharsetException uce) {
                throw DataETLException.asDataETLException(
                        HdfsReaderErrorCode.ILLEGAL_VALUE,
                        String.format("不支持的编码格式 : [%s]", encoding), uce);
            } catch (Exception e) {
                throw DataETLException.asDataETLException(
                        HdfsReaderErrorCode.ILLEGAL_VALUE,
                        String.format("运行配置异常 : %s", e.getMessage()), e);
            }

            // validate the Columns
            validateColumns();

            if (this.specifiedFileType.equalsIgnoreCase(Constant.CSV)) {
                //compress校验
                UnstructuredStorageReaderUtil.validateCompress(this.readerOriginConfig);
                UnstructuredStorageReaderUtil.validateCsvReaderConfig(this.readerOriginConfig);
            }

        }

        private void validateColumns() {

            // 检测是column 是否为 ["*"] 若是则填为空
            List<Configuration> column = this.readerOriginConfig
                    .getListConfiguration(Key.COLUMN);
            if (null != column
                    && 1 == column.size()
                    && ("\"*\"".equals(column.get(0).toString()) || "'*'"
                    .equals(column.get(0).toString()))) {
                readerOriginConfig
                        .set(Key.COLUMN, new ArrayList<String>());
            } else {
                // column: 1. index type 2.value type 3.when type is Data, may have format
                List<Configuration> columns = this.readerOriginConfig
                        .getListConfiguration(Key.COLUMN);

                if (null == columns || columns.size() == 0) {
                    throw DataETLException.asDataETLException(
                            HdfsReaderErrorCode.CONFIG_INVALID_EXCEPTION,
                            "您需要指定 columns");
                }

                if (null != columns && columns.size() != 0) {
                    for (Configuration eachColumnConf : columns) {
                        eachColumnConf.getNecessaryValue(Key.TYPE, HdfsReaderErrorCode.REQUIRED_VALUE);
                        Integer columnIndex = eachColumnConf.getInt(Key.INDEX);
                        String columnValue = eachColumnConf.getString(Key.VALUE);

                        if (null == columnIndex && null == columnValue) {
                            throw DataETLException.asDataETLException(
                                    HdfsReaderErrorCode.NO_INDEX_VALUE,
                                    "由于您配置了type, 则至少需要配置 index 或 value");
                        }

                        if (null != columnIndex && null != columnValue) {
                            throw DataETLException.asDataETLException(
                                    HdfsReaderErrorCode.MIXED_INDEX_VALUE,
                                    "您混合配置了index, value, 每一列同时仅能选择其中一种");
                        }

                    }
                }
            }
        }

        @Override
        public List<Configuration> split(int adviceNumber) {
            List<Configuration> readerSplitConfigs = new ArrayList<Configuration>();
            // warn:每个slice拖且仅拖一个文件,
            // int splitNumber = adviceNumber;
            int splitNumber = this.sourceFiles.size();
            if (0 == splitNumber) {
                throw DataETLException.asDataETLException(HdfsReaderErrorCode.EMPTY_DIR_EXCEPTION,
                        String.format("未能找到待读取的文件,请确认您的配置项path: %s", this.readerOriginConfig.getString(Key.PATH)));
            }

            List<List<String>> splitedSourceFiles = this.splitSourceFiles(new ArrayList<String>(this.sourceFiles), splitNumber);
            for (List<String> files : splitedSourceFiles) {
                Configuration splitedConfig = this.readerOriginConfig.clone();
                splitedConfig.set(Constant.SOURCE_FILES, files);
                readerSplitConfigs.add(splitedConfig);
            }

            return readerSplitConfigs;
        }

        private <T> List<List<T>> splitSourceFiles(final List<T> sourceList, int adviceNumber) {
            List<List<T>> splitedList = new ArrayList<List<T>>();
            int averageLength = sourceList.size() / adviceNumber;
            averageLength = averageLength == 0 ? 1 : averageLength;

            for (int begin = 0, end = 0; begin < sourceList.size(); begin = end) {
                end = begin + averageLength;
                if (end > sourceList.size()) {
                    end = sourceList.size();
                }
                splitedList.add(sourceList.subList(begin, end));
            }
            return splitedList;
        }

    }

    public static class Task extends Reader.Task {
        private static Logger LOG = LoggerFactory.getLogger(Reader.Task.class);
        private Configuration taskConfig;
        private List<String> sourceFiles;
        private String specifiedFileType;
        private String encoding;
        private DFSUtil dfsUtil = null;

        public Task(Configuration configuration) {
            super(configuration);
            this.taskConfig = super.getConfiguration();
            this.sourceFiles = this.taskConfig.getList(Constant.SOURCE_FILES, String.class);
            this.specifiedFileType = this.taskConfig.getNecessaryValue(Key.FILETYPE, HdfsReaderErrorCode.REQUIRED_VALUE);
            this.encoding = this.taskConfig.getString(Key.ENCODING, "UTF-8");
            this.dfsUtil = new DFSUtil(this.taskConfig);
        }

        @Override
        public void startRead(Channel channel) {
            for (String sourceFile : this.sourceFiles) {
                LOG.info(String.format("reading file : [%s]", sourceFile));

                if (specifiedFileType.equalsIgnoreCase(Constant.TEXT)
                        || specifiedFileType.equalsIgnoreCase(Constant.CSV)) {

                    InputStream inputStream = dfsUtil.getInputStream(sourceFile);
                    UnstructuredStorageReaderUtil.readFromStream(inputStream, sourceFile, this.taskConfig,
                            channel);
                } else if (specifiedFileType.equalsIgnoreCase(Constant.ORC)) {

                    dfsUtil.orcFileStartRead(sourceFile, this.taskConfig, channel);

                } else if (specifiedFileType.equalsIgnoreCase(Constant.SEQ)) {

                    dfsUtil.sequenceFileStartRead(sourceFile, this.taskConfig, channel);
                } else if (specifiedFileType.equalsIgnoreCase(Constant.RC)) {

                    dfsUtil.rcFileStartRead(sourceFile, this.taskConfig, channel);
                } else {
                    String message = "HdfsReader插件目前支持ORC, TEXT, CSV, SEQUENCE, RC五种格式的文件," +
                            "请将fileType选项的值配置为ORC, TEXT, CSV, SEQUENCE 或者 RC";
                    throw DataETLException.asDataETLException(HdfsReaderErrorCode.FILE_TYPE_UNSUPPORT, message);
                }
            }
        }

        @Override
        public void init() {

        }

        @Override
        public void prepare() {

        }

        @Override
        public void post() {

        }

        @Override
        public void destroy() {

        }

    }
}
