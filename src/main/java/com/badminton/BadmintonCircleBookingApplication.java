package com.badminton;

import com.badminton.config.LineProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(LineProperties.class)
public class BadmintonCircleBookingApplication {

    public static void main(String[] args) {
        SpringApplication.run(BadmintonCircleBookingApplication.class, args);
    }
}
