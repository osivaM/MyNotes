package com.freemyip.myvisitcard.MyNotes.controllers.telegrambot;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.telegram.telegrambots.meta.api.objects.Update;

@RestController
@RequiredArgsConstructor
public class TelegramWebhookController {
    private final UpdateController updateController;

    @RequestMapping(value = "/callback/update", method = RequestMethod.POST)
    public ResponseEntity<?> onUpdateReceive(@RequestBody Update update) {
        updateController.checkUpdate(update);

        return ResponseEntity.ok().build();
    }
}
