package com.YOGIITSU;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class YogiitsuApplication {

	public static void main(String[] args) {
		SpringApplication.run(YogiitsuApplication.class, args);
	}
}
