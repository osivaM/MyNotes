package com.freemyip.myvisitcard.MyNotes.controllers.telegrambot;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.*;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Controller
@RequiredArgsConstructor
public class TelegramBot extends TelegramWebhookBot {
    @Value("${bot.name}")
    private String botName;
    @Value("${bot.token}")
    private String botToken;
    @Value("${bot.path}")
    private String botPath;
    @Value("${bot.uri}")
    private String botUri;

    private final UpdateController updateController;

    @Override
    public String getBotUsername() {
        return botName;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public String getBotPath() {
        return botPath;
    }

    @PostConstruct
    public void init() {
        try {
            SetWebhook webhook = SetWebhook.builder()
                    .url(botUri)
                    .build();
            this.setWebhook(webhook);
        } catch (TelegramApiException e) {
            System.out.println(e.getMessage());
        }

        updateController.telegramBot(this);
    }

    @Override
    public BotApiMethod<?> onWebhookUpdateReceived(Update update) {
        return null;
    }

    public void sendTextMessage(SendMessage sendMessage) {
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            System.out.println(e.getMessage());
        }
    }

    public void sendVoiceMessage(SendVoice sendVoice) {
        try {
            execute(sendVoice);
        } catch (TelegramApiException e) {
            System.out.println(e.getMessage());
        }
    }

    public void sendAudioMessage(SendAudio sendAudio) {
        try {
            execute(sendAudio);
        } catch (TelegramApiException e) {
            System.out.println(e.getMessage());
        }
    }

    public void sendVideoMessage(SendVideo sendVideo) {
        try {
            execute(sendVideo);
        } catch (TelegramApiException e) {
            System.out.println(e.getMessage());
        }
    }

    public void sendPhotoMessage(SendPhoto sendPhoto) {
        try {
            execute(sendPhoto);
        } catch (TelegramApiException e) {
            System.out.println(e.getMessage());
        }
    }

    public void sendMediaGroupMessage(SendMediaGroup mediaGroup) {
        try {
            execute(mediaGroup);
        } catch (TelegramApiException e) {
            System.out.println(e.getMessage());
        }
    }
}
