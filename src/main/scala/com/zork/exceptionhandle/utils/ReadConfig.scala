package com.zork.exceptionhandle.utils

import scala.collection.mutable

/**
  * 读取配置信息
  */
object ReadConfig {
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

  case class Threshold(
                        highHrequency: Long
                        , cip: Int
                        , sip: Int
                        , rps: Int
                        , timeslot: String
                      )

  /**
    * 获取配置信息
    *
    * @return EConf
    */
  def getConf() = {

    RedisUtil.init("192.168.1.95", 6379)
    // 获取redis对象
    val jedis = RedisUtil.pool.getResource
    jedis.select(15)
    val url: String = jedis.hget("ExceptionHandleConfig", "url")
    val kafkaServer: String = jedis.hget("ExceptionHandleConfig", "kafka.server")
    val topoArray: String = jedis.hget("ExceptionHandleConfig", "kafka.topics")
    val kafkaGroupId: String = jedis.hget("ExceptionHandleConfig", "kafka.groupId")
    val zookeeperServers: String = jedis.hget("ExceptionHandleConfig", "zookeeper.servers")
    val redisHost: String = jedis.hget("ExceptionHandleConfig", "redis.host")
    val redisPort: Int = jedis.hget("ExceptionHandleConfig", "redis.port").toInt
    val bootstrapServers: String = jedis.hget("ExceptionHandleConfig", "bootstrap.servers")
    val batchDuration: Int = jedis.hget("ExceptionHandleConfig", "batchDuration").toInt

    topoArray.split(",").foreach(topic => {
      kafkaTopics += topic
    })

   new  EConf(url, kafkaServer, kafkaTopics, kafkaGroupId, zookeeperServers, redisHost, redisPort, bootstrapServers, batchDuration)
  }

  def getFrequency() = {
    RedisUtil.init("192.168.1.95", 6379)
    // 获取redis对象
    val jedis = RedisUtil.pool.getResource
    jedis.select(15)
    val highHrequency: Long = jedis.hget("ExceptionHandleConfig", "highHrequency").toLong
    val cip: Int = jedis.hget("ExceptionHandleConfig", "cip").toInt
    val sip: Int = jedis.hget("ExceptionHandleConfig", "sip").toInt
    val rps: Int = jedis.hget("ExceptionHandleConfig", "rps").toInt
    val timeslot: String = jedis.hget("ExceptionHandleConfig", "timeslot")
    new Threshold(highHrequency,cip,sip,rps,timeslot)
  }

  def main(args: Array[String]): Unit = {
    val a:Threshold = getFrequency
    println(a.highHrequency)
  }
}
