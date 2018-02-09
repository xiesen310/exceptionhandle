package com.zork.exceptionhandle.datahandle

import java.util

import com.alibaba.fastjson.JSON
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
object RuizhiExceptionHandleRps {

  val eConf = ReadConfig.getConf()
  val threshold = ReadConfig.getFrequency()

  val redisHost: String = eConf.redisHost
  val redisPort: Int = eConf.redisPort
  val bootstrap: String = eConf.bootstrapServers
  val topicTopo: mutable.MutableList[String] = eConf.kafkaTopics
  val groupId: String = eConf.kafkaGroupId
  val batchDuration: Int = eConf.batchDuration
  val url: String = eConf.url
  val appName = this.getClass.getSimpleName

  val rps = threshold.rps


  // 设置spark streaming 配置参数
  val conf = new SparkConf().setAppName(appName).setMaster("local[*]")
  val ssc = new StreamingContext(conf, Seconds(batchDuration));


  def main(args: Array[String]): Unit = {
    // 设置kafka参数
    val kafkaParams = Map[String, String](
      "bootstrap.servers" -> bootstrap
      , "key.deserializer" -> "org.apache.kafka.common.serialization.StringDeserializer"
      , "value.deserializer" -> "org.apache.kafka.common.serialization.StringDeserializer"
      , "group.id" -> groupId
    )

    val stream = KafkaUtils.createDirectStream[String, String](
      ssc,
      PreferConsistent,
      Subscribe[String, String](topicTopo, kafkaParams)
    )

    val message = stream.map(_.value())
    message.print()
    RedisUtil.init(redisHost, redisPort)

    def change2(x: (String, Long, Long, Long), y: (String, Long, Long, Long)): (String, Long, Long, Long) = {
      val funcId = x._1.split("__")(1)
      var lc: Long = 0L
      if (funcId == "410311") lc = x._3 + y._3
      var qc: Long = 0L
      if (funcId == "410312") qc = x._4 + y._4
      (x._1, x._2 + y._2, lc, qc)
    }
    val partitionSet = message.mapPartitions(itr => {
      val flowList = new scala.collection.mutable.ListBuffer[(String, (String, Long, Long, Long))]
      while (itr.hasNext) {
        val json = JSON.parseObject(itr.next())
        val source = json.get("normalFields").toString
        val dimensions = json.get("dimensions").toString

        val json1 = JSON.parseObject(source)
        val createTime = json1.get("logchecktime").toString.split("T")(0) + json1.get("logchecktime").toString.split("T")(1).replace(":","").substring(0,6)
        val funcId = json1.get("FuncID")
        val custId = json1.get("AccountID").toString
        val temp: String = createTime + "__" + funcId
        flowList.+=((custId, (temp, 1L,1L,1L)))
      }
      flowList.iterator
    }).reduceByKey(change2).foreachRDD(x => {
      val map = new util.HashMap[String, String]
      x.foreach(x => {
        // 获取redis对象
        val jedis = RedisUtil.pool.getResource
        jedis.select(9)
        val custId = x._1
        val split = x._2._1.split("__")
        val time = split(0)
        val pv = x._2._2
        val lc = x._2._3
        val qc = x._2._4

        val key = time + "-" + custId
        map.put("pv", pv.toString)
        map.put("lc", lc.toString)
        map.put("qc", qc.toString)
        map.put("time", time)
        map.put("custId", custId)
        if(pv > rps || lc > rps || qc > rps) {
          jedis.hmset(key, map)
        }
        RedisUtil.pool.returnResource(jedis)
      })
    })

    ssc.start()
    ssc.awaitTermination()
  }
}
