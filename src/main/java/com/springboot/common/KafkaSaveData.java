package com.springboot.common;

import com.alibaba.fastjson.JSONObject;
import com.springboot.scala.SaveCosumerData;
import org.apache.commons.logging.impl.Log4JLogger;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.serialization.StringDeserializer;
import scala.collection.JavaConverters;
import scala.collection.Seq;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;


public class KafkaSaveData extends Thread {
    private String topic;//主题
    private static Map<String, String> config = new HashMap<String, String>();
    private String table ="web_data_profil";//表名
    private String isDelInsert = "false";
    private String isTrancate = "false";

    public KafkaSaveData(String topic,String table,String isDelInsert,String isTruncate ) {
        super();
        this.topic = topic;
        this.table = table;
        this.isDelInsert = isDelInsert;
        this.isTrancate = isTruncate;
    }

    //创建消费者
    private Consumer<String, String> createConsumer() {

        try {
            if (config.keySet().size() <= 0)
                config.putAll(ReadPropertiesUtils.readConfig("project.properties"));
        } catch (IOException e) {
            System.out.println(e.toString());
        }

        Properties properties = new Properties();
        //声明zookeeper集群链接地址
        properties.put("zookeeper.connect", config.get("zkQuorum"));
        //必须要使用别的组名称， 如果生产者和消费者都在同一组，则不能访问同一组内的topic数据
        //当前消费者的组名称
        properties.put("group.id", "group1");
        properties.put("zookeeper.session.timeout.ms", "10000");
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, config.get("metadata.broker.list"));
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        return new KafkaConsumer<String, String>(properties);

    }

    @Override
    public void run() {
        Logger.getLogger(this.getClass().getName()).setLevel(Level.OFF);
        //创建消费者
        Consumer<String, String> consumer = createConsumer();
        //主题数map
        consumer.subscribe(Arrays.asList(topic));
        System.out.println("消费者对象1：" + consumer);

        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(100);
            //System.out.println(records);
            List<String[]> reds = new ArrayList<>();
            for (ConsumerRecord<String, String> record : records) {
                System.out.println("接收到: " + record.offset() + record.key() + record.value());
                String[] nameValue = {String.valueOf(record.offset()), record.value()};
                try {
                    String[] array = JsonObjectToAttach.getJsonList(record.value(),null);
                    if (array != null) {
                        //表名固定了，根据实际情况修改
                        for(int m=0;m<this.table.split(";").length;m++){
                            //删除当前表数据，保留历史表数据
                            String[] sql =JsonObjectToAttach.getBatchStatement(array, table.split(";")[m], "","",
                                    !(isDelInsert.indexOf(";")>0?isDelInsert.split(";")[m]:isDelInsert).equalsIgnoreCase("false"),new HashMap(),
                                    !(isTrancate.indexOf(";")>0?isTrancate.split(";")[m]:isTrancate).equalsIgnoreCase("false"));
                           if(!reds.contains(sql))
                                reds.add(sql);
                        }

                    }
                } catch (Exception e) {
                    System.out.println(e.toString());
                }
            }
            try {
                Seq<String[]> tmpSeq = JavaConverters.asScalaIteratorConverter(reds.iterator()).asScala().toSeq();
                if (tmpSeq.size() > 0) {
                    SaveCosumerData.main(tmpSeq.toList());
                }
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                System.out.println(e.toString());
            }
        }
    }

    //线程数量
    public static final int NUM_PROCESS = 6;
    public static void main(String[] args) {
        try {
            config.putAll(ReadPropertiesUtils.readConfig("project.properties"));
        } catch (IOException e) {
            System.out.println(e.toString());
        }
        System.out.println(config);
//        new KafkaSaveData("bingfu","web_data_profil").start();

        //取得有效主题
        Map<String,String> topicM = JsonObjectToAttach.getValidProperties("topics",null,null);
        ExecutorService executorService =  Executors.newFixedThreadPool(NUM_PROCESS);
        for(Map.Entry<String, String> m : topicM.entrySet()){
            String []tabAndMark = null;
            if(m.getValue().indexOf(",")>=0){
                tabAndMark =m.getValue().split(",");
            }

            KafkaSaveData kafkaSaveData = new KafkaSaveData(m.getKey(),tabAndMark==null?m.getValue():tabAndMark[0],tabAndMark==null?"false":tabAndMark[1],tabAndMark==null?"false":tabAndMark[2]);
            executorService.execute(kafkaSaveData);
        }
        executorService.shutdown();
    }

}
