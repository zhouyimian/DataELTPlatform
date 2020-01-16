package com.km.writer;

import com.google.common.collect.Sets;
import com.km.common.exception.DataETLException;
import com.km.common.util.Configuration;
import com.km.core.transport.channel.Channel;
import com.km.writer.hdfsWriterUtil.Constant;
import com.km.writer.hdfsWriterUtil.HdfsHelper;
import com.km.writer.hdfsWriterUtil.HdfsWriterErrorCode;
import com.km.writer.hdfsWriterUtil.Key;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CountDownLatch;

public class HdfsWriter extends Writer {

    public static class Job extends Writer.Job {
        private Configuration writerSliceConfig = null;

        private String defaultFS;
        private String path;
        private String fileType;
        private String fileName;
        private List<Configuration> columns;
        private String fieldDelimiter;
        private String compress;
        private String encoding;
        private HashSet<String> tmpFiles = new HashSet<String>();//临时文件全路径
        private HashSet<String> endFiles = new HashSet<String>();//最终文件全路径

        private HdfsHelper hdfsHelper = null;

        public Job(Configuration configuration) {

            super(configuration);
            this.writerSliceConfig = this.getConfiguration();
            this.validateParameter();

            //创建textfile存储
            hdfsHelper = new HdfsHelper();

            hdfsHelper.getFileSystem(defaultFS, this.writerSliceConfig);

            this.prepare();
        }

        @Override
        public void init() {

        }

        @Override
        public List<Configuration> split(int mandatoryNumber) {
            List<Configuration> writerSplitConfigs = new ArrayList<Configuration>();
            String filePrefix = fileName;

            Set<String> allFiles = new HashSet<String>();

            //获取该路径下的所有已有文件列表
            if (hdfsHelper.isPathexists(path)) {
                allFiles.addAll(Arrays.asList(hdfsHelper.hdfsDirList(path)));
            }

            String fileSuffix;

            String storePath = this.path.endsWith(String
                    .valueOf(IOUtils.DIR_SEPARATOR_UNIX))?this.path:this.path+IOUtils.DIR_SEPARATOR_UNIX;

            this.path = storePath;
            for (int i = 0; i < mandatoryNumber; i++) {

                Configuration splitedTaskConfig = this.writerSliceConfig.clone();
                String fullFileName = null;
                String endFullFileName = null;
                fileSuffix = UUID.randomUUID().toString().replace('-', '_');

                fullFileName = String.format("%s%s__%s%s", defaultFS, storePath, this.fileName,fileSuffix);
                endFullFileName = String.format("%s%s%s", defaultFS, storePath, this.fileName);

                if(allFiles.contains(endFullFileName)){
                    throw DataETLException.asDataETLException(HdfsWriterErrorCode.ILLEGAL_VALUE,
                            String.format("您配置的最终路径: [%s] 不是一个合法的目录, 请您注意文件重名, 不合法目录名等情况.",
                                    endFullFileName));
                }
                while (allFiles.contains(fullFileName)) {
                    fileSuffix = UUID.randomUUID().toString().replace('-', '_');
                    fullFileName = String.format("%s%s%s__%s", defaultFS, storePath, filePrefix, fileSuffix);
                }
                allFiles.add(endFullFileName);
                String checkPath = null;
                //设置临时文件全路径和最终文件全路径
                if ("GZIP".equalsIgnoreCase(this.compress)) {
                    this.tmpFiles.add(fullFileName + ".gz");
                    this.endFiles.add(endFullFileName + ".gz");
                    checkPath = endFullFileName+".gz";
                } else if ("BZIP2".equalsIgnoreCase(compress)) {
                    this.tmpFiles.add(fullFileName + ".bz2");
                    this.endFiles.add(endFullFileName + ".bz2");
                    checkPath = endFullFileName+".bz2";
                } else {
                    this.tmpFiles.add(fullFileName);
                    this.endFiles.add(endFullFileName);
                    checkPath = endFullFileName;
                }
                if(hdfsHelper.isPathexists(checkPath)){
                    throw DataETLException.asDataETLException(HdfsWriterErrorCode.ILLEGAL_VALUE,
                            String.format("您配置的最终路径: [%s] 已经有此路径的文件存在，请修改文件名或者文件路径",
                                    endFullFileName));
                }
                splitedTaskConfig
                        .set(Key.FILE_NAME,
                                fullFileName);

                writerSplitConfigs.add(splitedTaskConfig);
            }
            return writerSplitConfigs;
        }

