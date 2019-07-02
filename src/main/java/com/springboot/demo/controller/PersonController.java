package com.springboot.demo.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.springboot.demo.services.PersonService;
import jdk.nashorn.internal.runtime.options.LoggingOption;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.log4j2.Log4j2Impl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.logging.log4j2.Log4J2LoggingSystem;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by yzn00 on 2019/6/19.
 */
@Controller
@RequestMapping(value = "person/")
public class PersonController {
    @Autowired
    private PersonService personService;

    @RequestMapping(value = "all", method = RequestMethod.GET)
    @ResponseBody
    public Map onAll() {
        List list = personService.selectAllPerson();
        Map m = new HashMap();
        m.put("data", JSONObject.toJSONString(list));
        try {
            Thread.sleep(500);
        }catch (Exception e){
            System.out.print("system interrupt error!");
        }
        return m;
    }

    @RequestMapping(value = "index")
    @ResponseBody
    public ModelAndView main(){
        ModelAndView mode= new ModelAndView();
        mode.addObject("name","ddfsdfsd");
        mode.setViewName("index");
        return mode;
    }

    @RequestMapping(value = "home")
    public String index(){
        ModelAndView mode= new ModelAndView();
        mode.addObject("name","ddfsdfsd");
        mode.setViewName("main");
        return "main";
    }


}
