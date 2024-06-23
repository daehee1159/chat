package com.dh.chat.api.config.redis;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import redis.embedded.RedisServer;

/**
 * @author 최대희
 * @since 2024-05-31
 */
@Profile("local")
@Configuration
public class EmbeddedRedisConfig {

	private static final Logger logger = LoggerFactory.getLogger(EmbeddedRedisConfig.class);

	@Value("${spring.data.redis.port}")
	private int redisPort;

	private RedisServer redisServer;

	@PostConstruct
	public void startRedisServer() {
		try {
			redisServer = new RedisServer(redisPort);
			redisServer.start();
			logger.info("Embedded Redis server started on port {}", redisPort);
		} catch (Exception e) {
			logger.error("Failed to start embedded Redis server", e);
			throw new RuntimeException("Failed to start embedded Redis server", e);
		}
	}

	@PreDestroy
	public void stopRedisServer() {
		if (redisServer != null) {
			redisServer.stop();
			logger.info("Embedded Redis server stopped");
		}
	}
}
