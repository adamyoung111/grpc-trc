package com.bizzan.bc.trc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.consul.discovery.configclient.ConsulConfigServerAutoConfiguration;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan(basePackages = {"com.bizzan.bc.trc.**"})
@EnableEurekaClient
//@EnableKafka
@SpringBootApplication(exclude = {ConsulConfigServerAutoConfiguration.class})
public class Trc20Application {

    public static void main(String[] args) {
        SpringApplication.run(Trc20Application.class, args);
    }

}
