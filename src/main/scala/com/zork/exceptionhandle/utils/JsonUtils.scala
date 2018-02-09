package com.zork.exceptionhandle.utils

import com.alibaba.fastjson.JSON
import scala.collection.mutable

/**
  * Json转换工具类
  * @author  谢森
  */
object JsonUtils {
  /**
    * 将json字符串转换成scala中的map集合
    * @param json
    * @return
    */
  def json2Map(json : String) : mutable.HashMap[String,String] = {
    val map : mutable.HashMap[String,String]= mutable.HashMap()
    val jsonObj = JSON.parseObject(json)
    // 获取所有的键
    val jsonKey = jsonObj.keySet()
    val iter = jsonKey.iterator()
    while (iter.hasNext){
      val field = iter.next()
      val  value = jsonObj.get(field).toString
      map.put(field,value)
    }
    map
  }

  /**
    * 将map中的key与value同时打印出来
    * @param map
    */
  def mulPrintMap(map: mutable.HashMap[String,Object]): Unit ={
    map.keys.foreach{
      i => print("key = " + i)
        println(" ,value = " + map(i))
    }
  }

}
