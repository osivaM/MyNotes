package com.freemyip.mynotesproject.MyNotes.services;

import com.freemyip.mynotesproject.MyNotes.models.User;

public interface UserService {
    User getUserByUsername(String username);
    User updateUserData(User newUserData);
}
