package com.freemyip.myvisitcard.MyNotes.services;

import com.freemyip.myvisitcard.MyNotes.models.UserStatus;

public interface UserStatusService {
    UserStatus getUserStatusByTelegramUserId(Long telegramUserId);
    void updateUserStatus(UserStatus userStatus);
}
