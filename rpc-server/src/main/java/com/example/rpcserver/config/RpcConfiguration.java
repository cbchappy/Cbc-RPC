package com.example.rpcserver.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * @Author Cbc
 * @DateTime 2024/12/12 20:34
 * @Description
 */
@Configuration
@EnableConfigurationProperties({RegistryConfig.class})
public class RpcConfiguration {
}
