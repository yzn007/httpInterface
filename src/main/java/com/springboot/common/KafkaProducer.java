package com.springboot.common;


import kafka.serializer.StringEncoder;
import org.apache.commons.lang.StringUtils;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;


/**
 * Created by DFJX on 2019/6/23.
 */
public class KafkaProducer extends Thread {
    private static Map<String, String> config = new HashMap<String, String>();

    private String topic;
    private String value;
    public KafkaProducer(String topic, String value){
        super();
        this.topic = topic;
        this.value = value;
    }
    
    //创建生产者
    private Producer<Integer, String> createProducer(){
        Properties properties = new Properties();
        try {
            if(config.keySet().size()<=0)
                config.putAll(ReadPropertiesUtils.readConfig("project.properties"));
        } catch (IOException e) {
            System.out.println(e.toString());
        }

        //zookeeper单节点
        properties.put("zookeeper.connect",config.get("zkQuorum"));
        properties.put("serializer.class", StringEncoder.class.getName());
        // 声明kafka集群的 broker

        //kafka单节点
        properties.put("metadata.broker.list", config.get("metadata.broker.list"));
        properties.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        properties.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        properties.put(org.apache.kafka.clients.producer.ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,config.get("metadata.broker.list"));

        return new org.apache.kafka.clients.producer.KafkaProducer(properties);
    }
    
    public void run() {
        BufferedReader br = null;
        try {
            // 创建生产者
            Producer<Integer, String> producer = createProducer();
            if (!StringUtils.isEmpty(value)) {
                System.out.println("生产数据为：" + value);
                producer.send(new ProducerRecord<Integer, String>(topic, value + "\n"));
            }
        }catch (Exception e){
            System.out.print(e.toString());
        }
    }

    public static void main(String[] args) {
        try {
			config.putAll(ReadPropertiesUtils.readConfig("project.properties"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        System.out.println(config);
        // 使用kafka集群中创建好的主题 test
        new KafkaProducer("bingfu","D:/123.sql").start();
    }
}
