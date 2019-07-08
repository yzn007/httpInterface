package com.springboot.demo;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.SpringServletContainerInitializer;

import javax.servlet.http.HttpServletRequest;

//@RestController
@SpringBootApplication(scanBasePackages = {"com.springboot.demo","com.springboot.scala"})
//@RequestMapping("admin/")
@MapperScan(value = "com.springboot.demo.dao")
@EnableScheduling
public class DemoApplication extends SpringBootServletInitializer{

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder springApplicationBuilder){
		return super.configure(springApplicationBuilder);
	}
}
