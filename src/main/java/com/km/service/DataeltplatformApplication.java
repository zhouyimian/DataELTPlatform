package com.km.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication(exclude = MongoAutoConfiguration.class)
public class DataeltplatformApplication{

	public static void main(String[] args) {
		SpringApplication.run(DataeltplatformApplication.class, args);
	}

}
