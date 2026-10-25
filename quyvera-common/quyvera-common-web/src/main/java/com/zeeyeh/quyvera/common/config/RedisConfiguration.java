package com.zeeyeh.quyvera.common.config;

import io.lettuce.core.api.StatefulConnection;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.time.Duration;

/**
 * @author Qinloren
 * @date 2026/10/24 16:55
 */
@AutoConfiguration
public class RedisConfiguration {

    @Value("${spring.data.redis.host:localhost}")
    String host;

    @Value("${spring.data.redis.port:6379}")
    int port;

    @Value("${spring.data.redis.database:0}")
    int database;

    @Value("${spring.data.redis.password:}")
    String password;

    @Value("${spring.data.redis.timeout:2000}")
    long timeout;

    @Value("${spring.data.redis.lettuce.pool.max-active:8}")
    int maxActive;

    @Value("${spring.data.redis.lettuce.pool.max-idle:8}")
    int maxIdle;

    @Value("${spring.data.redis.lettuce.pool.min-idle:0}")
    int minIdle;

    @Value("${spring.data.redis.lettuce.pool.max-wait:30000}")
    int maxWait;

    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName(host);
        configuration.setPort(port);
        if (StringUtils.isNotBlank(password)) {
            configuration.setPassword(password);
        }
        configuration.setDatabase(database);
        GenericObjectPoolConfig<StatefulConnection<?, ?>> poolConfig = new GenericObjectPoolConfig<>();
        poolConfig.setMaxIdle(maxIdle);
        poolConfig.setMaxTotal(maxActive);
        poolConfig.setMinIdle(minIdle);
        poolConfig.setMaxWait(Duration.ofMillis(maxWait));
        LettucePoolingClientConfiguration lettucePoolingClientConfiguration = LettucePoolingClientConfiguration.builder()
                .commandTimeout(Duration.ofMillis(timeout))
                .poolConfig(poolConfig)
                .build();
        LettuceConnectionFactory connectionFactory = new LettuceConnectionFactory(configuration, lettucePoolingClientConfiguration);
        connectionFactory.afterPropertiesSet();
        connectionFactory.start();
        return connectionFactory;
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory());
        redisTemplate.setKeySerializer(RedisSerializer.string());
        redisTemplate.setHashKeySerializer(RedisSerializer.string());
        redisTemplate.setValueSerializer(RedisSerializer.json());
        redisTemplate.setHashValueSerializer(RedisSerializer.json());
        return redisTemplate;
    }
}
