package com.dh.chat.core.repository;

import com.dh.chat.api.dto.ChatMessage;
import com.dh.chat.api.dto.ChatRoom;
import com.dh.chat.api.service.RedisSubscriber;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Repository;

import java.util.*;

/**
 * @author 최대희
 * @since 2024-05-30
 * 채팅방 조회, 생성 Repository, 현재는 Mao으로 관리하지만 추후 DB로 변경해야함
 */
@RequiredArgsConstructor
@Repository
public class ChatRoomRepository {
	// Redis CacheKeys
	private static final String CHAT_ROOMS = "CHAT_ROOM"; // 채팅룸 저장
	public static final String USER_COUNT = "USER_COUNT"; // 채팅룸에 입장한 클라이언트수 저장
	public static final String ENTER_INFO = "ENTER_INFO"; // 채팅룸에 입장한 클라이언트의 sessionId와 채팅룸 id를 맵핑한 정보 저장

	@Resource(name = "redisTemplate")
	private HashOperations<String, String, ChatRoom> hashOpsChatRoom;
	@Resource(name = "redisTemplate")
	private HashOperations<String, String, String> hashOpsEnterInfo;
	@Resource(name = "redisTemplate")
	private ListOperations<String, String> listOpsChatMessages;
	@Resource(name = "redisTemplate")
	private ValueOperations<String, String> valueOps;

	private final ObjectMapper objectMapper;

	// 모든 채팅방 조회
	public List<ChatRoom> findAllRoom() {
		return hashOpsChatRoom.values(CHAT_ROOMS);
	}

	// 특정 채팅방 조회
	public ChatRoom findRoomById(String id) {
		return hashOpsChatRoom.get(CHAT_ROOMS, id);
	}

	// 채팅방 생성 : 서버간 채팅방 공유를 위해 redis hash에 저장한다.
	public ChatRoom createChatRoom(String name) {
		ChatRoom chatRoom = ChatRoom.create(name);
		hashOpsChatRoom.put(CHAT_ROOMS, chatRoom.getRoomId(), chatRoom);
		return chatRoom;
	}

	// 유저가 입장한 채팅방ID와 유저 세션ID 맵핑 정보 저장
	public void setUserEnterInfo(String sessionId, String roomId) {
		hashOpsEnterInfo.put(ENTER_INFO, sessionId, roomId);
	}

	// 메시지 저장
	public void saveMessage(String roomId, ChatMessage chatMessage) {
		try {
			String messageJson = objectMapper.writeValueAsString(chatMessage);
			listOpsChatMessages.rightPush(roomId, messageJson);
		} catch (JsonProcessingException e) {
			e.printStackTrace();;
		}

	}

	// 메시지 조회
	public List<ChatMessage> getMessages(String roomId, long start, long end) {
		List<String> messageJsonList = listOpsChatMessages.range(roomId, start, end);
		List<ChatMessage> messages = new ArrayList<>();

		for (String messageJson : messageJsonList) {
			try {
				ChatMessage chatMessage = objectMapper.readValue(messageJson, ChatMessage.class);
				messages.add(chatMessage);
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}
		}

		return messages;
	}

	// 유저 세션으로 입장해 있는 채팅방 ID 조회
	public String getUserEnterRoomId(String sessionId) {
		return hashOpsEnterInfo.get(ENTER_INFO, sessionId);
	}

	// 유저 세션정보와 맵핑된 채팅방ID 삭제
	public void removeUserEnterInfo(String sessionId) {
		hashOpsEnterInfo.delete(ENTER_INFO, sessionId);
	}

	// 채팅방 유저수 조회
	public long getUserCount(String roomId) {
		//TODO DB에서 채팅룸에 있는 사람수를 가져와야함
		return Long.valueOf(Optional.ofNullable(valueOps.get(USER_COUNT + "_" + roomId)).orElse("0"));
	}

	// 채팅방에 입장한 유저수 +1
	public long plusUserCount(String roomId) {
		return Optional.ofNullable(valueOps.increment(USER_COUNT + "_" + roomId)).orElse(0L);
	}

	// 채팅방에 입장한 유저수 -1
	public long minusUserCount(String roomId) {
		return Optional.ofNullable(valueOps.decrement(USER_COUNT + "_" + roomId)).filter(count -> count > 0).orElse(0L);
	}
}
