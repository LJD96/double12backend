package com.ljd.double12backend.config;

import com.alibaba.druid.pool.DruidDataSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Liu JianDong
 * @create 2023-03-28-13:45
 **/
@Configuration
public class DataSourceConfiguration {

    @ConfigurationProperties(prefix = "spring.datasource")
    @Bean(initMethod = "init", destroyMethod = "close")
    public DruidDataSource dataSource(){
        return new DruidDataSource();
    }
}
