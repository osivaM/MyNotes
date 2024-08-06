package com.freemyip.myvisitcard.MyNotes.services.impl;

import com.freemyip.myvisitcard.MyNotes.models.Role;
import com.freemyip.myvisitcard.MyNotes.models.User;
import com.freemyip.myvisitcard.MyNotes.repositories.UserRepository;
import com.freemyip.myvisitcard.MyNotes.services.RoleService;
import com.freemyip.myvisitcard.MyNotes.services.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {
    private RoleService roleService;
    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;

    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
    }

    @Override
    public User getUserByTelegramId(Long id) {
        return userRepository.findUserByTelegramId(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with telegram id: " + id));
    }

    @Override
    public User getUserByUsername(String username) {
        return userRepository.findUserByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found with username " + username));
    }

    @Override
    public boolean findUserByTelegramId(Long id) {
        return userRepository.existsUserByTelegramId(id);
    }

    public boolean findUserByUsername(String username) {
        return userRepository.existsUserByUsername(username);
    }

    @Override
    @Transactional
    public void createUserWithRole(User user, String roleName) {
        Role role = roleService.getRoleByName(roleName);

        user.addRole(role);
        user.setRegistrationDate(LocalDateTime.now());
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        userRepository.save(user);
    }

    @Override
    @Transactional
    public void updateUser(User user) {
        User userToUpdate = userRepository.findById(user.getId())
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + user.getId()));

        userToUpdate.setUsername(user.getUsername());
        userToUpdate.setFirstName(user.getFirstName());
        userToUpdate.setLastName(user.getLastName());
        userToUpdate.setPassword(passwordEncoder.encode(user.getPassword()));

        userRepository.save(userToUpdate);
    }
}
