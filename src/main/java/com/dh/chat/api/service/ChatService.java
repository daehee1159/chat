package com.dh.chat.api.service;

import com.dh.chat.api.dto.ChatMessage;
import com.dh.chat.core.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author 최대희
 * @since 2024-06-03
 */
@RequiredArgsConstructor
@Service
public class ChatService {

	private final ChannelTopic channelTopic;
	private final RedisTemplate redisTemplate;
	private final ChatRoomRepository chatRoomRepository;

	/**
	 * destination 정보에서 roomId 추출
	 */
	public String getRoomId(String destination) {
		int lastIndex = destination.lastIndexOf('/');
		if (lastIndex != -1) {
			return destination.substring(lastIndex + 1);
		} else {
			return "";
		}
	}

	/**
	 * 채팅방에 메시지 발송
	 */
	public void sendChatMessage(ChatMessage chatMessage) {
		chatMessage.setUserCount(chatRoomRepository.getUserCount(chatMessage.getRoomId()));
		if (ChatMessage.MessageType.ENTER.equals(chatMessage.getType())) {
			chatMessage.setMessage(chatMessage.getSender() + "님이 방에 입장하셨습니다.");
			chatMessage.setSender("[알림]");
		} else if (ChatMessage.MessageType.QUIT.equals(chatMessage.getType())) {
			chatMessage.setMessage(chatMessage.getSender() + "님이 방에서 나갔습니다.");
			chatMessage.setSender("[알림]");
		}
		System.out.println("토픽 = " + channelTopic.getTopic());
		// 메시지 저장 테스트
		chatRoomRepository.saveMessage(chatMessage.getRoomId(), chatMessage);
		redisTemplate.convertAndSend(channelTopic.getTopic(), chatMessage);
	}

}
