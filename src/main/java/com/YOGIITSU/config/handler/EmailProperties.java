package com.YOGIITSU.config.handler;

import org.springframework.boot.context.properties.ConfigurationProperties;
import java.util.List;

@ConfigurationProperties(prefix = "email")
public record EmailProperties(List<String> allowedDomains) {

}