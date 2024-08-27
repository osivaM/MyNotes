package com.freemyip.mynotesproject.MyNotes.repositories;

import com.freemyip.mynotesproject.MyNotes.models.User;
import org.springframework.data.repository.ListCrudRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends ListCrudRepository<User, Long> {
    List<User> findAllByRegistrationDateBeforeAndIdNotIn(LocalDateTime date, List<Long> userIds);
    Optional<User> findUserById(Long id);
    Optional<User> findUserByUsername(String username);
    Optional<User> findUserByTelegramId(Long id);
    boolean existsUserByUsername(String username);
    boolean existsUserByTelegramId(Long id);
}
