package com.springboot.httpInterface;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

//@RestController
@SpringBootApplication(scanBasePackages = {"com.springboot.httpInterface","com.springboot.scala"})
//@RequestMapping("admin/")
@MapperScan(value = "com.springboot.httpInterface.dao")
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
