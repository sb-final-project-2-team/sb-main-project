package com.codeit.closet.common.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

@Slf4j
@Configuration
@EnableCaching
public class CacheConfig {

  @Bean
  public RedisCacheConfiguration redisCacheConfiguration(ObjectMapper objectMapper) {
    ObjectMapper redisObjectMapper = objectMapper.copy();
    redisObjectMapper.activateDefaultTyping(
        LaissezFaireSubTypeValidator.instance,
        DefaultTyping.EVERYTHING,
        As.PROPERTY
    );

    return RedisCacheConfiguration.defaultCacheConfig()
        .serializeValuesWith(
            RedisSerializationContext.SerializationPair.fromSerializer(
                new GenericJackson2JsonRedisSerializer(redisObjectMapper)
            )
        )
        .prefixCacheNameWith("closet:")
        .entryTtl(Duration.ofSeconds(600))
        .disableCachingNullValues();
  }

  @Bean
  public CacheManager cacheManager(
      RedisConnectionFactory connectionFactory,
      RedisCacheConfiguration redisCacheConfiguration
  ) {
    return RedisCacheManager.builder(connectionFactory)
        .cacheDefaults(redisCacheConfiguration)
        .build();
  }
}
