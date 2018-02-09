package com.zork.exceptionhandle.utils

import com.alibaba.fastjson.JSON
import org.apache.spark.sql.SparkSession
import org.apache.spark.{SparkConf, SparkContext}

import scala.collection.mutable

/**
  * Created by ThinkPad on 2018/2/7.
  */
object ReadHDFS {
  val appName = this.getClass.getSimpleName
  val kafkaTopics: mutable.MutableList[String] = new mutable.MutableList[String]()

  case class EConf(url: String // resultful接口url
                   , kafkaServer: String
                   , kafkaTopics: mutable.MutableList[String]
                   , kafkaGroupId: String
                   , zookeeperServers: String
                   , redisHost: String
                   , redisPort: Int
                   , bootstrapServers: String
                   , batchDuration: Int // 批次时间，以秒为单位
                  )
  val spark = SparkSession.builder().master("local[1]").appName(appName).getOrCreate()


  def getConf(sparkSession:SparkSession): EConf = {
    val df = sparkSession.read.json("hdfs://zorkdata-1:8020//user/conf/exceptionHandle.conf")
    val json = df.toJSON.collect()
    var temp = ""
    json.foreach { x =>
      temp = x.toString
      x
    }
    val a = JSON.parseObject(temp)
    val url: String = a.get("url").toString

    val kafkaServer: String = JSON.parseObject(a.get("kafka").toString).get("server").toString
    val topicsStr: String = JSON.parseObject(a.get("kafka").toString).get("topics").toString
    val kafkaGroupId: String = JSON.parseObject(a.get("kafka").toString).get("groupId").toString
    val zookeeperServers: String = JSON.parseObject(a.get("zookeeper").toString).get("servers").toString
    val redisHost: String = JSON.parseObject(a.get("redis").toString).get("host").toString
    val redisPort: Int = JSON.parseObject(a.get("redis").toString).get("port").toString.toInt

    val bootstrapServers: String = JSON.parseObject(a.get("bootstrap").toString).get("servers").toString
    val batchDuration: Int = a.get("batchDuration").toString.toInt

    topicsStr.split(",").foreach(topic => {
      kafkaTopics += topic
    })
    new EConf(url, kafkaServer, kafkaTopics, kafkaGroupId, zookeeperServers, redisHost, redisPort, bootstrapServers, batchDuration)
  }

  def main(args: Array[String]): Unit = {

    println("========================")
    val a = getConf(spark)
    println(a.batchDuration + "," + a.bootstrapServers + "," + a.kafkaGroupId + "," + a.kafkaServer + "," + a.kafkaTopics + "," + a.redisHost + "," + a.redisPort + "," + a.url + "," + a.kafkaTopics)
    println(a.kafkaTopics)
    println(a.bootstrapServers)
    println("========================")
  }

}
