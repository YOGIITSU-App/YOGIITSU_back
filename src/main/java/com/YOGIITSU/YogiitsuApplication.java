package com.YOGIITSU;

import com.YOGIITSU.config.handler.EmailProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(EmailProperties.class)
public class YogiitsuApplication {

    public static void main(String[] args) {
        SpringApplication.run(YogiitsuApplication.class, args);
    }
}