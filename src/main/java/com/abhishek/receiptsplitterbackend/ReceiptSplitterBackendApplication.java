package com.abhishek.receiptsplitterbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class ReceiptSplitterBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReceiptSplitterBackendApplication.class, args);
    }

}
