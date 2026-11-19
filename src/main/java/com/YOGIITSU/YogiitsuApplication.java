package com.YOGIITSU;

import com.YOGIITSU.config.handler.EmailProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.TimeZone;

@SpringBootApplication
@EnableAsync
@EnableScheduling
@EnableConfigurationProperties(EmailProperties.class)
public class YogiitsuApplication {

    public static final String DEFAULT_TIME_ZONE = "Asia/Seoul";

    public static void main(String[] args) {
        configureDefaultTimeZone();
        SpringApplication.run(YogiitsuApplication.class, args);
    }

    /**
     * JVM 기본 시간대를 KST로 고정한다.
     * 컨테이너 기본 시간대(UTC)로 LocalDateTime.now()가 계산되어
     * 작성 시각 등이 KST보다 9시간 이르게 저장되는 문제를 방지한다.
     */
    static void configureDefaultTimeZone() {
        TimeZone.setDefault(TimeZone.getTimeZone(DEFAULT_TIME_ZONE));
    }
}