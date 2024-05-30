package com.dh.chat.api.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * @author 최대희
 * @since 2024-05-30
 */
@Getter
@Setter
public class ChatMessage {
	public enum MessageType {
		ENTER, TALK
	}
	private MessageType type;
	private String roomId;
	private String sender;
	private String message;
}
