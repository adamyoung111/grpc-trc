//package com.bizzan.bc.wallet.config;
//
//import org.springframework.cloud.client.loadbalancer.LoadBalanced;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.data.mongodb.MongoDatabaseFactory;
//import org.springframework.data.mongodb.core.MongoTemplate;
//import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
//import org.springframework.http.client.SimpleClientHttpRequestFactory;
//import org.springframework.web.client.RestTemplate;
//
//@Configuration
//public class MongodbTemplateConfig {
//    @Bean
//    @LoadBalanced
//    RestTemplate restTemplate() {
//        MongoDatabaseFactory mongoDatabaseFactory=new SimpleMongoClientDatabaseFactory()
//        MongoTemplate mongoTemplate = new MongoTemplate(mongoDatabaseFactory);
//        return restTemplate;
//    }
//
//}
