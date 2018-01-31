package com.zork.exceptionhandle.datahandle

import java.io.File
import java.util

import com.alibaba.fastjson.JSON
import com.typesafe.config.ConfigFactory
import com.zork.exceptionhandle.utils.RedisUtil
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.SparkConf
import org.apache.spark.streaming.kafka010.KafkaUtils
import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe

/**
  * 异常处理主类
  *
  * @author 谢森
  */
object ExceptionHandleMain {
  val conf = new SparkConf().setAppName("ExceptionHandleMain").setMaster("local[*]")
  val ssc = new StreamingContext(conf, Seconds(30));

  def main(args: Array[String]): Unit = {
    // 设置kafka参数
    val kafkaParams = Map[String, String](
      "bootstrap.servers" -> "master:9092,slaver1:9092"
      , "key.deserializer" -> "org.apache.kafka.common.serialization.StringDeserializer"
      , "value.deserializer" -> "org.apache.kafka.common.serialization.StringDeserializer"
      , "group.id" -> "scala-kafka"
    )
    // 订阅topic
    val topics = Array("test1")
    val stream = KafkaUtils.createDirectStream[String, String](
      ssc,
      PreferConsistent,
      Subscribe[String, String](topics, kafkaParams)
    )
    val message = stream.map(_.value())

    //    message.print()

    RedisUtil.init("192.168.1.95", 6379)

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
          jedis.select(2)

          key = cid + "|" + time
          map.put("time",time)
          map.put("count",pair._2.toString)
          jedis.hmset(key,map)
          RedisUtil.pool.returnResource(jedis)
        })
    })

    ssc.start()
    ssc.awaitTermination()
  }
}
