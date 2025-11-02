package com.bytewizard.searchsyncservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.bytewizard.searchsyncservice", "com.bytewizard.common"})
public class SearchSyncServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SearchSyncServiceApplication.class, args);
    }

}
