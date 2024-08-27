package com.freemyip.mynotesproject.MyNotes.controllers;

import com.freemyip.mynotesproject.MyNotes.configuration.AuthenticationResponse;
import com.freemyip.mynotesproject.MyNotes.models.AuthenticationRequest;
import com.freemyip.mynotesproject.MyNotes.services.impl.AuthenticationResponseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/authentication")
@RequiredArgsConstructor
public class AuthenticationController {
    private final AuthenticationResponseService responseService;

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(@RequestBody AuthenticationRequest authenticationRequest) {
        return ResponseEntity.ok(responseService.authenticate(authenticationRequest));
    }
}
