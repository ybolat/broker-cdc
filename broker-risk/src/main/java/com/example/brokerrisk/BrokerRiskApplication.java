package com.example.brokerrisk;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BrokerRiskApplication {

	public static void main(String[] args) {
		SpringApplication.run(BrokerRiskApplication.class, args);
	}

}
