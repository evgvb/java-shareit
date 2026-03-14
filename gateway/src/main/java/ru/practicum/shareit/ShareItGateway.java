package ru.practicum.shareit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
//import org.springframework.boot.context.properties.EnableConfigurationProperties;
//import ru.practicum.shareit.client.properties.ShareItServerProperties;

@SpringBootApplication
//@EnableConfigurationProperties(ShareItServerProperties.class)
public class ShareItGateway {
    public static void main(String[] args) {
        SpringApplication.run(ShareItGateway.class, args);
    }
}