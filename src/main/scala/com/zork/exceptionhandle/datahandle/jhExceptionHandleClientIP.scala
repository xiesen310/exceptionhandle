package com.zork.exceptionhandle.datahandle

import java.util

import com.alibaba.fastjson.JSON
import com.zork.exceptionhandle.restful.SendMessage
import com.zork.exceptionhandle.restful.SendMessage.Message
import com.zork.exceptionhandle.utils.{ReadConfig, RedisUtil}
import org.apache.spark.SparkConf
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe
import org.apache.spark.streaming.kafka010.KafkaUtils
import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent
import org.apache.spark.streaming.{Seconds, StreamingContext}

import scala.collection.mutable

/**
  * 异常处理主类
  *
  * @author 谢森
  */
object jhExceptionHandleClientIP {

  val eConf = ReadConfig.getConf()
  val threshold = ReadConfig.getFrequency()

  val redisHost: String = eConf.redisHost
  val redisPort: Int = eConf.redisPort
  val bootstrap: String = eConf.bootstrapServers
  val topicTopo: mutable.MutableList[String] = eConf.kafkaTopics
  val groupId: String = eConf.kafkaGroupId
  val batchDuration: Int = eConf.batchDuration
  //  val threshold: Long = eConf.threshold
  val url: String = eConf.url
  val appName = this.getClass.getSimpleName
  val cip = threshold.cip

  // 设置spark streaming 配置参数
  val conf = new SparkConf().setAppName(appName).setMaster("local[*]")
  val ssc = new StreamingContext(conf, Seconds(batchDuration));

  def change(x: (String, (String, Long)), y: (String, (String, Long))): (String, (String, Long)) = {
    x._1 match {
      case y._1 => {
        val temp = y._2._1 + "," + x._2._1
        val tempArr = temp.split(",").distinct
        (y._1, (tempArr.mkString(","), tempArr.length))
      }
      case _ => x
    }
  }

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
    message.print()
    RedisUtil.init(redisHost, redisPort)

    val partitionSet = message.mapPartitions(itr => {
      val flowList = new scala.collection.mutable.ListBuffer[(String, (String, Long))]
      while (itr.hasNext) {
        // 获取json字符串中normalFields的数据
        val json = JSON.parseObject(itr.next())
        val normalFields = json.get("normalFields").toString

        // 解析normalFields 中的json数据
        val json1 = JSON.parseObject(normalFields)
        // createTime: 时间；lanIp：内网IP；walIp: 外网IP
        val createTime = json1.get("ldate").toString
        val lanIp: String = json1.get("ip").toString
        val walIp: String = json1.get("hostname").toString
        val custId = json1.get("custId")
        // 获取维度的值
        val key: String = createTime + "__" + custId
        flowList.+=((key, (lanIp, 1L)))
      }
      flowList.iterator
    }).reduce(change).foreachRDD(x => {
      val map = new util.HashMap[String, String]
      x.foreach(x => {
        val split = x._1.split("__")
        val cid = split(1)
        val time = split(0)
        val count = x._2._2
        val key = time + "-" + cid
        map.put("time", split(0))
        map.put("ip", x._2._1)
        map.put("count", count.toString)
        // 获取redis对象
        val jedis = RedisUtil.pool.getResource
        jedis.select(5)
        //        if (count > cip) {
        //        SendMessage.send(url + "?cusId=" + cid + "&time=" + time, new Message(cid, count.toString))
        jedis.hmset(key, map)
        //        }
        RedisUtil.pool.returnResource(jedis)
      })
    })
    ssc.start()
    ssc.awaitTermination()
  }
}