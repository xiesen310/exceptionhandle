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
                   , threshold: Long // 阈值
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
    val threshold: Long = jedis.hget("ExceptionHandleConfig", "threshold").toLong
    topoArray.split(",").foreach(topic => {
      kafkaTopics += topic
    })
    new EConf(url, kafkaServer, kafkaTopics, kafkaGroupId, zookeeperServers, redisHost, redisPort, bootstrapServers, batchDuration, threshold
    )
  }

  def main(args: Array[String]): Unit = {
    println(getConf.kafkaTopics)
  }
}
