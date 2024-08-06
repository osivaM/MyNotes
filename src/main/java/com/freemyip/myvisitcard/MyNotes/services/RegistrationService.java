package com.freemyip.myvisitcard.MyNotes.services;

import com.freemyip.myvisitcard.MyNotes.models.UserStatus;
import org.telegram.telegrambots.meta.api.objects.Update;

public interface RegistrationService {
    String registration(Update update, UserStatus userStatus);
}
