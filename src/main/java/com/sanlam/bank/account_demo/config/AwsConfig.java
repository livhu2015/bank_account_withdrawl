package com.sanlam.bank.account_demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;

@Configuration
public class AwsConfig {

    @Value("${aws.region}")
    private String awsRegion;
    @Bean
    public SnsClient snsClient() {
        return SnsClient.builder().region(Region.of(awsRegion)).build();
    }
}
