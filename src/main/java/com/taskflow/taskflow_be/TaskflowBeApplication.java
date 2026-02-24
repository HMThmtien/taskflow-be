package com.taskflow.taskflow_be;

import com.taskflow.taskflow_be.security.jwt.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(JwtProperties.class)
@ConfigurationPropertiesScan
public class TaskflowBeApplication {

	public static void main(String[] args) {
		SpringApplication.run(TaskflowBeApplication.class, args);
	}

}
