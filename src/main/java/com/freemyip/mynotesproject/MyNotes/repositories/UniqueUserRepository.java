package com.freemyip.mynotesproject.MyNotes.repositories;

import com.freemyip.mynotesproject.MyNotes.models.UniqueUser;
import org.springframework.data.repository.ListCrudRepository;

public interface UniqueUserRepository extends ListCrudRepository<UniqueUser, Long> {
    boolean existsByTelegramId(Long telegramId);
    long count();
}
