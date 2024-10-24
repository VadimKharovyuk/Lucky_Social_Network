package com.example.lucky_social_network.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class MessageDTO {
    private String type;           // "CHAT", "DELETE"
    private Long messageId;        // ID сообщения
    private Long senderId;         // ID отправителя
    private Long recipientId;      // ID получателя
    private String content;        // Содержание сообщения
    private String timestamp;      // Временная метка
    private boolean forEveryone;   // Удалить для всех?
    private DeleteAction deleteAction; // Тип действия удаления

    // Конструктор для обычных сообщений чата
    public MessageDTO(String type, Long senderId, String content, String timestamp) {
        this.type = type;
        this.senderId = senderId;
        this.content = content;
        this.timestamp = timestamp;
    }

    // Конструктор для уведомлений об удалении
    public MessageDTO(String type, Long messageId, DeleteAction deleteAction, boolean forEveryone, String timestamp) {
        this.type = type;
        this.messageId = messageId;
        this.deleteAction = deleteAction;
        this.forEveryone = forEveryone;
        this.timestamp = timestamp;
    }


    // Enum для типов удаления
    public enum DeleteAction {
        SINGLE_MESSAGE,    // Удаление одного сообщения
        CHAT_HISTORY      // Удаление истории чата
    }

}
