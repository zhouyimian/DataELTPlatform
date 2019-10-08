package com.km.dataeltplatform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScans;

@SpringBootApplication
public class DataeltplatformApplication {

	public static void main(String[] args) {
		SpringApplication.run(DataeltplatformApplication.class, args);
	}

}
