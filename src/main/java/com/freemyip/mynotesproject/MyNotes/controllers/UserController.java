package com.freemyip.mynotesproject.MyNotes.controllers;

import com.fasterxml.jackson.annotation.JsonView;
import com.freemyip.mynotesproject.MyNotes.exceptions.DuplicateEntityException;
import com.freemyip.mynotesproject.MyNotes.models.User;
import com.freemyip.mynotesproject.MyNotes.services.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'USER')")
public class UserController {
    private final UserService userService;

    @GetMapping("/user-data")
    @JsonView(User.UserViews.class)
    public ResponseEntity<User> userData(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(userService.getUserByUsername(userDetails.getUsername()));
    }

    @PatchMapping("/edit-user-data")
    @JsonView(User.UserViews.class)
    public ResponseEntity<?> editUserData(@RequestBody User user) {
        User updatedUser;

        try {
            updatedUser = userService.updateUserData(user);
        } catch (EntityNotFoundException | DuplicateEntityException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }

        return ResponseEntity.ok(updatedUser);
    }
}
