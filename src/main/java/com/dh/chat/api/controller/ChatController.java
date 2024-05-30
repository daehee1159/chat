package com.dh.chat.api.controller;

import com.dh.chat.api.dto.ChatMessage;
import com.dh.chat.api.dto.ChatRoom;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author 최대희
 * @since 2024-05-30
 * MessageMapping을 통해 Websocket으로 들어오는 메시지 발행을 처리
 * 클라이언트에서 prefix를 붙여서 /pub/chat/message로 발행 요청을 하면 Controller가 해당 메시지를 받아 처리
 * 메시지가 발행되면 /sub/chat/room/{roomId}로 메시지를 send -> 클라이언트에서는 해당 주소를 구독하고 있다가 메시지가 전달되면 화면에 출력
 * 기존 WebSocketChatHandler의 역할을 ChatController가 대체
 */
@RequiredArgsConstructor
@Controller
public class ChatController {
	private final SimpMessageSendingOperations messagingTemplate;

	@MessageMapping("/chat/message")
	public void message(ChatMessage message) {
		if (ChatMessage.MessageType.ENTER.equals(message.getType())) {
			message.setMessage(message.getSender() + "님이 입장하셨습니다.");
		}
		messagingTemplate.convertAndSend("/sub/chat/room/" + message.getRoomId(), message);
	}
}
