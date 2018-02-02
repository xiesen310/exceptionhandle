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
public class JhSimulationData {
    private KafkaProducer<String, String> producer;
    private Properties properties;

    public JhSimulationData() {
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

            normalFields.put("custId","212122"+ random.nextInt(9))
                    .put("result", "1")
                    .put("ldate", "20180131")
                    .put("interval", "0")
                    .put("bz", "")
                    .put("baklogflag", "0")
                    .put("clitype", "14")
                    .put("macaddr", "14")
                    .put("wtsl", "")
                    .put("gddm", "")
                    .put("recsynid", "20180131-23:59:59-251121")
                    .put("hth", "")
                    .put("yybdm", "")
                    .put("nowlogbaking", "0")
                    .put("khh", "13601093111")
                    .put("imei", "")
                    .put("logtype", "YYZLog")
                    .put("reqtime", "23:59:59")
                    .put("hostname", "10.228.196.15" + random.nextInt(9)) //
                    .put("wtjg", "")
                    .put("udid", "")
                    .put("wtje", "")
                    .put("anstime", "23:59:59")
                    .put("ip", "36.149.29.8" + random.nextInt(9)) //
                    .put("synid", "251121")
                    .put("message", "synid: 251121")
                    .put("logchecktime", "2018-01-31T23:59:59.948+08:00")
                    .put("gnname", "FY10")
                    .put("gpdm", "");

            dimensions.put("appprogramname", "yiyangzhilog")
                    .put("hostname", "10.228.196.15" + random.nextInt(9))
                    .put("result", "1")
                    .put("appsystem", "JHSystem")
                    .put("func", "aa");

            _source.put("timestamp","2018-01-31T23:57:23.000+08:00")
                    .put("source","")
                    .put("indexTime","2018-01-31T23:57:38.978+08:00")
                    .put("normalFields",normalFields)
                    .put("logTypeName","yiyangzhi_kafka_logstash")
                    .put("dimensions",dimensions)
                    .put("measures",measures)
                    .put("offset", "0");

            JSONArray indexTime = new JSONArray();
            indexTime.put(1517414258978L);

            JSONArray normalFieldsLogchecktime = new JSONArray();
            normalFieldsLogchecktime.put(1517414258974L);

            JSONArray timestamp = new JSONArray();
            timestamp.put(System.currentTimeMillis());
            fields.put("indexTime", indexTime)
                    .put("normalFields.logchecktime", normalFieldsLogchecktime)
                    .put("timestamp", timestamp);

            event.put("_index", "fuyilog_2018.01.31")
                    .put("_type", "kcbp_biz_log")
                    .put("_id", "AWFM8Cw7OxRgjvauX-ga")
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
        JhSimulationData client = new JhSimulationData();

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
