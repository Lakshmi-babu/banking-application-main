package com.infy.bankingApplication.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import com.infy.bankingApplication.dto.AccountResponse;

@Configuration
@EnableCaching
public class RedisConfig {
	
	@Bean
    public RedisCacheManager cacheManager(
            RedisConnectionFactory redisConnectionFactory) {

        JacksonJsonRedisSerializer<AccountResponse> serializer =
                new JacksonJsonRedisSerializer<>(AccountResponse.class);

        RedisCacheConfiguration config =
                RedisCacheConfiguration.defaultCacheConfig()
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair
                        .fromSerializer(serializer)
                );

        return RedisCacheManager
                .builder(redisConnectionFactory)
                .cacheDefaults(config)
                .build();
    }

}
