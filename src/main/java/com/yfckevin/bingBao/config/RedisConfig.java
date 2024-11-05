package com.yfckevin.bingBao.config;

import com.yfckevin.bingBao.ConfigProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.Optional;

@Configuration
public class RedisConfig {

    private final ConfigProperties configProperties;

    public RedisConfig(ConfigProperties configProperties) {
        this.configProperties = configProperties;
    }

    /**
     * @Description: 防止redis入庫序列化亂碼的問題
     * @return     返回型別
     */
    @Bean(name="redis_db9_redisTemplate")
    public RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory){
        RedisTemplate<Object, Object> redisTemplate = new RedisTemplate<Object, Object>();
        redisTemplate.setConnectionFactory(redisConnectionFactory());
        redisTemplate.setKeySerializer(new StringRedisSerializer());//key序列化
        redisTemplate.setValueSerializer(new Jackson2JsonRedisSerializer<>(Object.class));  //value序列化
        redisTemplate.afterPropertiesSet();

        return redisTemplate;
    }


    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration configuration =
                new RedisStandaloneConfiguration(configProperties.getRedisDomain(), configProperties.getRedisPort());
        configuration.setDatabase(9);
        Optional.ofNullable(configProperties.getRedisPassword()).ifPresent(password->{
            RedisPassword redisPassword = RedisPassword.of(password);
            configuration.setPassword(redisPassword);
        });
        return new LettuceConnectionFactory(configuration);
    }
}
