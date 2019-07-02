package com.springboot.common;

import com.springboot.scala.SaveDataToMysql;

/**
 * Created by yzn00 on 2019/6/25.
 */
public class SaveDataToMysqlMain {
    public static void main(String []args){
        SaveDataToMysql saveDataToMysql = new SaveDataToMysql();
        saveDataToMysql.main(args);
//        TestKafka.testConsumer();
    }
}
