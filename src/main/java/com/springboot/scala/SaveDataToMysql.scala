package com.springboot.scala

import java.io.FileInputStream
import java.util.Properties
import java.util.logging.{Level, Logger}

import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.kafka.KafkaUtils
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.{SparkConf, SparkContext}



/**
  * Created by DFJX on 2019/6/19.
  */
class SaveDataToMysql {
  def main(args: Array[String]): Unit = {

    // 屏蔽不必要的日志 ,在终端上显示需要的日志
    Logger.getLogger("org.apache.spark").setLevel(Level.OFF)
    Logger.getLogger("org.eclipse.jetty.server").setLevel(Level.OFF)
    Logger.getLogger("org.apache.kafka.clients.consumer").setLevel(Level.OFF)

    //初始化sparkStreaming
    val conf = new SparkConf().setAppName("SaveDataToMysql").setMaster("local[*]")
    val sc = new SparkContext(conf)
    val ssc = new StreamingContext(sc, Seconds(10))

    val properties = new Properties()
    val path = Thread.currentThread().getContextClassLoader.getResource("project.properties").getPath //文件要放到resource文件夹下
    properties.load(new FileInputStream(path))

    //设置连接Kafka的配置信息
    val zkQuorum  = properties.getProperty("zkQuorum")    //zookeeper集群的IP：port，IP：port，IP：port
    val group = properties.getProperty("group")                 //在consumer.properties配置group.id
    val topics = properties.getProperty("topics")                //选择要连接的producer，它是以topic来区分每个producer的。例如：我这里的创建的topic是zjdata
    val numThreads = 2                    //线程
    val topicpMap = topics.split("\n").map((_,numThreads.toInt)).toMap   //这个是有可能有好几个topic同时提供数据，那么我们要把它用空格分割开，然后映射成(topic,2),再转换成map集合
    ssc.checkpoint("checkpoint1")
    val lines: DStream[String] = KafkaUtils.createStream(ssc,zkQuorum,group,topicpMap).map(_._2)    //创建流

    lines.print()

    //保存到mysql
    lines.map(x=>x.split(",")).foreachRDD(line =>{
      line.foreachPartition(rdd =>{
        val conn = ConnectPoolUtil.getConnection      //ConnectPoolUtil是我创建的一个数据库连接池，getConnection是它的一个方法

        conn.setAutoCommit(false);  //设为手动提交
        val  stmt = conn.createStatement()
        rdd.foreach(word=>{
          stmt.addBatch("insert into log(id, name) values('" + word(0)+"','"+word(1)+word(2)+word(3)+word(4)+word(5) + "')")
        })
        stmt.executeBatch()
        conn.commit()
        conn.close()
      })
    })
    ssc.start()
    ssc.awaitTermination()
  }
}
