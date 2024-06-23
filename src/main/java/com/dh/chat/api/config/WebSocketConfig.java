package com.dh.chat.api.config;

import com.dh.chat.api.handler.StompHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.*;

/**
 * @author 최대희
 * @since 2024-05-30
 */
@RequiredArgsConstructor
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
	private final StompHandler stompHandler;
	@Override
	public void configureMessageBroker(MessageBrokerRegistry registry) {
		// pub/sub 메시징을 구현하기 위해 메시지를 발행하는 요청의 prefix는 /pub, 구독하는 요청의 prefix는 /sub로 시작하도록 설정
		registry.enableSimpleBroker("/sub");
		registry.setApplicationDestinationPrefixes("/pub");
	}

	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		// stomp websocket의 연결 endpoint는 /ws-stomp로 설정, 따라서 서버 접속 주소는 ws://localhost:8080/ws-stomp
		registry.addEndpoint("/ws-stomp")
//			.setAllowedOriginPatterns("http://localhost:8080", "http://localhost:3000")
			.setAllowedOrigins("http://localhost:3000")
			.withSockJS();
	}

	@Override
	public void configureClientInboundChannel(ChannelRegistration registration) {
		registration.interceptors(stompHandler);
	}
}
