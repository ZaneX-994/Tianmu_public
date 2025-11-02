package com.bytewizard.realtimeservice;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class}, scanBasePackages = {"com.bytewizard.realtimeservice", "com.bytewizard.common"})
public class RealtimeServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(RealtimeServiceApplication.class, args);
    }

}
