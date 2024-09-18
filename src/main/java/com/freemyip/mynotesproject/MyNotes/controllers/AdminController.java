package com.freemyip.mynotesproject.MyNotes.controllers;

import com.fasterxml.jackson.annotation.JsonView;
import com.freemyip.mynotesproject.MyNotes.models.User;
import com.freemyip.mynotesproject.MyNotes.services.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {
    private final UserService userService;

    @GetMapping("/users")
    @JsonView(User.AdminViews.class)
    public ResponseEntity<List<User>> admin() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/unique-users")
    public ResponseEntity<Map<String, Long>> countOfUniqueUsers() {
        return ResponseEntity.ok(Map.of("count", userService.getCountUniqueUsers()));
    }

    @DeleteMapping("/delete-user")
    public ResponseEntity<HttpStatus> deleteUser(@RequestParam("id") Long id) {
        try {
            userService.deleteUserById(id);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/set-deletion-status")
    public ResponseEntity<HttpStatus> setDeletionStatus(@RequestBody List<User> userList) {
        userService.setDeletionStatus(userList);

        return ResponseEntity.noContent().build();
    }
}
