package com.springboot.common;


import com.alibaba.druid.sql.visitor.functions.Length;
import com.alibaba.druid.sql.visitor.functions.Substring;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.springboot.scala.SaveCosumerData;
import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.yarn.server.api.protocolrecords.UpdateNodeResourceRequest;
import org.beetl.ext.fn.Print;
import org.dom4j.io.SAXReader;
import org.dom4j.*;
import org.springframework.util.ResourceUtils;
import scala.Equals;
import scala.collection.JavaConverters;
import scala.collection.Seq;
import scala.tools.nsc.transform.patmat.Logic;

import javax.swing.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;

import static java.util.regex.Matcher.quoteReplacement;

/**
 * Created by yzn00 on 2019/6/27.
 */
public class JsonObjectToAttach {

    private static Document document;
    private static  Document tplDocument;
    private static Document preDocument;
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
            JSONObject jsonObject=null;
            Object obj = null;
            try{
                jsonObject = JSONObject.parseObject(jsonString);
                obj = jsonObject.get(dataJsonListNm);
                if (obj==null)
                    obj = jsonObject;
            }catch (Exception ex){
                listS = JSONArray.parseArray(jsonString, String.class);
                String[] a = new String[listS.size()];
                int i = 0;
                for (String r : listS) {
                    a[i++] = r;
                }
                return a;
            }
            return new String[]{obj.toString()};
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
//            System.out.println("Root element :" + document.getRootElement().getName());
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
        String headStr = "";
        String tailStr = "";
        String midStr = "";
        if(strSplit.indexOf(";")>-1){
            int lastCInx = strSplit.lastIndexOf(";");
            int tailInx = strSplit.indexOf(",",lastCInx);
            tailInx = strSplit.indexOf(",",tailInx+1)>-1?strSplit.indexOf(",",tailInx+1):strSplit.length();
            int firstInx = strSplit.indexOf(";");
            int firstC = 0;
            int firstS = 0;
            int i = 0;
            int k = 0;

            String subStr = strSplit;
            while(firstC<firstInx){
                i =subStr.indexOf(",");
                subStr = subStr.substring(i+1>subStr.length()?subStr.length():i+1);
                if(StringUtils.isEmpty(subStr))
                    break;
                if(++k%2==0)//保留前两个
                    firstS = firstC;
                firstC +=i+1;
            }
            headStr = strSplit.substring(0,firstS-1>0?firstS-1:0);
            midStr = strSplit.substring(firstS,tailInx);
            tailStr = strSplit.substring(tailInx+1>strSplit.length()?strSplit.length():tailInx+1,strSplit.length());
            return getJoinString(headStr) + (StringUtils.isEmpty(headStr)?"'":",'") +midStr + (!StringUtils.isEmpty(tailStr)?"'," :"'")+ getJoinString(tailStr);
        }else {

            String[] ars = strSplit.split(",");
            if(ars.length==1)
                return "'" + ars[0] + "'";
            else if(strSplit.length() == strSplit.lastIndexOf(",")+1){//最后一个为空如"2,3,"
                String []argT = new String [ars.length+1];
                for(int j =0;j<argT.length;j++){
                    argT[j] = j<ars.length?ars[j]:"";
                }
                ars = argT;
            }
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
    }

