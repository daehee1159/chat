package com.dh.chat.api.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.socket.WebSocketSession;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * @author 최대희
 * @since 2024-05-30
 */
@Getter
@Setter
public class ChatRoom implements Serializable {
	private static final long serialVersionUID = 6494678977089006639L;
	private String roomId;
	private String name;
	private Long userCount;

	// pub/sub 방식을 이용하면 구독자 관리가 알아서 되므로 웹소켓 세션 관리가 필요 없어짐
	// 발송의 구현도 알아서 해결되므로 일일이 클라이언트에게 메시지를 발송하는 구현이 필요 없어짐
	// 이에 따른 DTO 간소화
	public static ChatRoom create(String name) {
		ChatRoom chatRoom = new ChatRoom();
		chatRoom.roomId = UUID.randomUUID().toString();
		chatRoom.name = name;
		return chatRoom;
	}
}
