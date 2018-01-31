package com.zork.exceptionhandle.utils

import org.apache.commons.pool2.impl.GenericObjectPoolConfig
import redis.clients.jedis.JedisPool

/**
  * Created by Allen on 2018/1/30.
  * @author 谢森
  */
object RedisUtil {
  private var redishost: String = _
  private var redisPort: Int = _
  private var redisPassword: String = _
  private var redisTimeOut: Int = _

  // 初始化链接池
  def init(host: String, port: Int): Unit = {
    redishost = host
    redisPort = port
  }

  // 定义链接池
  lazy val pool: JedisPool = {
    new JedisPool(new GenericObjectPoolConfig(), redishost, redisPort)
  }
}
