package com.springboot.demo.job;

import com.springboot.demo.services.PersonService;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.quartz.Job.*;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.quartz.QuartzJobBean;



/**
 * Created by yzn00 on 2019/7/4.
 */
public class MyJob extends QuartzJobBean {
    @Autowired
    PersonService personService;
    private static Logger _log = LoggerFactory.getLogger(MyJob.class);

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        personService.selectAllPerson();
        System.out.println("hello,my first springboot job!" + jobExecutionContext.getJobDetail().getKey());
        _log.info("hello,my first springboot job!"+jobExecutionContext.getJobDetail().getKey());
    }
}
