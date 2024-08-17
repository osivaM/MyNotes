package com.freemyip.mynotesproject.MyNotes.services.impl;

import com.freemyip.mynotesproject.MyNotes.models.User;
import com.freemyip.mynotesproject.MyNotes.models.UserStatus;
import com.freemyip.mynotesproject.MyNotes.services.RegistrationService;
import com.freemyip.mynotesproject.MyNotes.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

@Service
@RequiredArgsConstructor
public class RegistrationServiceImpl implements RegistrationService {
    private final UserService userService;

    @Override
    public String registration(Update update, UserStatus userStatus) {
        String response = "";
        String registrationStatus = userStatus.getUserRegistrationStatus();
        String currentStep = userStatus.getCurrentStep();
        Message message = update.getMessage();
        String text = message.getText();
        long chatId = message.getChatId();

        if (registrationStatus == null) {
            if (userService.findUserByTelegramId(chatId)) {
                User user = userService.getUserByTelegramId(chatId);

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
                    if (userService.findUserByUsername(text)) {
                        response = "A user with the same name already exists, enter another name:";
                    } else if (!text.matches("^[a-zA-Z0-9._-]+$")) {
                        response = "Invalid characters for the login. Use uppercase and lowercase Latin letters, as well as characters ( _ - . ).";
                    } else {
                        User user = new User();

                        user.setTelegramId(chatId);
                        user.setUsername(text);
                        user.setPassword("password");
                        user.setFirstName(message.getFrom().getFirstName());
                        user.setLastName(message.getFrom().getLastName());
                        userService.createUserWithRole(user, "ROLE_USER");

                        userStatus.setCurrentStep(UserStatus.WAITING_PASSWORD);
                        userStatus.setUsername(text);
                        userStatus.setUserId(userService.getUserByTelegramId(chatId).getId());

                        response = "Great. Now enter the password:";
                    }
                }
                case UserStatus.WAITING_PASSWORD -> {
                    if (!text.matches("^[a-zA-Z0-9!@#$%^&*()_+=\\-\\[\\]{}|;:'\",.<>?/]+$")) {
                        response = "Invalid characters for the password. Use uppercase and lowercase Latin letters, and various symbols.";
                    } else {
                        User user = userService.getUserById(userStatus.getUserId());

                        user.setPassword(text);

                        userService.updateUser(user);

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
