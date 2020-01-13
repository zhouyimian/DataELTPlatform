package com.km.writer;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Strings;
import com.km.common.exception.DataETLException;
import com.km.common.util.Configuration;
import com.km.core.transport.channel.Channel;
import com.km.core.util.container.CoreConstant;
import com.km.writer.MongoDBWriterUtil.KeyConstant;
import com.km.writer.MongoDBWriterUtil.MongoDBWriterErrorCode;
import com.km.writer.MongoDBWriterUtil.MongoUtil;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MongoDBWriter extends Writer {

    public static class Job extends Writer.Job {


        public Job(Configuration configuration) {
            super(configuration);
        }

        @Override
        public List<Configuration> split(int mandatoryNumber) {
            List<Configuration> configList = new ArrayList<Configuration>();
            for (int i = 0; i < mandatoryNumber; i++) {
                configList.add(this.getConfiguration().clone());
            }
            return configList;
        }
    }

    public static class Task extends Writer.Task {

        private static final Logger logger = LoggerFactory.getLogger(Task.class);
        private Configuration writerSliceConfig;

        private MongoClient mongoClient;

        private String userName = null;
        private String password = null;

        private String database = null;
        private String collection = null;
        private int batchSize = 100;

        private JSONArray mongodbColumnMeta = null;


        public Task(Configuration configuration) {
            super(configuration.getConfiguration(CoreConstant.JOB_WRITER_PARAMETER));
            this.writerSliceConfig = this.getConfiguration();
            this.userName = writerSliceConfig.getString(KeyConstant.MONGO_USER_NAME);
            this.password = writerSliceConfig.getString(KeyConstant.MONGO_USER_PASSWORD);
            this.database = writerSliceConfig.getString(KeyConstant.MONGO_DB_NAME);
            if (!Strings.isNullOrEmpty(userName) && !Strings.isNullOrEmpty(password)) {
                this.mongoClient = MongoUtil.initCredentialMongoClient(this.writerSliceConfig, userName, password, database);
            } else {
                this.mongoClient = MongoUtil.initMongoClient(this.writerSliceConfig);
            }
            this.collection = writerSliceConfig.getString(KeyConstant.MONGO_COLLECTION_NAME);
            this.mongodbColumnMeta = JSON.parseArray(writerSliceConfig.getString(KeyConstant.MONGO_COLUMN));
        }

        public void startWrite(Channel channel) {
            if (Strings.isNullOrEmpty(database) || Strings.isNullOrEmpty(collection)
                    || mongoClient == null || mongodbColumnMeta == null) {
                throw DataETLException.asDataETLException(MongoDBWriterErrorCode.ILLEGAL_VALUE,
                        MongoDBWriterErrorCode.ILLEGAL_VALUE.getDescription());
            }
            MongoDatabase db = mongoClient.getDatabase(database);
            MongoCollection<BasicDBObject> col = db.getCollection(this.collection, BasicDBObject.class);
            List<JSONObject> writerBuffer = new ArrayList<JSONObject>();
            JSONObject object = null;
            while ((object = channel.remove()) != null) {
                writerBuffer.add(object);
                if (writerBuffer.size() >= this.batchSize) {
                    doBatchInsert(col, writerBuffer, mongodbColumnMeta);
                    writerBuffer.clear();
                }
            }
            if (!writerBuffer.isEmpty()) {
                doBatchInsert(col, writerBuffer, mongodbColumnMeta);
                writerBuffer.clear();
            }
        }

        private void doBatchInsert(MongoCollection<BasicDBObject> collection, List<JSONObject> writerBuffer, JSONArray columnMeta) {

            List<BasicDBObject> dataList = new ArrayList<BasicDBObject>();

            for (JSONObject object : writerBuffer) {
                BasicDBObject data = new BasicDBObject();
                for (int i = 0; i < object.entrySet().size(); i++) {
                    String type = columnMeta.getJSONObject(i).getString(KeyConstant.COLUMN_TYPE);
                    String name = columnMeta.getJSONObject(i).getString(KeyConstant.COLUMN_NAME);
                    //空记录处理
                    if (Strings.isNullOrEmpty(object.getString(name))) {
                        if (KeyConstant.isArrayType(type.toLowerCase())) {
                            data.put(name, new Object[0]);
                        } else {
                            data.put(name, object.get(name));
                        }
                        continue;
                    }
                    if ("INT".equalsIgnoreCase(type)) {
                        //int是特殊类型, 其他类型按照保存时Column的类型进行处理
                        data.put(name,
                                Integer.parseInt(
                                        String.valueOf(object.get(name))));

                    } else if ("String".equalsIgnoreCase(type)) {
                        //处理ObjectId和数组类型
                        if (KeyConstant.isObjectIdType(type.toLowerCase())) {
                            data.put(name,
                                    new ObjectId(object.get(name).toString()));
                        } else if (KeyConstant.isArrayType(type.toLowerCase())) {
                            String splitter = columnMeta.getJSONObject(i).getString(KeyConstant.COLUMN_SPLITTER);
                            if (Strings.isNullOrEmpty(splitter)) {
                                throw DataETLException.asDataETLException(MongoDBWriterErrorCode.ILLEGAL_VALUE,
                                        MongoDBWriterErrorCode.ILLEGAL_VALUE.getDescription());
                            }
                            String itemType = columnMeta.getJSONObject(i).getString(KeyConstant.ITEM_TYPE);
                            if (itemType != null && !itemType.isEmpty()) {
                                //如果数组指定类型不为空，将其转换为指定类型
                                String[] item = object.get(name).toString().split(splitter);
                                if (itemType.equalsIgnoreCase("DOUBLE")) {
                                    ArrayList<Double> list = new ArrayList<Double>();
                                    for (String s : item) {
                                        list.add(Double.parseDouble(s));
                                    }
                                    data.put(name, list.toArray(new Double[0]));
                                } else if (itemType.equalsIgnoreCase("INT")) {
                                    ArrayList<Integer> list = new ArrayList<Integer>();
                                    for (String s : item) {
                                        list.add(Integer.parseInt(s));
                                    }
                                    data.put(name, list.toArray(new Integer[0]));
                                } else if (itemType.equalsIgnoreCase("LONG")) {
                                    ArrayList<Long> list = new ArrayList<Long>();
                                    for (String s : item) {
                                        list.add(Long.parseLong(s));
                                    }
                                    data.put(name, list.toArray(new Long[0]));
                                } else if (itemType.equalsIgnoreCase("BOOL")) {
                                    ArrayList<Boolean> list = new ArrayList<Boolean>();
                                    for (String s : item) {
                                        list.add(Boolean.parseBoolean(s));
                                    }
                                    data.put(name, list.toArray(new Boolean[0]));
                                } else if (itemType.equalsIgnoreCase("BYTES")) {
                                    ArrayList<Byte> list = new ArrayList<Byte>();
                                    for (String s : item) {
                                        list.add(Byte.parseByte(s));
                                    }
                                    data.put(name, list.toArray(new Byte[0]));
                                } else {
                                    data.put(name, object.get(name).toString().split(splitter));
                                }
                            } else {
                                data.put(name, object.get(name).toString().split(splitter));
                            }
                        } else if (type.toLowerCase().equalsIgnoreCase("json")) {
                            //如果是json类型,将其进行转换
                            Object mode = com.mongodb.util.JSON.parse(object.get(name).toString());
                            data.put(name, JSON.toJSON(mode));
                        } else {
                            data.put(name, object.get(name).toString());
                        }

                    } else if ("Long".equalsIgnoreCase(type)) {
                        data.put(name, Long.parseLong(object.get(name).toString()));
                    } else if ("Date".equalsIgnoreCase(type)) {
                        data.put(name,
                                new Date(Long.parseLong(object.get(name).toString())));
                    } else if ("Double".equalsIgnoreCase(type)) {
                        data.put(name,
                                Double.parseDouble(object.get(name).toString()));
                    } else if ("Boolean".equalsIgnoreCase(type)) {
                        data.put(name,
                                Boolean.parseBoolean(object.get(name).toString()));

                    } else if ("Bytes".equalsIgnoreCase(type)) {
                        data.put(name,
                                object.get(name).toString().getBytes());
                    } else {
                        data.put(name, object.get(name).toString());
                    }
                }
                dataList.add(data);
            }
            collection.insertMany(dataList);
        }
    }
}
