package com.springboot.demo.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.springboot.common.JsonObjectToAttach;
import com.springboot.common.KafkaProducer;
import com.springboot.common.KafkaSaveData;
import com.springboot.common.Ret;
import com.springboot.demo.services.PersonService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * Created by yzn00 on 2019/6/18.
 */

@RestController
@RequestMapping("httpService/")
public class HttpServiceTest {
    @Resource
    PersonService personService;
    @RequestMapping(value = "sendPostDataByMap", method = RequestMethod.POST)
    public String sendPostDataByMap(HttpServletRequest request, HttpServletResponse response) {
        String result = "调用Post(map)成功：数据是 " + "name:" + request.getParameter("name") + " city:" + request.getParameter("city");
        return JSON.toJSONString(result);
    }

    @RequestMapping(value = "sendPostDataByJson", method = RequestMethod.POST)
    public String sendPostDataByJson(HttpServletRequest request, HttpServletResponse response, @RequestBody String requestBody) {
        JSONObject jsonObject = JSONObject.parseObject(requestBody);
        String result = "调用Post(json)成功：数据是 " + "name:" + jsonObject.getString("name") + " city:" + jsonObject.getString("status");
        return JSON.toJSONString(result);
    }

    private List getResultTable(String param){
        List<Map> result = new ArrayList<>();
        Map <String,String> m = new HashMap();
        //每个闸机当前进入人数
        if(param.equals("CurrEnterCnt")){
            m.put("Gate","1");
            m.put("Curr_Enter_Cnt","1");
            result.add(m);
        }else if (param.equals("CurrTotlCnt")){//当前闸机进入总人数
            m.put("Curr_Totl_Cnt","2");
            result.add(m);
        }else if (param.equals("GateAccmEnterCnt")){//每个闸机当日累计进入人数
            m.put("Gate","1");
            m.put("Today_Accm_Enter_Cnt","3");
            result.add(m);
        }else if (param.equals("CertAccmEnterCnt")){//按证件当日累计进入人数
            m.put("Cert","1");
            m.put("Today_Accm_Enter_Cnt","4");
            result.add(m);
        }else if (param.equals("ParkTotlQty")){//停车位总数量
            m.put("Park","1");
            m.put("Park_Totl_Qty","5");
            result.add(m);
        }else if (param.equals("MetroInOutCnt")){//周边轨道交通情况
            m.put("Stat","1");
            m.put("Metro_Enter_Cnt","6");
            m.put("Metro_Leave_Cnt","1");
            result.add(m);
        }else if (param.equals("NextDayWeat")){//天气情况
            m.put("Next_Day_Weat","7");
            result.add(m);
        }else if (param.equals("Rout")){//周边交通路线
            m.put("Rout_ID","1");
            m.put("Rout_Code","8");
            m.put("Rout_Nm","1");
            m.put("Begn_Stat","1");
            m.put("Term_Stat","1");
            result.add(m);
        }else if (param.equals("ParkCurrUseQty")){//停车位当前使用数量
            m.put("Park","1");
            m.put("Park_Curr_Use_Qty","9");
            result.add(m);
        }else if (param.equals("ParkLicnEvent")){//停车场车辆进出数据
            m.put("Licn","1");
            m.put("Enter_Tm","10");
            m.put("Leave_Tm","1");
            result.add(m);
        }else if (param.equals("SecuQty")){//安保人员数量
            m.put("Secu_Qty","11");
            result.add(m);
        }else if (param.equals("ExhiCnt")){//昨日、今日累计人数
            m.put("Yestd_Accm_Cnt","1");
            m.put("Today_Accm_Cnt","12");
            result.add(m);
        }else if (param.equals("TodayPrecnExhiCnt")){//当日预约观展人数
            m.put("Today_Precn_Exhi_Cnt","13");
            result.add(m);
        }else if (param.equals("TodayExhiCnt")){//当日观展人数
            m.put("Today_Exhi_Cnt","14");
            result.add(m);
        }else if (param.equals("StffTotlCnt")){//工作人员总数
            m.put("Stff_Totl_Cnt","15");
            result.add(m);
        }else if (param.equals("CurrSecuQty")){//当前安保人员数量
            m.put("Curr_Secu_Qty","16");
            result.add(m);
        }else if (param.equals("PubTrafCnt")){//观展人员到达方式（当日驾车人数、当日公共交通人数）
            m.put("Today_Driv_Cnt","1");
            m.put("Today_Pub_Traf_Cnt","17");
            result.add(m);
        }else if (param.equals("TmpStatQty")){//临时站点数量
            m.put("Tmp_Stat_Qty","18");
            result.add(m);
        }else if (param.equals("TodayDispCnt")){//公交当日调度次数
            m.put("Today_Disp_Cnt","19");
            result.add(m);
        }else if (param.equals("VenCurrCnt")){//各场馆实时人数
            m.put("Ven","1");
            m.put("Curr_Cnt","20");
            result.add(m);
        }else{//其它
            m.put("Gate","0");
            m.put("Curr_Enter_Cnt","0");
            result.add(m);
        }
        return result;
    }

