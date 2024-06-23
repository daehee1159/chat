package com.dh.chat.api.service;

import com.dh.chat.api.dto.ChatMessage;
import com.dh.chat.core.repository.ChatRoomRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author 최대희
 * @since 2024-05-31
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class RedisSubscriber implements MessageListener {
	private final ObjectMapper objectMapper;
	private final RedisTemplate redisTemplate;
	private final SimpMessageSendingOperations messagingTemplate;

	private final ChatRoomRepository chatRoomRepository;

	// Redis에서 메시지가 발행되면 대기하고 있던 onMessage가 해당 메시지를 받아서 처리

	@Override
	public void onMessage(Message message, byte[] pattern) {
		try {
			// redis에서 발행된 데이터를 받아 deserialize
			String publishMessage = (String) redisTemplate.getStringSerializer().deserialize(message.getBody());
			// ChateMessage 객체로 매핑
			ChatMessage roomMessage = objectMapper.readValue(publishMessage, ChatMessage.class);

			// 메시지 조회
			List<ChatMessage> messageList = chatRoomRepository.getMessages(roomMessage.getRoomId(), 0, -1);
			System.out.println("메시지 리스트 조회");
			for (int i = 0; i < messageList.size(); i++) {
				System.out.println(i + "번째");
				System.out.println(messageList.get(i).getSender());
				System.out.println(messageList.get(i).getMessage());
				System.out.println(messageList.get(i).getRegDt());
			}

			// WebSocket 구독자에게 채팅 메시지 send
			messagingTemplate.convertAndSend("/sub/chat/room/" + roomMessage.getRoomId(), roomMessage);
		} catch (Exception e) {
			log.error(e.getMessage());
		}
	}
}
