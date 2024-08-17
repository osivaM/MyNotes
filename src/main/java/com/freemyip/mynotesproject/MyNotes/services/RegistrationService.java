package com.freemyip.mynotesproject.MyNotes.services;

import com.freemyip.mynotesproject.MyNotes.models.UserStatus;
import org.telegram.telegrambots.meta.api.objects.Update;

public interface RegistrationService {
    String registration(Update update, UserStatus userStatus);
}
