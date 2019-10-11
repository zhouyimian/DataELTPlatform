package com.km.dataeltplatform.DataModule.DataTransferModule.service;


import com.alibaba.fastjson.JSONObject;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;


@Service
public class KafkaService {
    //将内存中的数据写入Kafka
    public void insertDataToKafka(List<JSONObject> mysqlData, HttpServletRequest req) {
        String bootstrapServers = req.getParameter("bootstrapServers");
        String keySerializer = req.getParameter("keySerializer");
        String valueSerializer = req.getParameter("valueSerializer");
        String topic = req.getParameter("topic");
        Properties properties = new Properties();
        properties.put("bootstrap.servers", bootstrapServers);
        properties.put("key.serializer", keySerializer);
        properties.put("value.serializer", valueSerializer);

        KafkaProducer<String, String> producer = new KafkaProducer<>(properties);
        for (JSONObject jsonObject : mysqlData) {
            ProducerRecord<String, String> record = new ProducerRecord<>(topic, null, jsonObject.toJSONString());
            producer.send(record, (recordMetadata, e) -> {
                if (e != null) {
                    e.printStackTrace();
                } else {
                    System.out.println("发送成功");
                }
            });
        }
    }

    /**
     * 将kafka的数据追加到HDFS文件
     * 该方法效率很低，以后需要进行改进。
     * @param req
     * @throws IOException
     */
    public void insertDataToHDFS(HttpServletRequest req) throws IOException {
        HDFSService hdfsService = new HDFSService();
        String bootstrapServers = req.getParameter("bootstrapServers");
        String keyDeSerializer = req.getParameter("keyDeSerializer");
        String valueDeSerializer = req.getParameter("valueDeSerializer");
        String HDFSFilePath = req.getParameter("HDFSFilePath");
        String topic = req.getParameter("topic");
        Properties properties = new Properties();
        properties.put("bootstrap.servers", bootstrapServers);
        properties.put("key.deserializer", keyDeSerializer);
        properties.put("value.deserializer", valueDeSerializer);

        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(properties);
        consumer.subscribe(Arrays.asList(topic));
        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(100);
            for (ConsumerRecord<String, String> record : records) {
                consumer.commitAsync();
                String value = record.value();
                hdfsService.appendDataToHDFSWithoutStruct(HDFSFilePath,value);
            }
        }
    }
}
