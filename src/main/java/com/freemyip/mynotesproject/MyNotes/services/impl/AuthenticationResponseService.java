package com.freemyip.mynotesproject.MyNotes.services.impl;

import com.freemyip.mynotesproject.MyNotes.models.AuthenticationResponse;
import com.freemyip.mynotesproject.MyNotes.models.AuthenticationRequest;
import com.freemyip.mynotesproject.MyNotes.models.User;
import com.freemyip.mynotesproject.MyNotes.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationResponseService {
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthenticationResponse authenticate(AuthenticationRequest authenticationRequest) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        authenticationRequest.getUsername(),
                        authenticationRequest.getPassword()
                )
        );

        User user = userRepository.findUserByUsername(authenticationRequest.getUsername())
                        .orElseThrow();
        String token = jwtService.generateToken(user);

        return AuthenticationResponse.builder()
                .token(token)
                .build();
    }
}
