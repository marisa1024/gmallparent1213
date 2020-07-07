package com.atguigu.gmall1213.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

@Configuration
public class KeyResolverConfig {
    //限流只支持一种方式

    @Bean
    public KeyResolver ipKeyResolver(){
        //使用IP限流
        return exchange -> Mono.just(exchange.getRequest().getRemoteAddress().getHostName());
    }

    //用户限流
    @Bean
    public KeyResolver userKeyResolver() {
        return exchange -> Mono.just(exchange.getRequest().getHeaders().get("token").get(0));
    }

    //接口限流
    @Bean
    public KeyResolver apiKeyResolver() {
        return exchange -> Mono.just(exchange.getRequest().getPath().value());
    }


}
