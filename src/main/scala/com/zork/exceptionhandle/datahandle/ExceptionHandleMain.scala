package com.zork.exceptionhandle.datahandle

import java.io.File
import java.util

import com.zork.exceptionhandle.restful.SendMessage
import com.alibaba.fastjson.JSON
import com.typesafe.config.{Config, ConfigFactory}
import com.zork.exceptionhandle.restful.SendMessage.Message
import com.zork.exceptionhandle.utils.{ReadConfig, RedisUtil}
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.SparkConf
import org.apache.spark.streaming.kafka010.KafkaUtils
import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe

import scala.collection.mutable

/**
  * 异常处理主类
  *
  * @author 谢森
  */
object ExceptionHandleMain {

  val eConf = ReadConfig.getConf()


  val redisHost: String  =  eConf.redisHost
  val redisPort: Int = eConf.redisPort
  val bootstrap: String = eConf.bootstrapServers
  val topicTopo: mutable.MutableList[String] = eConf.kafkaTopics
  val groupId: String = eConf.kafkaGroupId
  val batchDuration: Int = eConf.batchDuration
  val threshold: Long = eConf.threshold
  val url: String = eConf.url

  // 设置spark streaming 配置参数
  val conf = new SparkConf().setAppName("ExceptionHandleMain").setMaster("local[*]")
  val ssc = new StreamingContext(conf, Seconds(batchDuration));

  def main(args: Array[String]): Unit = {
    // 设置kafka参数
    val kafkaParams = Map[String, String](
      "bootstrap.servers" -> bootstrap
      , "key.deserializer" -> "org.apache.kafka.common.serialization.StringDeserializer"
      , "value.deserializer" -> "org.apache.kafka.common.serialization.StringDeserializer"
      , "group.id" -> groupId
    )
    // 订阅topic
    //    val topics = Array("test1")
    val stream = KafkaUtils.createDirectStream[String, String](
      ssc,
      PreferConsistent,
      Subscribe[String, String](topicTopo, kafkaParams)
    )
    val message = stream.map(_.value())
    //    message.print()
    RedisUtil.init(redisHost, redisPort)

    val partitionSet = message.mapPartitions(itr => {
      val flowList = new scala.collection.mutable.ListBuffer[(String, Long)]
      while (itr.hasNext) {
        // 获取json字符串中normalFields的数据
        val json = JSON.parseObject(itr.next())
        val normalFields = json.get("normalFields").toString
        // 解析normalFields 中的json数据
        val json1 = JSON.parseObject(normalFields)
        // custid: 客户id； beg_logtime: 日志时间
        val custid = json1.get("custid").toString
        val beg_logtime = json1.get("beg_logtime").toString
        // 获取维度的值
        val key = custid + "__" + beg_logtime
        flowList.+=((key, 1L))
      }
      flowList.iterator
    }).reduceByKey(_ + _).foreachRDD(x => {

      val map = new util.HashMap[String, String]
      var key = ""
      x.foreach(pair => {
        val keys = pair._1.split("__")
        val cid = keys(0)
        val time = keys(1)

        // 获取redis对象
        val jedis = RedisUtil.pool.getResource
        jedis.select(1)
        key = cid + "|" + time
        map.put("time", time)
        val count = pair._2
        map.put("count", count.toString)
        // 获取时间区间
        val timeSplit = time.split("-")
        val timeSection = (timeSplit(1).charAt(0).toString + timeSplit(1).charAt(1).toString).toInt

        // 异常交易时间段
//        if(timeSection > 15 && timeSection < 20) {
          jedis.set(cid, count.toString)
//        }

        // 高频交易
//        if (count > threshold) {
//          SendMessage.send(url + "?cusId=" + cid + "&time=" + time, new Message(cid, count.toString))
//          jedis.set(cid, count.toString)
//        }



        //          jedis.hmset(key,map)
        RedisUtil.pool.returnResource(jedis)
      })
    })
    ssc.start()
    ssc.awaitTermination()
  }
}
