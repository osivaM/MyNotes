package com.freemyip.mynotesproject.MyNotes.services.impl;

import com.freemyip.mynotesproject.MyNotes.repositories.UserStatusRepository;
import com.freemyip.mynotesproject.MyNotes.services.UserStatusService;
import com.freemyip.mynotesproject.MyNotes.models.UserStatus;
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
