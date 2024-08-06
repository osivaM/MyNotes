package com.freemyip.myvisitcard.MyNotes.repositories;

import com.freemyip.myvisitcard.MyNotes.models.UserStatus;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface UserStatusRepository extends CrudRepository<UserStatus, Long> {
    Optional<UserStatus> getUserStatusByTelegramUserId(Long id);
    void deleteByUserId(Long id);
}
