package com.freemyip.mynotesproject.MyNotes.services.impl;

import com.freemyip.mynotesproject.MyNotes.repositories.UserStatusRepository;
import com.freemyip.mynotesproject.MyNotes.repositories.content.NoteCategoryRepository;
import com.freemyip.mynotesproject.MyNotes.repositories.content.NoteRepository;
import com.freemyip.mynotesproject.MyNotes.models.User;
import com.freemyip.mynotesproject.MyNotes.repositories.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class UserDeleteService {

    private UserRepository userRepository;
    private NoteCategoryRepository noteCategoryRepository;
    private NoteRepository noteRepository;
    private UserStatusRepository userStatusRepository;

    @Scheduled(fixedRate = 3600000)
    @Transactional
    public void deleteUser() {
        LocalDateTime oneDay = LocalDateTime.now().minusDays(1L);
        List<User> usersToDelete = userRepository.findAllByRegistrationDateBeforeAndIdNotIn(oneDay, List.of(1L, 2L, 3L));

        for (User user : usersToDelete) {
            Long userId = user.getId();

            noteRepository.deleteAllByUserId(userId);
            noteCategoryRepository.deleteAllByUserId(userId);
            userStatusRepository.deleteByUserId(userId);
        }

        userRepository.deleteAll(usersToDelete);
    }
}
