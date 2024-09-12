package com.freemyip.mynotesproject.MyNotes.services.impl;

import com.freemyip.mynotesproject.MyNotes.exceptions.DuplicateEntityException;
import com.freemyip.mynotesproject.MyNotes.models.User;
import com.freemyip.mynotesproject.MyNotes.repositories.UserRepository;
import com.freemyip.mynotesproject.MyNotes.services.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

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
}
