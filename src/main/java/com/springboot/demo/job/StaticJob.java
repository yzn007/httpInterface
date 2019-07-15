package com.springboot.demo.job;

import com.alibaba.fastjson.JSONObject;
import com.springboot.common.JsonObjectToAttach;
import com.springboot.common.KafkaSaveData;
import com.springboot.common.ReadPropertiesUtils;
import com.springboot.common.SaveDataStatic;
import com.springboot.demo.controller.HttpServiceTest;
import com.springboot.demo.dao.RouteMapper;
import com.springboot.demo.entity.Route;
import com.springboot.demo.entity.Token;
import com.springboot.demo.services.RouteService;
import com.springboot.demo.services.TokenService;
import org.apache.commons.lang.StringUtils;
import org.apache.kafka.clients.consumer.Consumer;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
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

    @Autowired
    TokenService tokenService;

    public static Token token;

    HttpServiceTest httpServiceTest = null;

    private String getToken(){
        if(token== null ){
            List<Token> listT = tokenService.getAllToken();
            if (listT != null && listT.size() > 0) {
                token = listT.get(0);
            }
        }
        if (token!= null && token.getDateExp().after(new Date())){
            return token.getAccessToken();
        }

        //请求新的token
        try{
            JSONObject jsonObject = (JSONObject) httpServiceTest.getBusTest("token");
            return jsonObject.get("access_token").toString();
        }catch (Exception e){
            return null;
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

    @Override
    public void execute(JobExecutionContext context)
            throws JobExecutionException {
//        _log.error("New Job执行时间: " + new Date());

//
        ExecutorService executorService = Executors.newFixedThreadPool(NUM_PROCESS);
        if(httpServiceTest==null)
            httpServiceTest = new HttpServiceTest();
        try {
//    this.jsonStr = httpServiceTest.getJsonData("http://localhost/httpService/sendGetData?RayData=CurrTotlCnt", "utf-8");
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
                if(url.indexOf("token")>-1){//获取公交访问令牌
                    SaveDataStatic saveDataStatic = new SaveDataStatic(m.getKey(), tabAndMark == null ? m.getValue() : tabAndMark[0],
                            tabAndMark == null ? "false" : tabAndMark[1], tabAndMark == null ? "false" : tabAndMark[2],
                            httpServiceTest.getJsonData(url, "utf-8",""));
                    executorService.execute(saveDataStatic);
                }else if(url.indexOf("route")>-1){//获取所有线路
                    if(url.indexOf("?")>-1 ) {
                        url += "&access_token=" + getToken();
                    }else {
                        url += "?access_token=" + getToken();
                    }
                    SaveDataStatic saveDataStatic = new SaveDataStatic(m.getKey(), tabAndMark == null ? m.getValue() : tabAndMark[0],
                            tabAndMark == null ? "false" : tabAndMark[1], tabAndMark == null ? "false" : tabAndMark[2],
                            httpServiceTest.getJsonData(url, "utf-8",""));
                    executorService.execute(saveDataStatic);
                }
                else {
                    List<Route> listR = routeService.getAllRoute();


                    if(url.indexOf("?")>-1 ) {
                        url += "&access_token=" + getToken();
                    }else {
                        url += "?access_token=" + getToken();
                    }
                    for (Route r : listR) {
                        SaveDataStatic saveDataStatic = new SaveDataStatic(m.getKey(), tabAndMark == null ? m.getValue() : tabAndMark[0],
                                tabAndMark == null ? "false" : tabAndMark[1], tabAndMark == null ? "false" : tabAndMark[2],
                                httpServiceTest.getJsonData(url, "utf-8", r.getId()));
                        executorService.execute(saveDataStatic);
                    }
                }

            }
            executorService.shutdown();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}