package com.springboot.httpInterface.job;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.springboot.common.*;
import com.springboot.httpInterface.controller.HttpServiceTest;
import com.springboot.httpInterface.entity.Route;
import com.springboot.httpInterface.entity.RouteVehicle;
import com.springboot.httpInterface.entity.Token;
import com.springboot.httpInterface.services.RouteService;
import com.springboot.httpInterface.services.RouteVehicleService;
import com.springboot.httpInterface.services.TokenService;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StaticJobPer1Min implements BaseJob {

    private static Logger _log = LoggerFactory.getLogger(StaticJobPer1Min.class);

    final static int NUM_PROCESS = 6;
    static Map<String, String> config = new HashMap<String, String>();

    //取得静态表
    static Map<String, String> topicS = new HashMap<>();

    private String jsonStr = "";
    private String table = "BUS_VEHIC_LCTN_MSG";
    //令牌地址
    static String accessUrl = "http://183.66.65.155:9002/api/Token?appid=001&secret=ABCDEFG";

    final String topicName = StaticJobPer1Min.class.getSimpleName();
    final String configName = "project.properties";

    @Autowired
    TokenService tokenService;

    private Token token;

    HttpServiceTest httpServiceTest = null;
    //应用密钥
    final String appSecret = "757929A0F66E4AADB9C187F20C937899";

    private Token getToken() {
//        if(token== null ){
//            List<Token> listT = tokenService.getAllToken();
//            if (listT != null && listT.size() > 0) {
//                token = listT.get(0);
//                if(token.getDateExp()==null){
//                    token.SetDateExp();
//                }
//            }
//        }
//        return token;
        try {

            for (Map.Entry<String, String> m : topicS.entrySet()) {
                if (!m.getKey().equalsIgnoreCase("BUS_ACCESS_TOKEN"))
                    continue;

                String[] tabAndMark = null;
                if (m.getValue().indexOf(",") >= 0) {
                    tabAndMark = m.getValue().split(",");
                }
//                String tableNm = tabAndMark == null ? m.getValue() : tabAndMark[0];
                if (tabAndMark.length < 4) {
                    continue;
                }
                String url = tabAndMark[3];
                if (!StringUtils.isEmpty(url)) {
                    if (url.indexOf("?") < 0)
                        url += "?";
                    if (url.indexOf("appid") < 0)
                        url += "&appid=yuelai_zhibohui";
                    //随机数
                    long nonce = (long) Math.random() * 1000000;
                    //日期整型
                    long dateLong = System.currentTimeMillis();

                    String params = nonce + String.valueOf(dateLong) + appSecret;
                    String hyptApps = BusInfoAccessToken.hmacSha1Encrypt(params, appSecret);
                    url += "&nonce=" + nonce + "&timestamp=" + String.valueOf(dateLong) + "&signature=" + hyptApps;
                    accessUrl = url;
                }
                JSONObject jsonObject = JSONObject.parseObject(httpServiceTest.getJsonData(accessUrl, "utf-8", "RouteId", "", false));
                if (null == jsonObject || !jsonObject.get("errorCode").toString().equals("0"))
                    continue;
                token = new Token();
                token.setAccessToken(jsonObject.get("token").toString());
                token.setDataTm(new Date());
                token.setExpiresIn(Integer.parseInt(jsonObject.get("expiresIn").toString()));
                token.SetDateExp();
                //删除不用的key
                jsonObject.remove("resourceId");
                jsonObject.remove("errorCode");
                jsonObject.remove("errorDesc");
//                if(tabAndMark[3].indexOf("access?")>-1){//获取公交访问令牌
                if (url.indexOf("access") > -1) {//获取公交访问令牌
                    List<String> arrayJson = new ArrayList<>();
                    arrayJson.add(jsonObject.toJSONString());
                    SaveDataStatic saveDataStatic = new SaveDataStatic(m.getKey(), tabAndMark == null ? m.getValue() : tabAndMark[0],
                            tabAndMark == null ? "false" : tabAndMark[1], tabAndMark == null ? "false" : tabAndMark[2], arrayJson);

//                        executorService.execute(saveDataStatic);
                    saveDataStatic.run();
                }
                break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return token;
        }
    }

    public StaticJobPer1Min() {
//        this.jsonStr = josonStr;
//        this.table = table;
        try {
                if (config.size() == 0)
                    config.putAll(ReadPropertiesUtils.readConfig(configName));
            if (topicS.size() == 0)
                //取得静态表
                topicS = JsonObjectToAttach.getValidProperties(topicName, null, null, true);

//            HttpServiceTest httpServiceTest = new HttpServiceTest();
//            this.jsonStr = httpServiceTest.getJsonData("http://localhost/httpService/sendGetData?RayData=CurrTotlCnt", "utf-8");

        } catch (IOException e) {
            System.out.println(e.toString());
        }
    }

    @Autowired
    RouteService routeService;
    @Autowired
    RouteVehicleService routeVehicleService;

    @Override
    public void execute(JobExecutionContext context)
            throws JobExecutionException {
//        _log.error("New Job执行时间: " + new Date());

//
//        ExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        if (httpServiceTest == null)
            httpServiceTest = new HttpServiceTest();
        try {
//    this.jsonStr = httpServiceTest.getJsonData("http://localhost/httpService/sendGetData?RayData=CurrTotlCnt", "utf-8");
            token = getToken();
            Date insertDate = null;
            for (Map.Entry<String, String> m : topicS.entrySet()) {

                String[] tabAndMark = null;
                if (m.getValue().indexOf(",") >= 0) {
                    tabAndMark = m.getValue().split(",");
                }
//                String tableNm = tabAndMark == null ? m.getValue() : tabAndMark[0];
                if (tabAndMark.length < 4) {
                    continue;
                }
                String url = tabAndMark[3];
//                if(tabAndMark[3].indexOf("access?")>-1){//获取公交访问令牌
                if (url.indexOf("access") > -1) {//获取公交访问令牌

                } else if (url.indexOf("getroutelist") > -1) {//获取线路4.2
//                    if(url.indexOf("?")>-1 ) {
//                        url += "&access_token=" + token.getAccessToken();
//                    }else {
//                        url += "?access_token=" + token.getAccessToken();
//                    }

                    Map map = new HashedMap();
                    map.put("table_name", (tabAndMark == null ? m.getValue() : tabAndMark[0]));
                    routeService.deleAllRec(map);
                    List<String> listJson = new ArrayList<>();
                    for (String routeId : JsonObjectToAttach.routeidList) {
                        String urlAad = url + "?RouteId=" + routeId;
                        String getJson = httpServiceTest.sendGet(urlAad, "", token.getAccessToken());
                        JSONObject jsonObject = null;
                        if (Strings.isNotEmpty(getJson))
                            jsonObject = JSON.parseObject(getJson);
                        int k = 0;
                        while (null == jsonObject || !jsonObject.get("errorCode").toString().equals("0")) {
                            //token失效，再次拉取
                            token = getToken();
                            getJson = httpServiceTest.sendGet(urlAad, "", token.getAccessToken());
//                        token = null;
                            if (Strings.isNotEmpty(getJson))
                                jsonObject = JSON.parseObject(getJson);
                            Thread.sleep(1000);
                            if (k++ > 30)
                                break;
                        }
                        if (null != jsonObject.get("errorCode") && jsonObject.get("errorCode").toString().equals("0")) {
                            //删除不用的key
                            jsonObject.remove("resourceId");
                            jsonObject.remove("errorCode");
                            jsonObject.remove("errorDesc");
                            getJson = jsonObject.toJSONString();
                            listJson.add(getJson);
                        } else {
                            System.out.println(url + "[" + getJson + "]");
                        }
                    }
                    SaveDataStatic saveDataStatic = new SaveDataStatic(m.getKey(), tabAndMark == null ? m.getValue() : tabAndMark[0],
                            tabAndMark == null ? "false" : tabAndMark[1], tabAndMark == null ? "false" : tabAndMark[2], listJson);
//                    executorService.execute(saveDataStatic);
                    saveDataStatic.run();
//                        saveDataStatic.run();
                } else if (url.indexOf("access") < 0) {//除令牌外
                    Map map = new HashedMap();
                    //删除站点明细数据
                    if (tabAndMark[0].indexOf("cqyl_pre.BUS_STATION") > -1) {
                        map.put("table_name", "cqyl_pre.BUS_ROUTE_STATION");
                        routeService.deleAllRec(map);
                    } else if (tabAndMark[0].indexOf("cqyl_pre.BUS_ROUTE_DISPT") > -1) {//删除
                        map.put("table_name", "cqyl_pre.BUS_ROUTE_DISPT_DTL");
                        routeService.deleAllRec(map);
                    }

                    map.put("table_name", (tabAndMark == null ? m.getValue() : tabAndMark[0]));
                    routeService.deleAllRec(map);
                    if (url.indexOf("getvehicles") > 0) {//线路车辆4.3
//                        List<Route> listR = routeService.getAllRoute();
//                        int ret = 0;
//                        while(listR==null ||listR.size()==0 ){
//                            Thread.sleep(1000);
//                            listR = routeService.getAllRoute();
//                            if(ret ++ > 180)//三分钟
//                                break;
//                        }
//                        insertDate = listR != null && listR.size()>0 ?listR.get(0).getDataTm():null;

//                        if(url.indexOf("?")>-1 ) {
//                            url += "&access_token=" + token.getAccessToken();
//                        }else {
//                            url += "?access_token=" + token.getAccessToken();
//                        }

                        int k = 0;
                        List<String> listJson = new ArrayList<>();

                        for (String routeId : JsonObjectToAttach.routeidList) {
                            String urlAad = url + "?RouteId=" + routeId;
                            String getJson = httpServiceTest.sendGet(urlAad, "", token.getAccessToken());
                            int j = 0;
                            JSONObject jsonObject = null;
                            if (Strings.isNotEmpty(getJson))
                                jsonObject = JSON.parseObject(getJson);
                            while (null == jsonObject || !jsonObject.get("errorCode").toString().equals("0")) {
                                //token失效
                                token = getToken();
//                                k--;
                                getJson = httpServiceTest.sendGet(urlAad, "", token.getAccessToken());
                                if (Strings.isNotEmpty(getJson))
                                    jsonObject = JSON.parseObject(getJson);
//                                saveDataStatic = new SaveDataStatic(m.getKey(), tabAndMark == null ? m.getValue() : tabAndMark[0],
//                                        tabAndMark == null || k++ > 0 ? "false" : tabAndMark[1], tabAndMark == null ? "false" : tabAndMark[2],
//                                        getJson);
//                            token = null;
                                if (j++ > 30)
                                    break;
                            }
                            if (null != jsonObject.get("errorCode") && jsonObject.get("errorCode").toString().equals("0")) {
                                if (url.indexOf("GetVehicles") > -1) {//线路车辆，需要凭借线路Id，json没有返回
                                    JSONArray jsonArray = null;
                                    try {
                                        jsonArray = JSONObject.parseArray(getJson);
                                        for (Object obj : jsonArray) {
                                            JSONObject jsonObj = (JSONObject) obj;
                                            if (null == jsonObject.get("RouteId")) {
                                                jsonObj.put("RouteId", routeId);
                                            }
                                        }
                                        getJson = JSONObject.toJSONString(jsonArray);
                                    } catch (Exception ee) {
                                        //不是json数组的情况不处理
                                    }
                                }
                                //获取数据
                                String dataNm = "data";
                                if(url.indexOf("getvehicles")>0)
                                    dataNm = "vehicles";
                                else if(url.indexOf("getroutelist")>0)
                                    dataNm = "routes";
                                getJson = jsonObject.get(dataNm).toString();
                                listJson.add(getJson);
                            } else
                                System.out.println(url + "[" + getJson + "]");


                        }

                        SaveDataStatic saveDataStatic = new SaveDataStatic(m.getKey(), tabAndMark == null ? m.getValue() : tabAndMark[0],
                                tabAndMark == null || k++ > 0 ? "false" : tabAndMark[1], tabAndMark == null ? "false" : tabAndMark[2],
                                listJson);
//                        saveDataStatic.run();
//                        executorService.execute(saveDataStatic);
                        saveDataStatic.run();
                    } else {
//                        List<RouteVehicle> routeVehicleList = routeVehicleService.getAllVehicle();
//                        int rt = 0;
//                        while (routeVehicleList == null || routeVehicleList.size() == 0) {
//                            Thread.sleep(1000);
//                            routeVehicleList = routeVehicleService.getAllVehicle();
//                            if (rt++ > 180)//三分钟
//                                break;
//                        }
//
//                        if (url.indexOf("?") > -1) {
//                            url += "&access_token=" + token.getAccessToken();
//                        } else {
//                            url += "?access_token=" + token.getAccessToken();
//                        }
                        int k = 0;
                        List<String> listJson = new ArrayList<>();
//                        for (RouteVehicle rv : routeVehicleList) {
                        for (String routeId : JsonObjectToAttach.routeidList) {
                            //车辆位置4.1 RouteIds ：其它 RouteId
                            String urlAad = "";
                            if(url.indexOf("gettracksites")>0)//站点
                                urlAad = url+ "&RouteIds=" + routeId;
                            else //位置
                                urlAad = url+ (url.indexOf("BusPosition")>0?  "?RouteIds=":"?RouteId=") + routeId;
                            String getJson = httpServiceTest.sendGet(urlAad,"" ,token.getAccessToken());

                            int j = 0;
                            JSONObject jsonObject = null;
                            if (Strings.isNotEmpty(getJson))
                                jsonObject = JSON.parseObject(getJson);
                            while (null == jsonObject || !jsonObject.get("errorCode").toString().equals("0")) {
                                //token失效
                                token = getToken();
//                                k--;
                                getJson = httpServiceTest.sendGet(urlAad,"",token.getAccessToken());
//                                saveDataStatic = new SaveDataStatic(m.getKey(), tabAndMark == null ? m.getValue() : tabAndMark[0],
//                                        tabAndMark == null || k++ > 0 ? "false" : tabAndMark[1], tabAndMark == null ? "false" : tabAndMark[2],
//                                        getJson);
//                            token = null;
                                if (Strings.isNotEmpty(getJson))
                                    jsonObject = JSON.parseObject(getJson);
                                if (j++ > 30)
                                    break;
                            }
                            if (null != jsonObject.get("errorCode") && jsonObject.get("errorCode").toString().equals("0")) {
                                //删除不用的key
                                jsonObject = JsonObjectToAttach.JsonObjDeleteByKey(jsonObject,new ArrayList(){{add("resourceId");
                                    add("errorCode");
                                    add("errorDesc");}});

                                //获取数据
                                String dataNm = "data";
                                if(url.indexOf("getvehicles")>0)
                                    dataNm = "vehicles";
                                else if(url.indexOf("getroutelist")>0)
                                    dataNm = "routes";
                                getJson = jsonObject.get(dataNm).toString();
                                if(StringUtils.isNoneBlank(getJson)){
                                    try {
                                        JSONArray jsonArray = JSONArray.parseArray(getJson);
                                        getJson = JsonObjectToAttach.JsonArrayDeleteByKey(jsonArray,new ArrayList(){{add("status");
                                            add("routeName");
                                            add("departTime");
                                            add("gpsSpeed");
                                            add("azimuth");
                                        }}).toJSONString();
                                    }catch (Exception ee){

                                    }
                                    listJson.add(getJson);
                                }

                            }
                            else
                                System.out.println(url + "[" + getJson + "]");


                        }
                        SaveDataStatic saveDataStatic = new SaveDataStatic(m.getKey(), tabAndMark == null ? m.getValue() : tabAndMark[0],
                                tabAndMark == null || k++ > 0 ? "false" : tabAndMark[1], tabAndMark == null ? "false" : tabAndMark[2],
                                listJson);
                        saveDataStatic.run();
//                        executorService.execute(saveDataStatic);
                    }

                }
                //站点后执行写入更新信息
                if (m.getKey().equalsIgnoreCase("BUS_STATION")) {
                    UpdTblProducter updTblProducter = new UpdTblProducter(tabAndMark == null ? m.getValue() : tabAndMark[0], insertDate);
                    updTblProducter = null;
                }
            }

//            executorService.shutdown();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}