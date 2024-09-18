package com.freemyip.mynotesproject.MyNotes.services.impl;

import com.freemyip.mynotesproject.MyNotes.repositories.UserStatusRepository;
import com.freemyip.mynotesproject.MyNotes.repositories.content.NoteCategoryRepository;
import com.freemyip.mynotesproject.MyNotes.repositories.content.NoteRepository;
import com.freemyip.mynotesproject.MyNotes.models.User;
import com.freemyip.mynotesproject.MyNotes.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserDeleteService {

    private final UserRepository userRepository;
    private final NoteCategoryRepository noteCategoryRepository;
    private final NoteRepository noteRepository;
    private final UserStatusRepository userStatusRepository;

    @Scheduled(fixedRate = 3600000)
    @Transactional
    public void deleteUser() {
        LocalDateTime oneDay = LocalDateTime.now().minusDays(1L);
        List<User> usersToDelete = userRepository.findAllByRegistrationDateBeforeAndNotDeletionFalse(oneDay);

        for (User user : usersToDelete) {
            Long userId = user.getId();

            noteRepository.deleteAllByUserId(userId);
            noteCategoryRepository.deleteAllByUserId(userId);
            userStatusRepository.deleteByUserId(userId);
        }

        userRepository.deleteAll(usersToDelete);
    }
}
