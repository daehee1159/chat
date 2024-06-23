package com.dh.chat.api.listener;

import com.dh.chat.api.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

/**
 * @author 최대희
 * @since 2024-06-18
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class WebSocketEventListener {
	private final ChatService chatService;

	@EventListener
	public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
		StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());

		// 세션 ID를 가져와서 이를 통해 해당 사용자의 정보를 가져올 수 있음
		String sessionId = headerAccessor.getSessionId();
		System.out.println("여기는 언제?");
		if (sessionId != null) {
			log.info("User Disconnected : " + sessionId);
			// 세션 종료 시 해당 세션의 채팅 메시지를 MySQL에 저장
			System.out.println(headerAccessor.getSessionAttributes().get("username"));
		}
	}
}