    @RequestMapping(value = "sendGetData")
    public Ret sendGetData(HttpServletRequest request, HttpServletResponse response) {
        String result = "调用成功：数据是 " + "name:" + request.getParameter("name") + " city:" + request.getParameter("city");
        Map m = new HashMap();

        List mapLists = getResultTable(request.getParameter("RayData"));

//        List list = personService.selectAllPerson();
//        m.put("results",list);
        m.put("results",mapLists);
        m.put("status","ok");
//        String listJson = JSON.toJSONString(list);
//        return Ret.ok(listJson,"OK");
        return Ret.ok(m);
    }

    @RequestMapping(value = "getPostData",method=RequestMethod.POST)
    public Ret sendPostData(HttpServletRequest request, HttpServletResponse response, @RequestBody String requestBody) {
        Map m = new HashMap();
        String jsonString = "";
        String textCode ="";
        try {
            JSONObject jsonObject = JSONObject.parseObject(requestBody);
//            String [] b = JsonObjectToAttach.getJsonList(jsonObject.toJSONString());
//            for(int i = 0;i<b.length;i++){
//                String []a = JsonObjectToAttach.getColumsAndValues( JSONObject.parseObject(b[i]));
//                String c = JsonObjectToAttach.getPropertyRelation(a[0],"web_data_profil",null);
//                System.out.print(c);
//            }

            jsonString = jsonObject.toJSONString();
            textCode = (String) jsonObject.get("code");
        }catch (Exception e){
            JSONArray jsonArray = JSONArray.parseArray(requestBody);
            jsonString = jsonArray.toJSONString();
        }

        //取得有效主题
        Map<String,String> topicM = JsonObjectToAttach.getValidProperties("topics",null,textCode);
        if(topicM.keySet().size()>0) {
            ExecutorService executorService = Executors.newFixedThreadPool(6);
            for (Map.Entry<String, String> map : topicM.entrySet()) {
                KafkaProducer kafkaProducer = new KafkaProducer(map.getKey(), jsonString);
                executorService.execute(kafkaProducer);
            }
            executorService.shutdown();
        }

//        KafkaProducer kafkaProducer = new KafkaProducer("bingfu",jsonString);
//        kafkaProducer.run();
//
        m.put("results",jsonString);
        m.put("status","ok");
        return Ret.ok(m);
    }


    public String hello(HttpServletRequest request, HttpServletResponse response) throws IOException {
//        String result = "调用成功：数据是 " + "name:" + request.getParameter("name") + " city:" + request.getParameter("age");
        request.setCharacterEncoding("UTF-8");
        BufferedReader br = new BufferedReader(new InputStreamReader(request.getInputStream(), "UTF-8"));
        String line = "";
        String res = "";
        while (null != (line = br.readLine())) {
            res += line;
        }
        JSONArray array = JSONArray.parseArray(res);
        for (int i = 0; i < array.size(); i++) {
            JSONObject user = array.getJSONObject(i);
            System.out.println(String.format("name=%s age=%s", user.getString("name"), user.getString("age")));
        }
        response.setCharacterEncoding("utf-8");
        response.getWriter().append("Served at: ").append(res);
        return JSON.toJSONString(res);
    }
//
//    @WebServlet("/hello")
//    public class Hello extends HttpServlet {
//        private static final long serialVersionUID = 1L;

        protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
            request.setCharacterEncoding("UTF-8");
            BufferedReader br = new BufferedReader(new InputStreamReader(request.getInputStream(), "UTF-8"));
            String line = "";
            String res = "";
            while (null != (line = br.readLine())) {
                res += line;
            }
            JSONArray array = JSONArray.parseArray(res);
            for (int i = 0; i < array.size(); i++) {
                JSONObject user = array.getJSONObject(i);
                System.out.println(String.format("name=%s age=%s", user.getString("name"), user.getString("age")));
            }
            response.setCharacterEncoding("utf-8");
            response.getWriter().append("Served at: ").append(res);
        }
    @RequestMapping(value = "hello", method = RequestMethod.POST)
        protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
            doGet(request, response);
        }
//    }

}
