package com.freemyip.myvisitcard.MyNotes.services.impl;

import com.freemyip.myvisitcard.MyNotes.models.UserStatus;
import com.freemyip.myvisitcard.MyNotes.repositories.UserStatusRepository;
import com.freemyip.myvisitcard.MyNotes.services.UserStatusService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class UserStatusServiceImpl implements UserStatusService {
    private UserStatusRepository statusRepository;

    @Override
    public UserStatus getUserStatusByTelegramUserId(Long telegramUserId) {
        return statusRepository.getUserStatusByTelegramUserId(telegramUserId).orElse(new UserStatus());
    }

    @Override
    @Transactional
    public void updateUserStatus(UserStatus userStatus) {
        statusRepository.save(userStatus);
    }
}
