package com.zork.exceptionhandle.redis

import java.util

import com.zork.exceptionhandle.utils.RedisUtil

/**
  * 修改异常处理配置信息
  */
object ExceptionConfig {

  def main(args: Array[String]): Unit = {
    RedisUtil.init("192.168.1.95", 6379)
    // 获取redis对象
    val jedis = RedisUtil.pool.getResource
    jedis.select(15)

    val key = "ExceptionHandleConfig"
    val value = new util.HashMap[String, String]
    value.put("kafka.server", "192.168.175.100")
    value.put("kafka.topics", "test")
    value.put("kafka.groupId", "ExceptionHandle")

    value.put("redis.host", "192.168.1.95")
    value.put("redis.port", "6379")
    value.put("zookeeper.servers", "zorkdata-1:2181,zorkdata-2:2181,zorkdata-3:2181")
    value.put("bootstrap.servers", "zorkdata-1:9092,zorkdata-2:9092,zorkdata-3:9092")
    value.put("url", "http://192.168.30.31:8080/tradeanalyze/queryExeception/customerOccursExcepetion.do")
    value.put("batchDuration", "10")
    value.put("threshold", "20")

    jedis.hmset(key, value)
    RedisUtil.pool.returnResource(jedis)
  }

}
