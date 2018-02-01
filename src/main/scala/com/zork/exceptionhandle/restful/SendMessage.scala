package com.zork.exceptionhandle.restful

import java.text.SimpleDateFormat
import java.util
import java.util.{Calendar, Date}

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.serializer.SerializerFeature
import org.apache.http.client.methods.{CloseableHttpResponse, HttpPost}
import org.apache.http.client.protocol.HttpClientContext
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClients
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager

/**
  * Created by Allen on 2018/1/31.
  * @author 谢森
  */
object SendMessage extends Serializable {

  case class Message(cid: String, value: String)

  private val cm = new PoolingHttpClientConnectionManager()
  private val httpClient = HttpClients.custom().setConnectionManager(cm).build()
  private val context = HttpClientContext.create()

  def send(url: String, message: Message): Unit = {
    val httppost = new HttpPost(url)
    val dataList: util.ArrayList[Message] = new util.ArrayList[Message]()
    dataList.add(new Message(message.cid, message.value))
    val json = JSON.toJSONString(dataList, SerializerFeature.DisableCircularReferenceDetect)
    val entity = new StringEntity(json, "utf-8")
    entity.setContentEncoding("UTF-8")
    entity.setContentType("application/json")
    entity.setChunked(true)
    httppost.setEntity(entity)
    try {
      val response: CloseableHttpResponse = httpClient.execute(httppost, context)
      val code = response.getStatusLine.getStatusCode
      if (code > 301 || code == 0) {
        println("发送数据失败， code：" + code + "丢失数据为： " + json)
      }
      response.close()
    } catch {
      case e: Exception => {
        println("发送数据丢失，丢失数据为： " + json)
        e.printStackTrace()
      }
    }
  }

  def getTime: String = {
    val calendar = Calendar.getInstance()
    val dateFormat = new SimpleDateFormat("[yyyy.MM.dd HH:mm:ss] ")
    calendar.setTime(new Date())
    dateFormat.format(calendar.getTime)
  }
}
