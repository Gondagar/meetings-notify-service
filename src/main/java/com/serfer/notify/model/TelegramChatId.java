package com.serfer.notify.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "chatId")
public class TelegramChatId {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    String email;

    Long chapId;

}
