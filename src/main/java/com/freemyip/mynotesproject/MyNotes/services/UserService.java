package com.freemyip.mynotesproject.MyNotes.services;

import com.freemyip.mynotesproject.MyNotes.models.User;

import java.util.List;

public interface UserService {
    List<User> getAllUsers();
    User getUserByUsername(String username);
    User updateUserData(User newUserData);
    void deleteUserById(Long id);
    void setDeletionStatus(List<User> userList);
    Long getCountUniqueUsers();
}
