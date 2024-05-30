package com.dh.chat.core.repository;

import com.dh.chat.api.dto.ChatRoom;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Repository;

import java.util.*;

/**
 * @author 최대희
 * @since 2024-05-30
 * 채팅방 조회, 생성 Repository, 현재는 Mao으로 관리하지만 추후 DB로 변경해야함
 * 기존 ChatService를 Repository가 대체
 */
@Repository
public class ChatRoomRepository {
	private Map<String, ChatRoom> chatRoomMap;

	@PostConstruct
	private void init() {
		chatRoomMap = new LinkedHashMap<>();
	}

	public List<ChatRoom> findAllRoom() {
		// 채팅방 생성 순서 최근순으로 반환
		List<ChatRoom> chatRooms = new ArrayList<>(chatRoomMap.values());
		Collections.reverse(chatRooms);
		return chatRooms;
	}

	public ChatRoom findRoomById(String id) {
		return chatRoomMap.get(id);
	}

	public ChatRoom createChatRoom(String name) {
		ChatRoom chatRoom = ChatRoom.create(name);
		chatRoomMap.put(chatRoom.getRoomId(), chatRoom);
		return chatRoom;
	}
}
