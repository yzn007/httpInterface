package com.springboot.httpInterface.job;

import com.alibaba.fastjson.JSONObject;
import com.springboot.common.JsonObjectToAttach;
import com.springboot.common.ReadPropertiesUtils;
import com.springboot.common.SaveDataStatic;
import com.springboot.httpInterface.controller.HttpServiceTest;
import com.springboot.httpInterface.entity.Route;
import com.springboot.httpInterface.entity.RouteVehicle;
import com.springboot.httpInterface.entity.Token;
import com.springboot.httpInterface.services.RouteService;
import com.springboot.httpInterface.services.RouteVehicleService;
import com.springboot.httpInterface.services.TokenService;
import org.apache.commons.lang3.StringUtils;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StaticJob implements BaseJob {

    private static Logger _log = LoggerFactory.getLogger(StaticJob.class);

    final static int NUM_PROCESS = 6;
    static Map<String, String> config = new HashMap<String, String>();

    //取得静态表
    static Map<String, String> topicS = new HashMap<>();

    private String jsonStr = "";
    private String table = "BUS_VEHIC_LCTN_MSG";
    //令牌地址
    static String accessUrl = "http://39.108.107.62:8087/api/Token?appid=001&secret=ABCDEFG";

    @Autowired
    TokenService tokenService;

    private Token token;

    HttpServiceTest httpServiceTest = null;


    private Token getToken(){
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
                if(!m.getKey().equalsIgnoreCase("BUS_ACCESS_TOKEN"))
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
                if(!StringUtils.isEmpty(url))
                    accessUrl = url;
                JSONObject jsonObject = JSONObject.parseObject(httpServiceTest.getJsonData(accessUrl, "utf-8", "RouteId",""));
                token = new Token();
                token.setAccessToken(jsonObject.get("access_token").toString());
                token.setDataTm(new Date());
                token.setExpiresIn(Integer.parseInt(jsonObject.get("expires_in").toString()));
                token.SetDateExp();

//                if(tabAndMark[3].indexOf("Token?")>-1){//获取公交访问令牌
                if (url.indexOf("Token") > -1) {//获取公交访问令牌
                    SaveDataStatic saveDataStatic = new SaveDataStatic(m.getKey(), tabAndMark == null ? m.getValue() : tabAndMark[0],
                            tabAndMark == null ? "false" : tabAndMark[1], tabAndMark == null ? "false" : tabAndMark[2], jsonObject.toJSONString());

//                        executorService.execute(saveDataStatic);
                    saveDataStatic.run();
                }
                break;
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            return token;
        }
    }

    public StaticJob() {
//        this.jsonStr = josonStr;
//        this.table = table;
        try {
            if (config.size() == 0)
                config.putAll(ReadPropertiesUtils.readConfig("project.properties"));
            if (topicS.size() == 0)
                //取得静态表
                topicS = JsonObjectToAttach.getValidProperties("topics", null, null, true);

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
//        ExecutorService executorService = Executors.newFixedThreadPool(NUM_PROCESS);
        if(httpServiceTest==null)
            httpServiceTest = new HttpServiceTest();
        try {
//    this.jsonStr = httpServiceTest.getJsonData("http://localhost/httpService/sendGetData?RayData=CurrTotlCnt", "utf-8");
            token = getToken();
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
//                if(tabAndMark[3].indexOf("Token?")>-1){//获取公交访问令牌
                if(url.indexOf("Token")>-1 ){//获取公交访问令牌

                    if(token==null|| token.getDateExp()==null ||token.getDateExp().before(new Date())) {

//                        JSONObject jsonObject = JSONObject.parseObject(httpServiceTest.getJsonData(url, "utf-8",""));
//                        token = new Token();
//                        token.setAccessToken(jsonObject.get("access_token").toString());
//                        token.setDataTm(new Date());
//                        token.setExpiresIn(Integer.parseInt(jsonObject.get("expires_in").toString()));
//                        token.SetDateExp();
//
//                        SaveDataStatic saveDataStatic = new SaveDataStatic(m.getKey(), tabAndMark == null ? m.getValue() : tabAndMark[0],
//                                tabAndMark == null ? "false" : tabAndMark[1], tabAndMark == null ? "false" : tabAndMark[2],jsonObject.toJSONString());
//
////                        executorService.execute(saveDataStatic);
//                        saveDataStatic.run();
//                        accessUrl = url;
                    }
                }else if(url.indexOf("GetRoutes")>-1){//获取所有线路
                    if(url.indexOf("?")>-1 ) {
                        url += "&access_token=" + token.getAccessToken();
                    }else {
                        url += "?access_token=" + token.getAccessToken();
                    }


                    String getJson = httpServiceTest.getJsonData(url, "utf-8","RouteId","");

                    SaveDataStatic saveDataStatic = new SaveDataStatic(m.getKey(), tabAndMark == null ? m.getValue() : tabAndMark[0],
                            tabAndMark == null ? "false" : tabAndMark[1], tabAndMark == null ? "false" : tabAndMark[2],getJson           );

                    int k = 0;
                    while(getJson.indexOf("errcode")>-1) {
                        //token失效，再次拉取
                        token = getToken();
                        getJson = httpServiceTest.getJsonData(url.substring(0,url.indexOf("access_token"))+"access_token="+token.getAccessToken(), "utf-8","RouteId","");
                        saveDataStatic = new SaveDataStatic(m.getKey(), tabAndMark == null ? m.getValue() : tabAndMark[0],
                                tabAndMark == null ? "false" : tabAndMark[1], tabAndMark == null ? "false" : tabAndMark[2],getJson );
//                        token = null;
                        if(k++>30)
                            break;
                    }

//                    executorService.execute(saveDataStatic);
                    saveDataStatic.run();
                }
                else if(url.indexOf("Token")<0){//除令牌外
                    if(url.indexOf("Vehicle/Info")<0){//车辆信息在线路信息后取得
                        List<Route> listR = routeService.getAllRoute();

                        if(url.indexOf("?")>-1 ) {
                            url += "&access_token=" + token.getAccessToken();
                        }else {
                            url += "?access_token=" + token.getAccessToken();
                        }
                        int k = 0;
                        for (Route r : listR) {
                            String getJson = httpServiceTest.getJsonData(url, "utf-8","RouteId",r.getId());
                            SaveDataStatic saveDataStatic = new SaveDataStatic(m.getKey(), tabAndMark == null ? m.getValue() : tabAndMark[0],
                                    tabAndMark == null || k++ > 0 ? "false" : tabAndMark[1], tabAndMark == null ? "false" : tabAndMark[2],
                                    getJson);
                            int j = 0;
                            while(getJson.indexOf("errcode")>-1) {
                                //token失效
                                token = getToken();
                                k--;
                                getJson = httpServiceTest.getJsonData(url.substring(0,url.indexOf("access_token"))+"access_token="+token.getAccessToken(), "utf-8","RouteId",r.getId());
                                saveDataStatic = new SaveDataStatic(m.getKey(), tabAndMark == null ? m.getValue() : tabAndMark[0],
                                        tabAndMark == null || k++ > 0 ? "false" : tabAndMark[1], tabAndMark == null ? "false" : tabAndMark[2],
                                        getJson);
//                            token = null;
                                if(j++>30)
                                    break;
                            }
//                        executorService.execute(saveDataStatic);
                            saveDataStatic.run();
                        }
                    }else{
                        List<RouteVehicle> routeVehicleList = routeVehicleService.getAllVehicle();

                        if(url.indexOf("?")>-1 ) {
                            url += "&access_token=" + token.getAccessToken();
                        }else {
                            url += "?access_token=" + token.getAccessToken();
                        }
                        int k = 0;
                        for(RouteVehicle rv:routeVehicleList){
                            String getJson = httpServiceTest.getJsonData(url, "utf-8","VehId",rv.getId());
                            SaveDataStatic saveDataStatic = new SaveDataStatic(m.getKey(), tabAndMark == null ? m.getValue() : tabAndMark[0],
                                    tabAndMark == null || k++ > 0 ? "false" : tabAndMark[1], tabAndMark == null ? "false" : tabAndMark[2],
                                    getJson);
                            int j = 0;
                            while(getJson.indexOf("errcode")>-1) {
                                //token失效
                                token = getToken();
                                k--;
                                getJson = httpServiceTest.getJsonData(url.substring(0,url.indexOf("access_token"))+"access_token="+token.getAccessToken(), "utf-8","VehId",rv.getId());
                                saveDataStatic = new SaveDataStatic(m.getKey(), tabAndMark == null ? m.getValue() : tabAndMark[0],
                                        tabAndMark == null || k++ > 0 ? "false" : tabAndMark[1], tabAndMark == null ? "false" : tabAndMark[2],
                                        getJson);
//                            token = null;
                                if(j++>30)
                                    break;
                            }
//                        executorService.execute(saveDataStatic);
                            saveDataStatic.run();
                        }
                    }

                }

            }
//            executorService.shutdown();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}