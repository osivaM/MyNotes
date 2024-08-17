package com.freemyip.mynotesproject.MyNotes.services;

import com.freemyip.mynotesproject.MyNotes.models.User;

public interface UserService {
    User getUserById(Long id);
    User getUserByTelegramId(Long id);
    User getUserByUsername(String username);
    boolean findUserByTelegramId(Long id);
    boolean findUserByUsername(String username);
    void createUserWithRole(User user, String roleName);
    void updateUser(User user);
}
