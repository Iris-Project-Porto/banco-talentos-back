package com.vilt.talentos.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "matrix")
@Getter @Setter
public class MatrixProperties {
    private List<String> plenoKeywords;
    private List<String> srKeywords;
}
