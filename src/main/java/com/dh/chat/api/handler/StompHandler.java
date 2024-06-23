package com.dh.chat.api.handler;

import com.dh.chat.api.dto.ChatMessage;
import com.dh.chat.api.service.ChatService;
import com.dh.chat.core.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.Optional;

/**
 * @author 최대희
 * @since 2024-06-03
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class StompHandler implements ChannelInterceptor {
	private final ChatRoomRepository chatRoomRepository;
	private final ChatService chatService;

	// Websocket을 통해 들어온 요청이 처리 되기전에 실행
	// 핸들러의 역할은 연결요청, 구독, 해지만 함
	// 메시지를 받는건 RedisSubscriber ㅐnMessage에서
	@Override
	public Message<?> preSend(Message<?> message, MessageChannel channel) {
		StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

		// Websocket 연결 요청
		if (StompCommand.CONNECT == accessor.getCommand()) {
			String username = accessor.getFirstNativeHeader("username");
			accessor.getSessionAttributes().put("username", username);
			log.info("CONNECT {}, {}", accessor, accessor.getFirstNativeHeader("username"));

		} else if (StompCommand.SUBSCRIBE == accessor.getCommand()) { // 채팅룸 구독 요청
			// header 정보에서 구독 destination 정보를 얻고, roomId를 추출
			String roomId = chatService.getRoomId(Optional.ofNullable((String) message.getHeaders().get("simpDestination")).orElse("InvalidRoomId"));
			// 채팅방에 들어온 클라이언트 sessionId를 roomId와 매핑
			String sessionId = (String) message.getHeaders().get("simpSessionId");
			chatRoomRepository.setUserEnterInfo(sessionId, roomId);
			// 채팅방의 인원수를 +1
			chatRoomRepository.plusUserCount(roomId);
			// 클라이언트 입장 메시지를 채팅방에 발송 (redis publish)
			System.out.println("nativeHeaders");
			System.out.println(message.getHeaders().get("nativeHeaders"));
			String username = accessor.getFirstNativeHeader("username");
//			String name = Optional.ofNullable((Principal) message.getHeaders().get("simpUser")).map(Principal::getName).orElse("UnKnownUser");
			chatService.sendChatMessage(ChatMessage.builder().type(ChatMessage.MessageType.ENTER).roomId(roomId).sender(username).build());
			log.info("SUBSCRIBED {}, {}", username, roomId);
		} else if (StompCommand.DISCONNECT == accessor.getCommand()) { // Websocket 연결 종료
			// 연결이 종료된 클라이언트 sessionId로 채팅방 id를 얻음
			String sessionId = (String) message.getHeaders().get("simpSessionId");
			String roomId = chatRoomRepository.getUserEnterRoomId(sessionId);
			// 채팅방의 인원수를 -1
			chatRoomRepository.minusUserCount(roomId);
			// 클라이언트 퇴장 메시지를 채팅방에 발송 (redis publish)
			String username = (String) accessor.getSessionAttributes().get("username");
//			String name = Optional.ofNullable((Principal) message.getHeaders().get("simpUser")).map(Principal::getName).orElse("UnKnownUser");
			chatService.sendChatMessage(ChatMessage.builder().type(ChatMessage.MessageType.QUIT).roomId(roomId).sender(username).build());
			// 퇴장한 클라이언트의 roomId 매핑 정보 삭제
			chatRoomRepository.removeUserEnterInfo(sessionId);
			System.out.println("StompCommand.DISCONNECT!!");
			log.info("DISCONNECTED {}, {}, {}", sessionId, roomId, username);
		}
		System.out.println(message.getPayload());
		return message;
	}
}
