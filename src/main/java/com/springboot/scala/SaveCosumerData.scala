package com.springboot.scala

import java.io.FileInputStream
import java.util.Properties
import java.util.logging.{Level, Logger}

import org.apache.jasper.tagplugins.jstl.core.ForEach

import scala.collection.JavaConverters._
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.kafka.KafkaUtils
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.{SparkConf, SparkContext}


/**
  * Created by DFJX on 2019/6/19.
  */
object SaveCosumerData {
  def main(args: List[Array[String]]): Unit = {

    //    val list: java.util.List[Int] = Seq(1,2,3,4).asJava
    //    val buffer: scala.collection.mutable.Buffer[Int] = list.asScala

    // 屏蔽不必要的日志 ,在终端上显示需要的日志
    Logger.getLogger("org.apache.spark").setLevel(Level.OFF)
    Logger.getLogger("org.eclipse.jetty.server").setLevel(Level.OFF)
    Logger.getLogger("org.apache.kafka.clients.consumer").setLevel(Level.OFF)

    val conn = ConnectPoolUtil.getConnection //ConnectPoolUtil是我创建的一个数据库连接池，getConnection是它的一个方法
    try {
      conn.setAutoCommit(false); //设为手动提交
      val stmt = conn.createStatement()
      args.foreach(word => {
        //      stmt.addBatch("truncate log")
        word.foreach(w => {
          stmt.addBatch(w)
        })
      })
      stmt.executeBatch()
      conn.commit()
    } catch {
        case e: Exception => println(e)
          conn.rollback()
    } finally {
      conn.close()
    }


  }
}
