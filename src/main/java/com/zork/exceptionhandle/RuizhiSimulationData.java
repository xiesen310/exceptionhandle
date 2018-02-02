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
public class RuizhiSimulationData {
    private KafkaProducer<String, String> producer;
    private Properties properties;

    public RuizhiSimulationData() {
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

            normalFields.put("RecordNum", "1")
                    .put("SessionID", "1")
                    .put("AccountID", "25919" + random.nextInt(9))
                    .put("ThreadID", "00002590")
                    .put("BusinessID", "1770584")
                    .put("TradTime", "")
                    .put("RecordIndex", "")
                    .put("ProtocolID", "410512")
                    .put("TradeDateTime", "2018-01-31/23:59:59:999")
                    .put("TypeName", "Send")
                    .put("ConnectID", "4299")
                    .put("FuncID", "1108")
                    .put("TradeState", "1")
                    .put("message", "01-31/23:59:59:999[00002590][117.24.114.163/")
                    .put("FiledValue", "")
                    .put("logchecktime", "2018-02-01T00:00:03.283+08:00")
                    .put("timeError", "")
                    .put("kvFiled", "{ext}")
                    .put("FiledName", "")
                    .put("BodyLength", "00000231");

            dimensions.put("logdate", "20180131")
                    .put("appprogramname", "ruizhi")
                    .put("hostname", "wsjy-tdxrz1" + random.nextInt(10))
                    .put("appsystem", "ruizhiSystem")
                    .put("IPMac", "117.24.114.16"+random.nextInt(9)+"//18F46AB7B768");

            _source.put("timestamp","2018-01-31T23:57:23.000+08:00")
                    .put("source","D:\\tdx\\tdxtc50_7708\\log\\Trade_20180201.log")
                    .put("indexTime","2018-01-31T23:57:38.978+08:00")
                    .put("normalFields",normalFields)
                    .put("logTypeName","ruizhi_log_logstash")
                    .put("dimensions",dimensions)
                    .put("measures",measures)
                    .put("offset", random.nextInt(10));

            JSONArray indexTime = new JSONArray();
            indexTime.put(1517414258978L);

            JSONArray normalFieldsLogchecktime = new JSONArray();
            normalFieldsLogchecktime.put(1517414258974L);

            JSONArray timestamp = new JSONArray();
            timestamp.put(System.currentTimeMillis());
            fields.put("indexTime", indexTime)
                    .put("normalFields.logchecktime", normalFieldsLogchecktime)
                    .put("timestamp", timestamp);

            event.put("_index", "ruizhilog_2018.01.31")
                    .put("_type", "ruizhi_log_logstash")
                    .put("_id", "AWFM8o7ZOxRgjvauYrzA")
                    .put("_version", 1)
                    .put("_score", "null")
                    .put("sort", 1517414243000L)
                    .put("fields", fields)
                    .put("_source", _source);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return event.toString();
    }

    public static void main(String[] args) {
        RuizhiSimulationData client = new RuizhiSimulationData();

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
