package com.km.utils;

import com.km.data.common.util.Configuration;

import java.util.HashMap;
import java.util.Map;

public class LoadConfigureUtil {
    public static Map<String,Configuration> etlPlugNameToConf = new HashMap<>();
    public static Map<String,Configuration> readerPlugNameToConf = new HashMap<>();
    public static Map<String,Configuration> writerPlugNameToConf = new HashMap<>();

    public static Map<String, Configuration> getEtlPlugNameToConf() {
        return etlPlugNameToConf;
    }

    public static void setEtlPlugNameToConf(Map<String, Configuration> etlPlugNameToConf) {
        LoadConfigureUtil.etlPlugNameToConf = etlPlugNameToConf;
    }

    public static Map<String, Configuration> getReaderPlugNameToConf() {
        return readerPlugNameToConf;
    }

    public static void setReaderPlugNameToConf(Map<String, Configuration> readerPlugNameToConf) {
        LoadConfigureUtil.readerPlugNameToConf = readerPlugNameToConf;
    }

    public static Map<String, Configuration> getWriterPlugNameToConf() {
        return writerPlugNameToConf;
    }

    public static void setWriterPlugNameToConf(Map<String, Configuration> writerPlugNameToConf) {
        LoadConfigureUtil.writerPlugNameToConf = writerPlugNameToConf;
    }
}
