package com.springboot.demo.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by yzn00 on 2019/6/18.
 */
@RestController
@RequestMapping("hello/")
public class Hello {
    @RequestMapping("work")
    @ResponseBody
    public String hello(){
        return "hello world!";
    }

//    @Autowired
//    KafkaProducer kafkaProducer;
//
//    @RequestMapping(value = "/Kafka/send", method = RequestMethod.GET)
//    public void WarnInfo() throws Exception {
//        int count=10;
//        for (int i = 0; i < count; i++) {
//            kafkaProducer.kafkaSend();
//        }

//    }



}
