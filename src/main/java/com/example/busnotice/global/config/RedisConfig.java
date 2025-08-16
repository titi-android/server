//package com.example.busnotice.global.config;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping;
//import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.cache.CacheManager;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.data.redis.cache.RedisCacheConfiguration;
//import org.springframework.data.redis.cache.RedisCacheManager;
//import org.springframework.data.redis.connection.RedisConnectionFactory;
//import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
//import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
//import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
//import org.springframework.data.redis.serializer.RedisSerializationContext;
//import org.springframework.data.redis.serializer.StringRedisSerializer;
//
//import java.time.Duration;
//
//@Configuration
//@EnableRedisRepositories
//public class RedisConfig {
//
//    @Value("${spring.data.redis.host}")
//    private String host;
//
//    @Value("${spring.data.redis.port}")
//    private int port;
//
//    @Bean
//    public RedisConnectionFactory redisConnectionFactory() {
//        return new LettuceConnectionFactory(host, port);
//    }
//
//    @Bean
//    public CacheManager cacheManager(RedisConnectionFactory factory) {
//        ObjectMapper objectMapper = new ObjectMapper();
//        // 타입 정보를 활성화, 지금은 필요없음
//        objectMapper.activateDefaultTyping(
//                BasicPolymorphicTypeValidator.builder()
//                        .allowIfSubType("java.util") // 컬렉션 타입 허용
//                        .allowIfSubType("java.lang") // 기본 타입 허용
//                        .allowIfSubType("com.example") // 사용자 정의 클래스 허용, 이게 있어야 객체 캐싱 가능
//                        .build(),
//                DefaultTyping.EVERYTHING // 모든 객체에 타입 정보 추가
//        );
//
//        // GenericJackson2JsonRedisSerializer에 ObjectMapper 설정
//        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(
//                objectMapper);
//
//        RedisCacheConfiguration cacheConfig = RedisCacheConfiguration.defaultCacheConfig()
//                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(
//                        new StringRedisSerializer()))
//                .serializeValuesWith(
//                        RedisSerializationContext.SerializationPair.fromSerializer(serializer))
//                .entryTtl(Duration.ofHours(1L));
//
//        return RedisCacheManager
//                .RedisCacheManagerBuilder
//                .fromConnectionFactory(factory)
//                .cacheDefaults(cacheConfig)
//                .build();
//    }
//
//}