        private void validateParameter() {
            this.defaultFS = this.writerSliceConfig.getNecessaryValue(Key.DEFAULT_FS, HdfsWriterErrorCode.REQUIRED_VALUE);
            this.fileType = this.writerSliceConfig.getNecessaryValue(Key.FILE_TYPE, HdfsWriterErrorCode.REQUIRED_VALUE);
            if (!fileType.equalsIgnoreCase("ORC") && !fileType.equalsIgnoreCase("TEXT")) {
                String message = "HdfsWriter插件目前只支持ORC和TEXT两种格式的文件,请将filetype选项的值配置为ORC或者TEXT";
                throw DataETLException.asDataETLException(HdfsWriterErrorCode.ILLEGAL_VALUE, message);
            }
            this.path = this.writerSliceConfig.getNecessaryValue(Key.PATH, HdfsWriterErrorCode.REQUIRED_VALUE);
            if (!path.startsWith("/")) {
                String message = String.format("请检查参数path:[%s],需要配置为绝对路径", path);
                throw DataETLException.asDataETLException(HdfsWriterErrorCode.ILLEGAL_VALUE, message);
            } else if (path.contains("*") || path.contains("?")) {
                String message = String.format("请检查参数path:[%s],不能包含*,?等特殊字符", path);
                throw DataETLException.asDataETLException(HdfsWriterErrorCode.ILLEGAL_VALUE, message);
            }
            this.fileName = this.writerSliceConfig.getNecessaryValue(Key.FILE_NAME, HdfsWriterErrorCode.REQUIRED_VALUE);
            this.columns = this.writerSliceConfig.getListConfiguration(Key.COLUMN);
            if (null == columns || columns.size() == 0) {
                throw DataETLException.asDataETLException(HdfsWriterErrorCode.REQUIRED_VALUE, "您需要指定 columns");
            } else {
                for (Configuration eachColumnConf : columns) {
                    eachColumnConf.getNecessaryValue(Key.NAME, HdfsWriterErrorCode.COLUMN_REQUIRED_VALUE);
                    eachColumnConf.getNecessaryValue(Key.TYPE, HdfsWriterErrorCode.COLUMN_REQUIRED_VALUE);
                }
            }
            this.fieldDelimiter = this.writerSliceConfig.getString(Key.FIELD_DELIMITER, null);
            if (null == fieldDelimiter) {
                throw DataETLException.asDataETLException(HdfsWriterErrorCode.REQUIRED_VALUE,
                        String.format("您提供配置文件有误，[%s]是必填参数.", Key.FIELD_DELIMITER));
            } else if (1 != fieldDelimiter.length()) {
                throw DataETLException.asDataETLException(HdfsWriterErrorCode.ILLEGAL_VALUE,
                        String.format("仅仅支持单字符切分, 您配置的切分为 : [%s]", fieldDelimiter));
            }
            this.compress = this.writerSliceConfig.getString(Key.COMPRESS, null);
            if (fileType.equalsIgnoreCase("TEXT")) {
                Set<String> textSupportedCompress = Sets.newHashSet("GZIP", "BZIP2");
                //用户可能配置的是compress:"",空字符串,需要将compress设置为null
                if (StringUtils.isBlank(compress)) {
                    this.writerSliceConfig.set(Key.COMPRESS, null);
                } else {
                    compress = compress.toUpperCase().trim();
                    if (!textSupportedCompress.contains(compress)) {
                        throw DataETLException.asDataETLException(HdfsWriterErrorCode.ILLEGAL_VALUE,
                                String.format("目前TEXT FILE仅支持GZIP、BZIP2 两种压缩, 不支持您配置的 compress 模式 : [%s]",
                                        compress));
                    }
                }
            } else if (fileType.equalsIgnoreCase("ORC")) {
                Set<String> orcSupportedCompress = Sets.newHashSet("NONE", "SNAPPY");
                if (null == compress) {
                    this.writerSliceConfig.set(Key.COMPRESS, "NONE");
                } else {
                    compress = compress.toUpperCase().trim();
                    if (!orcSupportedCompress.contains(compress)) {
                        throw DataETLException.asDataETLException(HdfsWriterErrorCode.ILLEGAL_VALUE,
                                String.format("目前ORC FILE仅支持SNAPPY压缩, 不支持您配置的 compress 模式 : [%s]",
                                        compress));
                    }
                }

            }
            this.encoding = this.writerSliceConfig.getString(Key.ENCODING, Constant.DEFAULT_ENCODING);
            try {
                encoding = encoding.trim();
                this.writerSliceConfig.set(Key.ENCODING, encoding);
                Charsets.toCharset(encoding);
            } catch (Exception e) {
                throw DataETLException.asDataETLException(HdfsWriterErrorCode.ILLEGAL_VALUE,
                        String.format("不支持您配置的编码格式:[%s]", encoding), e);
            }
        }

        public void prepare() {
            //若路径已经存在，检查path是否是目录
            if (hdfsHelper.isPathexists(path)) {
                if (!hdfsHelper.isPathDir(path)) {
                    throw DataETLException.asDataETLException(HdfsWriterErrorCode.ILLEGAL_VALUE,
                            String.format("您配置的path: [%s] 不是一个合法的目录, 请您注意文件重名, 不合法目录名等情况.",
                                    path));
                }
            }
        }

        public void post() {
            hdfsHelper.renameFile(tmpFiles, endFiles);
        }

        public void destroy() {
            hdfsHelper.closeFileSystem();
        }
    }

    public static class Task extends Writer.Task {
        private Configuration writerSliceConfig;

        private String defaultFS;
        private String fileType;
        private String fileName;

        private HdfsHelper hdfsHelper = null;

        public Task(Configuration configuration) {
            super(configuration);
            this.writerSliceConfig = this.getConfiguration();
            this.defaultFS = this.writerSliceConfig.getString(Key.DEFAULT_FS);
            this.fileType = this.writerSliceConfig.getString(Key.FILE_TYPE);
            //得当的已经是绝对路径，eg：hdfs://10.101.204.12:9000/user/hive/warehouse/writer.db/text/test.textfile
            this.fileName = this.writerSliceConfig.getString(Key.FILE_NAME);

            hdfsHelper = new HdfsHelper();
            hdfsHelper.getFileSystem(defaultFS, writerSliceConfig);
        }

        public void startWrite(Channel channel) {
            if (fileType.equalsIgnoreCase("TEXT")) {
                //写TEXT FILE
                hdfsHelper.textFileStartWrite(channel, this.writerSliceConfig, this.fileName);
            } else if (fileType.equalsIgnoreCase("ORC")) {
                //写ORC FILE
                hdfsHelper.orcFileStartWrite(channel, this.writerSliceConfig, this.fileName);
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
