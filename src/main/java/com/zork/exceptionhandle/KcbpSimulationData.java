package com.zork.exceptionhandle;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import org.codehaus.jettison.json.JSONObject;

import java.util.Properties;
import java.util.Random;

/**
 * 模拟数据
 */
public class KcbpSimulationData {
    private KafkaProducer<String, String> producer;
    private Properties properties;

    public KcbpSimulationData() {
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
        Random random = new Random();
        try {
            measures.put("latence", 0);

            normalFields.put("authinfo", "")
                    .put("netaddr", "")
                    .put("bsflag", "")
                    .put("fmillsecond", "-401036973")
                    .put("productcode", "")
                    .put("fundid", "")
                    .put("collecttime", "2018-01-30T15:24:49.876+08:00")
                    .put("inputtype", "")
                    .put("nodeid", "")
                    .put("message", "20180130-152448 Req: NodeId=2008, QueueId=96, MsgId=10000001025D41631E49ADC0, Len=390, Buf=01200000000000000000100000000000000090E2BA930B19KCXP00  GV2gODkBbGg=                          150555  00264000000          076_CA=2.3&_ENDIAN=0&hqtype=TRDSES04_MD402&jsonstr=[{\\\"F_OP_USER\\\":\\\"9999\\\",\\\"F_OP_ROLE\\\":\\\"2\\\",\\\"F_SESSION\\\":\\\"\\\",\\\"F_OP_SITE\\\":\\\"0010a4c6199f\\\",\\\"F_OP_BRANCH\\\":\\\"999\\\",\\\"F_CHANNEL\\\":\\\"0\\\",\\\"NewType\\\":\\\"TRDSES04_MD402\\\",\\\"AmountStatus\\\":\\\"2\\\",\\\"PosAmt\\\":\\\"8569252323\\\",\\\"ThresholdAmount\\\":\\\"10500000000\\\"}]  Ans: NodeId=2008, QueueId=96, MsgId=10000001025D41631E49ADC0, Len=260, Buf=01300000000000000000302008020080200890E2BA930B19KCXP00  GV2gODkBbGg=5A701DC0002008000000016389150555  00134000000          076_CA=2.3&_SYSID=2008&_ENDIAN=0&_RS_1=MESSAGE;3;LEVEL,CODE,MSG;1&_1=0,0,沪港通额度信息及实时状态刷新成功!&_EORS_1=1&_RC=1&_CC=3&_TL=3:1")
                    .put("operway", "")
                    .put("versioninfo", "")
                    .put("beg_logtime", "20180130-"+(random.nextInt(14)+10)+"244" + random.nextInt(3))
                    .put("logchecktime", "2018-01-30T15:24:53.753+08:00")
                    .put("orgid", "")
                    .put("authcode", "")
                    .put("developercode", "")
                    .put("messid", "10000001025D41631E49ADC0")
                    .put("end_logtime", "20180130-152448")
                    .put("smillsecond", "-401036973")
                    .put("featurecode", "")
                    .put("custid", "KCXP" + (random.nextInt(5) + 10));

            dimensions.put("appprogramname", "jzc1-kcbp5_9600")
                    .put("hostname", "jzc1-kcbp5")
                    .put("appsystem", "kcbp")
                    .put("func", "150555")
                    .put("nodeid", "2008");

            event.put("logTypeName", "kcbp_biz_log")
                    .put("offset", 485847022)
                    .put("source", "d:\\kcbp\\log\\run\\20180130\\runlog0.log")
                    .put("indexTime", "2018-01-30T15:24:53.762+08:00")
                    .put("measures", measures)
                    .put("dimensions", dimensions)
                    .put("normalFields", normalFields)
                    .put("timestamp", "2018-01-30T15:24:48.000+08:00");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return event.toString();
    }

    public static void main(String[] args) {
        KcbpSimulationData client = new KcbpSimulationData();

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
