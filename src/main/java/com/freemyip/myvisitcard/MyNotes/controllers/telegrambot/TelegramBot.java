package com.freemyip.myvisitcard.MyNotes.controllers.telegrambot;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.*;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Controller
@RequiredArgsConstructor
public class TelegramBot extends TelegramLongPollingBot {
    @Value("${bot.name}")
    private String botName;
    @Value("${bot.token}")
    private String botToken;

    private final UpdateController updateController;

    @Override
    public String getBotUsername() {
        return botName;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @PostConstruct
    public void init() {
        updateController.telegramBot(this);

        try {
            TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);

            telegramBotsApi.registerBot(this);
        } catch (TelegramApiException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void onUpdateReceived(Update update) {
        updateController.checkUpdate(update);
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
