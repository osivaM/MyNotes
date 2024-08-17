package com.freemyip.mynotesproject.MyNotes.services;

import com.freemyip.mynotesproject.MyNotes.models.UserStatus;

public interface UserStatusService {
    UserStatus getUserStatusByTelegramUserId(Long telegramUserId);
    void updateUserStatus(UserStatus userStatus);
}
