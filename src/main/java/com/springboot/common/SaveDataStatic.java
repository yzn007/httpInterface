package com.springboot.common;

import com.springboot.scala.SaveCosumerData;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.serialization.StringDeserializer;
import scala.Dynamic;
import scala.collection.JavaConverters;
import scala.collection.Seq;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;


public class SaveDataStatic extends Thread {
    private String topic;//主题
    private static Map<String, String> config = new HashMap<String, String>();
    private String table = "web_data_profil";//表名
    private String isDelInsert = "false";
    private String isTrancate = "false";
    private String jsonString = "";

    public SaveDataStatic(String topic, String table, String isDelInsert, String isTruncate, String jsonString) {
        super();
        this.topic = topic;
        this.table = table;
        this.isDelInsert = isDelInsert;
        this.isTrancate = isTruncate;
        this.jsonString = jsonString;
    }


    @Override
    public void run() {
        Logger.getLogger(this.getClass().getName()).setLevel(Level.OFF);

        //System.out.println(records);
        List<String[]> reds = new ArrayList<>();
        List<String[]> listDynamic = new ArrayList<>();
        try {
            String[] array = JsonObjectToAttach.getJsonList(jsonString, null);
            if (array != null) {
                //表名固定了，根据实际情况修改
                for (int m = 0; m < this.table.split(";").length; m++) {
                    //删除当前表数据，保留历史表数据
                    String[] sql = JsonObjectToAttach.getBatchStatement(array, table.split(";")[m], "", "",
                            !(isDelInsert.indexOf(";") > 0 ? isDelInsert.split(";")[m] : isDelInsert).equalsIgnoreCase("false"), new HashMap(),
                            !(isTrancate.indexOf(";") > 0 ? isTrancate.split(";")[m] : isTrancate).equalsIgnoreCase("false"));

                        if (!reds.contains(sql) && sql!=null)
                            reds.add(sql);

                    String []strDynamic = JsonObjectToAttach.getMetaSqls(topic,null,array);
                    if(!listDynamic.contains(strDynamic)&& null != strDynamic)
                        listDynamic.add(strDynamic);
                }
            }
        } catch (Exception e) {
            System.out.println(e.toString());
        }

        try {
            Seq<String[]> tmpSeq = JavaConverters.asScalaIteratorConverter(reds.iterator()).asScala().toSeq();
            if (tmpSeq.size() > 0) {
                SaveCosumerData.main(tmpSeq.toList());
            }
//            Thread.sleep(50);
            tmpSeq = JavaConverters.asScalaIteratorConverter(listDynamic.iterator()).asScala().toSeq();
            if (tmpSeq.size() > 0) {
                SaveCosumerData.main(tmpSeq.toList());
            }

        } catch (Exception e) {
            System.out.println(e.toString());
//            System.out.println(reds.get(0).toString());
//            System.out.println(listDynamic.get(0));
        }
//        }
    }

    //线程数量
    public static final int NUM_PROCESS = 6;

    public static void main(String[] args) {
        try {
            config.putAll(ReadPropertiesUtils.readConfig("project.properties"));
        } catch (IOException e) {
            System.out.println(e.toString());
        }
//        System.out.println(config);
//        new KafkaSaveData("bingfu","web_data_profil").start();

        String jsonString = "{\n" +
                "  \"tx_code\": \"0102\",\n" +
                "  \"results\": [\n" +
                "    {\n" +
                "      \"activityName\": \"参加活动1\",\n" +
                "      \"cardNo\": \"渝B03789\",\n" +
                "      \"barCode\": \"1\",\n" +
                "      \"checkTime\": \"2018-06-01 10:11:00\",\n" +
                "      \"accessPoint\": \"北3\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        //取得静态数据表
        Map<String, String> topicM = JsonObjectToAttach.getValidProperties("topics", null, null,true);
        ExecutorService executorService = Executors.newFixedThreadPool(NUM_PROCESS);
        for (Map.Entry<String, String> m : topicM.entrySet()) {
            String[] tabAndMark = null;
            if (m.getValue().indexOf(",") >= 0) {
                tabAndMark = m.getValue().split(",");
            }

            SaveDataStatic saveDataStatic = new SaveDataStatic(m.getKey(), tabAndMark == null ? m.getValue() : tabAndMark[0],
                    tabAndMark == null ? "false" : tabAndMark[1], tabAndMark == null ? "false" : tabAndMark[2],jsonString);
            executorService.execute(saveDataStatic);
        }
        executorService.shutdown();
    }

}