    public static Map<String,String> getValidProperties(String propertyNm,String topicPath,String code,boolean isStatic){
        Map <String,String>m = new HashMap();
        String fileName = "Topic.xml";
        if(!StringUtils.isEmpty(topicPath))
            fileName = topicPath;
//        String path = Thread.currentThread().getClass().getResource("/").getPath() + fileName;


        try{
            String path = ResourceUtils.getURL("classpath:").getPath()+ fileName;
            if(document==null)
                document = parseDom4j(path);
            Element root = document.getRootElement();
            for (Iterator iterator = root.elementIterator(); iterator.hasNext(); ) {
                Element tblEle = (Element) iterator.next();
                if (tblEle.attribute(0).getValue().equals(propertyNm)) {
                    for (Element e : tblEle.elements()) {
                        if(  e.attribute(2).getValue().toLowerCase().equals("true"))
                            if(Boolean.parseBoolean(e.attribute(6).getValue().toString())==isStatic) {
                                if (!StringUtils.isEmpty(code) && code.equals(e.attribute(3).getValue()))//生产者topic取得
                                    m.put(e.attribute(0).getValue(), e.attribute(1).getValue());
                                else if (StringUtils.isEmpty(code))//返回消费者topics
                                    if(!isStatic)
                                        m.put(e.attribute(0).getValue(), e.attribute(1).getValue() + "," + e.attribute(4).getValue() + "," + e.attribute(5).getValue());
                                    else
                                        m.put(e.attribute(0).getValue(), e.attribute(1).getValue() + "," + e.attribute(4).getValue() + "," + e.attribute(5).getValue()+ "," + e.attribute(7).getValue());
                            }
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
     * @param mapKeys
     * @return
     * @throws IOException
     */
    public static String []getPropertyRelation(String cols, String table, String tmpFile,List<Map<String,String>> subTabs,String linkId,Map<String,Map<String,String>> mapKeys ) throws IOException {
        String tm = cols.toLowerCase();
        String [] rets = null;
        List<String> whereStr = new ArrayList<>();
        String spChr = "#";
        String fileName = "TableTpl.xml";
        if (!StringUtils.isEmpty(tmpFile))
            fileName = tmpFile;

        Map<String, String> m = new HashMap();

        try {

//            String path = Thread.currentThread().getClass().getResource("/").getPath() + fileName;
            String path = ResourceUtils.getURL("classpath:").getPath()+fileName;
            if(tplDocument==null)
                tplDocument = parseDom4j(path);
            Element root = tplDocument.getRootElement();
            for (Iterator iterator = root.elementIterator(); iterator.hasNext(); ) {
                Element tblEle = (Element) iterator.next();
                String tableName = tblEle.attribute(0).getValue();
                boolean isContain = false;
                if(tableName.indexOf(table)>-1 && (StringUtils.isEmpty(tableName.substring(tableName.indexOf(table)+table.length(),
                        tableName.indexOf(table)+table.length()+1>tableName.length()?tableName.length():tableName.indexOf(table)+table.length()+1))
                        ||tableName.substring(tableName.indexOf(table)+table.length(),
                        tableName.indexOf(table)+table.length()+1>tableName.length()?tableName.length():tableName.indexOf(table)+table.length()+1).equals(";")))
                    isContain = true;
                if (isContain || tableName.equals(table)) {
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

//            Map<String,Map<String,String>> mapKeys = new HashMap<>();
            if(mapKeys==null)
                mapKeys = new HashMap<String,Map<String,String>>();
            for (Map.Entry<String, String> e : m.entrySet()) {
                String value = e.getValue().toLowerCase();
                if(e.getValue().indexOf(spChr)>-1){
                    whereStr.add(e.getValue().split(spChr)[1]+"{"+ e.getKey() +"}");
                    value = value.split(spChr)[0];
                }
                if(e.getKey().indexOf(".")>0){
                    String key = e.getKey().substring(0,e.getKey().indexOf("."));
                    if(tm.indexOf(key.toLowerCase())>-1 && (tm.substring(tm.indexOf(key.toLowerCase())+key.length(),tm.indexOf(key.toLowerCase())+key.length()+1>tm.length()?tm.length():tm.indexOf(key.toLowerCase())+key.length()+1).equals(",")||
                           StringUtils.isEmpty(tm.substring(tm.indexOf(key.toLowerCase())+key.length(),tm.indexOf(key.toLowerCase())+key.length()+1>tm.length()?tm.length():tm.indexOf(key.toLowerCase())+key.length()+1)))){
                        if(mapKeys.get(key)==null){
                            Map mp = new HashMap();
                            mp.put(e.getKey().substring(key.length()+1,e.getKey().length()),e.getValue());
                            mapKeys.put(key,mp);
                        }else{
                            Map mp = mapKeys.get(key);
                            mp.put(e.getKey().substring(key.length()+1,e.getKey().length()),e.getValue());
                            mapKeys.put(key,mp);
                        }
                    }
                }else if(!linkId.equals(e.getKey()))//已经处理过的linkId存在跳过
//                    tm = tm.toLowerCase().replace(e.getKey().toLowerCase(),value);
                    tm = replace(tm.toLowerCase(),e.getKey().toLowerCase(),value,"");
                else if(tm.lastIndexOf(e.getKey())>0 && linkId.equals(e.getKey())){
                    String tmpS = tm.substring(linkId.length(),tm.length());
//                    tmpS = tmpS.toLowerCase().replace(e.getKey().toLowerCase(),value);
                    tmpS =replace(tmpS.toLowerCase(),e.getKey().toLowerCase(),value,"");
                    tm = tm.substring(0,linkId.length())+tmpS;
                }
            }


            for(Map.Entry<String,Map<String,String>> mp:mapKeys.entrySet()){
                if(tm.indexOf(mp.getKey().toLowerCase())>-1 && (tm.substring(tm.indexOf(mp.getKey().toLowerCase())+ mp.getKey().length(),tm.indexOf(mp.getKey().toLowerCase())+ mp.getKey().length()+1>tm.length()?
                        tm.length():tm.indexOf(mp.getKey().toLowerCase())+ mp.getKey().length()+1).equals(",")||
                        StringUtils.isEmpty(tm.substring(tm.indexOf(mp.getKey().toLowerCase())+ mp.getKey().length(),tm.indexOf(mp.getKey().toLowerCase())+ mp.getKey().length()+1>tm.length()?
                                tm.length():tm.indexOf(mp.getKey().toLowerCase())+ mp.getKey().length()+1)))){
                    Map <String,String>val = mp.getValue();
                    String replacStr = "";
                    for(Map.Entry<String,String> v:val.entrySet()){
                        replacStr += v.getValue() +",";
                    }
                    if(!StringUtils.isEmpty(replacStr)){
                        replacStr = replacStr.substring(0,replacStr.length()-1);
//                        tm = tm.replace(mp.getKey().toLowerCase(),replacStr);
                        tm = replace(tm,mp.getKey().toLowerCase(),replacStr,"");
                    }
                }
            }

//            System.out.println(tm);

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
     * 替换块内字符，防止替换局部字符串
     * @param orignal
     * @param match
     * @param replaceS
     * @param split
     * @return
     */
    public static String replace(String orignal,String match,String replaceS,String split){
        String rtnStr = "";
        String sl = StringUtils.isEmpty(split)?",":split;
        if(orignal.indexOf(sl)>-1){
            String mat[] = orignal.split(sl);
            int len = 0;
            int i = 0;
            for(;i<mat.length;i++){
                if(mat[i].equals(match))
                    break;
                len += mat[i].length();
                if(i<mat.length-1)
                    len += sl.length();
            }
            String head = orignal.substring(0,len);
            String mid = replaceS;
            String tail = orignal.substring(i<mat.length?len+mat[i].length()+sl.length()>orignal.length()?orignal.length():
                    len+mat[i].length()+sl.length():orignal.length());
            rtnStr = !StringUtils.isEmpty(head) || !StringUtils.isEmpty(tail) ? head + (!StringUtils.isEmpty(tail)?mid+sl:mid) + tail:orignal;
            if(head.equals(orignal))
                rtnStr = orignal;
        }else {
            rtnStr = orignal.replace(match,replaceS);
        }
        return rtnStr;
    }

    /**
     * 取得json字符串的key或value
     *
     * @param jsonObject
     * @param isCol
     * @param ky
     * @param noContains
     * @param linkId
     * @param keyMap
     * @return
     */


    public static String getColumsOrValues(JSONObject jsonObject, boolean isCol,Map ky,Map noContains,String linkId,Map<String,Map<String,String>> keyMap) {
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
                        (isCol ?  s.getKey() : ky.get(s.getKey())==null|| (!ky.get(s.getKey()).equals(linkId) && !StringUtils.isEmpty(linkId)
                        )?s.getValue():ky.get(s.getKey())) + ","));

        if(!isCol) {

            for (Map.Entry<String, Map<String, String>> m : keyMap.entrySet()) {
                if (jsonObject.get(m.getKey())!=null) {
                    Map<String, String> val = m.getValue();
                    String values = "";
                    JSONObject josnM = JSONObject.parseObject(jsonObject.get(m.getKey()).toString());
                    for (Map.Entry<String, String> v : val.entrySet()) {
                        String value = "";
                        try{
                            value = josnM.get(v.getKey()).toString();
                        }catch (Exception ee){
                            System.out.println("error【"+v.getKey()+"】:" +josnM);
                            return "";
                        }
                        values += value + ",";
                    }
                    String subStr = jsonObject.get(m.getKey()).toString();
                    String head = a[0].substring(0,a[0].indexOf(subStr)+subStr.length());
                    String tail = a[0].substring(a[0].indexOf(subStr)+subStr.length(),a[0].length());
//                    head = head.replace(subStr,values.substring(0,values.length()-1));
                    head = replace(head,subStr,values.substring(0,values.length()-1),"#");
                    a[0] = head + tail;
                }
            }
        }
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
     * 取得应用库sql语句
     * @param topic
     * @param tmpFile
     * @param jsonArray
     * @return
     */
    public static String[] getMetaSqls(String topic,String tmpFile,String[] jsonArray){
        List<String> att = new ArrayList<>();

        String[] ret = null;
        String fileName = "PreRelations.xml";
        if (!StringUtils.isEmpty(tmpFile))
            fileName = tmpFile;

        try{
            String path = ResourceUtils.getURL("classpath:").getPath()+fileName;
            if(null==preDocument)
                preDocument = parseDom4j(path);
            Element root = preDocument.getRootElement();
            for (Iterator iterator = root.elementIterator(); iterator.hasNext(); ) {
                Element tblEle = (Element) iterator.next();
                String isValid = "";
                try{
                    isValid = tblEle.attributeValue("valid");
                }catch (Exception ee){

                }
                if(!isValid.equalsIgnoreCase("true"))
                    continue;
                //取得主题
                String topicName = tblEle.attribute(1).getValue();
                String []tops = topicName.split(";");
                boolean isExists = false;
                for(String t:tops) {
                    if(t.equalsIgnoreCase(topic)){
                        isExists = true;
                        break;
                    }
                }
                if(isExists){
                    for(int j=0;j<jsonArray.length;j++) {
                        JSONObject jsonObject = null;
                        try{
                            jsonObject = JSONObject.parseObject(jsonArray[j]);
                        }catch (Exception ex){
                            System.out.println(ex.toString());
                        }
                        String isSplit = tblEle.attribute(2).getValue();
                        String sqlStr = quoteReplacement(tblEle.getStringValue().replaceAll("\r","").replaceAll("\n",""));

                        String subString = "";
                        int i = 0;
                        while(sqlStr.indexOf(quoteReplacement("{"))>-1) {
                            subString = sqlStr.substring(sqlStr.indexOf(quoteReplacement("{")) + 1, sqlStr.indexOf(quoteReplacement("}")));
                            String jsonKey = subString;
                            for(Map.Entry<String,Object> m:jsonObject.entrySet()){
                                if(m.getKey().equalsIgnoreCase(jsonKey)) {
                                    jsonKey = m.getKey();
                                    break;
                                }
                            }
                            int jj = 0;
                            if(null!=jsonObject.get(jsonKey)){
                                while(sqlStr.indexOf(quoteReplacement("{"+subString+"}"))>-1) {
                                    sqlStr = sqlStr.replace(quoteReplacement("{" + subString + "}"), getJoinString(jsonObject.get(jsonKey).toString()));
                                    if(jj++>sqlStr.length())//防止替换失败死循环
                                        break;
                                }
                            }
                            i++;
                            if(i>sqlStr.length())//防止替换失败死循环
                                break;
                        }

                        if (isSplit.equalsIgnoreCase("true")) {//需求分割sql
                            String[] sqls = sqlStr.split(";");
                            for (String s : sqls) {
                                att.add(s);
                            }
                        } else {
                            att.add(sqlStr);
                        }
                    }
                }
            }
        }catch (Exception e){
            System.out.println(e.toString());
        }
        if(att.size()>0) {
            ret = new String[att.size()];
            int i=0;
            for(String s :att){
                ret[i++] = s;
            }
        }
        return  ret;
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
            String []rets = getPropertyRelation("", table, null,subTabs,linkId,null);
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
            String column = getColumsOrValues(JSONObject.parseObject(jsons[0]), true,keyWhere,noContainCols,linkId,null);
            Map keyMap = new HashMap<String,Map<String,String>>();
            rets = getPropertyRelation(column, table, null,subTabs,linkId,keyMap);

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



                if(isTruncate && rets.length == 1){
                    String truncateStr = "truncate " + table;
                    //只清空一次
                    if(!att.contains(truncateStr))
                        att.add(truncateStr);

                }
                else if (isModify && rets.length == 1) {
                    String delSt = "delete from " + table + " where 1=1 ";
                    if (!StringUtils.isEmpty(where))
                        delSt += " and " + where;

                    if(!att.contains(delSt))
                        att.add(delSt);
                }
                //取得json value
                String values = getColumsOrValues(JSONObject.parseObject(json), false,keyWhere,noContainCols,linkId,keyMap);
                if(StringUtils.isEmpty(values))
                    continue;
                String insertSt = "insert into " + table + " (" + rets[0] + ") values(" + getJoinString(values) + ")";
                if(!att.contains(insertSt))
                    att.add(insertSt);
                //递归调用

                for (Map.Entry<String,String> e : noContainCols.entrySet()) {
                    String[] bb = getBatchStatement(getJsonList(json, e.getKey()), e.getValue(), null,tmpLink, isModify, keyWhere,isTruncate);
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
                    ret[i++] = t.replaceAll("''","null");
                }
            }
        } catch (Exception e) {
            System.out.println(e.toString());
        }
        //清空条件，防止条件不一样，一直 用同一个条件的情况出现
        keyWhere.clear();
        return ret;
    }

    public static void  main(String args[]){
        String stat = "";
        stat = replace("vehnum_routeid,times_routeid,time,vehnum","time1","times_id",",");
        String  jsonValue ="{\n" +
                "\t\"results\":[\n" +
                "        {\n" +
                "            \n" +
                "\t\t\t\"id\":\"1234557788\",\n" +
                "\t\t\t\"cname\":\"中南海\",\n" +
                "\t\t\t\"ename\":\"yyyy\",\n" +
                "\t\t\t\"nationality\":\"86\",\n" +
                "\t\t\t\"certificateNum\":\"00\",\n" +
                "\t\t\t\"certificateType\":\"12347898778\",\n" +
                "\t\t\t\"gender\":\"f\",\n" +
                "\t\t\t\"institution\":\"重庆市悦来集团投资有限公司\",\n" +
                "\t\t\t\"phone\":\"13818189988\",\n" +
                "\t\t\t\"position\":\"经理\",\n" +
                "\t\t\t\"headUrl\":\"http://localhost\",\t\t\t\n" +
                "\t\t\t\"roleType\":\"01\",\n" +
                "\t\t\t\"vapName\":\"zhangsan\",\n" +
                "\t\t\t\"vapPhone\":\"12345678901\",\n" +
                "\t\t\t\"certificateLevel\":\"1\",\n" +
                "\t\t\t\"isMeeting\":\"0101\",\n" +
                "\t\t\t\"dockingOrgUserName\":\"对接人\",\n" +
                "\t\t\t\"dockingOrgUserphone\":\"23433344\",\n" +
                "\t\t\t\"dockingOrgName\":\"对接人公司\",\n" +
                "\t\t\t\"travel\":\"步行加公交\",\n" +
                "\t\t\t\"carnumber\":\"渝B88888\",\n" +
                "\t\t\t\"datas\":[\n" +
                "\t\t\t\t{\n" +
                "\t\t\t\t\n" +
                "\t\t\t\t\t\"id\":\"3432423434\",\n" +
                "\t\t\t\t\t\"activityName\":\"参加展览\",\n" +
                "\t\t\t\t\t\"joinTime\":\"2019-06-30 09:00\"\n" +
                "\t\t\t\t},\n" +
                "\t\t\t\t{\n" +
                "\t\t\t\t\t\n" +
                "\t\t\t\t\t\"id\":\"897897897\",\n" +
                "\t\t\t\t\t\"activityName\":\"参加展览\",\n" +
                "\t\t\t\t\t\"joinTime\":\"2019-07-01 09:00\"\n" +
                "\t\t\t\t}\n" +
                "\t\t\t\t]\n" +
                "\n" +
                "        }\n" +
                "        ],\n" +
                "        \"tx_code\":\"0101\"\n" +
                "}";
        String tablePre = "GATE_EXPO_AUDI_INFO";
        String [] array = getJsonList(jsonValue,"");
        Map<String, String> config = new HashMap<String, String>();
        try {
            config.putAll(ReadPropertiesUtils.readConfig("project.properties"));
        } catch (IOException e) {
            System.out.println(e.toString());
        }
//        System.out.println(config);
//        new KafkaSaveData("bingfu","web_data_profil").start();

        //取得有效主题
        Map<String,String> topicM = JsonObjectToAttach.getValidProperties("topics",null,null,false);

        for(Map.Entry<String, String> m : topicM.entrySet()){
            String []tabAndMark = null;
            if(m.getValue().indexOf(",")>=0){
                tabAndMark =m.getValue().split(",");
            }

            String table = tabAndMark[0];
            if(table.indexOf(tablePre)<0)
                continue;
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
