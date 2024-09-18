package com.freemyip.mynotesproject.MyNotes.services.impl;

import com.freemyip.mynotesproject.MyNotes.exceptions.DuplicateEntityException;
import com.freemyip.mynotesproject.MyNotes.models.User;
import com.freemyip.mynotesproject.MyNotes.repositories.UniqueUserRepository;
import com.freemyip.mynotesproject.MyNotes.repositories.UserRepository;
import com.freemyip.mynotesproject.MyNotes.repositories.UserStatusRepository;
import com.freemyip.mynotesproject.MyNotes.repositories.content.NoteCategoryRepository;
import com.freemyip.mynotesproject.MyNotes.repositories.content.NoteRepository;
import com.freemyip.mynotesproject.MyNotes.services.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final NoteRepository noteRepository;
    private final NoteCategoryRepository categoryRepository;
    private final UserStatusRepository userStatusRepository;
    private final UniqueUserRepository uniqueUserRepository;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User getUserByUsername(String username) {
        return userRepository.findUserByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User with name " + username + " not found!"));
    }

    @Override
    @Transactional
    public User updateUserData(User newUserDate) {
        User currentUser = userRepository.findUserById(newUserDate.getId())
                .orElseThrow(() -> new EntityNotFoundException("Update error. User not found."));
        String newName = newUserDate.getUsername();

        if (!currentUser.getUsername().equals(newName)) {
            if (userRepository.existsUserByUsername(newName)) {
                throw new DuplicateEntityException("A user with the name " + newName + " already exists. Please choose another name.");
            }
        }

        currentUser.setUsername(newName);
        currentUser.setFirstName(newUserDate.getFirstName());
        currentUser.setLastName(newUserDate.getLastName());
        currentUser.setPassword(passwordEncoder.encode(newUserDate.getPassword()));

        userRepository.save(currentUser);

        return currentUser;
    }

    @Override
    @Transactional
    public void deleteUserById(Long id) {
        if (!userRepository.existsById(id)) {
            throw new EntityNotFoundException("User with id " + id + " not found.");
        }

        noteRepository.deleteAllByUserId(id);
        categoryRepository.deleteAllByUserId(id);
        userStatusRepository.deleteByUserId(id);
        userRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void setDeletionStatus(List<User> userList) {
        List<Long> ids = userList
                .stream()
                .map(User::getId)
                .toList();
        List<User> users = userRepository.findAllById(ids);
        Map<Long, User> userMap = users
                .stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        for (User u : userList) {
            User user = userMap.get(u.getId());

            if (user == null) {
                throw new EntityNotFoundException("User with id " + u.getId() + " not found.");
            }

            user.setNotDeletion(u.isNotDeletion());
        }

        userRepository.saveAll(users);
    }

    @Override
    public Long getCountUniqueUsers() {
        return uniqueUserRepository.count();
    }
}
