package com.dh.chat.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * @author 최대희
 * @since 2024-05-30
 */
@Getter
@Setter
public class ChatMessage {

	public ChatMessage() {}

	@Builder
	public ChatMessage(MessageType type, String roomId, String sender, String message, Long userCount, String regDt) {
		this.type = type;
		this.roomId = roomId;
		this.sender = sender;
		this.message = message;
		this.userCount = userCount;
		this.regDt = regDt;
	}
	public enum MessageType {
		ENTER, QUIT, TALK
	}
	private MessageType type;
	private String roomId;
	private String sender;
	private String message;
	private Long userCount;
	private String regDt;
}
