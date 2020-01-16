package com.km.writer;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Strings;
import com.km.common.element.*;
import com.km.common.exception.DataETLException;
import com.km.common.util.Configuration;
import com.km.core.transport.channel.Channel;
import com.km.writer.mongoDBWriterUtil.KeyConstant;
import com.km.writer.mongoDBWriterUtil.MongoDBWriterErrorCode;
import com.km.writer.mongoDBWriterUtil.MongoUtil;
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
            super(configuration);
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
            List<Record> writerBuffer = new ArrayList<Record>();
            Record record = null;
            while ((record = channel.remove()) != null) {
                writerBuffer.add(record);
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

        private void doBatchInsert(MongoCollection<BasicDBObject> collection, List<Record> writerBuffer, JSONArray columnMeta) {

            List<BasicDBObject> dataList = new ArrayList<BasicDBObject>();

            for (Record record : writerBuffer) {
                BasicDBObject data = new BasicDBObject();
                for (int i = 0; i < record.getColumnNumber(); i++) {
                    String type = columnMeta.getJSONObject(i).getString(KeyConstant.COLUMN_TYPE);
                    String name = columnMeta.getJSONObject(i).getString(KeyConstant.COLUMN_NAME);
                    //空记录处理
                    if (Strings.isNullOrEmpty(record.getColumn(i).asString())) {
                        if (KeyConstant.isArrayType(type.toLowerCase())) {
                            data.put(columnMeta.getJSONObject(i).getString(KeyConstant.COLUMN_NAME), new Object[0]);
                        } else {
                            data.put(columnMeta.getJSONObject(i).getString(KeyConstant.COLUMN_NAME), record.getColumn(i).asString());
                        }
                        continue;
                    }
                    if (Column.Type.INT.name().equalsIgnoreCase(type)) {
                        //int是特殊类型, 其他类型按照保存时Column的类型进行处理
                        data.put(columnMeta.getJSONObject(i).getString(KeyConstant.COLUMN_NAME),
                                Integer.parseInt(
                                        String.valueOf(record.getColumn(i).getRawData())));
                    } else if(record.getColumn(i) instanceof StringColumn){
                        //处理ObjectId和数组类型
                        if (KeyConstant.isObjectIdType(type.toLowerCase())) {
                            data.put(columnMeta.getJSONObject(i).getString(KeyConstant.COLUMN_NAME),
                                    new ObjectId(record.getColumn(i).asString()));
                        } else if (KeyConstant.isArrayType(type.toLowerCase())) {
                            String splitter = columnMeta.getJSONObject(i).getString(KeyConstant.COLUMN_SPLITTER);
                            if (Strings.isNullOrEmpty(splitter)) {
                                throw DataETLException.asDataETLException(MongoDBWriterErrorCode.ILLEGAL_VALUE,
                                        MongoDBWriterErrorCode.ILLEGAL_VALUE.getDescription());
                            }
                            String itemType = columnMeta.getJSONObject(i).getString(KeyConstant.ITEM_TYPE);
                            if (itemType != null && !itemType.isEmpty()) {
                                //如果数组指定类型不为空，将其转换为指定类型
                                String[] item = record.getColumn(i).asString().split(splitter);
                                if (itemType.equalsIgnoreCase(Column.Type.DOUBLE.name())) {
                                    ArrayList<Double> list = new ArrayList<Double>();
                                    for (String s : item) {
                                        list.add(Double.parseDouble(s));
                                    }
                                    data.put(columnMeta.getJSONObject(i).getString(KeyConstant.COLUMN_NAME), list.toArray(new Double[0]));
                                } else if (itemType.equalsIgnoreCase(Column.Type.INT.name())) {
                                    ArrayList<Integer> list = new ArrayList<Integer>();
                                    for (String s : item) {
                                        list.add(Integer.parseInt(s));
                                    }
                                    data.put(columnMeta.getJSONObject(i).getString(KeyConstant.COLUMN_NAME), list.toArray(new Integer[0]));
                                } else if (itemType.equalsIgnoreCase(Column.Type.LONG.name())) {
                                    ArrayList<Long> list = new ArrayList<Long>();
                                    for (String s : item) {
                                        list.add(Long.parseLong(s));
                                    }
                                    data.put(columnMeta.getJSONObject(i).getString(KeyConstant.COLUMN_NAME), list.toArray(new Long[0]));
                                } else if (itemType.equalsIgnoreCase(Column.Type.BOOL.name())) {
                                    ArrayList<Boolean> list = new ArrayList<Boolean>();
                                    for (String s : item) {
                                        list.add(Boolean.parseBoolean(s));
                                    }
                                    data.put(columnMeta.getJSONObject(i).getString(KeyConstant.COLUMN_NAME), list.toArray(new Boolean[0]));
                                } else if (itemType.equalsIgnoreCase(Column.Type.BYTES.name())) {
                                    ArrayList<Byte> list = new ArrayList<Byte>();
                                    for (String s : item) {
                                        list.add(Byte.parseByte(s));
                                    }
                                    data.put(columnMeta.getJSONObject(i).getString(KeyConstant.COLUMN_NAME), list.toArray(new Byte[0]));
                                } else {
                                    data.put(columnMeta.getJSONObject(i).getString(KeyConstant.COLUMN_NAME), record.getColumn(i).asString().split(splitter));
                                }
                            } else {
                                data.put(columnMeta.getJSONObject(i).getString(KeyConstant.COLUMN_NAME), record.getColumn(i).asString().split(splitter));
                            }
                        } else if (type.toLowerCase().equalsIgnoreCase("json")) {
                            //如果是json类型,将其进行转换
                            Object mode = com.mongodb.util.JSON.parse(record.getColumn(i).asString());
                            data.put(columnMeta.getJSONObject(i).getString(KeyConstant.COLUMN_NAME),JSON.toJSON(mode));
                        } else {
                            data.put(columnMeta.getJSONObject(i).getString(KeyConstant.COLUMN_NAME), record.getColumn(i).asString());
                        }

                    } else if(record.getColumn(i) instanceof LongColumn) {
                        data.put(columnMeta.getJSONObject(i).getString(KeyConstant.COLUMN_NAME),record.getColumn(i).asLong());
                    } else if(record.getColumn(i) instanceof DateColumn) {
                        data.put(columnMeta.getJSONObject(i).getString(KeyConstant.COLUMN_NAME),
                                record.getColumn(i).asDate());
                    } else if(record.getColumn(i) instanceof DoubleColumn) {
                        data.put(columnMeta.getJSONObject(i).getString(KeyConstant.COLUMN_NAME),
                                record.getColumn(i).asDouble());
                    } else if(record.getColumn(i) instanceof BoolColumn) {
                        data.put(columnMeta.getJSONObject(i).getString(KeyConstant.COLUMN_NAME),
                                record.getColumn(i).asBoolean());

                    } else if(record.getColumn(i) instanceof BytesColumn) {
                        data.put(columnMeta.getJSONObject(i).getString(KeyConstant.COLUMN_NAME),
                                record.getColumn(i).asBytes());
                    } else {
                        data.put(columnMeta.getJSONObject(i).getString(KeyConstant.COLUMN_NAME),record.getColumn(i).asString());
                    }
                }
                dataList.add(data);
            }
            collection.insertMany(dataList);
        }
    }
}
