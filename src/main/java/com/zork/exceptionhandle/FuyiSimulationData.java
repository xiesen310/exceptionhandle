package com.zork.exceptionhandle;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

import java.util.Properties;
import java.util.Random;

/**
 * 模拟数据
 */
public class FuyiSimulationData {
    private KafkaProducer<String, String> producer;
    private Properties properties;

    public FuyiSimulationData() {
        properties = new Properties();
        properties.put("bootstrap.servers", "master:9092,slaver1:9092");
        properties.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        properties.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        producer = new KafkaProducer<>(properties);
    }

    public void sendRecorder(String topic, String key, String value) {
        ProducerRecord<String, String> record = new ProducerRecord<>(topic, key, value);
        producer.send(record);
    }

    public static String message() {
        JSONObject event = new JSONObject();
        JSONObject measures = new JSONObject();
        JSONObject dimensions = new JSONObject();
        JSONObject normalFields = new JSONObject();
        JSONObject _source = new JSONObject();
        JSONObject fields = new JSONObject();
        Random random = new Random();
        try {
            measures.put("latence", random.nextInt(10));

            normalFields.put("custId", "12067449")
                    .put("message", "")
                    .put("funcId", "410312")
                    .put("createTime", "20180131 23:57:23")
                    .put("logchecktime", "2018-01-31T23:57:38.974+08:00")
                    .put("macAddr", "7824AF37301C")
                    .put("lanIp", "117.135.212.39") //
                    .put("isSuccess", "0")
                    .put("userTime", "14")
                    .put("ext", "")
                    .put("walIp", "FY10.232.238.82") //
                    .put("oraId", "5201");

            dimensions.put("logdate", "20180131")
                    .put("hostname", "6CU4100J8" + random.nextInt(10))
                    .put("appsystem", "fySystem")
                    .put("appprogramname", "fy");

            _source.put("timestamp","2018-01-31T23:57:23.000+08:00")
                    .put("source","D:\\\\ff_new_webrts\\\\applog\\\\20180131_log0.0")
                    .put("indexTime","2018-01-31T23:57:38.978+08:00")
                    .put("normalFields",normalFields)
                    .put("logTypeName","fy_log_logstash")
                    .put("dimensions",dimensions)
                    .put("measures",measures)
                    .put("offset","5739985");

            JSONArray indexTime = new JSONArray();
            indexTime.put(1517414258978L);

            JSONArray normalFieldsLogchecktime = new JSONArray();
            normalFieldsLogchecktime.put(1517414258974L);

            JSONArray timestamp = new JSONArray();
            timestamp.put(System.currentTimeMillis());
            fields.put("indexTime",indexTime)
                    .put("normalFields.logchecktime",normalFieldsLogchecktime)
                    .put("timestamp",timestamp);

            event.put("_index", "fuyilog_2018.01.31")
                    .put("_type", "kcbp_biz_log")
                    .put("_id", "AWFM8Cw7OxRgjvauX-ga")
                    .put("_version", 1)
                    .put("_score", "null")
                    .put("sort", 1517414243000L)
                    .put("fields",fields)
                    .put("_source", _source);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return event.toString();
    }

    public static void main(String[] args) {
        FuyiSimulationData client = new FuyiSimulationData();

        try {
            while (true) {
                client.sendRecorder("test1", "key", message());
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        /*for (int i = 0; i < 10; i++) {
            client.sendRecorder("test1", "key" + i, message());
        }*/
    }
}
