package com.serfer.notify.service;

import com.serfer.notify.entity.TelegramChatId;
import com.serfer.notify.repository.ChatIdRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Optional;


@Component
@Slf4j
public class TelegramService extends TelegramLongPollingBot {

    private final String BOT_USERNAME = "notify_meeting_bot";

    private final String BOT_TOKEN = "609993152:AAHYsyRuc_332OxJLYZCVKRQBrGbtQnMHDc";

    private final ChatIdRepository chatIdRepository;

    public TelegramService(ChatIdRepository chatIdRepository) {
        this.chatIdRepository = chatIdRepository;
    }

    @Override
    public void onUpdateReceived(Update update) {
        String message = update.getMessage().getText();
        log.info("Get new message: {}", message);
        Long chatId = update.getMessage().getChatId();
        log.info("Chat Id = {}", chatId);
        SendMessage sendMessage;

        if (message.contains("@")) {

            Optional<TelegramChatId> existChatId = chatIdRepository.findByEmail(message);
            if (existChatId.isPresent()) {
                TelegramChatId telegramChatId = existChatId.get();
                if (telegramChatId.getChatId().equals(chatId)) {
                    sendMessage = createMessage(update.getMessage().getChatId(), "You have already merged your account with the telegram");
                } else {

                    sendMessage = createMessage(update.getMessage().getChatId(), "The submitted email is already in use by another user");
                }

            } else {
                chatIdRepository.save(TelegramChatId.builder()
                        .email(message)
                        .chatId(chatId)
                        .build());
                sendMessage = createMessage(update.getMessage().getChatId(), "Email " + message + " successfully combined with telegram");
            }

        } else {
            sendMessage = createMessage(update.getMessage().getChatId(), "If You want to merge your account please write your email.");
        }

        sendMsg(sendMessage);

    }

    public void sendMsg(SendMessage msg) {

        try {
            Message execute = execute(msg);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }


    @Override
    public String getBotUsername() {
        return BOT_USERNAME;
    }

    @Override
    public String getBotToken() {
        return BOT_TOKEN;
    }

    public SendMessage createMessage(Long chatId, String textMessage) {

        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);
        sendMessage.setChatId(chatId);
        sendMessage.setText(textMessage);

        return sendMessage;
    }
}
