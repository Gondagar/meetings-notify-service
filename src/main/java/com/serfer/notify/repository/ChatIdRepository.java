package com.serfer.notify.repository;

import com.serfer.notify.entity.TelegramChatId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChatIdRepository extends JpaRepository<TelegramChatId, Long> {

    Optional<TelegramChatId> findByEmail(String email);

}