package com.freemyip.mynotesproject.MyNotes.services.impl;

import com.freemyip.mynotesproject.MyNotes.models.Role;
import com.freemyip.mynotesproject.MyNotes.models.UniqueUser;
import com.freemyip.mynotesproject.MyNotes.models.User;
import com.freemyip.mynotesproject.MyNotes.models.UserStatus;
import com.freemyip.mynotesproject.MyNotes.repositories.UniqueUserRepository;
import com.freemyip.mynotesproject.MyNotes.repositories.UserRepository;
import com.freemyip.mynotesproject.MyNotes.services.RegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RegistrationServiceImpl implements RegistrationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UniqueUserRepository uniqueUserRepository;

    @Override
    public String registration(Update update, UserStatus userStatus) {
        String response = "";
        String registrationStatus = userStatus.getUserRegistrationStatus();
        String currentStep = userStatus.getCurrentStep();
        Message message = update.getMessage();
        String text = message.getText();
        long chatId = message.getChatId();

        if (registrationStatus == null) {
            if (userRepository.existsUserByTelegramId(chatId)) {
                User user = userRepository.findUserByTelegramId(chatId)
                                .orElseThrow();

                userStatus.setUserId(user.getId());
                userStatus.setTelegramUserId(chatId);
                userStatus.setUsername(user.getUsername());
                userStatus.setUserRegistrationStatus(UserStatus.REGISTERED);
                userStatus.setCurrentStep(UserStatus.NOTHING);

                response = "You are already registered. You can /start using the service.";
            } else {
                userStatus.setTelegramUserId(chatId);
                userStatus.setUserRegistrationStatus(UserStatus.UNREGISTERED);
                userStatus.setCurrentStep(UserStatus.WAITING_USERNAME);
                response = """
                        Hi! This is the MyNotes project. It's designed to create and conveniently manage notes and catalog them. This is your first time here, so let's register.

                        Please enter your username (it will be used to log in to the service's website):""";
            }
        } else {
            switch (currentStep) {
                case UserStatus.WAITING_USERNAME -> {
                    if (userRepository.existsUserByUsername(text)) {
                        response = "A user with the same name already exists, enter another name:";
                    } else if (!text.matches("^[a-zA-Z0-9._-]+$")) {
                        response = "Invalid characters for the login. Use uppercase and lowercase Latin letters, as well as characters ( _ - . ).";
                    } else {
                        User user = User.builder()
                                .username(text)
                                .password(passwordEncoder.encode("password"))
                                .firstName(message.getFrom().getFirstName())
                                .lastName(message.getFrom().getLastName())
                                .role(Role.USER)
                                .registrationDate(LocalDateTime.now())
                                .telegramId(chatId)
                                .notDeletion(false)
                                .build();

                        userRepository.save(user);
                        userStatus.setCurrentStep(UserStatus.WAITING_PASSWORD);
                        userStatus.setUsername(text);
                        userStatus.setUserId(user.getId());

                        if (!uniqueUserRepository.existsByTelegramId(chatId)) {
                            uniqueUserRepository.save(UniqueUser
                                    .builder()
                                    .telegramId(chatId)
                                    .username(text)
                                    .build());
                        }

                        response = "Great. Now enter the password:";
                    }
                }
                case UserStatus.WAITING_PASSWORD -> {
                    if (!text.matches("^[a-zA-Z0-9!@#$%^&*()_+=\\-\\[\\]{}|;:'\",.<>?/]+$")) {
                        response = "Invalid characters for the password. Use uppercase and lowercase Latin letters, and various symbols.";
                    } else {
                        User user = userRepository.findUserById(userStatus.getUserId())
                                .orElseThrow();

                        user.setPassword(passwordEncoder.encode(text));

                        userRepository.save(user);

                        userStatus.setUserRegistrationStatus(UserStatus.REGISTERED);
                        userStatus.setCurrentStep(UserStatus.NOTHING);

                        response = "Done. Now you can /start using the service.";
                    }
                }
            }
        }

        return response;
    }
}
