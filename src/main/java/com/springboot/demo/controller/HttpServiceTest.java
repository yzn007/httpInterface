package com.springboot.demo.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.springboot.common.JsonObjectToAttach;
import com.springboot.common.KafkaProducer;
import com.springboot.common.Ret;
import com.springboot.demo.services.PersonService;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
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

    private JSONObject getBusTest(String param){
        String jsonStr = "";
        Map <String,String> m = new HashMap();
        if(param.equals("routePlan")){ //公交测试数据-线路计划时间
             jsonStr = "{\n" +
                    "\"RouteId\":1,\n" +
                    "\"RouteName\":\"XXX\",\n" +
                    "\"RouteCode\":\"X001\",\n" +
                    "\"Times\":[\n" +
                    "{\"Time\":\"10:00:00\", \"PlateNum\":\"CNG-9876\", \"VehNum\":\"CNG-9876\"},\n" +
                    "{\"Time\":\"10:05:00\", \"PlateNum\":\"CNG-9876\", \"VehNum\":\"CNG-9876\"}\n" +
                    "]\n" +
                    "}\n";
        }else if(param.equals("vehicle")){ //公交测试数据-车辆信息
             jsonStr = "{\n" +
                    "\"VehId\":1,\n" +
                    "\"VehNum\":\"XXX\",\n" +
                    "\"PlateNum\":\"X001\",\n" +
                    "\"OwnRoute\":{\n" +
                    "\"ID\":1,\n" +
                    "\"Code\":\"01\",\n" +
                    "\"Name\":\"Cargyi Gate\"\n" +
                    "},\n" +
                    "\"RunRoute\":{\n" +
                    "\"ID\":1,\n" +
                    "\"Code\":\"01\",\n" +
                    "\"Name\":\"Cargyi Gate\"\n" +
                    "}\n" +
                    "}\n";
        }else if(param.equals("route")){ //公交测试数据-线路
             jsonStr = "{\n" +
                    "\"ID\":1,\n" +
                    "\"Code\":\"01\",\n" +
                    "\"Name\":\" Cargyi Gate\",\n" +
                    "\"State\":0,\n" +
                    "\"DepartTime\":\"05:00:00\",\n" +
                    "\"ReturnTime\":\"22:00:00\",\n" +
                    "\"TicketPrice\": 0.0,\n" +
                    "\"StartSite\": {\"Name\":\"XX\", \"Latitude\":29, \"Longitude\":110},\n" +
                    "\"EndSite\": {\"Name\":\"YY\", \"Latitude\":29, \"Longitude\":110}\n" +
                    "}";
        }else if(param.equals("station")){ //公交测试数据-站点
             jsonStr = "{\n" +
                    "  \"RouteId\": 1,\n" +
                    "  \"RouteName\": \"XXX\",\n" +
                    "  \"RouteCode\": \"X001\",\n" +
                    "  \"Sites\": {\n" +
                    "    \"Name\": \"S1\",\n" +
                    "    \"Direct\": 0,\n" +
                    "    \"Num\": 1,\n" +
                    "    \"Lat\": 16,\n" +
                    "    \"Lng\": 96,\n" +
                    "    \"Attr\": 0,\n" +
                    "    \"Track\": \"96,16;96,15.8;95.8,17.2\"\n" +
                    "  }\n" +
                    "}";
        }else if(param.equals("routeVehicle")){ //公交测试数据-线路车辆
             jsonStr = "{\n" +
                    "\"Id\":1,\n" +
                    "\"PlateNum\":\" CNG-9876\",\n" +
                    "\"VehNum\":\"CNG-9876\",\n" +
                    "\"Position\":{\"Lat\":29,\"Lng\":\"111.34\" ,\"Direct\":1 ,\"Site\":3}\n" +
                    "}\n";
        }else{//公交测试数据-令牌
            jsonStr = "{\"access_token\":\"ACCESS_TOKEN\",\"expires_in\":7200}";
        }
        return JSONObject.parseObject(jsonStr);
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

    /**
     * get请求传输数据
     *
     * @param url
     * @param encoding
     * @return
     * @throws ClientProtocolException
     * @throws IOException
     */
    public String getJsonData(String url, String encoding) throws ClientProtocolException, IOException {
        String result = "";

        // 创建httpclient对象
        CloseableHttpClient httpClient = HttpClients.createDefault();

        // 创建get方式请求对象
//        HttpGet httpGet = new HttpGet(url);
        HttpPost post = new HttpPost(url);
        Map<String,String> map = new HashMap();
        map.put("routid","1");

        //设置参数发送
        List<BasicNameValuePair> pairs = new ArrayList<>();
        for(Map.Entry<String,String> entry : map.entrySet())	         {
            pairs.add(new BasicNameValuePair(entry.getKey(),entry.getValue()));
        }

//        httpGet.addHeader("Content-type", "application/json");
        // 通过请求对象获取响应对象
//        CloseableHttpResponse response = httpClient.execute(httpGet);
        CloseableHttpResponse response = null;
        try{
            post.setEntity(new UrlEncodedFormEntity(pairs,"UTF-8"));
            response = httpClient.execute(post);
            // 获取结果实体
            // 判断网络连接状态码是否正常(0--200都数正常)
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                result = EntityUtils.toString(response.getEntity(), "utf-8");
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            try{
                httpClient.close();
                if(response!=null){
                    // 释放链接
                    response.close();
                }
            }catch (Exception ee){
                ee.printStackTrace();
            }
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

    @RequestMapping(value = "getBusTestData")
    public JSONObject getBusTestData(HttpServletRequest request, HttpServletResponse response) {
        JSONObject object = getBusTest(request.getParameter("bus"));

        return object;
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
            textCode = (String) jsonObject.get("tx_code");
        }catch (Exception e){
            JSONArray jsonArray = JSONArray.parseArray(requestBody);
            jsonString = jsonArray.toJSONString();
        }

        //取得有效主题
        Map<String,String> topicM = JsonObjectToAttach.getValidProperties("topics",null,textCode,false);
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
