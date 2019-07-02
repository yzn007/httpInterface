package com.springboot.common;


import com.alibaba.druid.sql.visitor.functions.Substring;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.springboot.scala.SaveCosumerData;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.yarn.server.api.protocolrecords.UpdateNodeResourceRequest;
import org.dom4j.io.SAXReader;
import org.dom4j.*;
import scala.collection.JavaConverters;
import scala.collection.Seq;
import scala.tools.nsc.transform.patmat.Logic;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by yzn00 on 2019/6/27.
 */
public class JsonObjectToAttach {

    /**
     * 取得json数组
     * @param jsonString
     * @param dataName
     * @return
     */
    public static String[] getJsonList(String jsonString,String dataName) {
        List<String> listS = null;
        String dataJsonListNm = "results";
        if(!StringUtils.isEmpty(dataName))
           dataJsonListNm = dataName;
        try {
            JSONObject jsonObject = JSONObject.parseObject(jsonString);
            listS = JSONArray.parseArray(jsonObject.get(dataJsonListNm).toString(), String.class);
            String[] a = new String[listS.size()];
            int i = 0;
            for (String r : listS) {
                a[i++] = r;
            }
            return a;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 取得dom4j对象
     * @param fileName
     * @return
     */
    public static Document parseDom4j(String fileName) {
        try {
            // 创建dom4j解析器
            File file = new File(fileName);
            SAXReader reader = new SAXReader();
            Document document = reader.read(file);
            System.out.println("Root element :" + document.getRootElement().getName());
            return document;
        } catch (DocumentException e) {
            System.out.print(e.toString());
        }
        return null;
    }

    /**
     * 格式化值字符串'value1','value2'……
     * @param strSplit
     * @return
     */
    public static String getJoinString(String strSplit) {
        String[] ars = strSplit.split(",");
        StringBuffer statement = new StringBuffer();
        for (int i = 0; i < ars.length; i++) {
            if (i == 0)
                statement.append("'" + ars[i] + "','");
            else if (i < ars.length - 1)
                statement.append(ars[i] + "','");
            else
                statement.append(ars[i] + "'");
        }
        return statement.toString();
    }

    public static Map<String,String> getValidProperties(String propertyNm,String topicPath,String code){
        Map <String,String>m = new HashMap();
        String fileName = "Topic.xml";
        if(!StringUtils.isEmpty(topicPath))
            fileName = topicPath;
        String path = Thread.currentThread().getClass().getResource("/").getPath() + fileName;

        try{
            Document document = parseDom4j(path);
            Element root = document.getRootElement();
            for (Iterator iterator = root.elementIterator(); iterator.hasNext(); ) {
                Element tblEle = (Element) iterator.next();
                if (tblEle.attribute(0).getValue().equals(propertyNm)) {
                    for (Element e : tblEle.elements()) {
                        if(  e.attribute(2).getValue().toLowerCase().equals("true"))
                            if(!StringUtils.isEmpty(code) && code.equals(e.attribute(3).getValue()))//生产者topic取得
                                m.put(e.attribute(0).getValue(), e.attribute(1).getValue());
                            else if(StringUtils.isEmpty(code))//返回消费者topics
                                m.put(e.attribute(0).getValue(), e.attribute(1).getValue()+","+e.attribute(4).getValue()+","+e.attribute(5).getValue());
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return m;
    }

    /**
     * 取得json对应得表字段即where条件从句
     *
     * @param cols
     * @param table
     * @param tmpFile
     * @param subTabs
     * @param linkId
     * @return
     * @throws IOException
     */
    public static String []getPropertyRelation(String cols, String table, String tmpFile,List<Map<String,String>> subTabs,String linkId) throws IOException {
        String tm = cols;
        String [] rets = null;
        List<String> whereStr = new ArrayList<>();
        String spChr = "#";
        String fileName = "TableTpl.xml";
        if (!StringUtils.isEmpty(tmpFile))
            fileName = tmpFile;

        Map<String, String> m = new HashMap();

        try {

            String path = Thread.currentThread().getClass().getResource("/").getPath() + fileName;
            Document document = parseDom4j(path);
            Element root = document.getRootElement();
            for (Iterator iterator = root.elementIterator(); iterator.hasNext(); ) {
                Element tblEle = (Element) iterator.next();
                if (tblEle.attribute(0).getValue().equals(table)) {
                    for (Element e : tblEle.elements()) {
                        //存在子表
                        if(e.attribute(1).getValue().indexOf("[list]")>-1){
                            if(subTabs.size()>0)
                                for(Map mp :subTabs){
                                    if(mp.get(e.attribute(0).getStringValue())!=null)
                                        continue;

                                    Map map = new HashMap();
                                    map.put(e.attribute(0).getStringValue(),e.attribute(1).getValue().substring(e.attribute(1).getValue().indexOf("[list]")+6,
                                            e.attribute(1).getValue().length()));

                                    //关联主表Id
                                    map.put("{linkId}",e.attribute(2).getValue());
                                    subTabs.add(map);
                                }
                            else {
                                Map map = new HashMap();
                                map.put(e.attribute(0).getStringValue(),e.attribute(1).getValue().substring(e.attribute(1).getValue().indexOf("[list]")+6,
                                        e.attribute(1).getValue().length()));
                                //关联主表Id
                                map.put("{linkId}",e.attribute(2).getValue());
                                subTabs.add(map);
                            }

                        }else
                            m.put(e.attribute(1).getValue(), e.attribute(0).getValue()+
                                (!StringUtils.isEmpty(e.getStringValue())?
                                spChr+e.getStringValue():""));
                    }
                }
            }

            for (Map.Entry<String, String> e : m.entrySet()) {
                String value = e.getValue().toLowerCase();
                if(e.getValue().indexOf(spChr)>-1){
                    whereStr.add(e.getValue().split(spChr)[1]+"{"+ e.getKey() +"}");
                    value = value.split(spChr)[0];
                }
                if(!linkId.equals(e.getKey()))//已经处理过的linkId存在跳过
                    tm = tm.toLowerCase().replace(e.getKey().toLowerCase(),value);
                else if(tm.lastIndexOf(e.getKey())>0 && linkId.equals(e.getKey())){
                    String tmpS = tm.substring(linkId.length(),tm.length());
                    tmpS = tmpS.toLowerCase().replace(e.getKey().toLowerCase(),value);
                    tm = tm.substring(0,linkId.length())+tmpS;
                }


            }
            System.out.println(tm);

        } catch (Exception e) {
            e.printStackTrace();
        }
        int j = 0;
        if(StringUtils.isEmpty(tm)) {
            rets = new String[whereStr.size()];
        }
        else {
            rets = new String[whereStr.size() + 1];
            j = 1;
            rets[0] = tm;
        }

        for( ;j<rets.length;j++){
            if(StringUtils.isEmpty(tm))
                rets [j] = whereStr.get(j);
            else
                rets [j] = whereStr.get(j-1);
        }
        return rets;
    }

    /**
     * 取得json字符串的key或value
     *
     * @param jsonObject
     * @param isCol
     * @param ky
     * @param noContains
     * @param linkId
     * @return
     */


    public static String getColumsOrValues(JSONObject jsonObject, boolean isCol,Map ky,Map noContains,String linkId) {
        String[] a = new String[1];

        if(!StringUtils.isEmpty(linkId))
            if(isCol)
                a[0] = linkId+",";
            else
                a[0] = ky.get(linkId)+",";
        if(noContains.get("linkId")!= null && StringUtils.isEmpty(linkId))//设置linkId为空
            noContains.put("linkId","");
        jsonObject.entrySet().iterator().forEachRemaining(s ->
                a[0] = (!StringUtils.isEmpty(a[0]) ? a[0] : "") + (noContains.get(s.getKey().toString())!=null?"":
                        (isCol ?  s.getKey() : ky.get(s.getKey())==null?s.getValue():ky.get(s.getKey())) + ","));


        return a[0].substring(0, a[0].length() - 1);
    }


    /**
     * 取得嵌套json得表名
     * @param jsonObject
     * @return
     */
    public static String getDataListTableNm(JSONObject jsonObject) {
        String[] a = new String[1];

        jsonObject.entrySet().iterator().forEachRemaining(s ->
                a[0] = (a[0] != null ? a[0] : "") + (s.getValue().toString().indexOf("[list]")>-1?
                        s.getValue().toString().substring(s.getValue().toString().indexOf("[list]")+6,s.getValue().toString().length()-1)+",":""));

        return a[0].substring(0, a[0].length() - 1);
    }


    /**
     * 根据keys取得值连接串
     * @param jsonObject
     * @param keys
     * @param delimet
     * @return
     */
    public static String getValuesByKeys(JSONObject jsonObject,String keys,String delimet) {
        String retVals  = "";
        String splitChr = "\\+";
        if(!StringUtils.isEmpty(delimet)){
            splitChr = delimet;
        }
        String[] ks = keys.split(splitChr);
        for(int k=0;k<ks.length;k++){
            retVals += jsonObject.get( ks[k].trim());
        }
        return retVals;
    }

    /**
     * 生成插入或删除语句
     *
     * @param jsons
     * @param table
     * @param where
     * @param linkId
     * @param isModify
     * @param isTruncate
     * @return
     */
    public static String[] getBatchStatement(String[] jsons, String table,String where, String linkId, boolean isModify,Map keyWhere,boolean isTruncate) {
        List<String> att = new ArrayList<>();
//        Map keyWhere = new HashMap();
        String[] ret = null;
        if (jsons == null || jsons.length <= 0)
            return null;
        try {
            //映射数据库字段和where条件
            List<Map<String,String>> subTabs = new ArrayList<>();
            //取得子表
            String []rets = getPropertyRelation("", table, null,subTabs,linkId);
            String tmpLink = "";
            Map <String,String>noContainCols = new HashMap();
            for(Map<String,String> t:subTabs) {
                for (Map.Entry<String, String> e : t.entrySet()) {
                    if(!e.getKey().equals("{linkId}"))
                        noContainCols.put(e.getKey(),e.getValue());
                    else
                        tmpLink = e.getValue();
                }
            }
            //取得json key
            String column = getColumsOrValues(JSONObject.parseObject(jsons[0]), true,keyWhere,noContainCols,linkId);
            rets = getPropertyRelation(column, table, null,subTabs,linkId);

            for (String json : jsons) {

                for(int k=1;k<rets.length;k++){
                    String [] vals = rets[k].split("=");
                    String valByKeys = "";
                    if(keyWhere.get(vals[1].substring(0,vals[1].indexOf("{")).trim())==null){
                        valByKeys = getValuesByKeys(JSONObject.parseObject(json),vals[1].substring(0,vals[1].indexOf("{")),"");
                        keyWhere.put(vals[1].substring(vals[1].indexOf("{")+1,vals[1].indexOf("}")),valByKeys);
                    }else{
                        valByKeys = keyWhere.get(vals[1].substring(0,vals[1].indexOf("{")).trim()).toString();
                    }
                    if(isTruncate){
                        String truncateStr = "truncate " + table;
                        //只清空一次
                        if(!att.contains(truncateStr))
                            att.add(truncateStr);

                    }else if (isModify){
                        String delSt = "delete from " + table + " where 1=1 ";
                        if (!StringUtils.isEmpty(where))
                            delSt += " and " + where;

                        delSt += " and " + vals[0] + " = '" +  valByKeys + "'";
                        if(!att.contains(delSt))
                            att.add(delSt);
                    }
                }



//                if(isTruncate){
//                    String truncateStr = "truncate " + table;
//                    //只清空一次
//                    if(!att.contains(truncateStr))
//                        att.add(truncateStr);
//
//                }
//                else if (isModify) {
//                    String delSt = "delete from " + table + " where 1=1 ";
//                    if (!StringUtils.isEmpty(where))
//                        delSt += " and " + where;

//                    for(int k=1;k<rets.length;k++){
//                        String [] vals = rets[k].split("=");
//                        String valByKeys = "";
//                        if(keyWhere.get(vals[1].substring(0,vals[1].indexOf("{")).trim())==null){
//                            valByKeys = getValuesByKeys(JSONObject.parseObject(json),vals[1].substring(0,vals[1].indexOf("{")),"");
//                            keyWhere.put(vals[1].substring(vals[1].indexOf("{")+1,vals[1].indexOf("}")),valByKeys);
//                        }else{
//                            valByKeys = keyWhere.get(vals[1].substring(0,vals[1].indexOf("{")).trim()).toString();
//                        }
//                        delSt += " and " + vals[0] + " = '" +  valByKeys + "'";
//
//                    }
//                    if(!att.contains(delSt))
//                        att.add(delSt);
//                }
                //取得json value
                String values = getColumsOrValues(JSONObject.parseObject(json), false,keyWhere,noContainCols,linkId);
                String insertSt = "insert into " + table + " (" + rets[0] + ") values(" + getJoinString(values) + ")";
                if(!att.contains(insertSt))
                    att.add(insertSt);
                //递归调用

                for (Map.Entry<String,String> e : noContainCols.entrySet()) {
                    String[] bb = getBatchStatement(getJsonList(json, e.getKey()), e.getValue(), null,tmpLink, isModify, keyWhere,false);
                    if (bb == null)
                        return null;
                    else
                        for (int k = 0; k < bb.length; k++) {
                            if(!att.contains(bb[k]))
                                att.add(bb[k]);
                        }

                }

            }
            if (att.size() > 0) {
                ret = new String[att.size()];
                int i = 0;
                for (String t : att) {
                    ret[i++] = t;
                }
            }
        } catch (Exception e) {
            System.out.println(e.toString());
        }
        return ret;
    }

    public static void  main(String args[]){
        String  jsonValue ="{\n" +
                "  \"tx_code\": \"0304\",\n" +
                "  \"results\": [\n" +
                "    {\n" +
                "      \"Resv_ID\": \"1\",\n" +
                "      \"Plat_No\": \"渝B9878\",\n" +
                "      \"Resv_Park_Lot_ID\": \"1\",\n" +
                "      \"Resv_Park_Lot_Regn_ID\": \"1\",\n" +
                "      \"Resv_Arrv_Tm\": \"2019-07-02\",\n" +
                "      \"Resv_Stat\": \"2\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";
        String [] array = getJsonList(jsonValue,"");
        Map<String, String> config = new HashMap<String, String>();
        try {
            config.putAll(ReadPropertiesUtils.readConfig("project.properties"));
        } catch (IOException e) {
            System.out.println(e.toString());
        }
        System.out.println(config);
//        new KafkaSaveData("bingfu","web_data_profil").start();

        //取得有效主题
        Map<String,String> topicM = JsonObjectToAttach.getValidProperties("topics",null,null);

        for(Map.Entry<String, String> m : topicM.entrySet()){
            String []tabAndMark = null;
            if(m.getValue().indexOf(",")>=0){
                tabAndMark =m.getValue().split(",");
            }

            String table = tabAndMark[0];
            String isDelInsert = tabAndMark[1];
            String isTrancate = tabAndMark[2];
            List<String[]> reds = new ArrayList<>();

            //表名固定了，根据实际情况修改
            for(int k=0;k<table.split(";").length;k++){
                //删除当前表数据，保留历史表数据
                String[] sql = JsonObjectToAttach.getBatchStatement(array, table.split(";")[k], "","",
                        !(isDelInsert.indexOf(";")>0?isDelInsert.split(";")[k]:isDelInsert).equalsIgnoreCase("false"),new HashMap(),
                        !(isTrancate.indexOf(";")>0?isTrancate.split(";")[k]:isTrancate).equalsIgnoreCase("false"));
                if(!reds.contains(sql))
                    reds.add(sql);
            }

            try {
                Seq<String[]> tmpSeq = JavaConverters.asScalaIteratorConverter(reds.iterator()).asScala().toSeq();
                if (tmpSeq.size() > 0) {
                    SaveCosumerData.main(tmpSeq.toList());
                }
            } catch (Exception e) {
                System.out.println(e.toString());
            }

        }
    }

}
